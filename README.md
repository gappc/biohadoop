Biohadoop
=========

Biohadoop is a framework that provides the capabilities to run parallel algorithms on [Apache Hadoop](http://hadoop.apache.org/). It leverages the algorithm authors from the burden of writing a Hadoop program and provides services that can be used to offload compute intensive work to local threads and remote machines using a simple interface.

## Quickstart
The quickstart uses a pre-configured Hadoop environment from [https://github.com/gappc/docker-biohadoop](https://github.com/gappc/docker-biohadoop) and the examples from [https://github.com/gappc/biohadoop-algorithms](https://github.com/gappc/biohadoop-algorithms). The requirements are an installed gnome-terminal (provided by default in Ubuntu), Docker > 1.0 and Maven with MVN_HOME set to the correct path.

Build and start environment with one master and two slave nodes. The master node is started inside a red gnome-terminal window, at the end it prints the password for the root user:
```
$ git clone https://github.com/gappc/docker-biohadoop.git
$ sudo docker build -t="docker-biohadoop" ./docker-biohadoop
$ chmod +x ./docker-biohadoop/scripts/*.sh
$ ./docker-biohadoop/scripts/docker-run-hadoop.sh 2
```

Build and copy Biohadoop to Hadoop environment:
```
$ git clone https://github.com/gappc/biohadoop.git
$ chmod +x ./biohadoop/scripts/*.sh
$ ./biohadoop/scripts/copy-files.sh
```

Build and copy example algorithms to Hadoop environment:
```
$ git clone https://github.com/gappc/biohadoop-algorithms.git
$ chmod +x ./biohadoop-algorithms/scripts/*.sh
$ ./biohadoop-algorithms/scripts/copy-algorithms.sh
```

Run the Echo example in Hadoop (use this command in the red terminal - if there is no red terminal, please check [https://github.com/gappc/docker-biohadoop](https://github.com/gappc/docker-biohadoop)). This example uses `biohadoop-0.5.1-SNAPSHOT.jar`:
```
yarn jar /tmp/lib/biohadoop-0.5.1-SNAPSHOT.jar at.ac.uibk.dps.biohadoop.hadoop.BiohadoopClient /biohadoop/conf/biohadoop-echo.json
```

## Requirements
If Biohadoop should run as an Hadoop application, a Hadoop environment is needed. An example environment with all the needed settings can be found at [https://github.com/gappc/docker-biohadoop](https://github.com/gappc/docker-biohadoop), it uses [Docker](https://www.docker.com/) containers to build a multi-node setup.

Biohadoop can also be run as a local standalone application. This is recommended for development purposes. 

## Installation
```
$ git clone https://github.com/gappc/biohadoop
```

The following script builds Biohadoop and copies its files, along with the needed libraries, to the destination Hadoop environment, using secure Copy (scp). The configuration for the copy process can be found and altered in the script file. Currently, the destination user is root, and the destination IP address is 172.17.0.100. Please adjust them to your system.
```
$ biohadoop/scripts/copy-files.sh
```

## Running
To start Biohadoop in a Hadoop environment, provide the full path to the Biohadoop JAR file, set `at.ac.uibk.dps.biohadoop.hadoop.BiohadoopClient` as the class to start, and provide a valid location to a configuration file. The example builds on the Quickstart tutorial, where all libraries and configration files are placed in the right position. This example uses `biohadoop-0.5.1-SNAPSHOT.jar`:
```
$ yarn jar $PATH_TO_BIOHADOOP/biohadoop-0.5.1-SNAPSHOT.jar at.ac.uibk.dps.biohadoop.hadoop.BiohadoopClient /biohadoop/conf/biohadoop-echo.json
```

To start Biohadoop as a standalone application, set the flag `local`, set the correct claspath including the Biohadoop JAR file and the needed dependencies (can be generated by running `mvn -f biohadoop dependency:copy-dependencies`), set `at.ac.uibk.dps.biohadoop.hadoop.BiohadoopApplicationMaster` as the class to start, and provide a valid location to a configuration file. The example builds on the Quickstart tutorial:
```
$ java -Dlocal -cp biohadoop/target/*:biohadoop/target/dependency/*:biohadoop-algorithms/target/*  at.ac.uibk.dps.biohadoop.hadoop.BiohadoopApplicationMaster /tmp/testall/biohadoop-algorithms/conf/biohadoop-echo.json
```

## Examples
Examples of Biohadoop programs can be found at [https://github.com/gappc/biohadoop-algorithms](https://github.com/gappc/biohadoop-algorithms). You can use them to try out Biohadoop and as a template for your own experiments. Good luck and have fun :)
