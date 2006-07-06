#############################################################################
# Command: $(QMAKE) -o Makefile ddummy.pro
#############################################################################

####### Compiler, tools and options

CC       = gcc
CXX      = gcc
LEX      = flex
YACC     = yacc
CFLAGS   = -pipe -Wall -W -g  -DQT_SHARED -DQT_NO_DEBUG -DQT_THREAD_SUPPORT -D_REENTRANT
CXXFLAGS = -pipe -Wall -W -g -DDBUS_API_SUBJECT_TO_CHANGE -ggdb  -DQT_SHARED -DQT_NO_DEBUG -DQT_THREAD_SUPPORT -D_REENTRANT
INCPATH  = -I. -I/usr/include/dbus-1.0 -I/usr/lib/dbus-1.0/include 
LINK     = g++
LFLAGS   =  -shared
LIBS     = $(SUBLIBS) -ldbus-1 -lpthread
AR       = ar cqs
RANLIB   = 
TAR      = tar -cf
GZIP     = gzip -9f
COPY     = cp -f
COPY_FILE= $(COPY)
COPY_DIR = $(COPY) -r
INSTALL_FILE= $(COPY_FILE)
INSTALL_DIR = $(COPY_DIR)
DEL_FILE = rm -f
SYMLINK  = ln -sf
DEL_DIR  = rmdir
MOVE     = mv -f
CHK_DIR_EXISTS= test -d
MKDIR    = mkdir -p

####### Output directory

OBJECTS_DIR = .obj/

####### Files

HEADERS = javawrapper.h \
		dbuswrapper.h 
SOURCES = javawrapper.c \
		dbuswrapper.c
OBJECTS = .obj/javawrapper.o \
		.obj/dbuswrapper.o

MAKE_TARGET = javawrapper
DESTDIR  = 
TARGET   = lib/libJSA.so

first: all
####### Implicit rules

.SUFFIXES: .c .o .cpp .cc .cxx .C

.cpp.o:
	$(CXX) -c $(CXXFLAGS) $(INCPATH) -o $@ $<

.cc.o:
	$(CXX) -c $(CXXFLAGS) $(INCPATH) -o $@ $<

.cxx.o:
	$(CXX) -c $(CXXFLAGS) $(INCPATH) -o $@ $<

.C.o:
	$(CXX) -c $(CXXFLAGS) $(INCPATH) -o $@ $<

.c.o:
	$(CC) -c $(CFLAGS) $(INCPATH) -o $@ $<

####### Build rules

all: clean| Makefile $(TARGET) 

$(TARGET):  $(OBJECTS)   
	$(LINK) $(LFLAGS) -o $(TARGET) $(OBJECTS) $(OBJCOMP) $(LIBS)

dist: 
	@mkdir -p .obj/dist && $(COPY_FILE) --parents $(SOURCES) $(HEADERS) $(FORMS) $(DIST) .obj/dist/ && ( cd `dirname .obj/dist` && $(TAR) libJSA.tar dist && $(GZIP) libJSA.tar ) && $(MOVE) `dirname .obj/dist`/libJSA.tar.gz . && $(DEL_FILE) -r .obj/dist

clean: 
	-$(DEL_FILE) $(OBJECTS)
	-$(DEL_FILE) $(TARGET) 


####### Sub-libraries

distclean: clean
	-$(DEL_FILE) $(TARGET) $(TARGET)


FORCE:

####### Compile

.obj/javawrapper.o: javawrapper.c
	$(CXX) -c $(CXXFLAGS) $(INCPATH) -o .obj/javawrapper.o javawrapper.c

.obj/dbuswrapper.o: dbuswrapper.c
	$(CXX) -c $(CXXFLAGS) $(INCPATH) -o .obj/dbuswrapper.o dbuswrapper.c

####### Install

install:  

uninstall:  

