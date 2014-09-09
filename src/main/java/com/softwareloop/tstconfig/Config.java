package com.softwareloop.tstconfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Paolo Predonzani (paolo.predonzani@gmail.com)
 */
public class Config {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    public enum ParseMode {
        tokenized,
        fixed,
        keyvalue
    }

    public final static String HASH_COMMENT_REGEX      = "^\\s*#.*$";
    public final static String SLASH_COMMENT_REGEX     = "^\\s*//.*$";
    public final static String SEMICOLON_COMMENT_REGEX = "^\\s*;.*$";

    public final static Pattern HASH_COMMENT_PATTERN      =
            Pattern.compile(HASH_COMMENT_REGEX);
    public final static Pattern SLASH_COMMENT_PATTERN     =
            Pattern.compile(SLASH_COMMENT_REGEX);
    public final static Pattern SEMICOLON_COMMENT_PATTERN =
            Pattern.compile(SEMICOLON_COMMENT_REGEX);

    public final static boolean DEFAULT_HASH_COMMENT_ALLOWED      = false;
    public final static boolean DEFAULT_SLASH_COMMENT_ALLOWED     = false;
    public final static boolean DEFAULT_SEMICOLON_COMMENT_ALLOWED = false;

    public final static boolean DEFAULT_IGNORE_EMPTY_TOKENS    = true;
    public final static boolean DEFAULT_KEY_SEPARATOR_OPTIONAL = false;
    public final static char    DEFAULT_QUOTE_CHAR             = '"';

    public static final StrMatcher DEFAULT_SEPARATOR =
            StrMatcher.splitMatcher();

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    int     skipHeaderLines = 0;
    Integer readLines       = null;

    boolean hashCommentAllowed      = DEFAULT_HASH_COMMENT_ALLOWED;
    boolean slashCommentAllowed     = DEFAULT_SLASH_COMMENT_ALLOWED;
    boolean semicolonCommentAllowed = DEFAULT_SEMICOLON_COMMENT_ALLOWED;

    Pattern sectionHeaderPattern;
    Pattern sectionFooterPattern;

    String keySeparator = null;
    boolean    keySeparatorOptional = DEFAULT_KEY_SEPARATOR_OPTIONAL;
    StrMatcher separator            = DEFAULT_SEPARATOR;
    boolean    ignoreEmptyTokens    = DEFAULT_IGNORE_EMPTY_TOKENS;
    char       quoteChar            = DEFAULT_QUOTE_CHAR;
    int[]      positions            = null;

    ParseMode parseMode;

    protected final Map<String, List<String[]>> sections;

    protected List<String[]> currentSection;
    protected List<String[]> topLevelSection;


    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    protected Config() {
        sections = new HashMap<String, List<String[]>>();
    }


    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    public List<String[]> getSection(String sectionName) {
        return sections.get(sectionName);
    }

    public void parseLines(List<String> lines) {
        if (topLevelSection == null) {
            topLevelSection = new ArrayList<String[]>();
        }
        if (currentSection == null) {
            currentSection = topLevelSection;
        }

        for (String line : lines) {
            if (skipHeaderLines > 0) {
                skipHeaderLines = skipHeaderLines - 1;
                continue;
            }
            if (readLines != null) {
                if (readLines <= 0) {
                    continue;
                } else {
                    readLines = readLines - 1;
                }
            }
            parseLine(line);
        }
    }

    public void parseLine(String line) {
        if (isBlankLine(line)) {
            parseBlankLine(line);
        } else if (isCommentLine(line)) {
            parseCommentLine(line);
        } else {
            parseSectionLine(line);
        }
    }

    public void parseSectionLine(String line) {
        Matcher sectionHeaderMatcher;
        if (sectionHeaderPattern == null) {
            sectionHeaderMatcher = null;
        } else {
            sectionHeaderMatcher = sectionHeaderPattern.matcher(line);
        }

        Matcher sectionFooterMatcher;
        if (sectionFooterPattern == null) {
            sectionFooterMatcher = null;
        } else {
            sectionFooterMatcher = sectionFooterPattern.matcher(line);
        }

        if (sectionFooterMatcher != null && sectionFooterMatcher.matches()) {
            currentSection = topLevelSection;
        } else if (sectionHeaderMatcher != null &&
                   sectionHeaderMatcher.matches()) {
            String sectionName = sectionHeaderMatcher.group(1);
            currentSection = sections.get(sectionName);
            if (currentSection == null) {
                currentSection = new ArrayList<String[]>();
                sections.put(sectionName, currentSection);
            }
        } else {
            parseConfigLine(line);
        }
    }

    public void parseCommentLine(String line) {
    }

    public void parseBlankLine(String line) {
    }

    public boolean isCommentLine(String line) {
        boolean result = false;
        if (isHashCommentAllowed()) {
            Matcher matcher = HASH_COMMENT_PATTERN.matcher(line);
            result = matcher.matches();
        }
        if (isSlashCommentAllowed()) {
            Matcher matcher = SLASH_COMMENT_PATTERN.matcher(line);
            result = matcher.matches() || result;
        }
        if (isSemicolonCommentAllowed()) {
            Matcher matcher = SEMICOLON_COMMENT_PATTERN.matcher(line);
            result = matcher.matches() || result;
        }
        return result;
    }

