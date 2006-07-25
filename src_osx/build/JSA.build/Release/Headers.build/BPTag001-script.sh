#!/bin/sh
javah -classpath "${BUILT_PRODUCTS_DIR}/OSXConnector.jar" -force -d "${OBJROOT}/Headers" "com.skype.connector.osx.OSXConnector"
