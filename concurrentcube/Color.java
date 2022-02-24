package concurrentcube;

public enum Color {
    yellow(0),
    orange(1),
    blue(2),
    red(3),
    green(4),
    white(5);

    private final int value;
    private static final Color[] values = values();

    Color(int value) {
        this.value = value;
    }

    public static int colorToInt(Color c) {
        return c.value;
    }

    public static Color intToColor(int v) {
        return values[v];
    }

}
