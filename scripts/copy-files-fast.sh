#!/bin/bash

# Set Maven home directory
if [ "$#" -eq 1 ]
then
  MVN_HOME=$1
fi

# Test for parameters
if [ -z $MVN_HOME ]
then
  echo "Environment variable MVN_HOME (Maven home directory) must be set. As an alternative, provide MVN_HOME as an argument"
  echo "    e.g. copy-files.sh /opt/maven"
  exit 1
fi

# Set dir
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Set destination properties
DEST_USER=root
DEST_IP=172.17.0.100

# Set Biohadoop home directory
BIOHADOOP_PROJECT_HOME=$DIR/..

# Set Biohadoop version
BIOHADOOP_CURRENT=biohadoop-[0-9]*.jar

# Set remote lib dirs
LIB_TMP_DIR=/tmp/lib
LIB_HDFS_DIR=/biohadoop/lib

function build {
  echo "Building Biohadoop with Maven"
  $MVN_HOME/bin/mvn -f $BIOHADOOP_PROJECT_HOME clean install
  if [ "$?" -ne 0 ]
  then
    echo "Error while building Biohadoop"
    exit 1
  fi
}

function copyLibRemote {
  echo "Copying libs to remote FS"
  scp $BIOHADOOP_PROJECT_HOME/target/$BIOHADOOP_CURRENT $DEST_USER@$DEST_IP:$LIB_TMP_DIR
  
  echo "Copying libs from remote FS to remote HDFS"
  ssh $DEST_USER@$DEST_IP "/opt/hadoop/current/bin/hdfs dfs -copyFromLocal -f $LIB_TMP_DIR/$BIOHADOOP_CURRENT $LIB_HDFS_DIR/$BIOHADOOP_CURRENT"
  echo "Copying libs from remote FS to remote HDFS - OK"
}

# Build and copy to remote
build
copyLibRemote

