/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies
 * this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 * Bart Lamot - good javadocs
 * Adrian Cockcroft - fixed null friend problem
 ******************************************************************************/
package com.skype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.ConnectorMessageEvent;
import com.skype.connector.ConnectorListener;

/**
 * Implements the AP2AP API.
 * 
 * @see https://developer.skype.com/Docs/ApiDoc/Application_to_application_commands
 * @author Koji Hisano
 */
public final class Application extends SkypeObject {
    /**
     * Collection of Application objects.
     */
    private static final Map<String, Application> applications = new HashMap<String, Application>();
    
    /**
     * Returns the Application object by the specified id.
     * @param id whose associated Application object is to be returned.
     * @return Application object with ID == id.
     */
    static Application getInstance(final String id) throws SkypeException {
        synchronized(applications) {
            if (!applications.containsKey(id)) {
                applications.put(id, new Application(id));
            }
            return applications.get(id);
        }
    }

    /**
	 * Application name, used to register with the Skype client.
	 */
	private final String name;

	/**
	 * Used for synchronisation.
	 */
	private final Object connectMutex = new Object();

	/**
	 * List of listeners.
	 */
	private final List<ApplicationListener> listeners = Collections
			.synchronizedList(new ArrayList<ApplicationListener>());

	/**
	 * Table of streams.
	 */
	private final Map<String, Stream> streams = new HashMap<String, Stream>();

	/**
	 * Listener for messages received through Skype.
	 */
	private final ConnectorListener dataListener = new DataListener();

	/**
	 * Boolean to check the application state.
	 */
	private boolean isFinished;

	/**
	 * Ending synchronisation helper field.
	 */
	private final Object isFinishedFieldMutex = new Object();

	/**
	 * Shutdownhook thread for cleaning up all instances.
	 */
	private Thread shutdownHookForFinish;

	/**
	 * Application exception handler.
	 */
	private SkypeExceptionHandler exceptionHandler;

	/**
	 * Constructor.
	 * 
	 * @param newName
	 *            An arbitrary name to identify the application that will be
	 *            exchanging data.
	 */
	private Application(final String newName) throws SkypeException {
		assert newName != null;
		this.name = newName;
        initialize();
	}

