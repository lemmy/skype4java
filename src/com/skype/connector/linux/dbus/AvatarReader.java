/*
 * Date: Apr 3, 2011
 *
 * Copyright (C) 2011 Markus Alexander Kuppe. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skype.connector.linux.dbus;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class AvatarReader {

    /**
     * The Skype files that make up the user database
     */
    private static final String[] DBBs = new String[]{/*"user256.dbb",*/ "user1024.dbb", "user4096.dbb", "user16384.dbb", "user32768.dbb", "user65536.dbb"};

    /**
     * JPG Magic markers
     */
    private static final byte[] JPG_START_MARKER = new byte[]{(byte) 0xFF, (byte) 0xD8};
    private static final byte[] JPG_END_MARKER = new byte[]{(byte) 0xFF, (byte) 0xD9};
    
    /**
     * Marker which (appears to) separate user entries in .dbb files
     */
    private static final byte[] L33L_MARKER = "l33l".getBytes();

    private final String profilePath;

    public AvatarReader(final String aUser) {
        profilePath = System.getProperty("user.home") + File.separator + ".Skype" + File.separator + aUser;
    }

    public void readAvatarToFile(final String userId, final String path) {
        try {
            for(int i = 0; i < DBBs.length; i++) {
                File file = new File(profilePath + File.separator + DBBs[i]);
                if(file != null && file.exists()) {
                    // read dbb file into a byte[]
                    final DataInputStream stream = new DataInputStream(new FileInputStream(file));
                    final byte[] bytes = new byte[stream.available()];
                    stream.read(bytes);

                    int pos = 0;
                    while(pos != bytes.length - 1) {
                        int l33l1 = indexOf(bytes, L33L_MARKER, pos);
                        if(l33l1 == -1) {
                            break;
                        }
                        
                        int l33l2 = indexOf(bytes, L33L_MARKER, l33l1 + 1);
                        if(l33l2 == -1) { // end of file
                            l33l2 = bytes.length - 1;
                        }
                        
                        // is userid owner of current l33l block?
                        int user = indexOf(bytes, userId.getBytes(), l33l1, l33l2);
                        if(user != -1) { // current l33l block is user we are looking for
                            
                            int jpgStart = indexOf(bytes, JPG_START_MARKER, l33l1, l33l2);
                            if(jpgStart != -1) { // l33l block contains jpg image

                                int jpgEnd = indexOf(bytes, JPG_END_MARKER, jpgStart, l33l2);
                                if(jpgEnd != -1) { // might happen as well
                                    
                                    // slice off jpg from dbb
                                    byte[] bs = Arrays.copyOfRange(bytes, jpgStart, jpgEnd + 2);
                                    
                                    // write to temp file
                                    FileOutputStream fos = new FileOutputStream(path);
                                    fos.write(bs);
                                    fos.close();
                                    
                                    return;
                                }
                            } else {
                                break;
                            }
                        }
                        
                        // advance to the next l33l marker
                        pos = l33l2;
                    }
                }    
            }

            // write dummy file
            InputStream in = AvatarReader.class.getResourceAsStream("/dummy.jpg");
            FileOutputStream out = new FileOutputStream(path);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param bytes Where to search in
     * @param key The key to search for
     * @param from Must be < bytes.length
     * @return The first occurrence of the given key in bytes
     */
    private int indexOf(byte[] bytes, byte[] key, int from, int to) {
        OUTER: for(int i = from; i < to; i++) {
            // try to match first byte
            if(bytes[i] == key[0]) {
                for(int j = 0; j < key.length; j++) {
                    if(key[j] != bytes[i + j]) {
                        continue OUTER;
                    }
                }
                return i;
            }
        }
        return -1;
    }

    private int indexOf(byte[] bytes, byte[] key, int from) {
        return indexOf(bytes, key, from, bytes.length);
    }
}
