# Using the Java JAR application

The JAR file created by the genexplain-api build script is executable and offers a few *applications* described in the following sections. They can be executed by adding corresponding *commands* onto the commandline as first argument after the JAR path. To see which applications/commands are available just execute the JAR without arguments. Further arguments or parameters required by the applications can be seen by invoking the JAR with just their command name or the command name followed by *-h*. Often parameters are specified in a JSON file as second argument after the *command*. Here one can see four applications named *regulator-search, exec, apps, example*.

```Bash
genexplain-api$ java -jar build/libs/genexplain-api-1.0.jar 

Usage: java -jar (...).jar <command> <args> ...

Available commands:

regulator-search  -  JSON and Java interface to carry out a regulator search
exec  -  Executes tasks configured in provided JSON file
apps  -  Lists available analysis applications
example  -  Runs examples

Java packages to be scanned for ApplicationCommand implementations
can be specified as system property using the java -D option

For more info about each command try (java -jar ...) COMMAND -h

---------------------------
```

Briefly, the program *apps* connects to a platform server using a specified account and retrieves a list of available analysis tools. The *example* application offers execution of a few selectable examples. The *regulator-search* can be used to run regulator and effector inferences on molecular networks. Through the *exec* command one can interact with a platform instance to down- or upload data, or to run analysis processes.
