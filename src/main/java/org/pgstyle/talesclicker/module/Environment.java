package org.pgstyle.talesclicker.module;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.pgstyle.talesclicker.application.AppUtils;
import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;

/**
 * An Application Environment hold environment variables in globally accessible
 * and module protected scopes.
 *
 * @since 1.0
 * @author PGKan
 */
public final class Environment {

    /**
     * Get the unique instance of the {@code Environment}.
     * @return the environment
     */
    public static Environment getInstance() {
        return Environment.INSTANCE;
    }

    private static final Environment INSTANCE = new Environment();

    private Environment() {
        this.scopes = new HashMap<>();
        this.global = this.createScope(this.getClass());
    }

    private final Map<String, String> global;
    private final Map<Class<?>, Map<String, String>> scopes;

    /**
     * Check does a variable exists.
     *
     * @param name the name of the variable
     * @return {@code true} if the variable exists; or {@code false} otherwise
     */
    public synchronized boolean find(String name) {
        return this.createScope(AppUtils.getTopLevelClass(AppUtils.getCallerClass())).containsKey(name)
            || this.findGlobal(name);
    }

    /**
     * Check does a variable exists in protected scope.
     *
     * @param name the name of the variable
     * @return {@code true} if the variable exists; or {@code false} otherwise
     */
    public synchronized boolean findLocal(String name) {
        return this.createScope(AppUtils.getTopLevelClass(AppUtils.getCallerClass())).containsKey(name);
    }

    /**
     * Check does a variable exists in the global scope.
     *
     * @param name the name of the variable
     * @return {@code true} if the variable exists; or {@code false} otherwise
     */
    public synchronized boolean findGlobal(String name) {
        return this.global.containsKey(name);
    }

    /**
     * Get the value stored in a variable, protected variable will shade the
     * global one.
     *
     * @param name the variable name
     * @return the value of the variable
     * @throws EnvironmentException if the variable does not exist
     */
    public synchronized String get(String name) {
        Map<String, String> scope = this.createScope(AppUtils.getTopLevelClass(AppUtils.getCallerClass()));
        if (scope.containsKey(name)) {
            return scope.get(name);
        }
        else {
            return this.getGlobal(name);
        }
    }

    /**
     * Get the value stored in a variable from the global scope.
     *
     * @param name the variable name
     * @return the value of the variable
     * @throws EnvironmentException if the variable does not exist
     */
    public synchronized String getGlobal(String name) {
        this.checkDeclare(this.global, name);
        return this.global.get(name);
    }

    /**
     * Set the value of a variable stored in the protected scope.
     *
     * @param name the variable name
     * @return the value of the variable
     * @throws EnvironmentException
     *         if the variable is restricted; or if the variable does not exist
     */
    public synchronized String set(String name, String value) {
        this.checkRestricted(name);
        Map<String, String> scope = this.createScope(AppUtils.getTopLevelClass(AppUtils.getCallerClass()));
        this.checkDeclare(scope, name);
        scope.put(name, value);
        return scope.get(name);
    }

    /**
     * Set the value of a variable stored in the global scope.
     *
     * @param name the variable name
     * @return the value of the variable
     * @throws EnvironmentException
     *         if the variable is restricted; or if the variable does not exist
     */
    public synchronized String setGlobal(String name, String value) {
        this.checkRestricted(name);
        this.checkDeclare(this.global, name);
        this.global.put(name, value);
        return this.global.get(name);
    }

    /**
     * Declare a variable stored in the protected scope.
     *
     * @param name the variable name
     * @param value the variable value
     * @return the value of the variable
     * @throws EnvironmentException if the variable already existed
     */
    public synchronized String declare(String name, String value) {
        Map<String, String> scope = this.createScope(AppUtils.getTopLevelClass(AppUtils.getCallerClass()));
        this.checkDuplicate(scope, name);
        scope.put(name, value);
        return scope.get(name);
    }

    /**
     * Declare a variable stored in the global scope.
     *
     * @param name the variable name
     * @param value the variable value
     * @return the value of the variable
     * @throws EnvironmentException if the variable already existed
     */
    public synchronized String declareGlobal(String name, String value) {
        this.checkDuplicate(this.global, name);
        this.global.put(name, value);
        return this.global.get(name);
    }

    /**
     * Undeclare a variable stored in the protected scope.
     *
     * @param name the variable name
     * @return the value of the variable
     * @throws EnvironmentException
     *         if the variable is restricted; or if the variable does not exist
     */
    public synchronized String release(String name) {
        this.checkRestricted(name);
        Map<String, String> scope = this.createScope(AppUtils.getTopLevelClass(AppUtils.getCallerClass()));
        this.checkDeclare(scope, name);
        String value = scope.get(name);
        scope.remove(name);
        return value;
    }

    /**
     * Undeclare a variable stored in the global scope.
     *
     * @param name the variable name
     * @return the value of the variable
     * @throws EnvironmentException
     *         if the variable is restricted; or if the variable does not exist
     */
    public synchronized String releaseGlobal(String name) {
        this.checkRestricted(name);
        this.checkDeclare(this.global, name);
        String value = this.global.get(name);
        this.global.remove(name);
        return value;
    }

    /**
     * Release the variable scope of the caller.
     */
    public synchronized void dispose() {
        Application.log(Level.DEBUG, "dispose environment scope %s", AppUtils.getTopLevelClass(AppUtils.getCallerClass()).getName());
        Optional.ofNullable(this.scopes.remove(AppUtils.getTopLevelClass(AppUtils.getCallerClass()))).ifPresent(Map::clear);
    }

    /**
     * Prevent alteration of special variables.
     *
     * @param name the name of variable to be checked
     * @throws EnvironmentException if the variable is restricted
     */
    private void checkRestricted(String name) {
        if (AppUtils.getTopLevelClass(AppUtils.getCallerClass(1)) == ModuleManager.class) {
            // only module manager can bypass variable restriction
            return;
        }
        switch (name) {
        case "mm": // ModuleManager ID
        case "scope": // Access Scope
            throw new EnvironmentException(String.format("restricted variable \"%s\"", name));
        default:
            break;
        }
    }

    /**
     * Prevent redeclaration of existing variables.
     *
     * @param scope the scope to be checked
     * @param name the name of variable to be checked
     * @throws EnvironmentException if the variable already existed
     */
    private void checkDuplicate(Map<String, String> scope, String name) {
        if (scope.containsKey(name)) {
            throw new EnvironmentException(String.format("duplicated variable \"%s\" in scope %s", name, scope.get("scope")));
        }
    }

    /**
     * Prevent accessing of undeclared variables.
     *
     * @param scope the scope to be checked
     * @param name the name of variable to be checked
     * @throws EnvironmentException if the variable does not exist
     */
    private void checkDeclare(Map<String, String> scope, String name) {
        if (!scope.containsKey(name)) {
            throw new EnvironmentException(String.format("undeclared variable \"%s\" in scope %s", name, scope.get("scope")));
        }
    }

    /**
     * Get the access scope of a class.
     *
     * @param scope the scope
     * @return an existing scope; or a newly created scope for the class if it
     *         does not exist already
     */
    private Map<String, String> createScope(Class<?> scope) {
        if (Objects.isNull(scope)) {
            return this.global;
        }
        return this.scopes.computeIfAbsent(scope, s -> new HashMap<>(Collections.singletonMap("scope", scope.getName())));
    }

}