    /**
     * Initializes the AP2AP.
     * 
     @throws SkypeException when connection is gone bad.
     *             if connection could not be established.
     */
    void initialize() throws SkypeException {
        try {
            String response = Connector.getInstance().execute(
                    "CREATE APPLICATION " + name);
            Utils.checkError(response);
            Connector.getInstance().addConnectorListener(
                    dataListener);
            shutdownHookForFinish = new Thread() {
                @Override
                public void run() {
                    try {
                        Connector.getInstance().execute(
                                "DELETE APPLICATION "
                                        + Application.this.getName());
                    } catch (ConnectorException e) {
                        //because we are already stopping the app ignore the errors.
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownHookForFinish);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

	/**
	 * Enable nice printing of Object, by returning the app name.
	 * 
	 * @return AP2AP application name.
	 */
	public String toString() {
		return getName();
	};

	/**
	 * End the AP2AP data connection.
	 * 
	 @throws SkypeException when connection is gone bad.
	 */
	public void finish() throws SkypeException {
		synchronized (isFinishedFieldMutex) {
			if (!isFinished) {
				Connector.getInstance().removeConnectorListener(
						dataListener);
				Runtime.getRuntime().removeShutdownHook(shutdownHookForFinish);
				isFinished = true;
				try {
					String response = Connector.getInstance().execute(
							"DELETE APPLICATION " + getName());
					Utils.checkError(response);
				} catch (ConnectorException e) {
					Utils.convertToSkypeException(e);
				}
			}
		}
	}

	/**
	 * Return the application name.
	 * 
	 * @return the application name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Find connetable users. Connections are only allowed to connectable users,
	 * online parties who are in the user's contact list or have active ongoing
	 * communication with the user.
	 * 
	 * @return Stream of users.
	 @throws SkypeException when connection is gone bad.
	 */
	public Stream[] connectToAll() throws SkypeException {
		return connect(getAllConnectableFriends());
	}

	/**
	 * Setup an AP2AP connection with a Friend.
	 * 
	 * @param friends
	 *            The ppl to start a AP2AP with.
	 * @return The connected streams.
	 @throws SkypeException when connection is gone bad.
	 */
	public Stream[] connect(final Friend... friends) throws SkypeException {
		Utils.checkNotNull("friends", friends);
		synchronized (connectMutex) {
			try {
				final Object wait = new Object();
				ConnectorListener connectorListener = new AbstractConnectorListener() {
					public void messageReceived(ConnectorMessageEvent event) {
                        String message = event.getMessage();
						if (message.equals("APPLICATION " + getName()
								+ " CONNECTING ")) {
							synchronized (wait) {
								wait.notify();
							}
						}
					}
				};
				try {
					Connector.getInstance()
							.addConnectorListener(
									connectorListener);
					synchronized (wait) {
						for (Friend friend : friends) {
                            if(friend != null) {
                                String result = Connector.getInstance().execute("ALTER APPLICATION " + getName() + " CONNECT " + friend.getId());
                                Utils.checkError(result);
                            }
						}
						try {
							wait.wait();
						} catch (InterruptedException e) {
							throw new SkypeException(
									"The connecting was interrupted.", e);
						}
					}
					return getAllStreams(friends);
				} catch (ConnectorException e) {
					Utils.convertToSkypeException(e);
					return null;
				} finally {
					Connector.getInstance()
							.removeConnectorListener(
									connectorListener);
				}
			} catch (SkypeException e) {
				for (Stream stream : getAllStreams(friends)) {
					try {
						stream.disconnect();
					} catch (SkypeException e2) {
						// do nothing
					}
				}
				throw e;
			}
		}
	}

	/**
	 * Find a connected AP2AP stream with a connectable user.
	 * 
	 * @param friends
	 *            to search streams for.
	 * @return the found streams.
	 @throws SkypeException when connection is gone bad.
	 */
	public Stream[] getAllStreams(final Friend... friends) throws SkypeException {
		List<Stream> results = new ArrayList<Stream>();
		for (Stream stream : getAllStreams()) {
			Friend friend = stream.getFriend();
			for (Friend comparedFriend : friends) {
				if (friend.equals(comparedFriend)) {
					results.add(stream);
				}
			}
		}
		return results.toArray(new Stream[0]);
	}

	/**
	 * Find all AP2AP streams started.
	 * 
	 * @return all started streams.
	 @throws SkypeException when connection is gone bad.
	 */
	public Stream[] getAllStreams() throws SkypeException {
		String streamIds = Utils.getPropertyWithCommandId("APPLICATION",
				getName(), "STREAMS");
		synchronized (streams) {
			fireStreamEvents(streamIds);
			if ("".equals(streamIds)) {
				return new Stream[0];
			}
			String[] ids = streamIds.split(" ");
			Stream[] results = new Stream[ids.length];
			for (int i = 0; i < ids.length; i++) {
				results[i] = streams.get(ids[i]);
			}
			return results;
		}
	}

	/**
	 * @TODO Fill in this javadoc.
	 * @param newStreamIdList idunno.
	 */
	private void fireStreamEvents(final String newStreamIdList) {
		synchronized (streams) {
			String[] newStreamIds = "".equals(newStreamIdList)
					? new String[0]
					: newStreamIdList.split(" ");
			for (String streamId : newStreamIds) {
				if (!streams.containsKey(streamId)) {
					Stream stream = new Stream(this, streamId);
					streams.put(streamId, stream);
					fireConnected(stream);
				}
			}
			String[] oldStreamIds = streams.keySet().toArray(new String[0]);
			NEXT : for (String oldStreamId : oldStreamIds) {
				for (String newStreamId : newStreamIds) {
					if (oldStreamId.equals(newStreamId)) {
						continue NEXT;
					}
				}
				Stream stream = streams.remove(oldStreamId);
				fireDisconnected(stream);
			}
		}
	}

	/**
	 * Fire an event if a AP2AP connection is connected.
	 * 
	 * @param stream
	 *            The connected stream.
	 */
	private void fireConnected(final Stream stream) {
		assert stream != null;
		ApplicationListener[] myListeners = this.listeners
				.toArray(new ApplicationListener[0]); // to prevent
		// ConcurrentModificationException
		for (ApplicationListener listener : myListeners) {
			try {
				listener.connected(stream);
			} catch (Throwable e) {
				Utils.handleUncaughtException(e, exceptionHandler);
			}
		}
	}

	/**
	 * Fire an event if an AP2AP connection is closed.
	 * 
	 * @param stream
	 *            the closed AP2AP stream.
	 */
	private void fireDisconnected(final Stream stream) {
		assert stream != null;
		ApplicationListener[] myListeners = this.listeners
				.toArray(new ApplicationListener[0]); // to prevent
		// ConcurrentModificationException
		for (ApplicationListener listener : myListeners) {
			try {
				listener.disconnected(stream);
			} catch (Throwable e) {
				Utils.handleUncaughtException(e, exceptionHandler);
			}
		}
	}

	/**
	 * Add a listener for events to this AP2AP implementation.
	 * 
	 * @param listener
	 *            the listener which will be triggered.
	 */
	public void addApplicationListener(final ApplicationListener listener) {
		Utils.checkNotNull("listener", listener);
		listeners.add(listener);
	}

	/**
	 * Remove a listener for this AP2AP implementation. If listener is already
	 * removed nothing happens.
	 * 
	 * @param listener
	 *            The listener that has to be removed.
	 */
	public void removeApplicationListener(final ApplicationListener listener) {
		Utils.checkNotNull("listener", listener);
		listeners.remove(listener);
	}

	/**
	 * This class implements a listener for data packets.
	 * 
	 * @author Koji Hisano
	 * 
	 */
	private class DataListener extends AbstractConnectorListener {
		/**
		 * Message received event method. It checks the name of the AP2AP
		 * application name and strips the SKYPE protocol data from the message.
		 * Then it will call handleData(String) to process the inner data.
		 * @param message the message received.
		 */
        public void messageReceived(ConnectorMessageEvent event) {
            String message = event.getMessage();
			String streamsHeader = "APPLICATION " + getName() + " STREAMS ";
			if (message.startsWith(streamsHeader)) {
				String streamIds = message.substring(streamsHeader.length());
				fireStreamEvents(streamIds);
			}
			final String dataHeader = "APPLICATION " + getName() + " ";
			if (message.startsWith(dataHeader)) {
				handleData(message.substring(dataHeader.length()));
			}
		}

		/**
		 * This method will process the inner data of a data message.
		 * 
		 * @param dataResponse
		 *            the received data.
		 */
		private void handleData(final String dataResponse) {
			try {
				if (isReceivedText(dataResponse)) {
					String data = dataResponse.substring("RECEIVED ".length());
					String streamId = data.substring(0, data.indexOf('='));
					String dataHeader = "ALTER APPLICATION " + getName()
							+ " READ " + streamId;
					String response = Connector.getInstance().executeWithId(
							dataHeader, dataHeader);
					Utils.checkError(response);
					String text = response.substring(dataHeader.length() + 1);
					synchronized (streams) {
						if (streams.containsKey(streamId)) {
							streams.get(streamId).fireTextReceived(text);
						}
					}
				} else if (isReceivedDatagram(dataResponse)) {
					String data = dataResponse.substring("DATAGRAM ".length());
					String streamId = data.substring(0, data.indexOf(' '));
					String datagram = data.substring(data.indexOf(' ') + 1);
					synchronized (streams) {
						if (streams.containsKey(streamId)) {
							streams.get(streamId)
									.fireDatagramReceived(datagram);
						}
					}
				}
			} catch (Exception e) {
				throw new IllegalStateException("can't handle data", e);
			}
		}

		/**
		 * Check if received data is text instead of DATAGRAM.
		 * 
		 * @param dataResponse
		 *            the data to check.
		 * @return true if the data is text.
		 */
		private boolean isReceivedText(final String dataResponse) {
			return dataResponse.startsWith("RECEIVED ")
					&& ("RECEIVED ".length() < dataResponse.length());
		}

		/**
		 * Check if received data is DATAGRAM instead of text.
		 * 
		 * @param dataResponse
		 *            the data to check.
		 * @return true if the data is DATAGRAM.
		 */
		private boolean isReceivedDatagram(final String dataResponse) {
			return dataResponse.startsWith("DATAGRAM ");
		}
	}

	/**
	 * Find user to whom Skype can connect using AP2AP.
	 * 
	 * @return Array of connectable users.
	 @throws SkypeException when connection is gone bad.
	 */
	public Friend[] getAllConnectableFriends() throws SkypeException {
		return getAllFriends("CONNECTABLE");
	}

	/**
	 * Find all users to whom SKype is connecting a AP2AP connection.
	 * 
	 * @return Array of user to whom a connecting AP2AP is in progress.
	 @throws SkypeException when connection is gone bad.
	 */
	public Friend[] getAllConnectingFriends() throws SkypeException {
		return getAllFriends("CONNECTING");
	}

	/**
	 * Find all user with whom we have a established AP2AP connection.
	 * 
	 * @return all AP2AP connected users.
	 @throws SkypeException when connection is gone bad.
	 */
	public Friend[] getAllConnectedFriends() throws SkypeException {
		return getAllFriends("STREAMS");
	}

	/**
	 * Find all user to whom we are sending data using a AP2AP connection.
	 * 
	 * @return an array of users that we are sending to.
	 @throws SkypeException when connection is gone bad.
	 */
	public Friend[] getAllSendingFriends() throws SkypeException {
		return getAllFriends("SENDING");
	}

	/**
	 * Find all users which we have received data from using an AP2AP
	 * connection.
	 * 
	 * @return array of found users.
	 @throws SkypeException when connection is gone bad.
	 */
	public Friend[] getAllReceivedFriends() throws SkypeException {
		return getAllFriends("RECEIVED");
	}

	/**
	 * Search method to find friend with a parameter.
	 * 
	 * @param type
	 *            The searchstring.
	 * @return array of found friends.
	 @throws SkypeException when connection is gone bad.
	 */
	private Friend[] getAllFriends(final String type) throws SkypeException {
		try {
			String responseHeader = "APPLICATION " + getName() + " " + type
					+ " ";
			String response = Connector.getInstance()
					.executeWithId("GET APPLICATION " + getName() + " " + type,
							responseHeader);
			Utils.checkError(response);
			return extractFriends(response.substring(responseHeader.length()));
		} catch (ConnectorException e) {
			Utils.convertToSkypeException(e);
			return null;
		}
	}

	/**
	 * Parse the results of a search action for names.
	 * 
	 * @param list
	 *            with search results.
	 * @return array of parsed users.
	 @throws SkypeException when connection is gone bad.
	 */
	private Friend[] extractFriends(final String list) throws SkypeException {
		assert list != null;
		if ("".equals(list)) {
			return new Friend[0];
		}
		String[] ids = list.split(" ");
		for (int i = 0; i < ids.length; i++) {
			String id = ids[i];
			if (id.contains(":")) {
				ids[i] = id.substring(0, id.indexOf(':'));
			}
		}
		List<Friend> friends = new ArrayList<Friend>();
		for (String id : ids) {
			Friend friend = Skype.getContactList().getFriend(id);
            if (friend == null) {
                continue;
            }
			if (!friends.contains(friend)) {
				friends.add(friend);
			}
		}
		return friends.toArray(new Friend[0]);
	}
}
