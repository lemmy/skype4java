Skype API for Java <http://skype.sourceforge.jp/>

1.Introduction
2.Preparation
3.Links
4.Development history
5.Copyright and exemption matters


1.Introduction

When I started to use Skype, I was very surprised of the high quality of the voicechat. 
Now, Skype is an indispensable tool to me in work and private. 
Moreover, I was surprised further because Skype API was opened to the public
because it came to be able to make various plug-ins. 

However, developing plug-ins by not Java languages is difficult to me because my
main programming language is Java. 
Therefore, I searched for various Java libraries on the net. 

I found same libraries. But, They are not good to me.
Thus, I decided to make a new library using the winter break of the company oneself. 

The feature compared with other libraries
¥developed by the SWT library of the eclipse project to which a lot of tests are done. 
¥supports AP2AP APIs(P2P framework). 
¥supoorts Skype 2.0 APIs (group and video chat). 

By this library
¥You can notify a new blog entry to your friends by the mood text. 
¥You can make a auction management with telephone support. 
¥Groupware by using the P2P framework. 

You can develop various applications using Java easily. 

Please, enjoy this library.
When you developed an application, please, notify me of it by mail <hisano@gmail.com>.

"Skype Technologies S.A. supports developing this library, but don't send a question about this library to Skype Technologies S.A., please." 


2.Preparation

Please prepare the following environment. 
¥JDK5.0(http://java.sun.com/j2se/1.5.0/download.html)
¥eclise3.1(http://www.eclipse.org/downloads/)

Afterwards, please import this project by [File - Import] menu of the eclipse. 

Please, see the testcases by JUnit about the usage of APIs. 

You have to distribute these with your application.
¥skype.jar
¥swt.jar
¥swt-win32-3139.dll

Please note that the place in the native library should be specified as follows 
when the application start. 

java -Djava.library.path=lib -classpath bin;lib\skype.jar;lib\swt.jar [your application main class name]
(when your application's 'lib' directory contains these files)

3.Links

Main Site (Japanese)
http://skype.sourceforge.jp/index.php?%A5%EA%A5%F3%A5%AF%BD%B8

Forum
http://forum.skype.com/viewtopic.php?t=44387

Skype API document
https://developer.skype.com/Docs/ApiDoc/FrontPage

4.Development history

ver0.9 - 2006/01/15(Sunday)
 released Beta version


5.Copyright and exemption matters

Skype is a trademark of Skype Limited in the United States and other countries.

Koji Hisano <hisano@gmail.com>, UBION Inc. and Skype Technologies S.A. have the copyright of Skype API for Java.
Please, use this in user's self-responsibility. 
