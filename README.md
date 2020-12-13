Simple Turing machine EMulator (STEM)
---
### Inspired by JFLAP and xTuringmachine
Inspired by the design of JFLAP only implementing Turing machines  
Compatible with xTuringmachine files  
#### Contributors:
Sam MacLean [smaclean@vols.utk.edu] </br>  
Joel Kovalcson [jkovalcs@vols.utk.edu] </br>  
Dakota Sanders [dsande30@vols.utk.edu] </br>  
Matt Matto [hgd145@vols.utk.edu] </br>

Jonathan Bryan [jbryan74@vols.utk.edu] </br>

Josh Herman [jherman4@vols.utk.edu] </br>

#### Note for Linux Users
If you are running a linux machine Javafx is not included in the openjre package </br>
For Debian systems you can download it with </br>
```
sudo apt install openjfx
```

### Dependencies and Usage

- Java 9 or greater is <strong>required</strong> for to run this project. For assisstance in installing Java 9 refer to [this](https://www.oracle.com/java/technologies/javase/javase9-archive-downloads.html) page on oracle.
  - Due to the fact Java 10 is the last remaining version of Java before the removal of javafx, any later version will require openjfx.
- (optional) A makefile is provided to compile this project. To download make refer to [this link](http://gnuwin32.sourceforge.net/packages/make.htm) if you are on windows. Linux users can simply run the command ``sudo apt install build-essential`` (command may vary per distros)

- <strong>Usage using make</strong>
  - ``cd`` to the top level directory
  - run ``make``
  - after make has finished use the command ``java -jar out/STEM.jar``
- <strong>Usage without using make</strong>
  - ``cd`` to the top level directory
  - run ``javac -encoding utf8 -d out/classes src/*.java``
  - run ``jar cvfm out/STEM.jar src/META-INF/MANIFEST.MF -C out/classes/ . -C src/ checkmark.png``
  - After this has finished use the command ``java -jar out/STEM.jar``



- <strong>Easiest Usage</strong> 
  - Download the javafx SDK from [JavaFx Gluon](https://gluonhq.com/products/javafx/) and install where ever you wish
  - Make sure your JRE is JRE version 10 or higher. 
  - Make a shell script file, with the contents that are below.
    - Change the path variables to where your JRE/JavaFXSDK are located. The paths that are in the example are what's used for running the program on the Hydra machines. 
  - run ``source openjfx.sh`` or whatever you name the shell script
  - Then run the program by executing ``javafx STEM.jar``
  - If you are doing this on Windows: it is better to use a Linux subsystem, such as [Git Bash](https://gitforwindows.org/).
  - If you are running this on the University of Tennessee's Hydra/Tesla machines, simply writing this script, sourcing it, and executing should work. 

```
# Shell script for linking and running STEM.jar. The paths are set for what's on the Tesla/Hydra machines. 
#   You will need to update these paths if you're running it on your own machine. 

# path to the JRE - Update to where java is installed on your machine
export JAVA_HOME=/usr/lib/jvm/jre-14

# path to the javafx SDK. Update to where it is installed on your machine
export JAVAFX_HOME=/opt/eecs/javafx-sdk-11.0.2

# update the path
export PATH=$JAVA_HOME/bin:$PATH

export CLASSPATH=$JAVA_HOME/jre/lib/:$JAVAFX_HOME/lib

# make an alias for running the program
alias javafx="$JAVA_HOME/bin/java --module-path $JAVAFX_HOME/lib --add-modules javafx.controls -jar"
```
  
### License
Simple Turing machine EMulator (STEM)  
Copyright (C) 2020  Sam MacLean,  Joel Kovalcson, Dakota Sanders, Matt Matto, Jonathan Bryan

This program is free software: you can redistribute it and/or modify  
it under the terms of the GNU General Public License as published by  
the Free Software Foundation, either version 3 of the License, or  
(at your option) any later version.

This program is distributed in the hope that it will be useful,  
but WITHOUT ANY WARRANTY; without even the implied warranty of  
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
GNU General Public License for more details.
