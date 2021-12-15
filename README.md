[![Build Status](https://travis-ci.org/genexplain/genexplain-api.svg?branch=master)](https://travis-ci.org/genexplain/genexplain-api)
[![codecov](https://codecov.io/gh/genexplain/genexplain-api/branch/master/graph/badge.svg)](https://codecov.io/gh/genexplain/genexplain-api)

# genexplain-api #

The [geneXplain](http://genexplain.com) API is a Java programming interface for the [geneXplain platform](http://genexplain.com/genexplain-platform/). The geneXplain platform is an online toolbox and workflow management system for a broad range of bioinformatic and systems biology applications. This API can be used to write Java programs that can invoke functionality of the platform through its web interface, e.g. for import and export of biological data, or to submit and monitor analysis jobs. Furthermore, platform tasks can be specified in [JSON](https://json.org) format and submitted using the executable JAR file. Though not endowed with all possiblities of a programming language, the JSON interface facilitates, among other things, definition of templates, branch points, or nesting of tasks, so that one can build complex workflows from reusable components. It is intended to be applied as part of a dynamic and polyglot analysis environment that utilizes diverse programming languages and resources.

## Documentation ##

A documentation of the API is available on [GitHub Pages](https://genexplain.github.io/genexplain-api/).

## Quick start ##

A first step in using a geneXplain platform service is to sign into an existing user account. A server may offer a demo account to which one can connect through the API by omitting username and password. However, uploaded data or analysis results are accessible to everyone entering the demo workspace. A user account for the geneXplain platform can be created for free [here](http://genexplain.com/genexplain-platform-registration/).

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
    "withParameters": true
}
```

The *server* property specifies the URL of the platform server to connect to and consists of protocol
and hostname. The *withParameters* property is optional. If omitted or set to *false* the app 
will only produce a list of analysis tools. Otherwise a table with application names, their 
parameter names and descriptions is printed out.

The result is printed to standard output.

## Example applications

Besides [online manual and tutorial](https://genexplain.github.io/genexplain-api/), we have prepared several example applications. Their sources are located in the package `com.genexplain.api.eg`. Furthermore, they can be executed by invoking the JAR with the word _example_ as first argument as shown below. This will print out the list of available examples and a short description.

```sh
java -jar genexplain-api-1.0.jar example
```

