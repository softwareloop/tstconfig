package com.softwareloop.tstconfig;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Paolo Predonzani (paolo.predonzani@gmail.com)
 */
public class Test {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    enum DuplicatesPolicy {
        replace,
        ignore,
        append
    }

    public final static String  INI_SECTION_HEADER_REGEX   =
            "^\\s*\\[(.*)\\]\\s*$";
    public final static Pattern INI_SECTION_HEADER_PATTERN =
            Pattern.compile(INI_SECTION_HEADER_REGEX);

    public final static String APACHE_SECTION_HEADER_REGEXP =
            "^\\s*<(.*)>\\s*$";
    public final static String APACHE_SECTION_FOOTER_REGEXP = "^\\s*</.*>\\s*$";

    public final static Pattern APACHE_SECTION_HEADER_PATTERN =
            Pattern.compile(APACHE_SECTION_HEADER_REGEXP);
    public final static Pattern APACHE_SECTION_FOOTER_PATTERN =
            Pattern.compile(APACHE_SECTION_FOOTER_REGEXP);

    public final static String APT_SECTION_HEADER_REGEX =
            "^\\s*(\\S+)\\s*\\{\\s*$";
    public final static String APT_SECTION_FOOTER_REGEX = "^\\s*\\}\\s*;\\s*$";

    public final static Pattern APT_SECTION_HEADER_PATTERN =
            Pattern.compile(APT_SECTION_HEADER_REGEX);
    public final static Pattern APT_SECTION_FOOTER_PATTERN =
            Pattern.compile(APT_SECTION_FOOTER_REGEX);

    public final static StrMatcher COLON_SEPARATOR =
            StrMatcher.charMatcher(':');

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    List<String>     lines;
    boolean          linesParsed;
    int[]            columnMap;
    DuplicatesPolicy duplicatesPolicy;

    String         filename;
    String         commandLine;
    Config         config;
    String         sectionName;
    List<String[]> section;
    String[] propertyNames;
    String[]       values;

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
    // File/command methods
    //--------------------------------------------------------------------------

    public void file(String... args) {
        resetConfig();
        filename = joinArgs(args);
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
        commandLine = joinArgs(args);
        lines = ConfigUtils.readLinesFromCommand(args);
    }

    void resetConfig() {
        filename = null;
        commandLine = null;
        sectionName = null;
        propertyNames = null;
        lines = Collections.EMPTY_LIST;
        config = new Config();
        linesParsed = false;
        columnMap = null;
        section = Collections.EMPTY_LIST;
        values = null;
        duplicatesPolicy = DuplicatesPolicy.append;
    }

    //--------------------------------------------------------------------------
    // Syntax methods
    //--------------------------------------------------------------------------

    public void skip_header_lines(String... args) {
        String nLinesStr = joinArgs(args);
        try {
            int nLines = Integer.parseInt(nLinesStr);
            config.setSkipHeaderLines(nLines);
        } catch (NumberFormatException e) {
            errors++;
            System.out.println("ERROR: not a number: " + nLinesStr);
            System.out.println();
        }
    }

