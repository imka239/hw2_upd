package md2html;

import java.io.*;
import java.lang.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class Source {
    private static final int BUF_SIZE = 1024;

    protected int bufPos;
    protected int bufEnd;
    InputStreamReader input;
    private char[] buf;

    public char getChar() throws ExceptionParser {
        if (!hasNextChar()) {
            throw error("End of source");
        }
        return buf[bufPos];
    }

    public String readLine() throws ExceptionParser, IOException {
        StringBuilder ret = new StringBuilder();
        while (hasNextChar() && getChar() != '\n') {
            ret.append(readChar());
        }
        bufPos++;
        check();
        return ret.toString();
    }

    public boolean hasNextChar() {
        return bufEnd != -1;
    }

    public char readChar() throws ExceptionParser, IOException {
        check();
        char ret = getChar();
        bufPos++;
        check();
        return ret;
    }

    public Source(InputStream inputStr) throws ExceptionParser, IOException {
        input = new InputStreamReader(inputStr, StandardCharsets.UTF_8);
        buf = new char[BUF_SIZE];
        getChars();
    }

    private void check() throws IOException {
        if (bufPos == bufEnd) {
            getChars();
        }
    }

    private void getChars() throws IOException {
        bufPos = 0;
        do {
            bufEnd = input.read(buf, 0, BUF_SIZE);
        } while (bufEnd == 0);
    }
    public ExceptionParser error(String msg) {
        return new ExceptionParser(msg);
    }
}
