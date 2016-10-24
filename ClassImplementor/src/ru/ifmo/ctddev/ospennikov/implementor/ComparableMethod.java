package ru.ifmo.ctddev.ospennikov.implementor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;


/**
 * Class wrapper of {@link java.lang.reflect.Method} for using it in HashTables
 */
class ComparableMethod {
    /**
     * Wrapped method
     */
    private final Method myMethod;

    public ComparableMethod(Method myMethod) {
        this.myMethod = myMethod;
    }

    /**
     * Compare name and parameters of base method
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ComparableMethod)) {
            return false;
        }

        Method rmethod = ((ComparableMethod) obj).myMethod;
        return myMethod.getName().equals(rmethod.getName()) && Arrays.equals(myMethod.getParameterTypes(), rmethod.getParameterTypes());
    }


    /**
     * Calculate hashcode of method
     */
    @Override
    public int hashCode() {
        int hash = myMethod.getName().hashCode();
        for (Parameter p : myMethod.getParameters()) {
            hash = p.getType().hashCode() ^ hash;
        }
        return hash;
    }


    /**
     * @return String with right base implementation of method
     */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(Consts.TAB);
        out.append(Modifier.toString(myMethod.getModifiers() & ~Modifier.ABSTRACT & Modifier.methodModifiers())).append(" ");
        Parameter[] params = myMethod.getParameters();
        out.append(getHead(myMethod.getReturnType().getCanonicalName(), myMethod.getName(), params, myMethod.getExceptionTypes()));
        if (!Modifier.isNative(myMethod.getModifiers())) {
            out.append("{").append(Consts.LS);
            if (!myMethod.getReturnType().equals(Void.TYPE)) {
                out.append(Consts.TAB).append(Consts.TAB).append("return ")
                        .append(getTypeDefault(myMethod.getReturnType()))
                        .append(";").append(Consts.LS);
            }
            out.append(Consts.TAB).append("}");
        } else {
            out.append(";");
        }
        out.append(Consts.LS).append(Consts.LS);
        return out.toString();
    }

    /**
     * @return String with head of method
     */
    public static String getHead(String retType, String name, Parameter[] parameters, Class<?>[] exceptions) {
        StringBuilder out = new StringBuilder();
        out.append(retType).append(" ");
        out.append(name).append("(");
        out.append(getParameters(parameters, true));
        out.append(")");
        if (exceptions.length != 0) {
            out.append(" throws ");
            for (int i = 0; i < exceptions.length; i++) {
                out.append(exceptions[i].getCanonicalName());
                if (i < exceptions.length - 1) {
                    out.append(", ");
                }
            }
        }
        return out.toString();
    }

    /**
     * @return String default value of type
     */
    private String getTypeDefault(final Class type) {
        if (!type.isPrimitive()) {
            return Consts.NULL;
        } else if (type.getSimpleName().equals("void")) {
            return Consts.EMPTY;
        } else if (type.getSimpleName().equals("double")) {
            return Consts.ZERO_DOUBLE;
        } else if (type.getSimpleName().equals("boolean")) {
            return Consts.TRUE;
        } else {
            return Consts.ZERO;
        }
    }

    /**
     * @return String with parameters of method
     */
    public static String getParameters(Parameter[] parameters, boolean isNeedTypes) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            if (isNeedTypes) {
                out.append(parameters[i].getType().getCanonicalName()).append(" ");
            }
            if (i < parameters.length - 1) {
                out.append("p").append(i).append(", ");
            } else {
                out.append("p").append(i);
            }
        }
        return out.toString();
    }

    /**
     * @return base method
     */
    public Method getMethod() {
        return myMethod;
    }
}
