/*
 *  JSAjnilib.c
 *  JSA
 *
 *  Created by Bart on 17-8-05.
 *  Copyright (c) 2005 __MyCompanyName__. All rights reserved. 
 *
 */
#include "Carbon/Carbon.h" 
#include "Skype/Skype.h"
#include "jp_sf_skype_connector_osx_OSXConnector.h"
#include <CoreFoundation/CFString.h>

// cache for the JNIEnv* pointer
JNIEnv* g_pEnv = NULL;
// cache for the JavaVM* pointer
JavaVM* g_pJvm = NULL;
// cache for the class
jclass g_pClass = NULL;
// method to be called.
jmethodID onskypeapistatus = NULL; //(*g_pEnv)->GetStaticMethodID(g_pEnv,cls, "onSkypeAPIStatus","(I)V");
jmethodID oncallback = NULL; //(*g_pEnv)->GetStaticMethodID(g_pEnv, jklass, "onCallback","(ILjava/lang/String;)V");

struct SkypeDelegate mySkypeDelegate;

//declarations
void SkypeFoundToJava(jint);
void SkypeMsgToJava(unsigned long ,const char* );

//START CODE

void SkypeNotificationReceived(CFStringRef str){
	printf("SkypeNotificationReceived \n");
	
	const char * bytes; // = CreateUTF8CStingFromCFString(aNotificationString);
	bytes = CFStringGetCStringPtr(str, kCFStringEncodingMacRoman);
	if (bytes == NULL) {
	    CFIndex length = CFStringGetLength(str);
		char localBuffer[length];
		Boolean success;
		success = CFStringGetCString(str, localBuffer, 10,kCFStringEncodingMacRoman);
	}
	printf(" string: %s \n",bytes);
	SkypeMsgToJava(0,bytes);
}

void SkypeAttachResponse(unsigned int aAttachResponseCode){
	printf("SkypeAttachResponse \n");
	SkypeFoundToJava(4);		
}

void SkypeBecameAvailable(CFPropertyListRef aNotification){	
	printf("SkypeBecameAvailable \n");
	SkypeFoundToJava(5);	
}

void SkypeBecameUnavailable(CFPropertyListRef aNotification){
	printf("SkypeBecameUnavailable \n");	
	SkypeFoundToJava(4);		
	
}

void init() {
   mySkypeDelegate.SkypeNotificationReceived = SkypeNotificationReceived;
   mySkypeDelegate.SkypeAttachResponse = SkypeAttachResponse;
   mySkypeDelegate.SkypeBecameAvailable = SkypeBecameAvailable;
   mySkypeDelegate.SkypeBecameUnavailable = SkypeBecameUnavailable;
   mySkypeDelegate.clientApplicationName = CFSTR("Skype Java API");
   
   SetSkypeDelegate(&mySkypeDelegate);
   
   ConnectToSkype();
}

JNIEXPORT jint JNICALL Java_jp_sf_skype_connector_osx_OSXConnector_native_1method(JNIEnv *env, jobject this, jstring arg) {
  /* Convert to UTF8 */
  const char *argutf  = (*env)->GetStringUTFChars(env, arg, JNI_FALSE);

	init();
	
  /* Release created UTF8 string */
  (*env)->ReleaseStringUTFChars(env, arg, argutf);

  return 0;
}
JNIEXPORT jint JNICALL Java_jp_sf_skype_connector_osx_OSXConnector_nativeInit
  (JNIEnv *env, jclass jklass)
 {	
	//get the jvm pointer and set it to the global jvm pointer.
	(*env)->GetJavaVM(env, &g_pJvm);
	//now attach Threads...
	(*g_pJvm)->AttachCurrentThread(g_pJvm, (void **)&g_pEnv, NULL);
	
	g_pClass = jklass;
	onskypeapistatus = (*g_pEnv)->GetStaticMethodID(g_pEnv,jklass, "onSkypeAPIStatus","(I)V");
	oncallback = (*g_pEnv)->GetStaticMethodID(g_pEnv, jklass, "onCallback","(ILjava/lang/String;)V");
	
	jint status = 0;
	(*g_pEnv)->CallStaticIntMethod(g_pEnv,g_pClass, onskypeapistatus , status);
    //(*g_pJvm)->DetachCurrentThread(g_pJvm);
    init();
	
	return 0;
}

JNIEXPORT jint JNICALL Java_jp_sf_skype_connector_osx_OSXConnector_nativeDestroy
  (JNIEnv *env, jclass jklass)
 {
	DisconnectFromSkype();
	return 0;
}

JNIEXPORT jint JNICALL Java_jp_sf_skype_connector_osx_OSXConnector_nativeSendMessage
  (JNIEnv *env, jclass jklass, jstring msg) {
		printf("nativeSendMessage start \n");
		const char *str  = (*env)->GetStringUTFChars(env, msg, JNI_FALSE);
		printf("Sendmessage -->%s<-- lengte: %i\n",str,strlen(str));
		CFStringRef msgString = CFStringCreateWithCString (kCFAllocatorDefault,str,kCFStringEncodingASCII);
		SendSkypeCommand(msgString);
		CFRelease(msgString);
		(*env)->ReleaseStringUTFChars(env, msg, str);
        return 0;		 
}

void SkypeMsgToJava(unsigned long skypeID,const char* message) {
	printf("SkypeMsgToJava start \n");

	//get the jvm pointer and set it to the global jvm pointer.
	(*g_pEnv)->GetJavaVM(g_pEnv, &g_pJvm);
	(*g_pJvm)->AttachCurrentThread(g_pJvm, (void **)&g_pEnv, NULL);
	if(g_pEnv == NULL) {
		//ErrorHandler(_T("SkypeFoundToJava; cannot do a callback"));
		return;
	}
	//(*g_pEnv)->ExceptionClear(g_pEnv);
    (*g_pEnv)->CallStaticVoidMethod(g_pEnv, g_pClass, oncallback , skypeID, (*g_pEnv)->NewStringUTF(g_pEnv,message));				

	(*g_pJvm)->DetachCurrentThread(g_pJvm);
	
	printf("SkypeMsgToJava end \n");
}

void SkypeFoundToJava(jint status) {
	printf("SkypeFoundToJava start \n");
	//get the jvm pointer and set it to the global jvm pointer.
	(*g_pEnv)->GetJavaVM(g_pEnv, &g_pJvm);
	(*g_pJvm)->AttachCurrentThread(g_pJvm, (void **)&g_pEnv, NULL);
	if(g_pEnv == NULL) {
		//ErrorHandler(_T("SkypeFoundToJava; cannot do a callback"));
		return;
	}
	//(*g_pEnv)->ExceptionClear(g_pEnv);
    (*g_pEnv)->CallStaticIntMethod(g_pEnv,g_pClass, onskypeapistatus , status);
	(*g_pJvm)->DetachCurrentThread(g_pJvm);

	printf("SkypeFoundToJava end \n");
}

