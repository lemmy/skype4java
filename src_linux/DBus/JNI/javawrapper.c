/*******************************************************************************
 * Copyright (c) 2006 Bart Lamot (bart.lamot@gmail.com)
 *
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 *
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * File: javawrapper.c
 * Contains the code to implement the native java code and the callbacks to Java.
 * Uses dbuswrapper.c to do the DBus bits.
 * Uses x11wrapper.c to do the X11 bits.
 ******************************************************************************/

#include "javawrapper.h"
#include "com_skype_connector_linux_LinuxConnector.h"
#include "x11wrapper.h"
#include "dbuswrapper.h"


//Global declarations
JavaVM* g_Jvm = NULL;
JNIEnv *g_env = NULL;
jobject g_obj = NULL;
jmethodID midReceiveMessage = NULL;
jmethodID midsetConnectedStatus = NULL;
jclass clsMain = NULL;
int messagingMethod = 0;

/**
 * This method does a callback to Java to provide the received DBus message.
 **/
void sendToJava(char *message) { 
	if (g_env != NULL) {
		(*g_env)->GetJavaVM(g_env, &g_Jvm);
		(*g_Jvm)->AttachCurrentThread(g_Jvm, (void **)&g_env, NULL);
		if (midReceiveMessage == 0) {
			printf("sendToJava() method not found\n");
			return;
		}
		(*g_env)->CallStaticVoidMethod(g_env, clsMain, midReceiveMessage,(*g_env)->NewStringUTF(g_env,message));
	}
}

/**
 * This method provides a callback function to set the Status of the connection.
 **/
void statusToJava(int status) {
        if (g_env != NULL) {
                (*g_env)->GetJavaVM(g_env, &g_Jvm);
                (*g_Jvm)->AttachCurrentThread(g_Jvm, (void **)&g_env, NULL);
                if (midsetConnectedStatus == 0) {
                        printf("statusToJava() method not found\n");
                        return;
                }
                (*g_env)->CallStaticVoidMethod(g_env, clsMain, midsetConnectedStatus,status);
        }
}

/**
 * This method intializes the DBus connection but also cache the Java callback methods.
 * Start a new pthread with the mainloop which is used to receive messages from DBus.
 **/
JNIEXPORT void JNICALL Java_com_skype_connector_linux_LinuxConnector_init
  (JNIEnv *env, jobject obj, jstring appName){
	g_env = env;
	g_obj = obj;
        clsMain = (*g_env)->FindClass(g_env,"com/skype/connector/linux/LinuxConnector");
        midReceiveMessage = (*g_env)->GetStaticMethodID( g_env, clsMain, "receiveSkypeMessage", "(Ljava/lang/String;)V");
        midsetConnectedStatus = (*g_env)->GetStaticMethodID( g_env, clsMain, "setConnectedStatus", "(I)V");
	//Detect which messaging system to use.


	if (x11DetectSkype() == 1) {
		messagingMethod = 0;	
	} else {
		if (dbusDetectSkype() == 1) {
			messagingMethod = 1;
		}
	}

	pthread_t mainloopThread;
	if (messagingMethod == 0) {		
		pthread_create(&mainloopThread, NULL, x11Mainloop, NULL); 
	}

	//DBus
	if (messagingMethod == 1) {
		dbusInitSkypeConnection();
		//create thread to run the mainloop in.
		pthread_create(&mainloopThread, NULL, dbusMainloop, NULL); 
	}
}

/**
 * This method is called by Java to send a command to Skype.
 **/
JNIEXPORT void JNICALL Java_com_skype_connector_linux_LinuxConnector_sendSkypeMessage
  (JNIEnv *env, jobject obj, jstring message){
	/* convert message into usable C string */
	const char *pMessage = (*env)->GetStringUTFChars( env, message, 0);
	/*send the message*/
	if (messagingMethod == 0)
		x11SendToSkype(pMessage);
	if (messagingMethod == 1)
		dbusSendToSkype(pMessage);
	/*release strings */
	(*env)->ReleaseStringUTFChars( env, message, pMessage);
}

/**
 * This method stops the DBus connection and tries to stop the mainloop.
 **/
JNIEXPORT void JNICALL Java_com_skype_connector_linux_LinuxConnector_disposeNative
  (JNIEnv *env, jobject obj){
	//stopMainloop();	
}

