<a href="https://foojay.io/today/works-with-openjdk">
   <img align="right" 
        src="https://github.com/foojayio/badges/raw/main/works_with_openjdk/Works-with-OpenJDK.png"   
        width="100">
</a>

WebcamFX
============
_A project working with web cameras or ip cameras which will serve jpeg to JavaFX client side applications_

Currently the server side portion is written using Java 8 (32 bit Windows jvm) and Java Media Framework JMF2.1.1e (32 bit).
The client can be Java 7 or 8 using JavaFX 2.0 and above. Maven will build the necessary bits to run the demo. Client and Server is
created using the preview release of Java 8 (http://jdk8.java.net/download.html).


Prerequisites
-------------------
* Java 8 (32 bit) http://jdk8.java.net/download.html
* Java Media Framework (32 bit) http://www.oracle.com/technetwork/java/javase/download-142937.html
* Maven 3.0.x
* JAVA_HOME environment variable
* JMFHOME environment variable
* MAVEN_HOME environment variable

Quick Start
--------------------
1. Install Java 8 (32 bit) http://jdk8.java.net/download.html
2. Install Java Media Framework (32 bit) http://www.oracle.com/technetwork/java/javase/download-142937.html
3. Ensure the dlls are installed in JMFHOME/lib directory via unchecking a check box during install see thread: https://forums.oracle.com/forums/thread.jspa?messageID=10191973
4. Install Maven
5. git clone git@github.com:carldea/WebcamFX.git
6. cd to WebcamFX/WebcamSvr
7. mvn clean assembly:assembly
8. cd to WebcamFX/WebcamClient
9. mvn clean assembly:assembly
10. cd to WebcamFX/WebcamSvr/target
11. server_start.bat or double click on the bat file.
12. cd to WebcamFX/WebcamClient/target
13. java -jar webcamclient-1.0-SNAPSHOT-jar-with-dependencies.jar (or double click the file)


Additional Notes:
--------------------
At OTN this forum thread helped me regarding using 32 bit versions of Java on Windows 7 64 bit:
https://forums.oracle.com/forums/thread.jspa?messageID=10191973


Apache Maven 3.0.4 (r1232337; 2012-01-17 03:44:56-0500)
Maven home: C:\development\maven\apache-maven-3.0.4\bin\..
Java version: 1.8.0-ea, vendor: Oracle Corporation
Java home: C:\Program Files (x86)\Java\jdk1.8.0\jre
Default locale: en_US, platform encoding: Cp1252
OS name: "windows 7", version: "6.1", arch: "x86", family: "dos"

JMFHOME=C:\Program Files (x86)\JMF2.1.1e
JAVA_HOME=C:\Program Files (x86)\Java\jdk1.8.0

Contents of the server_start.bat file to run the server:
"%JAVA_HOME%\bin\java" -cp "webcamsvr-1.0-SNAPSHOT-jar-with-dependencies.jar;%JMFHOME%\lib\jmf.jar" -Djava.library.path="%JMFHOME%/lib;C:\WINDOWS\System32" org.carlfx.webcamfx.server.Main

Building with Maven
--------------------
* Set your environment variable JAVA_HOME to the installed JDK directory. I.e. C:\Program Files (x86)\Java\jdk1.8.0
* Set your environment variable JMFHOME to the installed Java Media framework directory. I.e. JMFHOME=C:\Program Files (x86)\JMF2.1.1e
* Server build
    * cd WebcamFX/WebcamSvr
    * mvn clean assembly:assembly
    * cd target
    * server_start.bat
    * double click server_start.bat
* Client build
    * cd WebcamFX/WebcamClient
    * mvn clean assembly:assembly
    * cd target
    * java -jar webcamclient-1.0-SNAPSHOT-jar-with-dependencies.jar
    * or in File Explorer double click webcamclient-1.0-SNAPSHOT-jar-with-dependencies.jar

Intellij IDE users
---------------------
* Open the parent pom.xml in the WebcamFX directory.
* Ensure the language and classpath info is correct. Select project folder then in the menu select File -> Project Structure
  In the middle section will display three projects (webcamclient, WebcamFX, webcamsvr) select each and select the source tab on the right (side).
  A combo box (drop down) regarding Language Level. Make sure this is selecting "Java 8 Lambdas, type annotations etc."
  It'll ask to restart the IDE. Sometimes this gets confused because the maven pom when changes the IDE will detect and change the language settings from
  time to time.

TODO
---------------------
* There are lots of optimizations that need to be done.
    * Limit blocking queues
    * Possible thread starvation
* Better ways to send data to client viewers. PTP protocols, MJPEG, and RTSP.
* Abstract the server piece more for ip cameras and other streaming protocols.
* Use Gradle
* Gradle to create projects for IntelliJ, Netbeans and Eclipse
