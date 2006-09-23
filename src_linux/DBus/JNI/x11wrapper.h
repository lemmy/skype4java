#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>
#include <X11/Xlib.h>
#include "com_skype_connector_linux_LinuxConnector.h"
#include "javawrapper.h"

 char *x11SendToSkype(const char *msg);
 int x11InitSkypeConnection();
 int x11DetectSkype();
 int x11sendMessageInternal(Window w_P, const char* message_P, Display *disp, Window handle_P);
 void *x11Mainloop(void *);
 void x11StopMainloop();

