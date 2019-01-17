# The geneXplain platform Java API

The geneXplain platform is a software that, as a web service, provides a comprehensive environment to analyze biomedical and biological data. Its functionality includes, among other things, data storage, management, sharing, running analysis tools from bioinformatics and systems biology, building and running analysis pipelines and workflows, building and visualizing molecuar network models, or developing quantitative models and simulation.

This document describes the Java API for the geneXplain platform, which allows operation of platform functionality on a remote server using Java programs. Like user accounts for the platform, the API is freely available at [GitHub](https://github.com/genexplain/genexplain-api).

You clone its GitHub repository with the following shell command or download a ZIP archive from the repository website.

```Bash
git clone https://github.com/genexplain/genexplain-api.git
```

A JAR package of the software can be built using the [Gradle](https://gradle.org/) build tool.

```Bash
cd genexplain-api
gradle build
```

There are two main ways to make use of _genexplain-api_. On one hand, the software can be used as a library to build Java programs that interact with the geneXplain platform. On the other hand, the JAR created by the build script is executable and offers several applicatons as well as a JSON interface that enables configuration and execution of individual analysis jobs up to complex workflows.

The core structure of the API is described in [Java API](java_api.md). A number of examples in package `com.genexplain.api.eg` show how to apply it, whereas the section named **JAR applications and JSON interface** describes the different applications that can be executed from the JAR file as well as how to use the JSON interface including a _Hello world_-tutorial.

