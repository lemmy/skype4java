/*
 *  JSAjnilib.c
 *  JSA
 *
 *  Created by Bart Lamot on 7-7-06.
 *  Copyright (c) 2006 __MyCompanyName__. All rights reserved. 
 *
 */

#include "com_skype_connector_osx_OSXConnector.h"
#include <Skype/Skype.h>
#include <Carbon/Carbon.h>

//Global JSAlib declarations
Boolean THREADED = FALSE;
Boolean debug = FALSE;
Boolean disposed = FALSE;
unsigned long MSG_MAX = 16000;

//Global Skype declarations
struct SkypeDelegate mySkypeDelegate;

//Java callback function declarations
void sendToJava(const char *);
void statusToJava(int);

//***************************************************************************************************
// HELPER CODE BLOCK
//***************************************************************************************************
void printDebug(char *message) {
	if (debug == TRUE) {
	 printf(message);
	 printf("\n");
	 fflush(stdout);
	}
}


//***************************************************************************************************
// SKYPE API CODE BLOCK
//***************************************************************************************************

/**
* This method gets called by Skype API when a message is received.
* It translates the message from CFStringRef to a char * and then
* it will try to send the message to Java.
**/
void SkypeNotificationReceived(CFStringRef aNotificationString){
	printDebug("JSAjnilib.SkypeNotificationReceived() start");

	//const char *bytes;
	//bytes = CFStringGetCStringPtr(aNotificationString, kCFStringEncodingMacRoman);	
	
	//get the path as a UTF-8 C string.
	//this is harder than Cocoa programmers expect. ;)
	void (*freeFunc)(void *) = NULL;
	char *msgUTF8 = CFStringGetCStringPtr(aNotificationString, kCFStringEncodingUTF8);
	if(!msgUTF8) {
        msgUTF8 = alloca(MSG_MAX);
        if(msgUTF8)
                CFStringGetCString(aNotificationString, msgUTF8, PATH_MAX, kCFStringEncodingUTF8);
        else {
                msgUTF8 = malloc(MSG_MAX);
                if(!msgUTF8) {
                        NSLog(CFSTR("Could not test the existence of %@: could not allocate %lu bytes for message (errno is %s)"), aNotificationString, (unsigned long)MSG_MAX, strerror(errno));
                        return false;
                } else {
                        freeFunc = free;
                        CFStringGetCString(aNotificationString, msgUTF8, MSG_MAX, kCFStringEncodingUTF8);
                }
        }
	}
	sendToJava(msgUTF8);
	
	printDebug("JSAjnilib.SkypeNotificationReceived() end");
}

/**
* This method gets called by Skype API when the status changes.
**/
void SkypeAttachResponse(unsigned int aAttachResponseCode){
	printDebug("JSAjnilib.SkypeAttachResponse(int) start");

	statusToJava(aAttachResponseCode);

	printDebug("JSAjnilib.SkypeAttachResponse(int) end");
}

/**
*
**/
void SkypeBecameAvailable(CFPropertyListRef aNotification){
	printDebug("JSAjnilib.SkypeBecameAvailable() start");
	statusToJava(1);
	printDebug("JSAjnilib.SkypeBecameAvailable() end");
}

/**
*
**/
void SkypeBecameUnavailable(CFPropertyListRef aNotification){
	printDebug("JSAjnilib.SkypeBecameUnavailable() start");
	statusToJava(5);
	printDebug("JSAjnilib.SkypeBecameUnavailable() end");
} 

void initSkypeConnection(CFStringRef cfStrAppname) {
	printDebug("JSAjnilib.initSkypeConnection() start");
	
	mySkypeDelegate.SkypeNotificationReceived = SkypeNotificationReceived;
	mySkypeDelegate.SkypeAttachResponse = SkypeAttachResponse;
	mySkypeDelegate.SkypeBecameAvailable = SkypeBecameAvailable;
	mySkypeDelegate.SkypeBecameUnavailable = SkypeBecameUnavailable;
	mySkypeDelegate.clientApplicationName = cfStrAppname;
	SetSkypeDelegate(&mySkypeDelegate);
	
	ConnectToSkype();
	
	printDebug("JSAjnilib.initSkypeConnection()  end");	
}

void skypeEventLoop() {
	printDebug("JSAjnilib.skypeEventLoop() start");
	while(disposed==FALSE) {		
		RunApplicationEventLoop();
	}
	
	printDebug("JSAjnilib.skypeEventLoop()  end");
}

//***************************************************************************************************
// JAVA CODE BLOCK
//***************************************************************************************************


/**
 * This method does a callback to Java to provide the received DBus message.
 **/
