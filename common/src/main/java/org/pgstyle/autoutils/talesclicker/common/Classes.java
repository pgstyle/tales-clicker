package org.pgstyle.autoutils.talesclicker.common;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Classes {
    
    /**
     * Get the class of the caller of the caller of this method.
     *
     * @return the class object; or {@code null} if the caller of this method
     *         has no caller
     */
    public static Class<?> getCallerClass() {
        return Classes.getCallerClass(1);
    }

    /**
     * Get the class of the caller of the caller of this method at the specific
     * call stack depth.
     *
     * @param depth call stack depth
     * @return the class object; or {@code null} if the caller of this method
     *         has no caller
     */
    public static Class<?> getCallerClass(int depth) {
        try {
            return Class.forName(Thread.currentThread().getStackTrace()[depth + 3].getClassName());
        } catch (ReflectiveOperationException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Get the top-level class of a class object. If the given class is an inner
     * class, the outer class will be returned; otherwise the class itself will
     * be returned.
     *
     * @param clazz the class
     * @return the outer class if the given class is an inner class; or the
     *         class itself
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Class<?> getTopLevelClass(Class<?> clazz) {
        return Optional.ofNullable((Class) clazz.getEnclosingClass()).map(Classes::getTopLevelClass)
                .orElse((Class) clazz);
    }

    /**
     * Get the full text of the caller of the caller of this method.
     *
     * @return the class name text or {@code null} if the caller of this method
     *         has no caller
     */
    public static String getCallerText() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace.length > 3) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
            String className = caller.getClassName();
            String packageName = Arrays.stream(className.substring(0, className.lastIndexOf(".")).split("\\."))
                    .map(s -> s.subSequence(0, 1))
                    .collect(Collectors.joining("."));
            className = className.substring(className.lastIndexOf(".") + 1);
            return packageName + "." + className + ":" + caller.getLineNumber();
        }
        return null;
    }

    private Classes() {}

}
