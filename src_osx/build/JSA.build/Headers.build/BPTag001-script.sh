#!/bin/sh
javah -classpath "../release/skype.jar" -force -d "${OBJROOT}/Headers" "jp.sf.skype.connector.osx.OSXConnector"
