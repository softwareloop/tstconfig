package com.softwareloop.tstconfig;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Paolo Predonzani (paolo.predonzani@gmail.com)
 */
public class ConfigUtils {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    public static final String DEFAULT_ENCODING = "UTF-8";

    //--------------------------------------------------------------------------
    // Static methods
    //--------------------------------------------------------------------------

    public static List<String> readLinesFromFile(
            String filename
    ) throws IOException {
        return readLinesFromFile(filename, DEFAULT_ENCODING);
    }

    public static List<String> readLinesFromFile(
            String filename, String encoding
    ) throws IOException {
        List<String> lines = new ArrayList<String>();
        File file = new File(filename);
        LineIterator it = FileUtils.lineIterator(file, encoding);
        try {
            while (it.hasNext()) {
                String line = it.nextLine();
                lines.add(line);
            }
        } finally {
            it.close();
        }
        return lines;
    }

    public static List<String> readLinesFromString(String string) {
        String[] lines = string.split(System.getProperty("line.separator"));
        return new ArrayList<String>(Arrays.asList(lines));
    }

    public static List<String> readLinesFromCommand(
            int sleepMillis, String... cmdArray
    ) {
        Command command = new Command(DEFAULT_ENCODING, cmdArray);
        command.execute();
        Throwable e = command.getThrowable();
        if (e != null) {
            System.out.print("ERROR: ");
            e.printStackTrace(System.out);
        }
        Integer exitValue = command.getExitValue();
        if (exitValue != 0) {
            System.out.println(
                    "ERROR: Command returned error code: " + exitValue
            );
        }
        String stderr = command.getStderr();
        if (!stderr.isEmpty()) {
            System.out.println("WARN: Command wrote to stderr:\n" + stderr);
        }
        return readLinesFromString(command.getStdout());
    }

    public static List<String> readLinesFromCommand(String... cmdArray) {
        return readLinesFromCommand(0, cmdArray);
    }
}