    public boolean isBlankLine(String line) {
        return StringUtils.isBlank(line);
    }

    public void storeLine(String[] line) {
        currentSection.add(line);
    }
    //--------------------------------------------------------------------------
    // Abstract methods
    //--------------------------------------------------------------------------

    public void parseConfigLine(String line) {
        switch (parseMode) {
            case tokenized:
                parseTokenized(line);
                break;
            case fixed:
                parseFixed(line);
                break;
            case keyvalue:
                parseKeyValue(line);
                break;
        }
    }

    public void parseKeyValue(String line) {
        int keySeparatorIndex = line.indexOf(keySeparator);
        String key;
        String valueString;
        if (keySeparatorIndex < 0) {
            if (keySeparatorOptional) {
                key = line.trim();
                valueString = "";
            } else {
                return;
            }
        } else {
            key = line.substring(0, keySeparatorIndex).trim();
            valueString = line.substring(
                    keySeparatorIndex + keySeparator.length()
            ).trim();
        }

        String[] values;
        if (separator == null) {
            values = new String[]{valueString};
        } else {
            StrTokenizer tokenizer = new StrTokenizer(valueString, separator);
            values = tokenizer.getTokenArray();
        }

        String[] result = new String[values.length + 1];
        result[0] = key;
        System.arraycopy(values, 0, result, 1, values.length);

        storeLine(result);
    }

    public void parseFixed(String line) {
        String[] values = new String[positions.length + 1];
        int start = 0;
        int i = 0;
        for (; i < positions.length; i++) {
            int stop = positions[i];
            values[i] = line.substring(start, stop).trim();
            start = stop;
        }
        values[i] = line.substring(start).trim();
        storeLine(values);
    }

    public void parseTokenized(String line) {
        StrTokenizer tokenizer = new StrTokenizer(line, separator);
        tokenizer.setIgnoreEmptyTokens(ignoreEmptyTokens);
        tokenizer.setQuoteChar(quoteChar);
        String[] tokens = tokenizer.getTokenArray();
        storeLine(tokens);
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------


    public boolean isHashCommentAllowed() {
        return hashCommentAllowed;
    }

    public void setHashCommentAllowed(boolean hashCommentAllowed) {
        this.hashCommentAllowed = hashCommentAllowed;
    }

    public boolean isSlashCommentAllowed() {
        return slashCommentAllowed;
    }

    public void setSlashCommentAllowed(boolean slashCommentAllowed) {
        this.slashCommentAllowed = slashCommentAllowed;
    }

    public boolean isSemicolonCommentAllowed() {
        return semicolonCommentAllowed;
    }

    public void setSemicolonCommentAllowed(boolean semicolonCommentAllowed) {
        this.semicolonCommentAllowed = semicolonCommentAllowed;
    }

    public Pattern getSectionHeaderPattern() {
        return sectionHeaderPattern;
    }

    public void setSectionHeaderPattern(Pattern sectionHeaderPattern) {
        this.sectionHeaderPattern = sectionHeaderPattern;
    }

    public Pattern getSectionFooterPattern() {
        return sectionFooterPattern;
    }

    public void setSectionFooterPattern(Pattern sectionFooterPattern) {
        this.sectionFooterPattern = sectionFooterPattern;
    }

    public List<String[]> getTopLevelSection() {
        return topLevelSection;
    }

    public ParseMode getParseMode() {
        return parseMode;
    }

    public void setParseMode(ParseMode parseMode) {
        this.parseMode = parseMode;
    }

    public int getSkipHeaderLines() {
        return skipHeaderLines;
    }

    public void setSkipHeaderLines(int skipHeaderLines) {
        this.skipHeaderLines = skipHeaderLines;
    }

    public Integer getReadLines() {
        return readLines;
    }

    public void setReadLines(Integer readLines) {
        this.readLines = readLines;
    }

    public StrMatcher getSeparator() {
        return separator;
    }

    public void setSeparator(StrMatcher separator) {
        this.separator = separator;
    }

    public boolean isIgnoreEmptyTokens() {
        return ignoreEmptyTokens;
    }

    public void setIgnoreEmptyTokens(boolean ignoreEmptyTokens) {
        this.ignoreEmptyTokens = ignoreEmptyTokens;
    }

    public int[] getPositions() {
        return positions;
    }

    public void setPositions(int... positions) {
        this.positions = positions;
    }

    public String getKeySeparator() {
        return keySeparator;
    }

    public void setKeySeparator(String keySeparator) {
        this.keySeparator = keySeparator;
    }

    public boolean isKeySeparatorOptional() {
        return keySeparatorOptional;
    }

    public void setKeySeparatorOptional(boolean keySeparatorOptional) {
        this.keySeparatorOptional = keySeparatorOptional;
    }

    public char getQuoteChar() {
        return quoteChar;
    }

    public void setQuoteChar(char quoteChar) {
        this.quoteChar = quoteChar;
    }
}
