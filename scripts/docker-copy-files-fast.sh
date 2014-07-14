#!/bin/bash

if [ -z $1 ]
then
	echo "Usage: docker-copy-files-fast.sh CONFIG_FILE"
	echo "    where CONFIG_FILE contains needed configuration parameters"
	exit 1
fi

# load configuration from file
source $1

LIB_TMP_DIR=/tmp/lib
LIB_HDFS_DIR=/biohadoop/lib
#DATA_TMP_DIR=/tmp/data
#DATA_HDFS_DIR=/biohadoop/data
#CONF_TMP_DIR=/tmp/conf
#CONF_HDFS_DIR=/biohadoop/conf

function build {
  $MVN_HOME/bin/mvn -f $BIOHADOOP_PROJECT_HOME clean install
}

function copyLibRemote {
  echo "Copying libs to remote FS"
  scp -r $BIOHADOOP_PROJECT_HOME/target/$BIOHADOOP_CURRENT root@172.17.0.100:$LIB_TMP_DIR
  
  echo "Copying libs from remote FS to remote HDFS"
  ssh root@172.17.0.100 "/opt/hadoop/current/bin/hdfs dfs -copyFromLocal -f $LIB_TMP_DIR/$BIOHADOOP_CURRENT $LIB_HDFS_DIR/$BIOHADOOP_CURRENT"
  echo "Copying libs from remote FS to remote HDFS - OK"
}

#function copyDataRemote {
#  echo "Copying data to remote FS"
#  ssh root@172.17.0.100 "mkdir -p $DATA_TMP_DIR"
#  ssh root@172.17.0.100 "rm $DATA_TMP_DIR/*"
#  scp -r $BIOHADOOP_PROJECT_HOME/data/* root@172.17.0.100:$DATA_TMP_DIR
#  
#  echo "Copying data from remote FS to remote HDFS"
#  ssh root@172.17.0.100 "/opt/hadoop/current/bin/hdfs dfs -copyFromLocal -f $DATA_TMP_DIR/* $DATA_HDFS_DIR"
#  echo "Copying data from remote FS to remote HDFS - OK"
#}
#
#function copyConfRemote {
#  echo "Copying configs to remote FS"
#  ssh root@172.17.0.100 "mkdir -p $CONF_TMP_DIR"
#  ssh root@172.17.0.100 "rm $CONF_TMP_DIR/*"
# scp -r $BIOHADOOP_PROJECT_HOME/conf/* root@172.17.0.100:$CONF_TMP_DIR
# 
#  echo "Copying configs from remote FS to remote HDFS"
#  ssh root@172.17.0.100 "/opt/hadoop/current/bin/hdfs dfs -copyFromLocal -f $CONF_TMP_DIR/* $CONF_HDFS_DIR"
#  echo "Copying configs from remote FS to remote HDFS - OK"
#}


#### main part ####
build
copyLibRemote
#copyDataRemote
#copyConfRemote
