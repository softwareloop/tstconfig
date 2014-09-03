package com.softwareloop.tstconfig;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by predo on 27/08/14.
 */
public class Test {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    enum DuplicatesPolicy {
        REPLACE,
        IGNORE,
        APPEND
    }

    public final static String INI_SECTION_HEADER_REGEX =
            "^\\s*\\[(.*)\\]\\s*$";
    public final static Pattern INI_SECTION_HEADER_PATTERN =
            Pattern.compile(INI_SECTION_HEADER_REGEX);

    public final static String APACHE_SECTION_HEADER_REGEXP =
            "^\\s*<(.*)>\\s*$";
    public final static String APACHE_SECTION_FOOTER_REGEXP =
            "^\\s*</.*>\\s*$";

    public final static Pattern APACHE_SECTION_HEADER_PATTERN =
            Pattern.compile(APACHE_SECTION_HEADER_REGEXP);
    public final static Pattern APACHE_SECTION_FOOTER_PATTERN =
            Pattern.compile(APACHE_SECTION_FOOTER_REGEXP);

    public final static String APT_SECTION_HEADER_REGEX =
            "^\\s*(\\S+)\\s*\\{\\s*$";
    public final static String APT_SECTION_FOOTER_REGEX =
            "^\\s*\\}\\s*;\\s*$";

    public final static Pattern APT_SECTION_HEADER_PATTERN =
            Pattern.compile(APT_SECTION_HEADER_REGEX);
    public final static Pattern APT_SECTION_FOOTER_PATTERN =
            Pattern.compile(APT_SECTION_FOOTER_REGEX);

    public final static StrMatcher COLON_SEPARATOR =
            StrMatcher.charMatcher(':');

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    List<String> lines;
    boolean linesParsed;
    int[] columnMap;
    DuplicatesPolicy duplicatesPolicy;

    String filename;
    String commandLine;
    Config config;
    String sectionName;
    List<String[]> section;
    String propertyName;
    String[] values;

    int assertionsTested;
    int assertionsFailed;
    int errors;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Interface/abstract class implementations
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // File methods
    //--------------------------------------------------------------------------

    public void file(String... args) {
        resetConfig();
        filename = StringUtils.join(args, ' ');
        try {
            lines = ConfigUtils.readLinesFromFile(filename);
        } catch (IOException e) {
            errors++;
            System.out.println("ERROR: cannot read file: " + filename);
            System.out.println();
        }
    }

    public void command(String... args) {
        resetConfig();
        commandLine = StringUtils.join(args, ' ');
        lines = ConfigUtils.readLinesFromCommand(args);
    }

    public void skip_header_lines(String... args) {
        String nLinesStr = StringUtils.join(args, ' ');
        try {
            int nLines = Integer.parseInt(nLinesStr);
            config.setSkipHeaderLines(nLines);
        } catch (NumberFormatException e) {
            errors++;
            System.out.println("ERROR: not a number: " + nLinesStr);
            System.out.println();
        }
    }

    public void columns(String... args) {
        columnMap = new int[args.length];
        for (int i = 0; i < args.length; i++) {
            try {
                int current = Integer.parseInt(args[i]);
                columnMap[i] = current;
            } catch (NumberFormatException e) {
                errors++;
                System.out.println("ERROR: not a number: " + args[i]);
                System.out.println();
            }
        }
    }

    void resetConfig() {
        filename = null;
        commandLine = null;
        sectionName = null;
        propertyName = null;
        lines = Collections.EMPTY_LIST;
        config = new Config();
        linesParsed = false;
        columnMap = null;
        section = Collections.EMPTY_LIST;
        values = null;
        duplicatesPolicy = DuplicatesPolicy.APPEND;
    }

