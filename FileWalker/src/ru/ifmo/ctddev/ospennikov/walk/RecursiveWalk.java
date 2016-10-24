package ru.ifmo.ctddev.ospennikov.walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class RecursiveWalk {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Expected more arguments");
            return;
        }
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(args[0]), "UTF-8"));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8"))) {
            String targetPath;
            while ((targetPath = bufferedReader.readLine()) != null) {
                Path start = Paths.get(targetPath);
                Files.walkFileTree(start, new FileWalker(writer));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
