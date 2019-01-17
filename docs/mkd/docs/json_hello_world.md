# JSON interface tutorial

The following sections show how to run platform tasks through the JSON interface and how to use its
possibilities to build analysis workflows and pipelines.

To execute the JSON configurations of the tutorial change to the subdirectory of the _genexplain-api_ Git repository
named _docs/tutorial_. Let us start with the Hello world example.

## Hello world example

The output of the *hello_world.json* file contained in  mentioned folder is shown below. The single task
specified for the _tasks_ property leads to execution of _docs/tutorial/script.sh_ which simply echos the commandline input.

```JSON
{
	"withoutConnect": true,
	"tasks": {
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "Hello world"]
	}
}
```

```Bash
gene@xplain:genexplain-api/docs/tutorial$ java -jar ../../build/libs/genexplain-api-1.0.jar exec hello_world.json 
INFO  com.genexplain.api.app.APIRunner - Running command exec
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Hello world
```

## Lists of tasks

Building pipelines and workflows is easy. One step to get started with this is to be able
to specify lists of tasks that are executed sequentially. For this you just let _tasks_ be a
JSON array.

```JSON
{
	"withoutConnect": true,
	"tasks": [
		{
			"do": "external",
			"showOutput": true,
			"bin": "sh",
			"params": ["script.sh", "Executing task 1"]
		},
		{
			"do": "external",
			"showOutput": true,
			"bin": "sh",
			"params": ["script.sh", "Executing task 2"]
		},
		{
			"do": "external",
			"showOutput": true,
			"bin": "sh",
			"params": ["script.sh", "Executing task 3"]
		}
	]
}
```
Execute the tutorial file _task_list.json_ to see this at work.

```Bash
gene@xplain:genexplain-api/docs/tutorial$ java -jar ../../build/libs/genexplain-api-1.0.jar exec task_list.json 
INFO  com.genexplain.api.app.APIRunner - Running command exec
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Executing task 1
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Executing task 2
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Executing task 3
```

## Loading tasks from file


A pipeline with many tasks may be more convenient to manage in multiple files. It is
possible to load tasks from a file.

We can simply write the three tasks of our previous example into a
separate file and load the task array in our main object.


```JSON
[
	{
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "Executing task 1"]
	},
	{
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "Executing task 2"]
	},
	{
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "Executing task 3"]
	}
]
```

In our main object we specify the file using the _fromFile_ property as shown below. explain
more details in the documentation.


```JSON
{
	"withoutConnect": true,
	"tasks": {
		"fromFile": {
			"file": "loadable_task_list.json"
		}
	}
}
```

Running that we get:


```Bash
genexplain-api/docs/tutorial$ java -jar ../../build/libs/genexplain-api-1.0.jar exec loading_task_list.json 
INFO  com.genexplain.api.app.APIRunner - Running command exec
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Executing task 1
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Executing task 2
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Executing task 3
```

### Deep nesting and infinite looping

So, now we can write bigger task lists into separate files which is already one step towards maintaining more complex
workflows, but sometimes it gets even more complex than is conveniently handled with one file for all the tasks. And indeed 
the JSON executor is not limited to what we have shown so far, but allows for arbitrary levels of nesting. What goes on
in the backend is a recursive method that calls itself with JSON arguments until it finds a task that is processed by the
specified executor.

#### Deep nesting

Let us create an extra level of nesting for _task 1_ in our example by specifying a list of tasks _1a - 1c_ in a separate file. We call this
file _nested_task_1.json_. 

```JSON
[
	{
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "Executing task 1a"]
	},
	{
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "Executing task 1b"]
	},
	{
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "Executing task 1c"]
	}
]
```

Next, we adapt our _loadable_task_list_ by replacing for _task 1_ again an instruction that will cause the new file to be loaded. We call this
new file _loadable_nested_task_list.json_.


```JSON
[
	{
		"fromFile": {
			"file": "nested_task_1.json"
		}
	},
	{
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "Executing task 2"]
	},
	{
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "Executing task 3"]
	}
]
```

Our main JSON object actually only requires a change of file name in the _fromFile_ property and we also save under the name _loading_nested_task.json_.


```JSON
{
	"withoutConnect": true,
	"tasks": {
		"fromFile": {
			"file": "loadable_nested_task_list.json"
		}
	}
}
```

Executing it, we see the expected output.

```Bash
genexplain-api/docs/tutorial$ java -jar ../../build/libs/genexplain-api-1.0.jar exec loading_nested_task_list.json 
INFO  com.genexplain.api.app.APIRunner - Running command exec
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Executing task 1a
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Executing task 1b
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Executing task 1c
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Executing task 2
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Executing task 3
```

