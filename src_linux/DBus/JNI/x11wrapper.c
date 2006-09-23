/*******************************************************************************
 * Copyright (c) 2006 Bart Lamot (bart.lamot@gmail.com)
 *
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 *
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * File: x11wrapper.c
 * Contains the code to connect to Skype using X11 messaging. 
 * Uses javawrapper.c to to do callback functions to
 * Java.
 ******************************************************************************/
#include "x11wrapper.h" 
#include <X11/Xlib.h>
#include <X11/Xatom.h>

#define BUFSIZE 512
Display *disp;
Window dummyWindow;
Window skype_win = (Window)-1;
int loop = 1;


 /**
  * Function called by the javawrapper to initialize the X11 connection.
  * Returns the Status value.
  **/
 int x11InitSkypeConnection() {
	return 1;
}

 /**********************************
  * Sends a message to the Skype client using X11.
  * This method is called by the javawrapper.c methods,
  * Returns a replystring.
  **********************************/
 char *x11SendToSkype(const char *msg) {
        Display *display;
        display = XOpenDisplay(NULL);
	if(x11sendMessageInternal(skype_win,msg, display, dummyWindow) == 0){
		printf("x11SendToSkype failed\r\n");
	} 
	return NULL; 
 } 


int x11DetectSkype() {
        Display *display;
	display = XOpenDisplay(NULL);
        int screen = XDefaultScreen(display);
        Window rootWindow = XRootWindow(display,screen);

        Atom skype_inst = XInternAtom(display, "_SKYPE_INSTANCE", True);

        Atom type_ret;
        int format_ret;
        unsigned long nitems_ret;
        unsigned long bytes_after_ret;
        unsigned char *prop;
        int status;

        status = XGetWindowProperty(display, rootWindow, skype_inst, 0, 1, False, XA_WINDOW, &type_ret, &format_ret, &nitems_ret, &bytes_after_ret, &prop);

        // sanity check
        if(status != Success || format_ret != 32 || nitems_ret != 1)
        {
                skype_win = -1;
                printf("Skype not detected, status %i \r\n",status);
		return 0;
        }
        else
        {
                skype_win = * (const unsigned long *) prop & 0xffffffff;
                //printf("Skype found, window id %d \r\n",skype_win);
        }
	return 1;
}

// for intercepting X Server error codes from XSendEvent
static XErrorHandler old_handler = 0;
static int xerror = 0;

int xmerrhandler(Display* dpy, XErrorEvent* err)
{
        (void)dpy;
        xerror = err->error_code;
	printf("xmerrhandler() error code: %d\r\n",xerror);
        return 0; // ignore the error
}

static void trap_errors()
{
        xerror = 0;
        old_handler = XSetErrorHandler(xmerrhandler);
}

static int untrap_errors()
{
        XSetErrorHandler(old_handler);
        return (xerror != BadValue) && (xerror != BadWindow);
}


int x11sendMessageInternal(Window w_P, const char* message_P, Display *disp, Window handle_P)
{
	Atom atom1 = XInternAtom( disp, "SKYPECONTROLAPI_MESSAGE_BEGIN", False );
	Atom atom2 = XInternAtom( disp, "SKYPECONTROLAPI_MESSAGE", False );
	unsigned int pos=0;
	unsigned int len = strlen( message_P);
	XEvent e;
	int ok;

	memset(&e, 0, sizeof(e));
	e.xclient.type = ClientMessage;
	e.xclient.message_type = atom1; //Only first message
	e.xclient.display = disp;
	e.xclient.window = handle_P;
	e.xclient.format = 8;	//Bytes

	trap_errors();
	do {
		unsigned int i;
		for (i=0; i <20 && i+pos <= len; i++) 
			e.xclient.data.b[i]=message_P[i+pos];
		XSendEvent( disp, w_P, False, 0, &e);

		e.xclient.message_type = atom2;
		pos += i;
	} while( pos <= len);
	XSync(disp, False);
	ok = untrap_errors();

	if (!ok)
		printf("x11sendMessageInternal() sending failed\r\n");
	return ok;
}

/**
 * This method initializes the connection to Skype client.
 * First check if Skype is available.
 * Second create a dummy/hidden window.
 * Start a loop waiting for events.
**/
void *x11Mainloop(void *args){
	(void)args;
	disp = XOpenDisplay(getenv("DISPLAY"));
        int status;
        if (x11DetectSkype()==1) {
                status = 0;
                //To receive X events, we need a hidden dummy window.
		Window root = DefaultRootWindow( disp );
                dummyWindow = XCreateSimpleWindow( disp, root, 0,0,1,1,0,
                                                BlackPixel( disp, DefaultScreen(disp)),
                                                BlackPixel(disp, DefaultScreen(disp)) );
                XFlush(disp);
        } else {
		//No Skype to be found, let's inform java client.
                status = 3;
        }
        statusToJava(status);

	XEvent an_event;
	char buf[21]; //Events cant be longer
	char buffer[17000];
	int i;
	while(loop == 1) {
		XNextEvent(disp, &an_event);
		switch(an_event.type) {
			case ClientMessage:
				if (status == 0) {
					status=1;
					statusToJava(status);
				}
				if (an_event.xclient.format != 8)
					break;
				for (i=0; i< 20 && an_event.xclient.data.b[i] != '\0'; i++)
					buf[i] = an_event.xclient.data.b[i];
				buf[i] ='\0';
				strcat(buffer,buf);
				if (i<20) //last fragment
				{
					//printf("ClientMessage buffer: |||%s|||%ld|||\r\n",buffer,an_event.xclient.serial);
					sendToJava(buffer);
					memset(buffer, '\0', 17000);
				}
				break;
			default:
				break;
		}
	}
 }

 void x11StopMainloop(){
	loop = 0;
	XCloseDisplay(disp);
 }
