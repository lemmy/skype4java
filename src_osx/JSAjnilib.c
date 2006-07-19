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
Boolean sending = FALSE;
Boolean statusing = FALSE;

//Global Java declarations
JavaVM* g_Jvm = NULL;
JNIEnv *g_env = NULL;
jobject g_obj = NULL;
jmethodID midReceiveMessage = NULL;
jmethodID midsetConnectedStatus = NULL;
jclass clsMain = NULL;

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

	const char *bytes;
	bytes = CFStringGetCStringPtr(aNotificationString, kCFStringEncodingMacRoman);	
	sendToJava(bytes);
	
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
		usleep(500);
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
	
	while (sending == TRUE);
	sending = TRUE;
	if (g_env != NULL) {
		(*g_env)->GetJavaVM(g_env, &g_Jvm);
		(*g_Jvm)->AttachCurrentThread(g_Jvm, (void **)&g_env, NULL);
		clsMain = (*g_env)->FindClass(g_env,"com/skype/connector/osx/OSXConnector");
		if (midReceiveMessage == 0) {
			printDebug("sendToJava() method not found");
			return;
		}
		(*g_env)->CallStaticVoidMethod(g_env, clsMain, midReceiveMessage,(*g_env)->NewStringUTF(g_env,message));
		(*g_Jvm)->DetachCurrentThread(g_Jvm);
	}
	sending = FALSE;
	
	printDebug("JSAjnilib.sendToJava() end");	
}

/**
 * This method provides a callback function to set the Status of the connection.
 **/
void statusToJava(int status) {
	printDebug("JSAjnilib.statusToJava(int) start");
	
	while (statusing == TRUE);
	statusing = TRUE;
	if (g_env != NULL) {
        (*g_env)->GetJavaVM(g_env, &g_Jvm);
        (*g_Jvm)->AttachCurrentThread(g_Jvm, (void **)&g_env, NULL);
		clsMain = (*g_env)->FindClass(g_env,"com/skype/connector/osx/OSXConnector");
		if (midsetConnectedStatus == 0) {
            printDebug("statusToJava() method not found");
			return;
        }
		(*g_env)->CallStaticVoidMethod(g_env, clsMain, midsetConnectedStatus,status);
		(*g_Jvm)->DetachCurrentThread(g_Jvm);
	}
	statusing = FALSE;
	
	printDebug("JSAjnilib.statusToJava() end");	
}

/**
 * This method intializes the DBus connection but also cache the Java callback methods.
 * Start a new pthread with the mainloop which is used to receive messages from DBus.
 **/
JNIEXPORT void JNICALL Java_com_skype_connector_osx_OSXConnector_init
  (JNIEnv *env, jobject obj, jstring appName){
	printDebug("JSAjnilib. .._OSXConnector_init() start");
	
	//Initialize Java environment for the callbacks.
  	g_env = env;
	g_obj = obj;
	clsMain = (*g_env)->FindClass(g_env,"com/skype/connector/osx/OSXConnector");
	midReceiveMessage = (*g_env)->GetStaticMethodID( g_env, clsMain, "receiveSkypeMessage", "(Ljava/lang/String;)V");
	midsetConnectedStatus = (*g_env)->GetStaticMethodID( g_env, clsMain, "setConnectedStatus", "(I)V");

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
