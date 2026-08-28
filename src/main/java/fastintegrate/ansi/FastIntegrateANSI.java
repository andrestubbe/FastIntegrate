package fastintegrate.ansi;

/**
 * High-performance ANSI styling and 120-column terminal formatter.
 * Provides dark gray tree branches, bold white values, and middle-path truncation.
 */
public final class FastIntegrateANSI {

    public static final int TERMINAL_WIDTH = 120;

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";

    // Colors
    public static final String DARK_GRAY = "\u001B[90m";
    public static final String BRIGHT_WHITE = "\u001B[97m";
    public static final String BOLD_WHITE = "\u001B[1;97m";
    public static final String CYAN = "\u001B[36m";
    public static final String BOLD_CYAN = "\u001B[1;36m";
    public static final String GREEN = "\u001B[32m";
    public static final String BOLD_GREEN = "\u001B[1;32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BOLD_YELLOW = "\u001B[1;33m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String BOLD_MAGENTA = "\u001B[1;35m";
    public static final String RED = "\u001B[31m";
    public static final String BOLD_RED = "\u001B[1;31m";

    // Tree characters
    public static final String TREE_BRANCH = DARK_GRAY + "├─ " + RESET;
    public static final String TREE_LAST   = DARK_GRAY + "└─ " + RESET;
    public static final String TREE_PIPE   = DARK_GRAY + "│  " + RESET;
    public static final String TREE_SPACE  = "   ";

    private FastIntegrateANSI() {}

    public static String boxHeader(String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(DARK_GRAY).append("╔").append("═".repeat(TERMINAL_WIDTH - 2)).append("╗").append(RESET).append("\n");
        String paddedTitle = " " + title + " ";
        int padLeft = (TERMINAL_WIDTH - 2 - stripAnsi(paddedTitle).length()) / 2;
        int padRight = TERMINAL_WIDTH - 2 - stripAnsi(paddedTitle).length() - padLeft;
        sb.append(DARK_GRAY).append("║").append(RESET)
          .append(" ".repeat(Math.max(0, padLeft)))
          .append(BOLD_CYAN).append(title).append(RESET)
          .append(" ".repeat(Math.max(0, padRight)))
          .append(DARK_GRAY).append("║").append(RESET).append("\n");
        sb.append(DARK_GRAY).append("╚").append("═".repeat(TERMINAL_WIDTH - 2)).append("╝").append(RESET);
        return sb.toString();
    }

    public static String sectionHeader(String title) {
        String bar = "── " + title + " ";
        int remaining = TERMINAL_WIDTH - stripAnsi(bar).length();
        if (remaining < 0) remaining = 0;
        return DARK_GRAY + "┌" + bar + "─".repeat(remaining) + RESET;
    }

    public static String sectionFooter() {
        return DARK_GRAY + "└" + "─".repeat(TERMINAL_WIDTH - 1) + RESET;
    }

    public static String keyValue(String key, Object value) {
        String valStr = value != null ? value.toString() : "null";
        return TREE_BRANCH + CYAN + String.format("%-28s", key) + RESET + DARK_GRAY + ": " + RESET + BOLD_WHITE + valStr + RESET;
    }

    public static String keyValueLast(String key, Object value) {
        String valStr = value != null ? value.toString() : "null";
        return TREE_LAST + CYAN + String.format("%-28s", key) + RESET + DARK_GRAY + ": " + RESET + BOLD_WHITE + valStr + RESET;
    }

    public static String truncateMiddle(String str, int maxLen) {
        if (str == null || str.length() <= maxLen) {
            return str;
        }
        if (maxLen <= 5) {
            return str.substring(0, maxLen);
        }
        int leftChars = (maxLen - 3) / 2;
        int rightChars = maxLen - 3 - leftChars;
        return str.substring(0, leftChars) + "..." + str.substring(str.length() - rightChars);
    }

    public static String stripAnsi(String text) {
        if (text == null) return "";
        return text.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}