As the output confirms, tasks _1a - 1c_ were loaded from the file specified in the outer task list. We can add more and more levels
to this which allows us to break down workflows and pipelines into conveniently small and reusable pieces of JSON configurations.
And on top of that, there are more ways of loading executable tasks as well as specifying task parameters through placeholders that
make the whole more even easier to use and more flexible.

#### Infinite flow

Since the core executor function continues to call itself until it encounters an executable task, it is possible to build an infinitely running
analysis pipeline. A possible use case is a process that continues to check for new data and analyzes them as soon as they
are available. Here we focus on showing a way to build a self-perpetuating pipeline. Another important component for the described use case is to follow alternative analysis branches selected by some condition which is shown in a later part of the tutorial.

Looking at the previous section, one can obviously create a self-perpetuating process by specifying an array of tasks in a file which itself is loaded again in the end. Let us call such a task file _infinite_loop.json_ and configure tasks as shown below. So, at the end a _fromFile_ instructions causes the same file to be loaded again, starting over the pipeline.


```JSON
[
	{
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "Starting infinite loop period"]
	},
	{
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "Middle of infinite loop period"]
	},
	{
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "End of infinite loop period"]
	},
	{
		"fromFile": {
			"file": "infinite_loop.json"
		}
	}
]
```

Our main config only requires a change of file name to load the _infinite_loop.json_ and we rename it as _loading_infinite_loop.json_.

```JSON
{
	"withoutConnect": true,
	"tasks": {
		"fromFile": {
			"file": "infinite_loop.json"
		}
	}
}
```

Execution of the _loading_infinite_loop.json_ results in the expected output and requires forceful interruption.

```Bash
genexplain-api/docs/tutorial$ java -jar ../../build/libs/genexplain-api-1.0.jar exec loading_infinite_loop.json 
INFO  com.genexplain.api.app.APIRunner - Running command exec
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Starting infinite loop period
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Middle of infinite loop period
INFO  c.genexplain.api.core.GxJsonExecutor - External output: End of infinite loop period
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Starting infinite loop period
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Middle of infinite loop period
INFO  c.genexplain.api.core.GxJsonExecutor - External output: End of infinite loop period
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Starting infinite loop period
[...]
```

## Loading from a task library

Until now, we have worked with lists of task objects. When I have taken the time to nicely write down the JSON config for some
analysis tool, I would like to reuse it at different places. In the simple list format we have covered so far, the tasks are unnamed
items and therefore not so easily accessible.
Let us extend the main JSON config by one property that instructs the Java application to load task definitions from files. This property
is called _loadTasks_ and its value is an array with a list of files. A file that can be loaded contains JSON objects with key/value-pairs consisting of task name and task definition.
Here we create such a file named _task_lib.json_ for the three example tasks.

```JSON
{
	"task1": {
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "Executing task 1"]
	},
	"task2": {
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "Executing task 2"]
	},
	"task3": {
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "Executing task 3"]
	}
}
```

This file is similar to the array of tasks in _loadable_task_list.json_, only that the outer structure is an object and the tasks have names. Next we adapt the main JSON config to load this small library of tasks.

```JSON
{
	"withoutConnect": true,
	"loadTasks": [
		"task_lib.json"
	],
	"tasks": [
		{ "fromLib": "task1" },
		{ "fromLib": "task2" },
		{ "fromLib": "task3" }
	]
}
```

Executing the main JSON config shows the expected output, where it is important to note that the tasks are nevertheless executed in the specified order:

```Bash
genexplain-api/docs/tutorial$ java -jar ../../build/libs/genexplain-api-1.0.jar exec loading_task_lib.json 
INFO  com.genexplain.api.app.APIRunner - Running command exec
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Executing task 1
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Executing task 2
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Executing task 3
```

Since the _loadTasks_ value is an array, one can specify many files to load task definitions from.

## Parameter placeholders

To make task definitions more reusable one can insert placeholders for parameters which are replaced by the desired
value specified in the main config. Let us modify the _task_lib.json_ file and substitute the output strings with
placeholders as shown below. While we often use capital strings delimited by $-symbols, any readable string can be used as placeholder.


```JSON
{
	"task1": {
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "$FIRST_MESSAGE$"]
	},
	"task2": {
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "SECOND_MESSAGE"]
	},
	"task3": {
		"do": "external",
		"showOutput": true,
		"bin": "sh",
		"params": ["script.sh", "third message"]
	}
}
```

In the main config we need to add a _replaceStrings_ property whose value is an array of arrays. We use arrays here, because we want to maintain the order specified placeholders. For our example we add one array for each placeholder that shall be recognized with the replacement as second element.

