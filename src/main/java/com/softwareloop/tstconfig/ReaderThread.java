package com.softwareloop.tstconfig;

import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * @author Paolo Predonzani (paolo.predonzani@gmail.com)
 */
public class ReaderThread extends Thread {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    final InputStreamReader reader;
    final StringWriter      buffer;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public ReaderThread(
            InputStream inputStream, String encoding
    ) throws UnsupportedEncodingException {
        this.reader = new InputStreamReader(inputStream, encoding);
        buffer = new StringWriter();
    }

    //--------------------------------------------------------------------------
    // Thread implementation
    //--------------------------------------------------------------------------

    @Override
    public void run() {
        try {
            IOUtils.copy(reader, buffer);
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace(System.out);
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

    public StringWriter getBuffer() {
        return buffer;
    }
}