void sendToJava(const char *message) { 
	printDebug("JSAjnilib.sendToJava(char *) start");
	
	 JNIEnv *env;
     JavaVM *jvm;
     JDK1_1AttachArgs *args;
     jsize count;
		
    JNI_GetDefaultJavaVMInitArgs(&args);
    if (!JNI_GetCreatedJavaVMs(&jvm,1,&count))
	{  
		if (!(*jvm)->AttachCurrentThread(jvm,(void **)&env,&args))
		{   
			jstring msg = (*env)->NewStringUTF(env,message);
			jclass OSXConnector  = (*env)->FindClass(env,"com/skype/connector/osx/OSXConnector");
			jmethodID midReceiveMessage = (*env)->GetStaticMethodID(env, OSXConnector, "receiveSkypeMessage", "(Ljava/lang/String;)V");
			(*env)->CallStaticVoidMethod(env, OSXConnector, midReceiveMessage, msg);
		}
	} 	 
	printDebug("JSAjnilib.sendToJava() end");	
}

/**
 * This method provides a callback function to set the Status of the connection.
 **/
void statusToJava(int status) {
	printDebug("JSAjnilib.statusToJava(int) start");
		
	JNIEnv *env;
    JavaVM *jvm;
    JDK1_1AttachArgs *args;
    jsize count;
	 		
    JNI_GetDefaultJavaVMInitArgs(&args);
    if (!JNI_GetCreatedJavaVMs(&jvm,1,&count))
	{  
		if (!(*jvm)->AttachCurrentThread(jvm,(void **)&env,&args))
		{   
			jclass OSXConnector  = (*env)->FindClass(env,"com/skype/connector/osx/OSXConnector");
			jmethodID midsetConnectedStatus = (*env)->GetStaticMethodID(env, OSXConnector, "setConnectedStatus", "(I)V");
			(*env)->CallStaticVoidMethod(env, OSXConnector, midsetConnectedStatus,status);
		}
	} 
	printDebug("JSAjnilib.statusToJava() end");	
}

/**
 * This method intializes the DBus connection but also cache the Java callback methods.
 * Start a new pthread with the mainloop which is used to receive messages from DBus.
 **/
JNIEXPORT void JNICALL Java_com_skype_connector_osx_OSXConnector_init
  (JNIEnv *env, jobject obj, jstring appName){
	printDebug("JSAjnilib. .._OSXConnector_init() start");

	//Convert appname JString into a CFString
	const char *appNameChar  = (*env)->GetStringUTFChars(env, appName, JNI_FALSE);
	CFStringRef cfStrAppname = CFStringCreateWithCString (kCFAllocatorDefault, appNameChar, kCFStringEncodingASCII);	
	//initialise the Skype connection.
	initSkypeConnection(cfStrAppname);
	//Release the Strings.
	(*env)->ReleaseStringUTFChars( env, appName, appNameChar);
	fflush(stdout);
	printDebug("JSAjnilib. .._OSXConnector_init() end");	
}
  

/**
 * This method is called by Java to send a command to Skype.
 **/
JNIEXPORT void JNICALL Java_com_skype_connector_osx_OSXConnector_sendSkypeMessage
  (JNIEnv *env, jobject obj, jstring message){
	printDebug("JSAjnilib. ..OSXConnector_sendSkypeMessage() start");
	
	/* convert message into usable C string */
	const char *Sendstr  = (*env)->GetStringUTFChars(env, message, JNI_FALSE);
	/*send the message*/
	CFStringRef msgString = CFStringCreateWithCString (kCFAllocatorDefault,Sendstr,kCFStringEncodingASCII);	
	 
	SendSkypeCommand(msgString);
		
	/*release strings */
	(*env)->ReleaseStringUTFChars( env, message, Sendstr);
	printDebug("JSAjnilib. ..OSXConnector_sendSkypeMessage() end");
	 
  }

/**
* Stop the connection, remove the delegate.
**/
JNIEXPORT void JNICALL Java_com_skype_connector_osx_OSXConnector_disposeNative
  (JNIEnv *env, jobject obj){
		printDebug("JSAjnilib. ..OSXConnector_disposeNative() start");
		
		disposed=TRUE;
		RemoveSkypeDelegate();
		DisconnectFromSkype();
		
		printDebug("JSAjnilib. ..OSXConnector_disposeNative() end");
  }

/**
* Start the EventLoop.
*/
JNIEXPORT void JNICALL Java_com_skype_connector_osx_OSXConnector_startEventLoop
  (JNIEnv *env, jobject obj) {
	printDebug("JSAjnilib. ..OSXConnector_startEventLoop() start");
		
	skypeEventLoop();
	
	printDebug("JSAjnilib. ..OSXConnector_startEventLoop() end");
  }
  
/**
* Turn debug printing off or on.
*/
JNIEXPORT void JNICALL Java_com_skype_connector_osx_OSXConnector_setDebugPrinting
  (JNIEnv *env , jobject obj, jboolean value) {
  			  
	if (value == JNI_TRUE)
		debug = TRUE;
  }
