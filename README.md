# Simple Turing Machine EMulator (STEM)

### Inspired by JFLAP and xTuringmachine
Inspired by the design of JFLAP only implementing Turing machines  
Compatible with xTuringmachine files  

### Contributors:
- Andrei Cozma [acozma@vols.utk.edu]     
- Hunter Price [hprice7@vols.utk.edu]   
- Sam MacLean [smaclean@vols.utk.edu]   
- Joel Kovalcson [jkovalcs@vols.utk.edu]    
- Dakota Sanders [dsande30@vols.utk.edu]   
- Matt Matto [hgd145@vols.utk.edu]   
- Jonathan Bryan [jbryan74@vols.utk.edu]   
- Josh Herman [jherman4@vols.utk.edu]   
- Jahneulie Weste [wsv346@vols.utk.edu]

## Pre-requisites
- Java 9 or greater
- https://www.oracle.com/java/technologies/javase-downloads.html

## Build instructions
- This project uses the Maven build system. The configuration is in the _pom.xml_ file.
- You can use the Maven wrapper executable, _mvnw_ which is included with the project.

### Install Dependencies
```
./mvnw validate
```

### Compile Source Code
```
./mvnw clean compile
```

### Package JAR Executable
- JAR file will be placed at target/STEM.jar
```
./mvnw install
```

## Execution Instructions

### Option 1: Run Executable
```
cd target
java -jar STEM.jar
```

### Option 2: Run with Maven
```
./mvnw exec:java
```

### Option 2.1: Compile & Run with Maven
```
./mvnw clean compile exec:java
```
  
### License
Simple Turing machine EMulator (STEM)  
Copyright (C) 2021 Andrei Cozma, Hunter Price, Sam MacLean,  Joel Kovalcson, Dakota Sanders, Matt Matto, Jonathan Bryan

This program is free software: you can redistribute it and/or modify  
it under the terms of the GNU General Public License as published by  
the Free Software Foundation, either version 3 of the License, or  
(at your option) any later version.

This program is distributed in the hope that it will be useful,  
but WITHOUT ANY WARRANTY; without even the implied warranty of  
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
GNU General Public License for more details.
