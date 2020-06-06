package messagerosa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {

    private final static List<Character> KEYWORDS = Arrays.asList('*', '_', '~', '`');
    private final static List<Character> NO_SUB_PARSING_KEYWORDS = Arrays.asList('`');
    private final static List<Character> BLOCK_KEYWORDS = Arrays.asList('`');
    private final static boolean ALLOW_EMPTY = false;
    private final static boolean PARSE_HIGHER_ORDER_END = true;

    public static List<Style> parse(CharSequence text) {
        return parse(text, 0, text.length() - 1);
    }

    public static List<Style> parse(CharSequence text, int start, int end) {
        List<Style> styles = new ArrayList<>();
        for (int i = start; i <= end; ++i) {
            char c = text.charAt(i);
            if (KEYWORDS.contains(c) && precededByWhiteSpace(text, i, start) && !followedByWhitespace(text, i, end)) {
                if (BLOCK_KEYWORDS.contains(c) && isCharRepeatedTwoTimes(text, c, i + 1, end)) {
                    int to = seekEndBlock(text, c, i + 3, end);
                    if (to != -1 && (to != i + 5 || ALLOW_EMPTY)) {
                        String keyword = String.valueOf(c) + String.valueOf(c) + String.valueOf(c);
                        styles.add(new Style(keyword, i, to));
                        i = to;
                        continue;
                    }
                }
                int to = seekEnd(text, c, i + 1, end);
                if (to != -1 && (to != i + 1 || ALLOW_EMPTY)) {
                    styles.add(new Style(c, i, to));
                    if (!NO_SUB_PARSING_KEYWORDS.contains(c)) {
                        styles.addAll(parse(text, i + 1, to - 1));
                    }
                    i = to;
                }
            }
        }
        return styles;
    }

    private static boolean isCharRepeatedTwoTimes(CharSequence text, char c, int index, int end) {
        return index + 1 <= end && text.charAt(index) == c && text.charAt(index + 1) == c;
    }

    private static boolean precededByWhiteSpace(CharSequence text, int index, int start) {
        return index == start || Character.isWhitespace(text.charAt(index - 1));
    }

    private static boolean followedByWhitespace(CharSequence text, int index, int end) {
        return index >= end || Character.isWhitespace(text.charAt(index + 1));
    }

    private static int seekEnd(CharSequence text, char needle, int start, int end) {
        for (int i = start; i <= end; ++i) {
            char c = text.charAt(i);
            if (c == needle && !Character.isWhitespace(text.charAt(i - 1))) {
                if (!PARSE_HIGHER_ORDER_END || followedByWhitespace(text, i, end)) {
                    return i;
                } else {
                    int higherOrder = seekHigherOrderEndWithoutNewBeginning(text, needle, i + 1, end);
                    if (higherOrder != -1) {
                        return higherOrder;
                    }
                    return i;
                }
            } else if (c == '\n') {
                return -1;
            }
        }
        return -1;
    }

    private static int seekHigherOrderEndWithoutNewBeginning(CharSequence text, char needle, int start, int end) {
        for (int i = start; i <= end; ++i) {
            char c = text.charAt(i);
            if (c == needle && precededByWhiteSpace(text, i, start) && !followedByWhitespace(text, i, end)) {
                return -1; // new beginning
            } else if (c == needle && !Character.isWhitespace(text.charAt(i - 1)) && followedByWhitespace(text, i, end)) {
                return i;
            } else if (c == '\n') {
                return -1;
            }
        }
        return -1;
    }

    private static int seekEndBlock(CharSequence text, char needle, int start, int end) {
        for (int i = start; i <= end; ++i) {
            char c = text.charAt(i);
            if (c == needle && isCharRepeatedTwoTimes(text, needle, i + 1, end)) {
                return i + 2;
            }
        }
        return -1;
    }

    public static class Style {

        private final String keyword;
        private final int start;
        private final int end;

        public Style(char character, int start, int end) {
            this(String.valueOf(character), start, end);
        }

        public Style(String keyword, int start, int end) {
            this.keyword = keyword;
            this.start = start;
            this.end = end;
        }

        public String getKeyword() {
            return keyword;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }
}