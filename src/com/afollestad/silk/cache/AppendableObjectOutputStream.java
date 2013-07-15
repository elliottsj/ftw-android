package com.afollestad.silk.cache;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * @author Aidan Follestad (afollestad)
 */
public class AppendableObjectOutputStream extends ObjectOutputStream {

    public AppendableObjectOutputStream(OutputStream output) throws IOException {
        super(output);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        reset();
    }
}
