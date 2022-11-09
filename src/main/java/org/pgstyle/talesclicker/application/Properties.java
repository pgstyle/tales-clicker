package org.pgstyle.talesclicker.application;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

public final class Properties implements Map<String, String>, Serializable {

    private static final long serialVersionUID = 4494657387898296931L;

    private static String escape(String comments) {
        int len = comments.length();
        StringBuilder builder = new StringBuilder((len & 1 << 30) != 0 ? Integer.MAX_VALUE : len << 1);
        builder.append("# ");
        int current = 0;
        int last = 0;
        String unicodeEscape = "\\u";
        while (current < len) {
            char c = comments.charAt(current);
            if (c > '\u00fe') {
                builder.append(last != current ? comments.substring(last, current) : "")
                       .append(unicodeEscape).append(Integer.toHexString(c));
                last = current + 1;
            }
            else if (c == '\n' || c == '\r') {
                builder.append(last != current ? comments.substring(last, current) : "")
                       .append(System.lineSeparator());
                if (c == '\r' && current != len - 1 && comments.charAt(current + 1) == '\n') {
                    current++;
                }
                boolean comment = current == len - 1 || comments.charAt(current + 1) != '#' && comments.charAt(current + 1) != '!';
                if (comment) {
                    builder.append("# ");
                }
                last = current + 1;
            }
            current++;
        }
        builder.append(last != current ? comments.substring(last, current) : "")
                .append(System.lineSeparator());
        return builder.toString();
    }

    private static String escape(Entry<String, String> entry) {
        return Properties.escape(entry.getKey(), true) + "=" + Properties.escape(entry.getValue(), false);
    }

    private static String escape(String string, boolean key) {
        int len = string.length();
        StringBuilder builder = new StringBuilder((len & 1 << 30) != 0 ? Integer.MAX_VALUE : len << 1);
        for (int i = 0; i < len; i++) {
            char c = string.charAt(i);
            switch(c) {
                case ' ':
                    if (c == 0 || key)
                        builder.append('\\');
                    builder.append(' ');
                    break;
                case '\t':builder.append("\\t");
                          break;
                case '\n':builder.append("\\n");
                          break;
                case '\r':builder.append("\\r");
                          break;
                case '\f':builder.append("\\f");
                          break;
                case '=':
                case ':':
                case '#':
                case '!':
                case '\\':
                    builder.append('\\'); builder.append(c);
                    break;
                default:
                    if (((c < 0x0020) || (c > 0x007e))) {
                        builder.append("\\u").append(Integer.toHexString(c));
                    }
                    else {
                        builder.append(c);
                    }
            }
        }
        return builder.toString();
    }

    public static Properties from() {
        return new Properties();
    }

    public static Properties from(java.util.Properties defaultProperties) {
        Map<String, String> map = new HashMap<>();
        defaultProperties.stringPropertyNames().stream().forEach(k -> map.put(k, defaultProperties.getProperty(k)));
        return new Properties(map);
    }

    public static Properties from(Map<String, String> defaultProperties) {
        return new Properties(defaultProperties);
    }

    public static Properties from(Properties defaultProperties) {
        return new Properties(defaultProperties);
    }

    private Properties() {
        this.properties = Collections.synchronizedMap(new TreeMap<>());
        this.newKeys = Collections.synchronizedSet(new HashSet<>());
    }

    private Properties(Map<String, String> defaultProperties) {
        this();
        this.properties.putAll(Objects.requireNonNull(defaultProperties, "defaultProperties == null"));
    }

    private final Map<String, String> properties;
    private transient Set<String> newKeys;

    public String getProperties(String key) {
        return this.get(key);
    }

    public String getProperties(String key, String defaultValue) {
        return this.getOrDefault(key, defaultValue);
    }

    public void list(PrintStream out) {
        out.println("-- listing properties --");
        this.list().forEach(out::println);
    }

    public void list(PrintWriter out) {
        out.println("-- listing properties --");
        this.list().forEach(out::println);
    }

    private Stream<String> list() {
        return this.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue());
    }

    public void load(InputStream stream) throws IOException {
        this.load(new InputStreamReader(stream));
    }

    public void load(Reader reader) throws IOException {
        java.util.Properties prop = new java.util.Properties();
        prop.load(reader);
        this.properties.putAll(Properties.from(prop));
    }

    public String setProperties(String key, String value) {
        return this.put(key, value);
    }

    public void store(OutputStream out, String comments) throws IOException {
        this.store(new OutputStreamWriter(out), comments);
    }

    public void store(Writer writer, String comments) throws IOException {
        if (Objects.nonNull(comments)) {
            writer.write(Properties.escape(comments));
        }
        writer.write("# " + OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        writer.write(System.lineSeparator());
        writer.write(System.lineSeparator());
        for (Entry<String, String> entry : this.entrySet()) {
            if (this.newKeys.contains(entry.getKey())) {
                String string = Properties.escape(new SimpleEntry<>(entry.getKey(), ""));
                string = string.substring(0, string.length() - 1);
                writer.write(String.format("# \"%s\" is added as default%n", string));
            }
            writer.write(Properties.escape(entry));
            writer.write(System.lineSeparator());
            writer.flush();
        }
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
