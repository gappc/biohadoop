#!/bin/bash

echo $BH_HOME

TEMP=`getopt -o a --longoptions mvn-home:,user:,host:,hdfs-cmd:,bh-dir:,bh-jar:,lib-dir:,lib-hdfs: -n 'copy-files.sh' -- "$@"`

if [ $? != 0 ] ; then echo "Terminating..." >&2 ; exit 1 ; fi

eval set -- "$TEMP"

unset MVN_HOME
unset USER
unset HOST
unset HDFS_CMD
unset BIOHADOOP_PROJECT_DIR
unset BIOHADOOP_JAR
unset LIB_TMP_DIR
unset LIB_HDFS_DIR

while true ; do
	case "$1" in
		--mvn-home) echo asd;MVN_HOME=$2 ; shift 2 ;;
		--user) USER=$2 ; shift 2 ;;
		--host) HOST=$2 ; shift 2 ;;
		--hdfs-cmd) HDFS_CMD=$2 ; shift 2 ;;
		--bh-dir) BIOHADOOP_PROJECT_DIR=$2 ; shift 2 ;;
		--bh-jar) BIOHADOOP_JAR=$2 ; shift 2 ;;
		--lib-dir) LIB_TMP_DIR=$2 ; shift 2 ;;
		--lib-hdfs) LIB_HDFS_DIR=$2 ; shift 2 ;;
		--) shift ; break ;;
		*) echo "Internal error!" ; exit 1 ;;
	esac
done

function usage {
  echo "Usage: --mvn-home Maven home dir --user Remote user --host Remote host --hdfs-cmd HDFS command on Hadoop --bh-dir Biohadoop project dir --bh-jar Biohadoop JAR file name --lib-dir Temporary lib dir on Hadoop --lib-hdfs Lib dir in HDFS"
  exit 1
}

function build {
  echo "Building Biohadoop with Maven"
  $MVN_HOME/bin/mvn -f $BIOHADOOP_PROJECT_DIR clean install
  if [ "$?" -ne 0 ]
  then
    echo "Error while building Biohadoop"
    exit 1
  fi
}

function copyLibRemote {
  echo "Copying libs to remote FS"
  ssh $USER@$HOST "mkdir -p $LIB_TMP_DIR"
  ssh $USER@$HOST "rm $LIB_TMP_DIR/*"
  scp -r $BIOHADOOP_PROJECT_DIR/target/dependency/* $USER@$HOST:$LIB_TMP_DIR
  scp $BIOHADOOP_PROJECT_DIR/target/$BIOHADOOP_JAR $USER@$HOST:$LIB_TMP_DIR

  echo "Copying libs from remote FS to remote HDFS"
  ssh $USER@$HOST "$HDFS_CMD dfs -rm -r $LIB_HDFS_DIR"
  ssh $USER@$HOST "$HDFS_CMD dfs -mkdir -p $LIB_HDFS_DIR"
  ssh $USER@$HOST "$HDFS_CMD dfs -copyFromLocal -f $LIB_TMP_DIR/* $LIB_HDFS_DIR/"
  echo "Copying libs from remote FS to remote HDFS - OK"
}

echo $MVN_HOME
echo $USER
echo $HOST
echo $HDFS_CMD
echo $BIOHADOOP_PROJECT_DIR
echo $BIOHADOOP_JAR
echo $LIB_TMP_DIR
echo $LIB_HDFS_DIR

if [ -z "$MVN_HOME" ] | [ -z "$USER" ] | [ -z "$HOST" ] | [ -z "$HDFS_CMD" ] | [ -z "$BIOHADOOP_PROJECT_DIR" ] | [ -z "$BIOHADOOP_JAR" ] | [ -z "$LIB_TMP_DIR" ] | [ -z "$LIB_HDFS_DIR" ] 
then
  usage
  exit 1
fi

# Build and copy to remote
build
copyLibRemote

