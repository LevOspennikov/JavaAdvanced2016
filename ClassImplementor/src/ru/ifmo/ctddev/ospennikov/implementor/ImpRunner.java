package ru.ifmo.ctddev.ospennikov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ImpRunner {
    public static void main(String... args){
        try {
            Class k = Class.forName(args[0]);
            new Implementor().implement(k, Paths.get(args[1]));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ImplerException e){
            e.printStackTrace();
        }
    }
}
