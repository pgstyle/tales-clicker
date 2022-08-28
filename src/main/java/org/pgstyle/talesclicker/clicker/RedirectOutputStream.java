package org.pgstyle.talesclicker.clicker;

import java.io.IOException;
import java.io.OutputStream;
public final class RedirectOutputStream extends OutputStream {

    private final OutputStream stdout;
    private final OutputStream file;
    
    public RedirectOutputStream(OutputStream stdout, OutputStream file) {
        this.stdout = stdout;
        this.file = file;
    }
    
    @Override
    public void write(int b) throws IOException {
        stdout.write(b);
        file.write(b);
    }
    
    @Override
    public void write(byte[] b) throws IOException {
        stdout.write(b);
        file.write(b);
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        stdout.write(b, off, len);
        file.write(b, off, len);
    }
    
    @Override
    public void flush() throws IOException {
        stdout.flush();
        file.flush();
    }
    
    @Override
    public void close() throws IOException {
        try {
            file.close();
        } finally {
            stdout.close();
        }
    }

}
