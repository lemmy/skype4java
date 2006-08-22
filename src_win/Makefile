NODEBUG=1

!include <Win32.Mak>

TARGET = JNIConnector.dll

OBJS = Win32Connector.obj

LIBS =  user32.lib advapi32.lib

RES = 

ALL: $(TARGET)

CC=cl.exe

$(TARGET): $(OBJS) $(RES)
	$(link) $(ldebug) $(dlllflags) -out:$*.dll $(OBJS) $(dlllibsmt)  $(LIBS)

$(OBJS): 

Win32Connector.obj:	Win32Connector.cpp com_skype_connector_win32_Win32Connector.h

.cpp.obj:
	 $(cc) $(cdebug)	\
		-I"$(JAVA_HOME)\include"	\
		-I"$(JAVA_HOME)\include\win32"	\
		$(cflags) $(cvarsmt) /EHsc $*.cpp

clean:
	-del *.obj *.res *.exp *.lib

cleanall:clean
	-del *.dll