    public void syntax(String... args) {
        String syntax = StringUtils.join(args, ' ');
        if ("apache".equals(syntax)) {
            config.setParseMode(Config.ParseMode.TOKENIZED);
            config.setSectionHeaderPattern(APACHE_SECTION_HEADER_PATTERN);
            config.setSectionFooterPattern(APACHE_SECTION_FOOTER_PATTERN);
            config.setHashCommentAllowed(true);
        } else if ("apt".equals(syntax)) {
            config.setParseMode(Config.ParseMode.TOKENIZED);
            config.setSectionHeaderPattern(APT_SECTION_HEADER_PATTERN);
            config.setSectionFooterPattern(APT_SECTION_FOOTER_PATTERN);
            config.setSeparator(StrMatcher.charSetMatcher(" \t;"));
            config.setSlashCommentAllowed(true);
        } else if ("etc_group".equals(syntax)
                || "etc_passwd".equals(syntax)
                || "etc_shadow".equals(syntax)) {
            config.setParseMode(Config.ParseMode.TOKENIZED);
            config.setSeparator(COLON_SEPARATOR);
            config.setHashCommentAllowed(true);
            config.setIgnoreEmptyTokens(false);
        } else if ("etc_hosts".equals(syntax)) {
            config.setParseMode(Config.ParseMode.TOKENIZED);
            config.setHashCommentAllowed(true);
        } else if ("fail2ban".equals(syntax)) {
            config.setParseMode(Config.ParseMode.KEYVALUE);
            config.setSectionHeaderPattern(INI_SECTION_HEADER_PATTERN);
            config.setKeySeparator("=");
            config.setHashCommentAllowed(true);
        } else if ("fixed".equals(syntax)) {
            config.setParseMode(Config.ParseMode.FIXED);
        } else if ("key_value".equals(syntax)) {
            config.setParseMode(Config.ParseMode.KEYVALUE);
        } else if ("ini".equals(syntax)) {
            config.setParseMode(Config.ParseMode.KEYVALUE);
            config.setSectionHeaderPattern(INI_SECTION_HEADER_PATTERN);
            config.setKeySeparator("=");
            config.setKeySeparatorOptional(true);
            config.setHashCommentAllowed(true);
            config.setSemicolonCommentAllowed(true);
        } else if ("ssh".equals(syntax)) {
            config.setParseMode(Config.ParseMode.TOKENIZED);
            config.setHashCommentAllowed(true);
        } else if ("swapon".equals(syntax)) {
            config.setParseMode(Config.ParseMode.TOKENIZED);
            config.setSkipHeaderLines(1);
        } else if ("tokenized".equals(syntax)) {
            config.setParseMode(Config.ParseMode.TOKENIZED);
        } else if ("ufw".equals(syntax)) {
            config = new UfwConfig();
        } else {
            errors++;
            System.out.println("ERROR: unrecognized syntax: " + syntax);
            System.out.println();
        }
    }

    public void key_separator(String... args) {
        String keySeparator = StringUtils.join(args, ' ');
        config.setKeySeparator(keySeparator);
    }

    public void separator(String... args) {
        String separator = StringUtils.join(args, ' ');
        config.setSeparator(StrMatcher.stringMatcher(separator));
    }

    public void ignore_empty_tokens(String... args) {
        String ignoreEmptyTokensStr = StringUtils.join(args, ' ');
        boolean ignoreEmptyTokens = Boolean.parseBoolean(ignoreEmptyTokensStr);
        config.setIgnoreEmptyTokens(ignoreEmptyTokens);
    }

    public void positions(String... args) {
        int[] positions = new int[args.length];
        for (int i = 0; i < args.length; i++) {
            positions[i] = Integer.parseInt(args[i]);
        }
        config.setPositions(positions);
    }

    void ensureConfigInitialized() {
        if (!linesParsed) {
            linesParsed = true;
            config.parseLines(lines);
            section = config.getTopLevelSection();
        }
    }

    //--------------------------------------------------------------------------
    // Section methods
    //--------------------------------------------------------------------------

    public void section(String... args) {
        ensureConfigInitialized();
        sectionName = StringUtils.join(args, ' ');
        section = config.getSection(sectionName);
        if (section == null) {
            errors++;
            System.out.println("ERROR: section is undefined: " + sectionName);
            System.out.println();
            section = Collections.EMPTY_LIST;
        }
    }

    public void section_size(String... args) {
        ensureConfigInitialized();
        values = new String[1];
        values[0] = Integer.toString(section.size());
    }

    //--------------------------------------------------------------------------
    // Property methods
    //--------------------------------------------------------------------------

    public void property(String... args) {
        ensureConfigInitialized();
        values = null;
        propertyName = StringUtils.join(args, ' ');
        boolean first = true;
        for (String[] line : section) {
            String firstColumn = getMappedColumn(line, 0);
            if (ObjectUtils.equals(propertyName, firstColumn)) {
                String[] lineValues = extractValues(line);
                if (first) {
                    first = false;
                    values = lineValues;
                } else {
                    switch (duplicatesPolicy) {
                        case REPLACE:
                            values = lineValues;
                        case APPEND:
                            values = ArrayUtils.addAll(values, lineValues);
                            break;
                        default:
                            /* IGNORE DUPLICATE */
                    }
                }
            }
        }
    }

