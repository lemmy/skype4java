#!/bin/sh
javah -classpath "${BUILD_DIR}/SkypeAPIConnector.jar" -force -d "${OBJROOT}/Headers" "com.skype.jsa.connector.osx.SkypeAPIConnector"
