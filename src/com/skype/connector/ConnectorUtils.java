/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/> All rights reserved.
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 * Bart Lamot - ExtractFormJar methods.
 ******************************************************************************/
package com.skype.connector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Connector helper class.
 * Generic helper methods for all connectors.
 */
public final class ConnectorUtils {
	
	/**
	 * Check an object if its not null.
	 * If it is a NullPointerException will be thrown.
	 * @param name Name of the object, used in the exception message.
	 * @param value The object to check.
	 */
	public static void checkNotNull(String name, Object value) {
        if (value == null) {
            throw new NullPointerException("The" + name + " must not be null.");
        }
    }

	
	/**
	 * Extract a file for a jarfile on the classpath.
	 * Used to extract library files to the System temp directory.
	 * <code>System.getProperty("java.io.tmpdir")</code>.
	 * @param filename The file to search and extract.
	 * @return true if file could be found and extracted.
	 */
	public static boolean extractFromJarToTemp(String filename) {
		return extractFromJar(filename, filename, System.getProperty("java.io.tmpdir"));
	}
	
	/**
	 * Extract a file for a jarfile on the classpath.
	 * Used to extract library files.
	 * @param filename The file to search and extract.
	 * @param destinationDirectory The directory to place it in.
	 * @return true if file could be found and extracted.
	 */
	public static boolean extractFromJar(String filename, String destinationDirectory) {
		return extractFromJar(filename, filename, destinationDirectory);
	}
	
	/**
	 * Extract a file for a jarfile on the classpath.
	 * Used to extract library files.
	 * @param searchString The path+filename to search for.
	 * @param filename The file to search and extract.
	 * @param destinationDirectory The directory to place it in.
	 * @return true if file could be found and extracted.
	 */
	public static boolean extractFromJar(String searchString, String filename, String destinationDirectory) {
		boolean extracted = false;
    	String classpath = System.getProperty("java.class.path");
    	File jarfile = null;
    	byte[] buf = new byte[1024];
    	String jarFileName;
    	StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
    	//Try each jarfile on the classpath.
    	while (st.hasMoreTokens() && !extracted) {
    		jarFileName = st.nextToken();
    		jarfile = new File(jarFileName);
    		//Only try to read the jarfile if it exists.
    		if (jarfile.exists() && jarfile.isFile()) {
    			 //Check the contents of this Jar file for the searchstring.
    			 FileInputStream fis = null;
				try {
					fis = new FileInputStream(jarFileName);
	    	        BufferedInputStream bis=new BufferedInputStream(fis);
	    	        ZipInputStream zis=new ZipInputStream(bis);
	    	        ZipEntry ze=null;
	    	        while ((ze=zis.getNextEntry())!=null) {
					     if (ze.getName().endsWith(searchString)){
					    	 //File found, now try to extract it.
					     	if (!destinationDirectory.endsWith(File.separator)) { 
					     		destinationDirectory = destinationDirectory+File.separator;
					     	}
					    	 int n;
					    	 FileOutputStream fileoutputstream;
				                fileoutputstream = new FileOutputStream(destinationDirectory+filename);             
					                while ((n = zis.read(buf, 0, 1024)) > -1) {
					                    fileoutputstream.write(buf, 0, n);
					                }
					                fileoutputstream.close();
					                extracted=true;
					     }			         		
					}
				} catch (Exception e) {
					e.printStackTrace();
					extracted = false;
				}
    		}
    	}
		return extracted;
	}
	
	/**
	 * Search for a file in the jars of the classpath, return true if found.
	 * @param searchString The path+filename to search for.
	 * @return true if file could be found.
	 */
	public static boolean isInJar(String searchString) {
		boolean found = false;
    	String classpath = System.getProperty("java.class.path");
    	File jarfile = null;
    	String jarFileName;
    	StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
    	//Try each jarfile on the classpath.
    	while (st.hasMoreTokens() && !found) {
    		jarFileName = st.nextToken();
    		jarfile = new File(jarFileName);
    		//Only try to read the jarfile if it exists.
    		if (jarfile.exists() && jarfile.isFile()) {
    			 //Check the contents of this Jar file for the searchstring.
    			 FileInputStream fis = null;
				try {
					fis = new FileInputStream(jarFileName);
	    	        BufferedInputStream bis=new BufferedInputStream(fis);
	    	        ZipInputStream zis=new ZipInputStream(bis);
	    	        ZipEntry ze=null;
	    	        while ((ze=zis.getNextEntry())!=null) {
					     if (ze.getName().endsWith(searchString)){
					    	 //File found, now try to extract it.
					     	found = true;
					     }			         		
					}
				} catch (Exception e) {
					found = false;
				}
    		}
    	}
		return found;
	}
	
	/**
	 * Return the extended library path on which the JVM looks for lib files.
	 * @return String with extended lib path.
	 */
    protected static String getLibrarySearchPath() {
    	return System.getProperty("java.library.path")+File.pathSeparatorChar+System.getProperty("user.dir")+File.pathSeparatorChar;
    }
    
    /**
     * Check if a lib file is to be found by the JVM.
     * @param libFilename the filename.
     * @return true if the file is found.
     */
    public static boolean checkLibraryInPath(String libFilename) {
    	boolean libfilefound = false;
    	String libpath = getLibrarySearchPath();
    	File libfile = new File("");
    	StringTokenizer st = new StringTokenizer(libpath, File.pathSeparator);
    	while (st.hasMoreTokens() && !libfilefound) {
    		libfile = new File(st.nextToken()+File.separatorChar+libFilename);
    		libfilefound = libfile.exists();
    	}
    	return libfilefound;
    }
	
	/**
	 * The methods of this class should be used staticly.
	 * That is why the constructor is private.
	 */
    private ConnectorUtils() {
    }
}