    public String[] extractValues(String[] current) {
        if (columnMap == null) {
            return Arrays.copyOfRange(current, 1, current.length);
        } else {
            String[] result = new String[columnMap.length - 1];
            for (int i = 1; i < columnMap.length; i++) {
                result[i-1] = getMappedColumn(current, i);
            }
            return result;
        }
    }

    public String getMappedColumn(String[] array, int index) {
        if (columnMap == null) {
            return safeArrayGet(array, index);
        } else if (columnMap.length <= index) {
            return null;
        } else {
            return safeArrayGet(array, columnMap[index]);
        }
    }

    public String safeArrayGet(String[] array, int index) {
        if (array.length <= index ) {
            return null;
        } else {
            return array[index];
        }
    }

    //--------------------------------------------------------------------------
    // Assert methods
    //--------------------------------------------------------------------------

    public void assert_eq(final String... args) {
        assertionsTested++;
        if (!Arrays.equals(args, values)) {
            assertionFailed("assert_eq", args);
        }
    }

    public void assert_defined(String... args) {
        assertionsTested++;
        if (values == null) {
            assertionFailed("assert_defined");
        }
    }

    public void assert_undefined(String... args) {
        assertionsTested++;
        if (values != null) {
            assertionFailed("assert_undefined");
        }
    }

    public void assert_empty(String... args) {
        assertionsTested++;
        String joinedValues = StringUtils.join(values, ' ');
        if (StringUtils.isNotEmpty(joinedValues)) {
            assertionFailed("assert_empty");
        }
    }

    public void assert_not_empty(String... args) {
        assertionsTested++;
        String joinedValues = StringUtils.join(values, ' ');
        if (StringUtils.isEmpty(joinedValues)) {
            assertionFailed("assert_not_empty");
        }
    }

    public void assert_contains(String... args) {
        assertionsTested++;
        Boolean success = true;
        for (String arg : args) {
            if (!ArrayUtils.contains(values, arg)) {
                success = false;
            }
        }
        if (!success) {
            assertionFailed("assert_contains", args);
        }
    }

    public void assert_not_contains(String... args) {
        assertionsTested++;
        Boolean success = true;
        for (String arg : args) {
            if (ArrayUtils.contains(values, arg)) {
                success = false;
            }
        }
        if (!success) {
            assertionFailed("assert_not_contains", args);
        }
    }

    public void assert_starts_with(String... args) {
        assertionsTested++;
        String joinedValues = StringUtils.join(values, ' ');
        String joinedArgs = StringUtils.join(values, ' ');
        if (!StringUtils.startsWith(joinedValues, joinedArgs)) {
            assertionFailed("assert_starts_with", args);
        }
    }

    public void assert_ends_with(String... args) {
        assertionsTested++;
        String joinedValues = StringUtils.join(values, ' ');
        String joinedArgs = StringUtils.join(values, ' ');
        if (!StringUtils.endsWith(joinedValues, joinedArgs)) {
            assertionFailed("assert_ends_with", args);
        }
    }

    public void assertionFailed(String cmd, String... args) {
        assertionsFailed++;
        System.out.println("ASSERTION FAILED");

        if (filename != null) {
            System.out.println(String.format(" File:      %s", filename));
        } else if (commandLine != null) {
            System.out.println(String.format(" Command:   %s", commandLine));
        }

        if (sectionName != null) {
            System.out.println(String.format(" Section:   %s", sectionName));
        }

        if (propertyName != null) {
            System.out.println(String.format(" Property:  %s", propertyName));
        }

        if (values == null) {
            System.out.println(" Value:     <undefined>");
        } else {
            System.out.println(
                    String.format(
                            " Value:     %s",
                            StringUtils.join(values, ' ')));
        }

        System.out.println(
                String.format(
                        " Assertion: %s %s",
                        cmd,
                        StringUtils.join(args, ' ')));
        System.out.println();
    }

    //--------------------------------------------------------------------------
    // Abstract methods
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------


    public int getAssertionsTested() {
        return assertionsTested;
    }

    public int getAssertionsFailed() {
        return assertionsFailed;
    }

    public int getAssertionsPassed() {
        return assertionsTested - assertionsFailed;
    }

    public int getErrors() {
        return errors;
    }

    boolean isTestPass() {
        return assertionsFailed == 0 && errors == 0;
    }
}
