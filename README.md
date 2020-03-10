# agent-java-soapui

[![Build Status](https://travis-ci.org/reportportal/agent-java-soapui.svg?branch=master)](https://travis-ci.org/reportportal/agent-java-soapui)


## Building the plugin
```sh
./gradlew clean build
```

## Installing

* Build the project
* Unpack ./build/distributions/agent-java-soapui-full.zip
* Copy JAR file to soapUI's ext folder and listeners folder to the soapUI installation root.
For example, MacOS paths would be:
* `/Applications/SoapUI-5.5.0.app/Contains/java/app/bin/ext` for JAR library
* `/Applications/SoapUI-5.5.0.app/Contains/java/app/bin/listeners` for listeners

Add necessary properties to your project (as custom properties). For example:
```
rp.endpoint=https://rp.epam.com
rp.api.key={YOUR_TOKEN}
rp.project={YOUR_PROJECT}
rp.launch={NAME_OF_YOUR_LAUNCH}
```
- Reload your project
- RUN your project and enjoy result in ReportPortal

## Configuration parameters
* Agent supports all parameters of JVM-based agents [described here](http://reportportal.io/#documentation/JVM-based-clients-configuration)
* `rp.reporter.type` - specifies type calculation statistics for your tests:
    * STEP_BASED - calculates statistics based on steps (**default**)
    * TEST_BASED - calculates statistcs based on tests
* `rp.reporter.disable` - specifies the state of sending automatic logs to RP:
    * FALSE - enable all automatic logs (**default**)
    * TRUE - disable all automatic logs except custom logs from Groovy scripts

## Custom logs from Groovy scripts
If you need to send additional logs from your test cases to RP you always could use 'CustomLoggerService' from any Groovy script.

**Available methods:**
* `CustomLoggerService.log(String log, String logLevel)` - send custom log without attachment
* `CustomLoggerService.logFile(String log, File file, String logLevel)` - send custom log with attachment

**Example:**
```groovy
import com.epam.reportportal.soapui.service.CustomLoggerService

// ...

CustomLoggerService.log("Info log!", "INFO")
CustomLoggerService.log("Warn log!", "WARN")
CustomLoggerService.log("Error log!", "ERROR")

CustomLoggerService.logFile("Screenshot of info message!", screenshotFile, "INFO")
CustomLoggerService.logFile("Screenshot of warn message!", screenshotFile, "WARN")
CustomLoggerService.logFile("Screenshot of error message!", screenshotFile, "ERROR")
```
