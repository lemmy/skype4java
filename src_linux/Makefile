JDK_DIR = /usr/lib/jvm/java-6-sun

CC = gcc
CFLAGS = -Wall -W -g -fPIC
INCPATH = -I. -I$(JDK_DIR)/include -I$(JDK_DIR)/include/linux

LINK = g++
LFLAGS = -shared -L/usr/X11R6/lib
LIBS = -lX11

libskype.so: SkypeFramework.o
	$(LINK) $(LFLAGS) -o ../res/libskype.so SkypeFramework.o $(LIBS)

SkypeFramework.o: SkypeFramework.c
	javah -classpath ../bin com.skype.connector.linux.SkypeFramework
	$(CC) -c $(CFLAGS) $(INCPATH) -o SkypeFramework.o SkypeFramework.c

clean:
	rm *.h *.o *.so
