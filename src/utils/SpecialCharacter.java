package utils;

public interface SpecialCharacter {

    static String LINE_BREAK = "\n";
    static String END_OF_STATEMENT = ";";
    static String TAB = "\t";
    static String DOUBLE_QUOTES = "\"";
    static final String STRUCTURE_DESTRUCTOR = "~";
    static final String STRUCTURE_OR_NAMESPACE_ACCESS = "::";
    static final String FILE_SCOPE_ACCESS = "::";

    static final char SPACE = ' ';
    static final char CLOSE_BRACE = '}';
    static final String MARK = "";
    static final char OPEN_BRACE = '{';

    String EMPTY = "";

    String DOT_IN_STRUCT = ".";

    char CLOSE_SQUARE_BRACE = ']';
    char OPEN_SQUARE_BRACE = '[';

    String POINTER = "*";

    String STD_NAMESPACE = "std::";

    char UNDERSCORE_CHAR = '_';
    String UNDERSCORE = "_";
    char DOT = '.';
    char EQUAL = '=';
    String NO = "#";

    String POINT_TO = "->";
}
