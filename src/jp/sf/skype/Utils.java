/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Koji Hisano - initial API and implementation
 *******************************************************************************/
package jp.sf.skype;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import jp.sf.skype.connector.Connector;
import jp.sf.skype.connector.ConnectorException;

final class Utils {
    static void convertToSkypeException(ConnectorException e) throws SkypeException {
        SkypeException r;
        if (e instanceof jp.sf.skype.connector.NotAttachedException) {
            r = new NotAttachedException();
        } else if (e instanceof jp.sf.skype.connector.TimeOutException) {
            r = new TimeOutException(e.getMessage());
        } else {
            throw new IllegalStateException("can't convert connector exception to skype exception");
        }
        r.initCause(e);
        throw r;
    }

    static void checkError(String response) throws SkypeException {
        if (response == null) {
            return;
        }
        if (response.startsWith("ERROR ")) {
            throw new CommandFailedException(response);
        }
    }

    static String getProperty(String type, String id, String name) throws SkypeException {
        try {
            String command = "GET " + type + " " + id + " " + name;
            String responseHeader = type + " " + id + " " + name + " ";
            String response = Connector.getInstance().execute(command, responseHeader);
            checkError(response);
            return response.substring((responseHeader).length());
        } catch (ConnectorException e) {
            convertToSkypeException(e);
            return null;
        }
    }

    static String getProperty(String type, String name) throws SkypeException {
        try {
            String command = "GET " + type + " " + name;
            String responseHeader = type + " " + name + " ";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
            return response.substring((responseHeader).length());
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    static String getProperty(String name) throws SkypeException {
        try {
            String command = "GET " + name + " ";
            String responseHeader = name + " ";
            String response = Connector.getInstance().execute(command, responseHeader);
            checkError(response);
            return response.substring(responseHeader.length());
        } catch (ConnectorException e) {
            convertToSkypeException(e);
            return null;
        }
    }

    static void setProperty(String type, String id, String name, String value) throws SkypeException {
        try {
            String command = "SET " + type + " " + id + " " + name + " " + value;
            String responseHeader = type + " " + id + " " + name + " " + value;
            String response = Connector.getInstance().execute(command, responseHeader);
            checkError(response);
        } catch (ConnectorException e) {
            convertToSkypeException(e);
        }
    }

    static void setProperty(String type, String name, String value) throws SkypeException {
        try {
            String command = "SET " + type + " " + name + " " + value;
            String responseHeader = type + " " + name + " " + value;
            String response = Connector.getInstance().execute(command, responseHeader);
            checkError(response);
        } catch (ConnectorException e) {
            convertToSkypeException(e);
        }
    }

    static void setProperty(String name, String value) throws SkypeException {
        try {
            String command = "SET " + name + " " + value;
            String responseHeader = name + " " + value;
            String response = Connector.getInstance().execute(command, responseHeader);
            checkError(response);
        } catch (ConnectorException e) {
            convertToSkypeException(e);
        }
    }

    static void executeWithErrorCheck(String command) throws SkypeException {
        try {
            String response = Connector.getInstance().execute(command);
            checkError(response);
        } catch (ConnectorException e) {
            convertToSkypeException(e);
        }
    }

    static void checkNotNull(String name, Object value) {
        if (value == null) {
            throw new NullPointerException("The " + name + " must not be null.");
        }
    }

    static String[] convertToArray(String listString) {
        String[] array = listString.split(",");
        for (int i = 0, length = array.length; i < length; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }

    static Date parseUnixTime(String time) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(Long.parseLong(time) * 1000);
        calendar.setTimeZone(TimeZone.getDefault());
        return calendar.getTime();
    }

    private Utils() {
    }
}
