package md2html;

import java.io.FileInputStream;
import java.lang.*;


public class Text {
    public static void main(String[] args) throws ExceptionParser, Exception {
        Source src = new Source(new FileInputStream("test.txt"));
        while (src.hasNextChar()) {
            System.out.println(src.getChar());
            System.out.println(src.readLine());
        }
    }
}
