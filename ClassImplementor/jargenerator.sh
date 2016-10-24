#!/usr/bin/env bash

javac -classpath C:/Users/ospen/IdeaProjects/java-advanced-2016/artifacts/ImplementorTest.jar src/ru/ifmo/ctddev/ospennikov/implementor/* -d ./out/production/


touch Manifest
echo "Manifest-Version: 1.0" > Manifest
echo "Main-Class: ru.ifmo.ctddev.ospennikov.implementor.ImpRunner" >> Manifest
echo "Class-Path: ../java-advanced-2016/artifacts/ImplementorTest.jar" >> Manifest
jar cfm Implementor.jar Manifest ru/ifmo/ctddev/ospennikov/implementor/*


rm -f Manifest
