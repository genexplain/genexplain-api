[![Build Status](https://travis-ci.org/genexplain/genexplain-api.svg?branch=master)](https://travis-ci.org/genexplain/genexplain-api)
[![codecov](https://codecov.io/gh/genexplain/genexplain-api/branch/master/graph/badge.svg)](https://codecov.io/gh/genexplain/genexplain-api)

# genexplain-api #

The [geneXplain](http://genexplain.com) API is a Java programming interface for the [geneXplain platform](http://genexplain.com/genexplain-platform/). The geneXplain platform is an online toolbox and workflow management system for a broad range of bioinformatic and systems biology applications. This API can be used to write Java programs that can invoke functionality of the platform through its web interface, e.g. for import and export of biological data, or to submit and monitor analysis jobs. Furthermore, platform tasks can be specified in [JSON](https://json.org) format and submitted using the executable jar file.

## Quick start ##

A prerequisite for using the geneXplain platform is a user account which can be created for free [here](http://genexplain.com/genexplain-platform-registration/).

Here is a small example class that logs into the platform and retrieves the list of available analysis applications. Running the example requires three commandline parameters

1. the server - e.g. https://platform.genexplain.com
2. username - your registered username
3. password - the password that belongs to the username

```java
import com.genexplain.api.core.GxHttpClient;
import com.genexplain.api.core.GxHttpClientImpl;
import com.genexplain.api.core.GxHttpConnection;
import com.genexplain.api.core.GxHttpConnectionImpl;

import com.eclipsesource.json.JsonObject;

public class APIExample {
    public static void main(String[] args) throws Exception {
    
       // We need a connection object for low-level web service access
       GxHttpConnection connection = new GxHttpConnectionImpl();
       connection.setServer(args[0]);
       connection.setUsername(args[1]);
       connection.setPassword(args[2]);
       connection.setVerbose(true);
       connection.login();
	    
       // And we need a client which provides higher-level methods
       GxHttpClient client = new GxHttpClientImpl();
       client.setConnection(connection);
       client.setVerbose(true);
        
       // Most client methods return a JsonObject that contains
       // the requested results or some other status response
       JsonObject apps = client.listApplications();
       
       apps.get("values").asArray().forEach(app -> { System.out.println(app.asString()); });
       connection.logout();
    }
}
```

## Getting application parameters ##

The executable jar can be used to produce a list of available analysis applications
together with their parameters using the following command


```sh
java -jar genexplain-api-1.0.jar apps config.json

```

where the JSON file contains an object with the following properties.

```json
{
    "server":          "<https://example.com>",
    "user":            "<username>",
    "password":        "<password>",
    "with-parameters": true
}
```

The *server* property specifies the URL of the platform server to connect to containing protocol
and hostname. The *with-parameters* property is optional. If omitted or set to *false* the app 
will only produce a list of analysis tools. Otherwise a table with application names, their 
parameter names and descriptions is printed out.

The result is printed to standard output.

## Executable types ##

The following task types can be executed through the JSON interface. The config parameters
are provided as part of the *tasks* array:

```JSON
{
    "user":      "<user>",
    "password":  "<password>",
    "server":    "<server>",
    "verbose":   true,
    "reconnect": true,
    "tasks": [
       // Tasks to execute
       // Here one can place a list of task objects described below.
    ]
}
```

**Note** that in the following the parameters *toFile* and *toStdout* are optional.

### list ###

Lists contents of specified folder

Config parameters:

```JSON
{
  "do":       "list",
  "folder":   "<folder path>",
  "toFile":   "<outfile>",
  "toStdout": true
}
```

### get ###

Gets specified table

Config parameters:

```JSON
{
  "do":       "get",
  "table":    "<table path>",
  "toFile":   "<outfile>",
  "toStdout": true
}
```

### put ### 

Puts table into specified folder

Config parameters:


```JSON
{
  "do":       "put",
  "path":     "<destination folder path>",
  "table":    [[... column 1 data ...],[... column 2 data ...],...],
  "file":     "<file with table>",
  "columns":  [["column 1","Text"],["column 2","Float"],...]
}
```

The program will accept either data specified in *table* **or** parsed from the *file*,
in that order. The *columns* property is not optional. The type of a column must be one
enumerated in [com.genexplain.api.core.GxColumnDef.ColumnType](https://colossus.genexplain.com/gitblit/blob/?r=genexplain-api.git&f=src/main/java/com/genexplain/api/core/GxColumnDef.java&h=master).

### analyze ### 

Calls the analysis method with specified parameters

Config parameters:

```JSON
{
  "do":       "analyze",
  "method":   "<analysis tool or workflow path>",
  "workflow": true, // whether 'method' is a workflow
  "wait":     true, // true if program shall wait for analysis to complete
  "progress": true, // true if progress shall be shown
  "parameters": {
      // Parameters required by the method
  }
}
```

### imPort ### 

Import a data file using a dedicated importer

Config parameters:

```JSON
{
  "do":       "imPort",
  "path":     "<folder path>",
  "file":     "<file path>",
  "importer": "<exporter>",
  "parameters": {
      // Parameters required by the importer
  }   
}
```

### export ### 

Export data using a dedicated exporter

Config parameters:

```JSON
{
  "do":       "export",
  "path":     "<export folder path>",
  "file":     "<file path>",
  "exporter": "<exporter>",
  "parameters": {
      // Parameters required by the importer
  }   
}
```

### createFolder ### 

Creates a folder

Config parameters:

```JSON
{
  "do":   "createFolder",
  "path": "destination path",
  "name": "<folder name>"
}
```

### itemParameters ### 

Lists parameters for specified application, importer, or exporter

Config parameters:

```JSON
{
  "do":   "itemParameters",
  "type":   "<one of (applications, exporters, importers)>",
  "name":   "<name of app, exporter, or importer>",
  "path":   "<path for export or import>", // This is not needed if 'type' is 'application'
  "toFile": "<outfile>",
  "toStdout": true
}
```

### listItems ### 

Lists available application, importer, or exporter items

Config parameters:

```JSON
{
  "do":     "listItems",
  "type":   "<one of (applications, exporters, importers)>",
  "toFile": "<outfile>",
  "toStdout": true
}
```

### jobStatus ### 

Gets the status for a job id


Config parameters:

```JSON
{
  "do":     "jobStatus",
  "jobId":  "<job id>",
  "toFile": "<outfile>",
  "toStdout": true
}
```

