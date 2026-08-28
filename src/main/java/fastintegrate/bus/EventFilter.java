package fastintegrate.bus;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Filter mechanism for matching event topics with support for exact, single-token wildcard (*),
 * and multi-token recursive wildcard (#) patterns.
 */
public final class EventFilter {

    private final String patternString;
    private final boolean isExact;
    private final boolean isCatchAll;
    private final Pattern compiledRegex;

    private EventFilter(String patternString) {
        this.patternString = Objects.requireNonNull(patternString, "patternString cannot be null");
        if (patternString.equals("#") || patternString.equals("*")) {
            this.isCatchAll = true;
            this.isExact = false;
            this.compiledRegex = null;
        } else if (!patternString.contains("*") && !patternString.contains("#")) {
            this.isExact = true;
            this.isCatchAll = false;
            this.compiledRegex = null;
        } else {
            this.isExact = false;
            this.isCatchAll = false;
            this.compiledRegex = compileToRegex(patternString);
        }
    }

    public static EventFilter of(String pattern) {
        return new EventFilter(pattern);
    }

    public static EventFilter all() {
        return new EventFilter("#");
    }

    public boolean matches(String topic) {
        if (topic == null) {
            return false;
        }
        if (isCatchAll) {
            return true;
        }
        if (isExact) {
            return patternString.equals(topic);
        }
        return compiledRegex.matcher(topic).matches();
    }

    public String pattern() {
        return patternString;
    }

    private static Pattern compileToRegex(String pattern) {
        final StringBuilder sb = new StringBuilder("^");
        final String[] segments = pattern.split("\\.", -1);
        for (int i = 0; i < segments.length; i++) {
            final String seg = segments[i];
            if (seg.equals("*")) {
                sb.append("[^.]+");
            } else if (seg.equals("#")) {
                sb.append(".*");
            } else {
                sb.append(Pattern.quote(seg));
            }
            if (i < segments.length - 1) {
                sb.append("\\.");
            }
        }
        sb.append("$");
        return Pattern.compile(sb.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventFilter that = (EventFilter) o;
        return Objects.equals(patternString, that.patternString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patternString);
    }

    @Override
    public String toString() {
        return "EventFilter[" + patternString + "]";
    }
}
