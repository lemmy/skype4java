/*******************************************************************************
 * Copyright (c) 2006 Bart Lamot (bart.lamot@gmail.com)
 *
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 *
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * File: dbuswrapper.c
 * Contains the code to connect to Skype using DBus
 * Uses javawrapper.c to to do callback functions to
 * Java.
 ******************************************************************************/

#include "dbuswrapper.h" 

#define DISPATCH_STATUS_NAME(s)                                               \
                      ((s) == DBUS_DISPATCH_COMPLETE ? "complete" :           \
                       (s) == DBUS_DISPATCH_DATA_REMAINS ? "data remains" : \
                       (s) == DBUS_DISPATCH_NEED_MEMORY ? "need memory" :   \
                       "???")
 
static int runmainloopBool = 0; 
static int sendingMessage = 0;
static int receivingMessage = 0;

DBusConnection *connection  = NULL;
DBusWatch      *g_watch = NULL;
 
 /**
  * Function used by the DBus connection.
  * Called when a watch is added to this connection.
  **/
  dbus_bool_t dbusAddWatchFunction( DBusWatch *watch, void *data ){
     (void) data;
     g_watch = watch;
     return TRUE;
 }

 /**
  * Function used by the DBus connection.
  * Called when a watch is removed from this connection.
  **/
 void dbusRemoveWatchFunction( DBusWatch *watch, void *data ){
     (void) watch;
     (void) data;
 }
 
 /**
  * Function used by the DBus connection.
  * Called when a watch is toggled.
  **/
 void dbusWatchToggleFunction( DBusWatch *watch, void *data ){
     (void) watch;
     (void) data;
 }

 /**
  * Function used by the DBus connection.
  * Called when memory is freed.
  **/
 void dbusFreeFunction( void *memory ){
     (void) memory;
 }

 /**
  * Function used by the DBus connection.
  * Called when a handler is unregistred.
  **/
 static void nm_unregister_handler( DBusConnection * connection, void *user_data ){
     (void) connection;
     (void) user_data;
 }

 /**
  * Function used by the DBus connection.
  * Called when a message is received.
  **/
 static DBusHandlerResult nm_message_handler(
             DBusConnection * connection, DBusMessage * message, void *user_data )
 {
     (void) user_data; 
     DBusMessage *tmpmsg;
     DBusMessageIter iter;
     //API Message pointer.
     char *apim;
 
     tmpmsg = dbus_message_ref( message );
 
     dbus_message_iter_init( tmpmsg, &iter );
     if( dbus_message_iter_get_arg_type( &iter ) != DBUS_TYPE_STRING ){
             fprintf (stderr, "Error: reply is not except format 1\n");
             return FALSE;
     }
     for( ; ; dbus_message_iter_next( &iter ) ){
         apim = dbus_message_iter_get_string( &iter );
         //Send the received message to Java
	 sendToJava(apim); 
	if( !dbus_message_iter_has_next(&iter) )
             break;
     }
     if( dbus_message_get_no_reply( tmpmsg ) != TRUE ){
         DBusMessage *reply = dbus_message_new_method_return( tmpmsg );
         dbus_connection_send(connection, reply, NULL );
         dbus_message_unref( reply );
     }
     //clear the message on the message queue 
     dbus_message_unref( message );
 
     return DBUS_HANDLER_RESULT_HANDLED;
 }

 /**
  * Function called by the javawrapper to initialize the DBus connection.
  * Returns the Status value.
  **/
 int dbusInitSkypeConnection() {
     DBusError error;
     dbus_error_init (&error);
     //Connect to DBus
     connection = dbus_bus_get (DBUS_BUS_SESSION, &error);
     //if connection failed set the Java status to NOT_CONNECTED 
     if (connection == NULL){
         fprintf(stderr, "Failed to open connection to bus: %s\n", error.message);
         dbus_error_free (&error);
        statusToJava(3); 
	return -1;
     }
     //Test if the service is around....
     //Service can be on SYSTEM bus if there is no SESSION bus.
	if (!dbus_bus_service_exists(connection,"com.Skype.API",&error)) {
 	 connection = dbus_bus_get (DBUS_BUS_SYSTEM, &error);
     	 //if connection failed set the Java status to NOT_CONNECTED
     	 if (connection == NULL){
          fprintf(stderr, "Failed to open connection to bus: %s\n", error.message);
          dbus_error_free (&error);
          statusToJava(3);
          return -1;
     	 }
        }
     dbus_error_free (&error);
     DBusObjectPathVTable vtable =
         { &nm_unregister_handler, &nm_message_handler,
                         NULL, NULL, NULL, NULL };

     	dbus_connection_set_watch_functions(
                 connection,
                 dbusAddWatchFunction,
                 dbusRemoveWatchFunction,
                 dbusWatchToggleFunction,
                 NULL,
                 dbusFreeFunction );
 
     	dbus_connection_register_object_path(
         connection, "/com/Skype/Client", &vtable, NULL);
	//Added watch functions
        //Set Java status to PENDING_AUTHORIZATION
	statusToJava(0);
	return 0;
}