    public void read_lines(String... args) {
        String nLinesStr = joinArgs(args);
        try {
            Integer nLines = null;
            if (!nLinesStr.isEmpty()) {
                nLines = Integer.parseInt(nLinesStr);
            }
            config.setReadLines(nLines);
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

    public void syntax(String... args) {
        String syntax = joinArgs(args);
        if ("apache".equals(syntax)) {
            config.setParseMode(Config.ParseMode.tokenized);
            config.setSectionHeaderPattern(APACHE_SECTION_HEADER_PATTERN);
            config.setSectionFooterPattern(APACHE_SECTION_FOOTER_PATTERN);
            config.setHashCommentAllowed(true);
        } else if ("apt".equals(syntax)) {
            config.setParseMode(Config.ParseMode.tokenized);
            config.setSectionHeaderPattern(APT_SECTION_HEADER_PATTERN);
            config.setSectionFooterPattern(APT_SECTION_FOOTER_PATTERN);
            config.setSeparator(StrMatcher.charSetMatcher(" \t;"));
            config.setSlashCommentAllowed(true);
        } else if ("etc_group".equals(syntax) || "etc_passwd".equals(syntax) ||
                   "etc_shadow".equals(syntax)) {
            config.setParseMode(Config.ParseMode.tokenized);
            config.setSeparator(COLON_SEPARATOR);
            config.setHashCommentAllowed(true);
            config.setIgnoreEmptyTokens(false);
        } else if ("ini".equals(syntax)) {
            config.setParseMode(Config.ParseMode.keyvalue);
            config.setSectionHeaderPattern(INI_SECTION_HEADER_PATTERN);
            config.setKeySeparator("=");
            config.setKeySeparatorOptional(true);
            config.setHashCommentAllowed(true);
            config.setSemicolonCommentAllowed(true);
        } else {
            errors++;
            System.out.println("ERROR: unrecognized syntax: " + syntax);
            System.out.println();
        }
    }

    public void key_separator(String... args) {
        String keySeparator = joinArgs(args);
        config.setKeySeparator(keySeparator);
    }

    public void separator(String... args) {
        String separator = StringEscapeUtils.unescapeJava(joinArgs(args));
        config.setSeparator(StrMatcher.charSetMatcher(separator));
    }

    public void ignore_empty_tokens(String... args) {
        String ignoreEmptyTokensStr = joinArgs(args);
        boolean ignoreEmptyTokens = Boolean.parseBoolean(ignoreEmptyTokensStr);
        config.setIgnoreEmptyTokens(ignoreEmptyTokens);
    }

    public void key_separator_optional(String... args) {
        String keySeparatorOptionalStr = joinArgs(args);
        boolean keySeparatorOptional =
                Boolean.parseBoolean(keySeparatorOptionalStr);
        config.setKeySeparatorOptional(keySeparatorOptional);
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

    public void parse_mode(String... args) {
        String modeStr = joinArgs(args);
        try {
            Config.ParseMode parseMode = Config.ParseMode.valueOf(modeStr);
            config.setParseMode(parseMode);
        } catch (IllegalArgumentException e) {
            errors++;
            System.out.println("ERROR: unrecognized parse mode: " + modeStr);
            System.out.println();

        }
    }

    public void duplicates_policy(String... args) {
        String duplicatesPolicyStr = joinArgs(args);
        try {
            duplicatesPolicy = DuplicatesPolicy.valueOf(duplicatesPolicyStr);
        } catch (IllegalArgumentException e) {
            errors++;
            System.out.println(
                    "ERROR: unrecognized duplicates policy: " +
                    duplicatesPolicyStr
            );
            System.out.println();
        }
    }

    public void section_header_pattern(String... args) {
        String regex = joinArgs(args);
        Pattern pattern = Pattern.compile(regex);
        config.setSectionHeaderPattern(pattern);
    }

    public void section_footer_pattern(String... args) {
        String regex = joinArgs(args);
        Pattern pattern = Pattern.compile(regex);
        config.setSectionFooterPattern(pattern);
    }

    public void hash_comment_allowed(String... args) {
        String hashCommentAllowedStr = joinArgs(args);
        boolean hashCommentAllowed =
                Boolean.parseBoolean(hashCommentAllowedStr);
        config.setHashCommentAllowed(hashCommentAllowed);
    }

    public void slash_comment_allowed(String... args) {
        String slashCommentAllowedStr = joinArgs(args);
        boolean slashCommentAllowed =
                Boolean.parseBoolean(slashCommentAllowedStr);
        config.setSlashCommentAllowed(slashCommentAllowed);
    }

    public void semicolon_comment_allowed(String... args) {
        String semicolonCommentAllowedStr = joinArgs(args);
        boolean semicolonCommentAllowed =
                Boolean.parseBoolean(semicolonCommentAllowedStr);
        config.setSemicolonCommentAllowed(semicolonCommentAllowed);
    }

    public void quote_char(String... args) {
        String quoteCharStr = joinArgs(args);
        if (quoteCharStr.length() == 1) {
            config.setQuoteChar(quoteCharStr.charAt(0));
        } else {
            errors++;
            System.out.println(
                    "ERROR: illegal quote char: " + quoteCharStr
            );
            System.out.println();

        }
    }

    //--------------------------------------------------------------------------
    // Section methods
    //--------------------------------------------------------------------------

    public void section(String... args) {
        ensureConfigInitialized();
        sectionName = joinArgs(args);
        if (args.length == 0) {
            section = config.getTopLevelSection();
        } else {
            section = config.getSection(sectionName);
        }
        if (section == null) {
            errors++;
            System.out.println("ERROR: section is undefined: " + sectionName);
            System.out.println();
            section = Collections.EMPTY_LIST;
        }
    }

    public void section_size(String... args) {
        ensureConfigInitialized();
        propertyNames = null
        values = new String[1];
        values[0] = Integer.toString(section.size());
    }

    //--------------------------------------------------------------------------
    // Property methods
    //--------------------------------------------------------------------------

    public void property(String... args) {
        ensureConfigInitialized();
        values = null;
        propertyNames = args;
        boolean first = true;
        for (String[] line : section) {
            String[] firstColumns = getFirstMappedColumns(line, args.length);
            if (Arrays.equals(args, firstColumns)) {
                String[] lineValues = getLastMappedColumns(args.length, line);
                if (first) {
                    first = false;
                    values = lineValues;
                } else {
                    switch (duplicatesPolicy) {
                        case replace:
                            values = lineValues;
                            break;
                        case append:
                            values = ArrayUtils.addAll(values, lineValues);
                            break;
                        case ignore:
                            /* IGNORE DUPLICATE */
                            break;
                        default:
                            throw new IllegalStateException(
                                    "Illegal duplicate policy: " +
                                    duplicatesPolicy
                            );
                    }
                }
            }
        }
    }

    public String[] getLastMappedColumns(int startFrom, String[] current) {
        if (columnMap == null) {
            return Arrays.copyOfRange(current, startFrom, current.length);
        } else {
            String[] result = new String[columnMap.length - startFrom];
            for (int i = startFrom; i < columnMap.length; i++) {
                result[i - startFrom] = getMappedColumn(current, i);
            }
            return result;
        }
    }

    public String[] getFirstMappedColumns(String[] array, int n) {
        String[] result = new String[n];
        for (int i = 0; i < n; i++) {
            result[i] = getMappedColumn(array, i);
        }
        return result;
    }

    public String[] getMappedColumns(String[] array, int... indexes) {
        String[] result = new String[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            result[i] = getMappedColumn(array, indexes[i]);
        }
        return result;
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
        if (array.length <= index) {
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
        String joinedValues = joinArgs(values);
        if (StringUtils.isNotEmpty(joinedValues)) {
            assertionFailed("assert_empty");
        }
    }

    public void assert_not_empty(String... args) {
        assertionsTested++;
        String joinedValues = joinArgs(values);
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
        String joinedValues = joinArgs(values);
        String joinedArgs = joinArgs(values);
        if (!StringUtils.startsWith(joinedValues, joinedArgs)) {
            assertionFailed("assert_starts_with", args);
        }
    }

    public void assert_ends_with(String... args) {
        assertionsTested++;
        String joinedValues = joinArgs(values);
        String joinedArgs = joinArgs(values);
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

        if (propertyNames != null) {
            System.out.println(
                    String.format(
                            " Property:  %s", joinArgs(propertyNames)
                    )
            );
        }

        if (values == null) {
            System.out.println(" Value:     <undefined>");
        } else {
            System.out.println(
                    String.format(
                            " Value:     %s", joinArgs(values)
                    )
            );
        }

        System.out.println(
                String.format(
                        " Assertion: %s %s", cmd, joinArgs(args)
                )
        );
        System.out.println();
    }

    //--------------------------------------------------------------------------
    // Utility methods
    //--------------------------------------------------------------------------

    public static String joinArgs(String... args) {
        return StringUtils.join(args, ' ');
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
