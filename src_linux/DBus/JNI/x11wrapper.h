/*
 * Copyright (c) 2006 Bart Lamot <bart.almot@gmail.com> 
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * Skype4Java is licensed under either the Apache License, Version 2.0 or
 * the Eclipse Public License v1.0.
 * You may use it freely in commercial and non-commercial products.
 * You may obtain a copy of the licenses at
 *
 *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
 *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
 *
 * If it is possible to cooperate with the publicity of Skype4Java, please add
 * links to the Skype4Java web site <https://developer.skype.com/wiki/Java_API> 
 * in your web site or documents.
 * 
 * Contributors: 
 * Bart Lamot - initial API and implementation
 */
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

