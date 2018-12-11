#!/usr/bin/bash
BASEDIR=$(dirname "$0")
mvn -q com.mycila:license-maven-plugin:3.0:format -f src/pom.xml -Dlicensefile=$BASEDIR/license/license-header.txt
