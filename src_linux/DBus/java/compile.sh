#!/bin/sh
export LD_LIBRARY_PATH=../JNI/lib
rm *.class
rm com/skype/connector/linux/*.class
javac -classpath .:skype.jar com/skype/connector/linux/LinuxConnector.java
javac -classpath .:skype.jar Main.java
javah -classpath .:skype.jar -d ../JNI com.skype.connector.linux.LinuxConnector``

