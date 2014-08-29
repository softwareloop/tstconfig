package com.softwareloop.tstconfig;

/**
 * Created by predo on 13/08/14.
 */
public class Command {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    final String encoding;
    final String[] cmdArray;

    String stdout;
    String stderr;
    Integer exitValue;
    Throwable throwable;


    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public Command(String encoding, String... cmdArray) {
        this.encoding = encoding;
        this.cmdArray = cmdArray;
    }

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    public void execute() {
        try {
            Runtime rt = Runtime.getRuntime();

            Process proc = rt.exec(cmdArray);

            ReaderThread stdoutReader =
                    new ReaderThread(proc.getInputStream(), encoding);
            ReaderThread stderrReader =
                    new ReaderThread(proc.getErrorStream(), encoding);

            stdoutReader.start();
            stderrReader.start();

            exitValue = proc.waitFor();
            stdoutReader.join();
            stderrReader.join();

            stdout = stdoutReader.getBuffer().toString();
            stderr = stderrReader.getBuffer().toString();
        } catch (Throwable t) {
            throwable = t;
        }
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------


    public String getEncoding() {
        return encoding;
    }

    public String[] getCmdArray() {
        return cmdArray;
    }

    public Integer getExitValue() {
        return exitValue;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

}