```JSON
{
	"withoutConnect": true,
	"replaceStrings": [
		["$FIRST_MESSAGE$", "First task"],
		["SECOND_MESSAGE", "Second task"],
		["third message", "Third task"]
	],
	"loadTasks": [
		"placeholder_task_lib.json"
	],
	"tasks": [
		{ "fromLib": "task1" },
		{ "fromLib": "task2" },
		{ "fromLib": "task3" }
	]
}
```

In the output of our example workflow the placeholders are replaced by the specified strings.

```Bash
genexplain-api/docs/tutorial$ java -jar ../../build/libs/genexplain-api-1.0.jar exec loading_task_lib_with_placeholders.json 
INFO  com.genexplain.api.app.APIRunner - Running command exec
INFO  c.genexplain.api.core.GxJsonExecutor - External output: First task
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Second task
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Third task
```

Note that any JSON value can be applied for replacement.

### Cascaded replacement

To build pipelines from reusable task definitions and smaller workflows, it is important to know that the _replaceStrings_ value is intentionally an array in order to take advantage of the sequence of substitutions. What happens in the code is that the program iterates over the placeholder strings and substitutes all occurrences in a task definition with the current replacement. This means that one can replace a placeholder with another placeholder whose value is defined a later position in the _replaceStrings_ array. Here is an example. We change placeholders in the main config as shown below and save the new main config as _loading_task_lib_multiple_replacements.json_.

```JSON
{
	"withoutConnect": true,
	"replaceStrings": [
		["$FIRST_MESSAGE$", "SECOND_MESSAGE"],
		["SECOND_MESSAGE", "third message"],
		["third message", "FINALLY"],
		["FINALLY", "Finally, they are all equal."]
	],
	"loadTasks": [
		"placeholder_task_lib.json"
	],
	"tasks": [
		{ "fromLib": "task1" },
		{ "fromLib": "task2" },
		{ "fromLib": "task3" }
	]
}
```

And the output shows the result indicated by the final replacement value.

```Bash
genexplain-api/docs/tutorial$ java -jar ../../build/libs/genexplain-api-1.0.jar exec loading_task_lib_multiple_replacements.json 
INFO  com.genexplain.api.app.APIRunner - Running command exec
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Finally, they are all equal.
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Finally, they are all equal.
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Finally, they are all equal.
```

This property of the placeholder handling is important to be able to create reusable task definitions with general placeholders, which can be customized as needed using a cascade of replacements defined in the main config.

### Local parameter settings

Being able to specify parameters as needed is certainly important. Therefore, there is an executor named _setParameters_ that can alter the _replaceStrings_ items. In the following example we show how to modify existing items. In addition, _setParameters_ can add items in the beginning or at the end of the _replaceStrings_ array as well as remove placeholders. The latter features are explained in the corresponding documentation section.

```JSON
{
	"withoutConnect": true,
	"replaceStrings": [
		["$FIRST_MESSAGE$", "SECOND_MESSAGE"],
		["SECOND_MESSAGE", "third message"],
		["third message", "FINALLY"],
		["FINALLY", "Finally, they are all equal."]
	],
	"loadTasks": [
		"placeholder_task_lib.json"
	],
	"tasks": [
		{ "fromLib": "task1" },
		{ "fromLib": "task2" },
		{ "fromLib": "task3" },
		{ 
			"do": "setParameters",
			"set": {
				"$FIRST_MESSAGE$": "Not anymore!",
				"SECOND_MESSAGE": "What happened?",
				"FINALLY": "This can be changed again."
			}
		},
		{ "fromLib": "task1" },
		{ "fromLib": "task2" },
		{ "fromLib": "task3" },
		{ 
			"do": "setParameters",
			"set": {
				"$FIRST_MESSAGE$": "SECOND_MESSAGE",
				"SECOND_MESSAGE": "FINALLY",
				"FINALLY": "All equal again."
			}
		},
		{ "fromLib": "task1" },
		{ "fromLib": "task2" },
		{ "fromLib": "task3" }
	]
}
```

And the output of this is:

```Bash
genexplain-api/docs/tutorial$ java -jar ../../build/libs/genexplain-api-1.0.jar exec loading_task_lib_local_replacements.json 
INFO  com.genexplain.api.app.APIRunner - Running command exec
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Finally, they are all equal.
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Finally, they are all equal.
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Finally, they are all equal.
INFO  c.genexplain.api.core.GxJsonExecutor - External output: Not anymore!
INFO  c.genexplain.api.core.GxJsonExecutor - External output: What happened?
INFO  c.genexplain.api.core.GxJsonExecutor - External output: This can be changed again.
INFO  c.genexplain.api.core.GxJsonExecutor - External output: All equal again.
INFO  c.genexplain.api.core.GxJsonExecutor - External output: All equal again.
INFO  c.genexplain.api.core.GxJsonExecutor - External output: All equal again.
```
