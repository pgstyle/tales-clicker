package org.pgstyle.talesclicker.module;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.pgstyle.talesclicker.application.AppUtils;
import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;

public final class Environment {

    public static Environment getInstance() {
        return Environment.INSTANCE;
    }

    private static final Environment INSTANCE = new Environment();

    public Environment() {
        this.scopes = new HashMap<>();
        this.global = new HashMap<>();
        this.global.put("scope", this.getClass().getName());
        this.scopes.put(null, this.global);
    }

    private final Map<String, String> global;
    private final Map<Class<?>, Map<String, String>> scopes;

    public synchronized boolean find(String name) {
        return this.scopes.containsKey(AppUtils.getTopLevelClass(AppUtils.getCallerClass())) && this.scopes.get(AppUtils.getTopLevelClass(AppUtils.getCallerClass())).containsKey(name)
            || this.findGlobal(name);
    }

    public synchronized boolean findLocal(String name) {
        return this.scopes.containsKey(AppUtils.getTopLevelClass(AppUtils.getCallerClass())) && this.scopes.get(AppUtils.getTopLevelClass(AppUtils.getCallerClass())).containsKey(name);
    }

    public synchronized boolean findGlobal(String name) {
        return this.global.containsKey(name);
    }

    public synchronized String get(String name) {
        if (this.scopes.containsKey(AppUtils.getTopLevelClass(AppUtils.getCallerClass())) && this.scopes.get(AppUtils.getTopLevelClass(AppUtils.getCallerClass())).containsKey(name)) {
            return this.scopes.get(AppUtils.getTopLevelClass(AppUtils.getCallerClass())).get(name);
        }
        else {
            return this.getGlobal(name);
        }
    }

    public synchronized String getGlobal(String name) {
        this.checkDeclare(this.global, name);
        return this.global.get(name);
    }

    public synchronized String set(String name, String value) {
        this.checkRestricted(name);
        Map<String, String> scope = this.createScope(AppUtils.getTopLevelClass(AppUtils.getCallerClass()));
        this.checkDeclare(scope, name);
        scope.put(name, value);
        return scope.get(name);
    }

    public synchronized String setGlobal(String name, String value) {
        this.checkRestricted(name);
        this.checkDeclare(this.global, name);
        this.global.put(name, value);
        return this.global.get(name);
    }

    public synchronized String declare(String name, String value) {
        this.checkRestricted(name);
        Map<String, String> scope = this.createScope(AppUtils.getTopLevelClass(AppUtils.getCallerClass()));
        this.checkDuplicate(scope, name);
        scope.put(name, value);
        return scope.get(name);
    }

    public synchronized String declareGlobal(String name, String value) {
        if (AppUtils.getTopLevelClass(AppUtils.getCallerClass()) != ModuleManager.class) {
            this.checkRestricted(name);
        }
        this.checkDuplicate(this.global, name);
        this.global.put(name, value);
        return this.global.get(name);
    }

    public synchronized String release(String name) {
        this.checkRestricted(name);
        Map<String, String> scope = this.createScope(AppUtils.getTopLevelClass(AppUtils.getCallerClass()));
        this.checkDeclare(scope, name);
        String value = scope.get(name);
        scope.remove(name);
        return value;
    }

    public synchronized String releaseGlobal(String name) {
        this.checkRestricted(name);
        String value = this.global.get(name);
        this.global.remove(name);
        return value;
    }

    public synchronized void dispose() {
        Application.log(Level.DEBUG, "dispose environment scope %s", AppUtils.getTopLevelClass(AppUtils.getCallerClass()).getName());
        Optional.ofNullable(this.scopes.remove(AppUtils.getTopLevelClass(AppUtils.getCallerClass()))).ifPresent(Map::clear);
    }

    private void checkRestricted(String name) {
        switch (name) {
        case "mm":
        case "scope":
            throw new EnvironmentException(String.format("restricted variable \"%s\"", name));
        default:
            break;
        }
    }

    private void checkDuplicate(Map<String, String> scope, String name) {
        if (scope.containsKey(name)) {
            throw new EnvironmentException(String.format("duplicated variable \"%s\" in scope %s", name, scope.get("scope")));
        }
    }

    private void checkDeclare(Map<String, String> scope, String name) {
        if (!scope.containsKey(name)) {
            throw new EnvironmentException(String.format("undeclared variable \"%s\" in scope %s", name, scope.get("scope")));
        }
    }

    private Map<String, String> createScope(Class<?> scope) {
        if (Objects.isNull(scope) || scope.equals(this.getClass())) {
            return this.global;
        }
        return this.scopes.computeIfAbsent(scope, s -> new HashMap<>(Collections.singletonMap("scope", scope.getName())));
    }

}