/**
 * Detect Skype on DBus.
 * Returns 0 when Skype is not found.
 * Returns 1 when Skype is found.
**/
int dbusDetectSkype() {
     DBusError error;
     dbus_error_init (&error);
     //Connect to DBus
     connection = dbus_bus_get (DBUS_BUS_SESSION, &error);
     if (connection == NULL){
        dbus_error_free (&error);
        return 0;
     }
     //Test if the service is around....
     //Service can be on SYSTEM bus if there is no SESSION bus.
     if (!dbus_bus_service_exists(connection,"com.Skype.API",&error)) {
       connection = dbus_bus_get (DBUS_BUS_SYSTEM, &error);
       if (connection == NULL){
         dbus_error_free (&error);
         return 0;
       }
       if (!dbus_bus_service_exists(connection,"com.Skype.API",&error)) {
	 return 0;
       }
     }
     return 1;
}

 /**********************************
  * Stops the main loop and kills the DBus connection
  **********************************/
 void dbusStopMainloop() {
 	runmainloopBool = 1; 
	// Current DBus documentation says that disconnect is deprecated, but version .22 doesn't support close() yet.
	//dbus_connection_close(connection);
	dbus_connection_disconnect(connection);
 }

 /**********************************
  * This loop is needed to check the DBus message queue.
  * By using select() we just wait for a flag on the Connection file descriptor.
  * 
  **********************************/
 void *dbusMainloop(void *args) {
       (void) args;	
       //SELECT preparations
	fd_set rfds;
	int retval=-1;
        FD_ZERO(&rfds);
        FD_SET(dbus_watch_get_fd(g_watch), &rfds);
	//SELECT preparations end
 

     DBusDispatchStatus status;
     while (runmainloopBool == 0){
        receivingMessage = 1;
	if( g_watch != NULL )
            dbus_watch_handle ( g_watch, DBUS_WATCH_READABLE );
        dbus_connection_ref( connection );
        status = dbus_connection_dispatch( connection );
        dbus_connection_unref( connection );
        dbus_connection_flush (connection);
        usleep(500);
	if( DBUS_DISPATCH_DATA_REMAINS != status ){
        	receivingMessage = 0;
		//SELECT preparations
        	FD_ZERO(&rfds);
	        FD_SET(dbus_watch_get_fd(g_watch), &rfds);
		//SELECT preparations end
	
		retval = select(dbus_watch_get_fd(g_watch)+1, &rfds, NULL, NULL, NULL);
		while (sendingMessage != 0) {
			usleep(200);
		}
	}
    }   
 }

 /**********************************
  * Sends a message to the Skype client using DBus.
  * This method is called by the javawrapper.c methods,
  *  those methods shouldn't know anything about Connection.
  * Returns a replystring.
  **********************************/
 char *dbusSendToSkype(const char *msg) {
	if (connection != NULL ){
		return dbusSendToSkypeConnection(connection, msg);
	}
	return NULL; 
 } 
 
 /**********************************
  * Sends a message to the Skype client using DBus.
  * Returns a replystring.
  **********************************/
 char *dbusSendToSkypeConnection( DBusConnection * connection,const char *msg){
     sendingMessage = 1;
     while (receivingMessage != 0) {
        usleep(200);
     }
 
     DBusMessage *message;
     DBusMessage *reply;
     DBusError error;
 
     static char returnbuf[64*1024];
 
     char *tmp;
     int reply_timeout = -1;   /*don't timeout*/
 
     dbus_error_init (&error);
 
     /* Construct the message */
     message = dbus_message_new_method_call (
                         "com.Skype.API",    /*service*/
                         "/com/Skype",        /*path*/
                         "com.Skype.API",  /*interface*/
                         "Invoke"); 
     dbus_message_set_auto_activation( message, TRUE );
 
     if( !dbus_message_append_args( message,
                         DBUS_TYPE_STRING, msg,
                         DBUS_TYPE_INVALID ) ){
         fprintf (stderr, "Error: reply is not except format\n");
         dbus_error_free (&error);
        statusToJava(3); 
     	sendingMessage = 0;
	return returnbuf;
     }
 
     reply_timeout = -1;   /*don't timeout*/
     reply = dbus_connection_send_with_reply_and_block (connection,
                         message,
                         reply_timeout,
                         &error);

 
     if (dbus_error_is_set (&error)){
         fprintf (stderr, "Error in send_with_reply_and_block: %s\n", error.message);
         dbus_error_free (&error);
        statusToJava(3); 
     	sendingMessage = 0;
	return returnbuf;
     }
 
     dbus_message_get_args( reply, &error,
                                 DBUS_TYPE_STRING, &tmp,
                                 DBUS_TYPE_INVALID);
     if (dbus_error_is_set (&error)){
         fprintf (stderr, "Error in dbus_message_get_args: %s\n", error.message);
         dbus_error_free (&error);
        statusToJava(3); 
     	sendingMessage = 0;
	 return returnbuf;
     }
 
     strcpy( returnbuf, tmp );
 
     dbus_message_unref( reply );
     
     dbus_message_unref( message );
     dbus_error_free (&error);
     //Added a microsleep to prevent crashes.
     usleep(500); 
     sendToJava(returnbuf); 
     sendingMessage = 0;
     return returnbuf;
 }

