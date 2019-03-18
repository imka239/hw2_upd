package md2html;

import java.io.IOException;
import java.util.ArrayList;


public class ParserParagraph {
    private Source source;
    private Out out;

    private void skip() throws ExceptionParser, IOException {
        while (source.hasNextChar() && (source.getChar() == '\n' || source.getChar() == '\r')) {
            source.readLine();
        }
    }

    public void convert() throws ExceptionParser, IOException {
        while (source.hasNextChar()) {
            skip();
            if (!source.hasNextChar()) {
                break;
            }
            ArrayList<String> strings = new ArrayList<>();
            while (source.hasNextChar() &&  !isEmptyLine()) {
                strings.add(source.readLine());
            }
            i = 0;
            now = 0;
            parseHead(strings);
        }
    }

    private int now;


    private boolean boldStar, boldUnder, strongBoldStar, strongBoldUnder;
    private boolean code, mark, under, strikethrough, opened;
    private StringBuilder http = new StringBuilder();

    private void parseHead(ArrayList<String> strings) throws ExceptionParser, IOException {
        if (strings.get(0).charAt(now) == '#') {
            while (strings.get(0).charAt(now) == '#') {
                now++;
            }
            if (strings.get(0).charAt(now) == ' ' && now < 7) {
                out.print("<h" + now + '>');
                int par = now;
                now++;
                parseMainBody(strings);
                out.println("</h" + par + '>');
            } else {
                now = 0;
                parseParagraph(strings);
            }
        } else {
            parseParagraph(strings);
        }
    }

    private void parseParagraph(ArrayList<String> strs) throws ExceptionParser, IOException {
        out.print("<p>");
        parseMainBody(strs);
        out.println("</p>");
    }

    private int i;
    private StringBuilder str = new StringBuilder();

    private void app(String ans) {
        if (!opened) {
            str.append(ans);
        } else {
            http.append(ans);
        }
    }

    private void parseMainBody(ArrayList<String> strs) throws ExceptionParser, IOException {
        str = new StringBuilder();
        while (i < strs.size()) {
            String s = strs.get(i);
            while (now < s.length() && s.charAt(now) != '\r' && s.charAt(now) != '\n') {
                int x = 1;
                if (s.charAt(now) == '*' || s.charAt(now) == '+') {
                    x = 0;
                }
                if (s.charAt(now) == '*' || s.charAt(now) == '_') {
                    app(testBold(s, x));
                } else if (s.charAt(now) == '-' || s.charAt(now) == '+') {
                    app(testStrikeUnder(s, x));
                } else if (s.charAt(now) == '~') {
                    app(testMark(s));
                } else if (s.charAt(now) == '`') {
                    app(testCode(s));
                } else if (s.charAt(now) == '!' && now < s.length() - 1 && s.charAt(now + 1) == '[') {
                    str.append(testPict(strs));
                    s = strs.get(i);
                } else if (s.charAt(now) == '(') {
                    if (now > 0 && s.charAt(now - 1) == ']') {
                        str.append(testHttp(strs));
                        s = strs.get(i);
                    } else {
                        str.append('(');
                        now++;
                    }
                } else if (s.charAt(now) == '[') {
                    str.append(testOpenBrace(s));
                    opened = true;
                } else if (s.charAt(now) == ']') {
                    opened = false;
                    now++;
                } else {
                    app(testChar(s));
                }
            }
            if (i != strs.size() - 1) {
                app("\n");
            }
            now = 0;
            i++;
        }
        out.print(str.toString());
    }

    private String testMark(String s) {
        now++;
        mark = !mark;
        return make(mark, "<mark>", "</mark>");
    }

    private String testPict(ArrayList<String> strin) {
        StringBuilder sb = new StringBuilder();
        now += 2;
        sb.append("<img alt='");
        while (i < strin.size() && now < strin.get(i).length() && strin.get(i).charAt(now) != ')') {
            if (strin.get(i).charAt(now) == ']') {
                now += 2;
                sb.append("' src='");
                continue;
            }
            sb.append(strin.get(i).charAt(now));
            now++;
            if (now == strin.get(i).length()) {
                now = 0;
                i++;
                sb.append('\n');
            }
        }
        now++;
        sb.append("'>");
        return sb.toString();
    }

    private String testHttp(ArrayList<String> strin) {
        StringBuilder sb = new StringBuilder();
        now++;
        while (i < strin.size() && now < strin.get(i).length() && strin.get(i).charAt(now) != ')') {
            sb.append(strin.get(i).charAt(now));
            now++;
            if (now == strin.get(i).length()) {
                i++;
                now = 0;
                sb.append('\n');
            }
        }
        now++;
        sb.append("'>").append(http).append("</a>");
        http = new StringBuilder();
        return sb.toString();
    }

    private String testOpenBrace(String s) {
        now++;
        return "<a href='";
    }

    private String testChar(String s) {
        now++;
        switch (s.charAt(now - 1)) {
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            case '&':
                return "&amp;";
            case '\\':
                if (now < s.length() && (s.charAt(now) == '*' || s.charAt(now) == '_')) {
                    now++;
                    return String.valueOf(s.charAt(now - 1));
                }
            default:
                return String.valueOf(s.charAt(now - 1));
        }
    }

    private String testCode(String s) {
        now++;
        code = !code;
        return make(code, "<code>", "</code>");
    }

    private String testStrikeUnder(String s, int x) throws ExceptionParser, IOException {
        int k = amountOfChars(s);
        switch (k) {
            case 1:
                now++;
                if (x == 1) {
                    return String.valueOf('-');
                }
                return String.valueOf('+');
            case 2:
                now += 2;
                if (x == 1) {
                    strikethrough = !strikethrough;
                    return make(strikethrough, "<s>", "</s>");
                }
                under = !under;
                return make(under, "<u>", "</u>");
            default:
                out.close();
                throw new ExceptionParser("It's more than 2 plus|minus");
        }
    }

    private String testBold(String s, int x) throws ExceptionParser, IOException {
        int k = amountOfChars(s);
        switch (k) {
            case 1:
                now++;
                if ((now == s.length() || s.charAt(now) == ' ' || s.charAt(now) == '\n') && (now == 1 ||s.charAt(now - 2) == ' ')) {
                    return String.valueOf(s.charAt(now - 1));
                }
                if (x == 0) {
                    boldStar = !boldStar;
                    return make(boldStar, "<em>", "</em>");
                }
                boldUnder = !boldUnder;
                return make(boldUnder, "<em>", "</em>");
            case 2:
                now += 2;
                if (x == 0) {
                    strongBoldStar = !strongBoldStar;
                    return make(strongBoldStar, "<strong>", "</strong>");
                }
                strongBoldUnder = !strongBoldUnder;
                return make(strongBoldUnder, "<strong>", "</strong>");
            default:
                out.close();
                throw new ExceptionParser("It's more than 2 bolds|stars");
        }
    }

    private String make(boolean x, String a1, String a2) {
        if (x) {
            return a1;
        }
        return a2;
    }


    private int amountOfChars(String s) {
        int ans = 0; int pos = now; int ch = s.charAt(now);
        while (pos < s.length() && s.charAt(pos) == ch) {
            ans++; pos++;
        }
        return ans;
    }


    private boolean isEmptyLine() throws ExceptionParser {
        return (!(source.hasNextChar() && (source.getChar() != '\n' && source.getChar() != '\r')));
    }

    ParserParagraph(Source src, Out out) {
        this.source = src;
        this.out = out;
    }
}