package concurrentcube;

public enum Side {
    top(0),
    left(1),
    front(2),
    right(3),
    back(4),
    bottom(5);

    private final int value;

    Side(int value) {
        this.value = value;
    }

    private static final Side[] values = values();

    public static int sideToInt(Side c) {
        return c.value;
    }

    public static Side intToSide(int v) {
        return values[v];
    }

}
