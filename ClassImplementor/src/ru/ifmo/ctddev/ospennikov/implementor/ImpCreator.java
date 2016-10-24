package ru.ifmo.ctddev.ospennikov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.HashSet;


/**
 * Processes one class and generates an implementation for it
 * through {@link #implement}
 * <p>
 * Expected usage is the following:
 * <blockquote><pre>
 *     new ImplementCreator(MyClass.class, "MyClassImpl", new FileWriter(new File("MyClassImpl.java")))
 * </pre></blockquote><p>
 *
 * @author Lev Ospennikov
 * @see ru.ifmo.ctddev.ospennikov.implementor.Implementor
 */
class ImpCreator {

    /**
     * Class, that will be implemented
     */
    private final Class<?> myClass;

    /**
     * Name of implementation
     */
    private final String myName;

    /**
     * Output of ImpCreator
     */
    private final Appendable myOutput;

    /**
     * Class constructor, it recognizes which class should be implemented (<code>clazz</code>)
     * and name of generated class (<code>name</code>).
     *
     * @param clazz  class or interface which should be implemented
     * @param name   name of new class
     * @param output entity which contains generated class
     */
    public ImpCreator(Class clazz, String name, Appendable output) {
        this.myClass = clazz;
        this.myName = name + "Impl";
        this.myOutput = output;
    }

    /**
     * Do implementation of method part by part
     *
     * @throws ImplerException - some exception
     */
    void implement() throws ImplerException {
        if (Modifier.isFinal(myClass.getModifiers())) {
            throw new ImplerException("Can't implement final class");
        }
        if (myClass.isPrimitive()) {
            throw new ImplerException("Can't implement primitives");
        }
        try {
            System.out.println(this.myName);
            myOutput.append(getPackage());
            myOutput.append(getClassDeclaration());
            myOutput.append(getConstructors());
            writeMethods();
            myOutput.append("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Implements package part
     * @return String - string package
     */
    private String getPackage() {
        StringBuilder out = new StringBuilder();
        if (myClass.getPackage() != null) {
            out.append("package ").append(myClass.getPackage().getName()).append(";").append(Consts.LS).append(Consts.LS);
        }
        return out.toString();
    }

    /**
     * Implements class declaration
     * @return String - class declaration
     */
    private String getClassDeclaration() throws IOException {
        StringBuilder out = new StringBuilder();
        if (!myClass.isInterface()) {
            getModifiers(myClass.getModifiers(), Modifier.interfaceModifiers());
        } else {
            getModifiers(myClass.getModifiers(), Modifier.classModifiers());
        }
        out.append("class ");
        out.append(myName);
        out.append(myClass.isInterface() ? " implements " : " extends ");
        out.append(myClass.getCanonicalName());
        out.append(" {").append(Consts.LS);
        return out.toString();
    }

    /**
     * Implements modifiers from mod bitSet
     * @return String - modifiers
     */
    private String getModifiers(int mod, int type) {
        return Modifier.toString(mod & ~Modifier.ABSTRACT & type) + " ";
    }

    private String getConstructors() throws ImplerException {
        StringBuilder out = new StringBuilder();
        boolean isOneConstructor = false;
        for (Constructor<?> constructor : myClass.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(constructor.getModifiers())) {
                isOneConstructor = true;
                Parameter[] params = constructor.getParameters();
                out.append(Consts.TAB);
                out.append(getModifiers(constructor.getModifiers(), Modifier.constructorModifiers()));
                out.append(ComparableMethod.getHead("", myName, params, constructor.getExceptionTypes()));
                out.append("{").append(Consts.LS)
                        .append(Consts.TAB).append(Consts.TAB).append("super(");
                out.append(ComparableMethod.getParameters(params, false));
                out.append(");").append(Consts.LS).append(Consts.TAB).append("}")
                        .append(Consts.LS).append(Consts.LS);
            }
        }
        if (!isOneConstructor && myClass.getDeclaredConstructors().length > 0) {
            throw new ImplerException("all constructors are private");
        }
        return out.toString();
    }

    /**
     * Write methods to output
     * @see ru.ifmo.ctddev.ospennikov.implementor.ComparableMethod
     */
    private void writeMethods() throws IOException {
        HashSet<ComparableMethod> allMethods = new HashSet<>();
        for (Method method : myClass.getDeclaredMethods()) {
            if (Modifier.isAbstract(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers())) {
                allMethods.add(new ComparableMethod(method));
            }
        }
        for (Method method : myClass.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                allMethods.add(new ComparableMethod(method));
            }
        }
        if (myClass.getSuperclass() != null) {
            getMethods(myClass.getSuperclass(), allMethods);
        }
        for (ComparableMethod method : allMethods) {
            myOutput.append(method.toString());
        }

    }

    /**
     * Get methods from <code>base</code>
     */
    private void getMethods(Class<?> base, HashSet<ComparableMethod> allMethods) {
        for (Method method : base.getDeclaredMethods()) {
            if (!Modifier.isPrivate(method.getModifiers()) && Modifier.isAbstract(method.getModifiers())) {
                allMethods.add(new ComparableMethod(method));
            }
        }
        if (base.getSuperclass() != null) {
            getMethods(base.getSuperclass(), allMethods);
        }
    }
}
