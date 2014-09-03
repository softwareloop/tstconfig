package com.softwareloop.tstconfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Created by predo on 27/08/14.
 */
public class Runner {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    public final static String TSTCONFIG_PROPERTIES = "tstconfig.properties";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Interface/abstract class implementations
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    public static void main(String[] args) {
        Properties prop = new Properties();

        ClassLoader classLoader = Runner.class.getClassLoader();
        InputStream inputStream =
                classLoader.getResourceAsStream(TSTCONFIG_PROPERTIES);
        try {
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        String version = prop.getProperty("version");
        System.out.println(String.format("Tstconfig %s", version));

        if (args.length == 0) {
            System.out.println("No definition file provided.");
            System.out.println("Command line syntax:");
            System.out.println("tstconfig [DEFINITION_FILE]...");
            System.exit(0);
        }

        Test test = new Test();
        Parser parser = new Parser(test);
        for (String filename : args) {
            System.out.println(
                    String.format("Reading definition file: %s", filename));
            try {
                List<String> lines =
                        ConfigUtils.readLinesFromFile(filename);
                parser.parseLines(lines);
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }

        System.out.println();
        if (test.isTestPass() ) {
            System.out.println("SUMMARY REPORT: PASS");
        } else {
            System.out.println("SUMMARY REPORT: FAIL");
        }
        System.out.println(
                String.format(
                        "Assertions tested: %d",
                        test.getAssertionsTested()));
        System.out.println(
                String.format(
                        "Assertions passed: %d",
                        test.getAssertionsPassed()));
        System.out.println(
                String.format(
                        "Assertions failed: %d",
                        test.getAssertionsFailed()));
        System.out.println(
                String.format(
                        "Errors: %d",
                        test.getErrors()));
    }

    //--------------------------------------------------------------------------
    // Abstract methods
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

}
