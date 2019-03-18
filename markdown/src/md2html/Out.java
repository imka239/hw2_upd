package md2html;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class Out {
    OutputStreamWriter out;
    public void println(String s) throws IOException {
        out.write(s);
        out.write('\n');
    }

    public void println() throws IOException {
        out.write('\n');
    }

    public void println(char s) throws IOException {
        out.write(s);
        out.write('\n');
    }

    public Out(OutputStream out) {
        this.out = new OutputStreamWriter(out);
    }

    public void close() throws IOException {
        out.close();
    }

    public void print(String s) throws IOException {
        out.write(s);
    }
}
