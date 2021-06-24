# JarBox

## What is JarBox
You can consider JarBox like a container or a uber-jar ora a fat-jar, but with dynamic contents. 
It is possible to "install" and "uninstall" a job (an executable sub-jar) into a JarBox so it can generate or process data inside a local workspace.
JarBox includes a Java job tool, dependencies and data that can be moved alltogheter.

## Features
JarBox
- needs a small payload (about 45KB) 
- is a pure Java project, without any third-party dependency
- is JDK 1.7+ compatible (tested up to JRE 15) 
- is multi-platform (Windows, OSX, Linux)
- has a scriptable command line interface
- includes a context and allows a status persistance
- parameters and environments stored in context will be passed to job every time
- supports double click start

## Limits
...but JarBox
- is not tuned to store huge data. It's performances get worse increasing work data size
- require free access to user temporary folder ("java.io.tmpdir")

## How to use
Any command line parameter recognized by JarBox must starts by "[sj]" prefix.
```
[sj]addpar=<param>, add a parameter to context. It will be passed to job by command line
[sj]delpar=<param>, delete a parameter from context
[sj]addenv=<variable>, add a environment variable to context. It will be set using -D to job
[sj]delenv=<variable>, delete a environment variable from context
[sj]install=<path/job.zip>, the path to job's archive to be installed
[sj]install=clean, delete the installed job
[sj]main=<job jar filename>, the workspace relative path to the executable job jar
[sj]import=<path/to/file>;<path/relative/workspace>;[true|false], import a external file in a workspace location, replacing if exists
[sj]export=<path/folder>, export the workspace to an external folder path
[sj]delete=<path/workspace/file>, remove a file from the internal workspace
[sj]loglevel=[debug|info|none], sets the console logger level. none is default
[sj]info=true, prints JarBox status and content to console
[sj]help=true, prints this help to console
```  
any other parameter passed to JarBox will be directly sent to internal job.  

### Some samples
- Install a job
```
java -jar JarBox.jar [sj]install=../pippo.zip
```
- Start the job passing a one shot parameter
```
java -jar JarBox.jar -noDisplay
```
- Delete a job
```
java -jar JarBox.jar [sj]install=clean
```  
- Add params and environments to persistent context
```
java -jar JarBox.jar [sj]addenv=temp.folder=C:\temp [sj]addpar=-noDisplay"
```
- Export workspace
```
java -jar JarBox.jar [sj]export=c:\temp\export"
```
- Import a file into workspace
```
java -jar JarBox.jar [sj]import=c:\temp\test.txt;mytests\test.txt;true"
```
- Delete a file from workspace
```
java -jar JarBox.jar [sj]delete=mytests\test.txt"
```
- Shows informations
```
java -jar JarBox.jar [sj]info=true"
```

## Technical details
You can find a technical description in the Wiki section
