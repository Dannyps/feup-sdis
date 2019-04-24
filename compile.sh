#!/bin/bash

javac -cp bin/ -d bin/ src/*/*.java
cd src
javac TestApp.java
mv TestA*.class ../bin

