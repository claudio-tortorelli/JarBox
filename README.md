# JarBox

![logo](./JarBox/docs/logo/jarbox_logo.gif)

## What is JarBox
You can consider JarBox like a container or a "uber-jar" or a "fat-jar", but with dynamic contents. 
It is possible to "install" and "uninstall" a job (an executable sub-jar) into a JarBox so it can generate or process data inside a local workspace.
JarBox is a portable environment that includes a Java job tool, dependencies and data moved alltogheter.


## Features
JarBox
- needs a small payload (less than 100KB) 
- is a pure Java project, without any third-party dependency
- is JDK 1.7+ compatible (tested up to JRE 15) 
- is (virtually) multi-platform (Windows, OSX, Linux)
- has a scriptable command line interface
- includes a context and allows a status persistance
- parameters and environments stored in context will be passed to job every execution
- is a wrapper, but a standard jar too, so its content is natively compressed
- supports double click start

## Limits
...but JarBox
- is not tuned to store huge data. It's performances get worse increasing work data size
- requires write privileges to user temporary folder ("java.io.tmpdir")

## How to use
Any command line parameter recognized by JarBox must starts by "[sj]" prefix.
```
[jb]addpar=<param>, add a parameter to context. It will be passed to job by command line
[jb]delpar=<param>, delete a parameter from context
[jb]addenv=<variable>, add a environment variable to context. It will be set using -D to job
[jb]delenv=<variable>, delete a environment variable from context
[jb]install=<path/job.zip>, the path to job's archive to be installed
[jb]install=clean, delete the installed job
[jb]main=<job jar filename>, the workspace relative path to the executable job jar
[jb]import=<path/to/file>;<path/relative/workspace>;[true|false], import a external file in a workspace location, replacing if exists
[jb]export=<path/folder>, export the workspace to an external folder path
[jb]delete=<path/workspace/file>, remove a file from the internal workspace
[jb]loglevel=[debug|info|none], sets the console logger level. none is default
[jb]info=true, prints JarBox status and content to console
[jb]help=true, prints this help to console
```  
any other parameter passed to JarBox will be directly sent to internal job.  

### Some samples
- <b>Install</b> a job
```
java -jar JarBox.jar [jb]install=../pippo.zip
```
- <b>Start</b> the job passing a one shot parameter
```
java -jar JarBox.jar -noDisplay
```
- <b>Delete</b> a job
```
java -jar JarBox.jar [jb]install=clean
```  
- <b>Add params and environment variable</b> to persistent context
```
java -jar JarBox.jar [jb]addenv=temp.folder=C:\temp [jb]addpar=-noDisplay"
```
- <b>Export</b> workspace
```
java -jar JarBox.jar [jb]export=c:\temp\export"
```
- <b>Import</b> a file into workspace
```
java -jar JarBox.jar [jb]import=c:\temp\test.txt;mytests\test.txt;true"
```
- <b>Delete</b> a file from workspace
```
java -jar JarBox.jar [jb]delete=mytests\test.txt"
```
- <b>Shows</b> informations
```
java -jar JarBox.jar [jb]info=true"
```

## Technical details and info
You can find a technical description in the Wiki section: https://github.com/claudio-tortorelli/JarBox/wiki
