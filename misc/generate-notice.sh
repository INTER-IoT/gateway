#!/usr/bin/bash
BASEDIR=$(dirname "$0")
mvn generate-resources -q -f $BASEDIR/license/pom.xml 
