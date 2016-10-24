package ru.ifmo.ctddev.ospennikov.implementor;

import java.io.File;

import info.kgeorgiy.java.advanced.implementor.*;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Implementation of interface {@link info.kgeorgiy.java.advanced.implementor.Impler}
 * and {@link info.kgeorgiy.java.advanced.implementor.JarImpler}
 *
 * @author Lev Ospennikov
 * @see ru.ifmo.ctddev.ospennikov.implementor.ImpCreator
 */

public class Implementor implements Impler, JarImpler {
    /**
     * Base path of implementation
     */

    String myPath;

    /**
     * Implements class, puts it's java code in path where root is located
     *
     * @param token class to implement
     * @param path  file, containing root of directory where to put generated file
     * @throws ImplerException if there is no correct implementation for <code>token</code>
     * @see #implement(Class, java.nio.file.Path)
     */
    @Override
    public void implement(Class<?> token, Path path) throws ImplerException {
        if (token == null || path == null) {
            throw new ImplerException("Wrong arguments");
        }
        myPath = path + File.separator + token.getName().replace(".", File.separator) + Consts.SUFFIX + ".java";

        File file = new File(myPath).getAbsoluteFile();
        if (!file.exists()) {
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                throw new ImplerException("Can't create dirs");
            }

            try (FileWriter out = new FileWriter(file, true)) {
                ImpCreator impCreator = new ImpCreator(token, token.getSimpleName(), out);
                impCreator.implement();
            } catch (IOException e) {
                throw new ImplerException("Can't open output file");
            }
        }
    }

    /**
     * Implements class, puts it's jar version in path where root is located
     *
     * @param token class to implement
     * @param jarPath  file, containing root of directory where to put generated jar
     * @throws ImplerException if there is no correct implementation for <code>token</code>
     * @see #implement(Class, java.nio.file.Path)
     */
    @Override
    public void implementJar(Class<?> token, Path jarPath) throws ImplerException {
        if (token == null || jarPath == null) {
            throw new ImplerException("Wrong args");
        }
        Path tempPath = Paths.get(".").resolve("tmpDir");
        implement(token, tempPath);
        String name = token.getPackage().getName().replace(".", File.separator) + File.separator + token.getSimpleName() + "Impl";
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int result = compiler.run(null, null, null,
                tempPath + File.separator + name + ".java", "-cp",
                tempPath + File.pathSeparator + System.getProperty("java.class.path"));
        if (result != 0) {
            throw new ImplerException("can't compile jar");
        }
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
            String param = name + ".class";
            param = param.replace("\\", "/");
            JarEntry entry = new JarEntry(param);
            output.putNextEntry(entry);
            try (FileInputStream input = new FileInputStream(tempPath.toAbsolutePath().toString() + "/" + name
                    + ".class")) {
                byte buffer[] = new byte[1024];
                int count;
                while ((count = input.read(buffer)) > 0) {
                    output.write(buffer, 0, count);
                }
            }
        } catch (IOException e) {
            throw new ImplerException("Can't write jar");
        }
    }


}
