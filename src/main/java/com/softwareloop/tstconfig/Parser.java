package com.softwareloop.tstconfig;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by predo on 27/08/14.
 */
public class Parser extends Config {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    final Test test;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public Parser(Test test) {
        this.test = test;
        setParseMode(ParseMode.TOKENIZED);
        setHashCommentAllowed(true);
    }

    //--------------------------------------------------------------------------
    // Interface/abstract class implementations
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // AbstractConfig overrides
    //--------------------------------------------------------------------------


    @Override
    public void parseLine(String line) {
        System.out.println(line);
        super.parseLine(line);
    }

    public void storeLine(String[] line) {
        String command = line[0];
        String[] args = Arrays.copyOfRange(line, 1, line.length);
        try {
            Method method = test.getClass().getMethod(command, String[].class);
            method.invoke(test, new Object[] {args});
        } catch (Throwable e) {
            Throwable rootException = ExceptionUtils.getRootCause(e);
            if (rootException == null) {
                rootException = e;
            }
            System.out.print("ERROR: ");
            rootException.printStackTrace(System.out);
        }
    }

//--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------


    //--------------------------------------------------------------------------
    // Abstract methods
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

}
