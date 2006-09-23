#include <stdlib.h>
#include <string.h>
#include <dbus/dbus.h>
#include <stdio.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>
#include "com_skype_connector_linux_LinuxConnector.h"
#include "javawrapper.h"

 char *dbusSendToSkypeConnection( DBusConnection *conn,const char *msg);
 char *dbusSendToSkype(const char *msg);
 int dbusInitSkypeConnection();
 void *dbusMainloop(void *);
 void dbusStopMainloop();
 int dbusDetectSkype();

