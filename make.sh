#!/bin/bash
if /usr/lib/jvm/jdk8/bin/javac IPConfig.java; then
	/usr/lib/jvm/jdk8/bin/jar cvfe IPConfig.jar IPConfig *.class
fi
rm *.class
/usr/lib/jvm/jdk8/bin/java -jar IPConfig.jar
