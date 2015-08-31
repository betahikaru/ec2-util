#!/bin/sh

func() {
  DIR=$(cd $(dirname $0); pwd)
  JAR_PATH=${DIR}/../target/ec2-util-0.0.1-SNAPSHOT-jar-with-dependencies.jar
  CLASS_NAME=com.betahikaru.aws.main.Ec2CommandMain
  CONF_PATH=${DIR}/../conf/credentials.properties
  java -cp ${JAR_PATH} ${CLASS_NAME} -c ${CONF_PATH} $@
}

func $@
echo $?