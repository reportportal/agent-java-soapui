# agent-java-soapui

[![Build Status](https://travis-ci.org/reportportal/agent-java-soapui.svg?branch=master)](https://travis-ci.org/reportportal/agent-java-soapui)


## Building the plugin
```sh
./gradlew clean build
```

## Installing

* Build the project
* Unpack ./build/distributions/agent-java-soapui-full.zip
* Copy JAR file to soapUI's lib folder and listeners folder to the soapUI installation root.


## Configuration parameters
* Agent supports all parameters of JVM-based agents [described here](http://reportportal.io/#documentation/JVM-based-clients-configuration)
* `rp.reporter.type` - specifies type calculation statistics for your tests.
    * STEP_BASED - calculates statistics based on steps (**default**)
    * TEST_BASED - calculates statistcs based on tests 