#!/bin/sh

WORKING_DIR=`pwd`
echo $WORKING_DIR

echo "building petclinic monolith ..."
cd $WORKING_DIR/spring-petclinic
mvn clean package -DskipTests

echo "building quarkus owner service ..."
cd $WORKING_DIR/quarkus-owner-service
mvn clean package

echo "building kstreams table joiner ..."
cd $WORKING_DIR/kstreams-table-joiner
mvn clean package

echo "DONE!"
