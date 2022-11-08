package org.pgstyle.talesclicker.application;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Properties implements Map<String, String>, Serializable {

    private static final long serialVersionUID = 4494657387898296931L;

    private static String escape(Entry<String, String> entry) {
        return Properties.escape(entry.getKey(), true) + "=" + Properties.escape(entry.getValue(), false);
    }

    private static String escape(String string, boolean key) {
        int len = string.length();
        StringBuilder outBuffer = new StringBuilder((len & 1 << 30) != 0 ? Integer.MAX_VALUE : len << 1);
        for (int i = 0; i < len; i++) {
            char c = string.charAt(i);
            switch(c) {
                case ' ':
                    if (c == 0 | key)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                          break;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                          break;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                          break;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                          break;
                case '=':
                case ':':
                case '#':
                case '!':
                case '\\':
                    outBuffer.append('\\'); outBuffer.append(c);
                    break;
                default:
                    if (((c < 0x0020) || (c > 0x007e))) {
                        outBuffer.append("\\u").append(Integer.toHexString(c));
                    }
                    else {
                        outBuffer.append(c);
                    }
            }
        }
        return outBuffer.toString();
    }

    public static Properties from() {
        return new Properties();
    }

    public static Properties from(Properties defaultProperties) {
        return new Properties(defaultProperties);
    }

    private Properties() {
        this.properties = Collections.synchronizedMap(new TreeMap<>());
        this.newKeys = Collections.synchronizedList(new ArrayList<>());
    }

    private Properties(Properties defaultProperties) {
        this();
        this.properties.putAll(Objects.requireNonNull(defaultProperties, "defaultProperties == null"));
    }

    private final Map<String, String> properties;
    private transient List<String> newKeys;

    public String getProperties(String key) {
        return this.get(key);
    }

    public String getProperties(String key, String defaultValue) {
        return this.getOrDefault(key, defaultValue);
    }

    public void list(PrintStream out) {
        out.print(this.list());
    }

    public void list(PrintWriter out) {
        out.print(this.list());
    }

    private String list() {
        return this.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(System.lineSeparator()));
    }

    public void load(InputStream stream) {
        this.load(new InputStreamReader(stream));
    }

    public void load(Reader reader) {
        // TODO
    }

    public String setProperties(String key, String value) {
        return this.put(key, value);
    }

    public void store(OutputStream out, String comments) {
        this.store(new OutputStreamWriter(out), comments);
    }

    public void store(Writer writer, String comments) {
        // TODO
    }

    public Stream<String> names() {
        return this.properties.keySet().stream();
    }

    @Override
    public int size() {
        return this.properties.size();
    }

    @Override
    public boolean isEmpty() {
        return this.properties.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.properties.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.properties.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return this.properties.get(key);
    }

    @Override
    public String put(String key, String value) {
        Optional.ofNullable(key).filter(k -> !this.containsKey(k)).ifPresent(this.newKeys::add);
        return this.properties.put(key, value);
    }

    @Override
    public String remove(Object key) {
        this.newKeys.remove(key);
        return this.properties.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> map) {
        map.keySet().stream().filter(k -> !this.containsKey(k)).forEach(this.newKeys::add);
        this.properties.putAll(map);
    }

    @Override
    public void clear() {
        this.newKeys.clear();
        this.properties.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.properties.keySet();
    }

    @Override
    public Collection<String> values() {
        return this.properties.values();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return this.properties.entrySet();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Properties && this.properties.equals(((Properties) object).properties);
    }

    @Override
    public int hashCode() {
        return this.properties.hashCode();
    }

    @Override
    public String toString() {
        return this.properties.toString();
    }

}
