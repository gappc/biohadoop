biohadoop
=========

Biohadoop is a framework that provides the capabilities to run parallel algorithms on [Apache Hadoop](http://hadoop.apache.org/). It leverages the algorithm authors from the burden of writing a Hadoop program and provides services that can be used to offload compute intensive work to local threads and remote machines using a simple interface.

## Quickstart
The quickstart uses a pre-configured Hadoop environment from[https://github.com/gappc/docker-biohadoop](https://github.com/gappc/docker-biohadoop) and the examples from [https://github.com/gappc/biohadoop-algorithms](https://github.com/gappc/biohadoop-algorithms). The requrements are Docker > 1.0 and Maven with MVN_HOME set to the correct path.

Build and start environment with one master and two slave nodes:
```
$ git clone https://github.com/gappc/docker-biohadoop.git
$ sudo docker build -t="docker-biohadoop" ./docker-biohadoop
$ chmod +x ./docker-biohadoop/scripts/*.sh
$ ./docker-biohadoop/scripts/docker-run-hadoop.sh 2
```

Build and copy Biohadoop to Hadoop environment:
```
$ git clone https://github.com/gappc/biohadoop
$ ./biohadoop/scripts/copy-files.sh
```

Build and copy example algorithms to Hadoop environment:
```
$ git clone https://github.com/gappc/biohadoop-algorithms
$ ./biohadoop/scripts/copy-algorithms.sh
```

Run the Echo example:
```
yarn jar /tmp/lib/biohadoop-0.0.5-SNAPSHOT.jar at.ac.uibk.dps.biohadoop.hadoop.BiohadoopClient /biohadoop/conf/biohadoop-echo.json
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
$ scripts/copy-files.sh
```

## Running
Biohadoop must be started with one argument, which is the location of the configuration file

To start Biohadoop in a Hadoop environment, give the full path to the Biohadoop JAR file, set `at.ac.uibk.dps.biohadoop.hadoop.BiohadoopClient` as the class to start, and provide a valid HDFS location to a configuration path, e.g.
```
$ yarn jar $PATH_TO_BIOHADOOP/biohadoop-0.0.5-SNAPSHOT.jar at.ac.uibk.dps.biohadoop.hadoop.BiohadoopClient /biohadoop/conf/biohadoop-ga.json
```

## Examples
Examples of Biohadoop programs can be found at [https://github.com/gappc/biohadoop-algorithms](https://github.com/gappc/biohadoop-algorithms). You can use them to try out Biohadoop and as a template for your own experiments.
