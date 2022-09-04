package org.pgstyle.talesclicker.module;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public final class Environment {

    public Environment() {
        this.variables = new HashMap<>();
        this.global = new Properties();
        this.variables.put(null, this.global);
    }

    private final Properties global;
    private final Map<Set<Class<? extends Module>>, Properties> variables;

    public String get(String name) {
        if (this.global.contains(name)) {
            return this.global.getProperty(name);
        }
        else {
            // TODO protected variables
            return null;
        }
    }


}
