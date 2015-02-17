#LogAnalyzer Overview
The log reader generator is a research tool which can generate specialized log reader 
based on the provided property specification.


## Prerequisites

1. [JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
v.7 or higher
 * Check Java is installed properly: run `java -version` from a
  terminal.

2. [Maven](http://maven.apache.org/download.cgi)
v.3.0 or higher
 * Check Maven is installed properly: run `mvn -version` from a terminal.

3. [Git](http://git-scm.com/book/en/Getting-Started-Installing-Git)
v.1.8 or higher
 * Check Git is installed properly: run `git` from a terminal.


## Install

Currently, the only option for setting up the tool is building from source.

1. Clone Repository: In the whatever directory you like, execute:
```git clone https://github.com/xiaohe27/LogAnalyzer.git```

2. Build: In the top directory of the LogAnalyzer project, execute the command:
 ```mvn package```

3. The application will be built in `<Directory You did the git clone command>/LogAnalyzer/target/release/LogAnalyzer/LogAnalyzer`


## Test

1. Inside the directory which contains the built application, execute the command: 
```sh logAnalyzer.sh YourRVM-Spec.rvm [-liveness]```

2. The specialized log reader which is able to monitor the log file with pre-defined events will be generated in folder
`/CustomizedLogReader` at current directory.

3. Use `rv-monitor` to generate the monitoring library. Assume the current directory is the same as that of `logAnalyzer.sh`
 ```rv-monitor -d /CustomizedLogReader/rvm YourRVM-Spec.rvm```

4. Compile the log analyzer:
 ```javac /CustomizedLogReader/rvm/*.java```

5. The ready-to-use log analyzer will be built successfully in `/CustomizedLogReader/` if no errors occur.

6. In `/CustomizedLogReader/`, execute the command like this to monitor the log file:
 ```java rvm.LogReader <The-Path-To-Your-Log-File>```
