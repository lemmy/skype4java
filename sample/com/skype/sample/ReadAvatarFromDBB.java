/**
 * 
 */
package com.skype.sample;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.skype.Friend;
import com.skype.Skype;
import com.skype.SkypeException;

public class ReadAvatarFromDBB {

    private static final String[] DBBs = new String[]{/* "user256.dbb", */"user1024.dbb", "user4096.dbb", "user16384.dbb", "user32768.dbb", "user65536.dbb"};

    /**
     * JPG Magic markers
     */
    private static final byte[] JPG_START_MARKER = new byte[]{(byte) 0xFF, (byte) 0xD8};

    private static final byte[] JPG_END_MARKER = new byte[]{(byte) 0xFF, (byte) 0xD9};

    public static void main(String[] args) throws IOException, SkypeException {
        ReadAvatarFromDBB dbb = new ReadAvatarFromDBB();
        dbb.readAvatarFromDBB("/home/markus/.Skype/lemmster");
    }

    private void readAvatarFromDBB(final String path) throws IOException, SkypeException {
        final Friend[] allFriends = Skype.getContactList().getAllFriends();
        Skype.removeAllListeners();
        // convert all friend names into markers
        int j = 0;
        byte[][] markers = new byte[allFriends.length + 3][];
        for(; j < allFriends.length; j++) {
            Friend friend = allFriends[j];
            markers[j] = friend.getId().getBytes();
        }
        markers[j++] = JPG_START_MARKER;
        markers[j++] = "l33l".getBytes();
        markers[j] = JPG_END_MARKER;
        
        for(int i = 0; i < DBBs.length; i++) {
            final File file = new File(path + File.separator + DBBs[i]);
            System.out.println("\n\n\n" + file.getAbsolutePath());

            // read dbb file into a byte[]
            final DataInputStream stream = new DataInputStream(new FileInputStream(file));
            final byte[] bytes = new byte[stream.available()];
            stream.read(bytes);


            // for all markers print the occurrence in the dbb
            Set set = new TreeSet();
            for(int k = 0; k < markers.length; k++) {
                byte[] key = markers[k];

                // loop over the dbb
                for(int l = 0; l < bytes.length; l++) {
                    int indexOf = indexOf(bytes, key, l);
                    if(indexOf != -1) {
                        if(key == JPG_START_MARKER) {
                            set.add(new LineNumToValue(indexOf, "JPEG--Begin (0xFF,0xD8)"));
                        } else if(key == JPG_END_MARKER) {
                            set.add(new LineNumToValue(indexOf, "JPEG--End (0xFF,0xD9)"));
                        } else {
                            set.add(new LineNumToValue(indexOf, new String(key)));
                        }
                        l = indexOf;
                    } else {
                        break;
                    }
                }
            }
            // print the content of the set
            for(Iterator iterator = set.iterator(); iterator.hasNext();) {
                System.out.println(iterator.next());
            }
        }
    }

    /**
     * @param bytes Where to search in
     * @param key The key to search for
     * @param startFrom Must be < bytes.length
     * @return The first occurrence of the given key in bytes
     */
    private int indexOf(byte[] bytes, byte[] key, int startFrom) {
        OUTER: for(int i = startFrom; i < bytes.length; i++) {
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
    
    private static class LineNumToValue implements Comparable<LineNumToValue> {
        private long lineNum;
        private String value;
        
        public LineNumToValue(int aLineNum, String aValue) {
            lineNum = aLineNum;
            value = aValue;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(LineNumToValue o) {
            if(lineNum == o.lineNum) {
                return 0;
            } else if (lineNum < o.lineNum) {
                return -1;
            }
            return 1;
        }
        
        public String toString() {
            if(value.equals("l33l")) {
                return "\n" + lineNum +  "\t" + value;
            }
            return lineNum +  "\t" + value;
        }
    }
}
