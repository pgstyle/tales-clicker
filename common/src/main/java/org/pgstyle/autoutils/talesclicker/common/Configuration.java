package org.pgstyle.autoutils.talesclicker.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The {@code Configuration} class loads settings from external properties file
 * and internal default settings to modify the behaviour of the
 * {@code Tales-Clicker} program.
 *
 * @since 1.0
 * @author PGKan
 */
public abstract class Configuration {

    public static boolean asBoolean(String string) {
        return Boolean.parseBoolean(string);
    }

    public static long asInteger(String string, long defaults) {
        try {
            return Long.parseLong(string);
        } catch (NumberFormatException | NullPointerException e) {
            return defaults;
        }
    }

    public static double asFloat(String string, double defaults) {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException | NullPointerException e) {
            return defaults;
        }
    }

    public static boolean[] asBooleans(String[] strings) {
        boolean[] booleans = new boolean[strings.length];
        IntStream.range(0, strings.length).forEach(i -> booleans[i] = Configuration.asBoolean(strings[i]));
        return booleans;
    }

    public static long[] asIntegers(String[] strings) {
        long[] integers = new long[strings.length];
        IntStream.range(0, strings.length).forEach(i -> integers[i] = Configuration.asInteger(strings[i], 0));
        return integers;
    }

    public static double[] asFloats(String[] strings) {
        double[] floats = new double[strings.length];
        IntStream.range(0, strings.length).forEach(i -> floats[i] = Configuration.asFloat(strings[i], 0));
        return floats;
    }

    private static Properties load(String file, Properties defaults) {
        Path path = Paths.get(file);
        Properties properties = Properties.from(defaults);
        if (Files.exists(path)) {
            try (FileInputStream fis = new FileInputStream(path.toFile())) {
                properties.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
                defaults.store(fos, "Settings for Tales Clicker");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    protected Configuration(String properties, Properties defaults) {
        this(Configuration.load(properties, defaults));
    }

    private Configuration(Properties properties) {
        this.properties = properties;
    }

    private final Properties properties;

    protected final String getString(String key) {
        return this.getString(key, null);
    }

    protected final String getString(String key, String defaults) {
        return this.properties.getProperty(key, defaults);
    }

    protected final boolean getBoolean(String key) {
        return this.getBoolean(key, false);
    }

    protected final boolean getBoolean(String key, boolean defaults) {
        return Boolean.parseBoolean(this.properties.getProperty(key, Boolean.toString(defaults)));
    }

    protected final long getInteger(String key) {
        return this.getInteger(key, 0);
    }

    protected final long getInteger(String key, long defaults) {
        return Configuration.asInteger(this.getString(key, Long.toString(defaults)), defaults);
    }

    protected final double getFloat(String key) {
        return this.getInteger(key, 0);
    }

    protected final double getFloat(String key, double defaults) {
        return Configuration.asFloat(this.getString(key, Double.toString(defaults)), defaults);
    }

    private static String[] asStrings(String string) {
        Pattern syntax = Pattern.compile("(?<Text>[^\"]\\S*|\".+?\")(?<Separator>\\s+)*", Pattern.DOTALL);
        List<String> strings = new ArrayList<>();
        Matcher matcher = syntax.matcher(string);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            buffer.append(matcher.group("Text"));
            if (Objects.nonNull(matcher.group("Separator"))) {
                strings.add(buffer.toString().replace("\"", ""));
                buffer = new StringBuilder();
            }
        }
        Optional.ofNullable(buffer).filter(b -> b.length() > 0).map(StringBuilder::toString).ifPresent(strings::add);
        return strings.stream().toArray(String[]::new);
    }

    protected final String[][] getStringsSet(String key) {
        Pattern syntax = Pattern.compile("(?<Text>[^\"][^;]*|\".+?\")(?<Separator>[;]+)*", Pattern.DOTALL);
        List<String> strings = new ArrayList<>();
        Matcher matcher = syntax.matcher(this.getString(key, ""));
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            buffer.append(matcher.group("Text"));
            if (Objects.nonNull(matcher.group("Separator"))) {
                strings.add(buffer.toString().replace("\"", ""));
                buffer = new StringBuilder();
            }
        }
        Optional.ofNullable(buffer).filter(b -> b.length() > 0).map(StringBuilder::toString).ifPresent(strings::add);
        return strings.stream().map(Configuration::asStrings).toArray(String[][]::new);
    }

    protected final String[] getStrings(String key) {
        return Configuration.asStrings(this.getString(key, ""));
    }

    protected final boolean[][] getBooleansSet(String key) {
        String[][] stringsSet = this.getStringsSet(key);
        boolean[][] booleansSet = new boolean[stringsSet.length][];
        IntStream.range(0, stringsSet.length).forEach(i -> booleansSet[i] = Configuration.asBooleans(stringsSet[i]));
        return booleansSet;
    }

    protected final boolean[] getBooleans(String key) {
        return Configuration.asBooleans(this.getStrings(key));
    }

    protected final long[][] getIntegersSet(String key) {
        String[][] stringsSet = this.getStringsSet(key);
        long[][] integersSet = new long[stringsSet.length][];
        IntStream.range(0, stringsSet.length).forEach(i -> integersSet[i] = Configuration.asIntegers(stringsSet[i]));
        return integersSet;
    }

    protected final long[] getIntegers(String key) {
        return Configuration.asIntegers(this.getStrings(key));
    }

    protected final double[][] getFloatsSet(String key) {
        String[][] stringsSet = this.getStringsSet(key);
        double[][] floatsSet = new double[stringsSet.length][];
        IntStream.range(0, stringsSet.length).forEach(i -> floatsSet[i] = Configuration.asFloats(stringsSet[i]));
        return floatsSet;
    }

    protected final double[] getFloats(String key) {
        return Configuration.asFloats(this.getStrings(key));
    }

}
