#!/bin/bash

javadoc -private -link https://docs.oracle.com/javase/8/docs/api/ -sourcepath . -classpath ./ImplementorTest.jar:./junit-4.11.jar -d doc ../java-advanced-2016/java/info/kgeorgiy/java/advanced/implementor/ImplerException.java ../java-advanced-2016/java/info/kgeorgiy/java/advanced/implementor/Impler.java ../java-advanced-2016/java/info/kgeorgiy/java/advanced/implementor/JarImpler.java src/ru/ifmo/ctddev/ospennikov/implementor/*.java
sleep 1m