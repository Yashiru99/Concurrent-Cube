package concurrentcube;

import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static concurrentcube.Color.*;
import static concurrentcube.Side.*;

public class Cube {

    public static final String SOLVED =
            "0000"
                    + "0000"
                    + "0000"
                    + "0000"

                    + "1111"
                    + "1111"
                    + "1111"
                    + "1111"

                    + "2222"
                    + "2222"
                    + "2222"
                    + "2222"

                    + "3333"
                    + "3333"
                    + "3333"
                    + "3333"

                    + "4444"
                    + "4444"
                    + "4444"
                    + "4444"

                    + "5555"
                    + "5555"
                    + "5555"
                    + "5555";

    private final BiConsumer<Integer, Integer> beforeRotation;
    private final BiConsumer<Integer, Integer> afterRotation;
    private final Runnable beforeShowing;
    private final Runnable afterShowing;
    private final int size;
    static private final int numberOfWalls = 6;
    private final Color[][][] cubeMatrix;
    private final MonitorCube mc;

    public BiConsumer<Integer, Integer> getBeforeRotation() {
        return beforeRotation;
    }

    public BiConsumer<Integer, Integer> getAfterRotation() {
        return afterRotation;
    }

    public Runnable getBeforeShowing() {
        return beforeShowing;
    }

    public Runnable getAfterShowing() {
        return afterShowing;
    }

    public int getSize() {
        return size;
    }

    public Color[][][] getCubeMatrix() {
        return cubeMatrix;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        IntStream.range(0, numberOfWalls)
                .forEach(wall -> IntStream.range(0, size)
                        .forEach(row -> IntStream.range(0, size)
                                .forEach(column -> stringBuilder.append(colorToInt(cubeMatrix[wall][row][column])))));

        return stringBuilder.toString();
    }

    public Cube(int size,
                BiConsumer<Integer, Integer> beforeRotation,
                BiConsumer<Integer, Integer> afterRotation,
                Runnable beforeShowing,
                Runnable afterShowing) {

        this.size = size;
        this.beforeRotation = beforeRotation;
        this.afterRotation = afterRotation;
        this.beforeShowing = beforeShowing;
        this.afterShowing = afterShowing;
        this.cubeMatrix = new Color[numberOfWalls][size][size];

        IntStream.range(0, numberOfWalls)
                .forEach((wall -> IntStream.range(0, size)
                        .forEach(row -> IntStream.range(0, size).forEach(column -> cubeMatrix[wall][column][row] = intToColor(wall)))));

        this.mc = new MonitorCube(this);
    }

    public boolean isValid() {
        int[] checkColors = new int[numberOfWalls];
        for (int i = 0; i < numberOfWalls; i++)
            checkColors[i] = size * size;
        for (int i = 0; i < numberOfWalls; i++) {
            for (int col = 0; col < size; col++) {
                for (int row = 0; row < size; row++) {
                    checkColors[colorToInt(cubeMatrix[i][col][row])]--;
                }
            }
        }
        for (int i = 0; i < numberOfWalls; i++) {
            if (checkColors[i] != 0) return false;
        }

        return true;
    }

    public boolean isSolved() {
        return toString().equals(SOLVED);
    }

    public void rotate(int side, int layer) throws InterruptedException {
        Rotation r = new Rotation(this, intToSide(side), layer);

        mc.rotationEntryProtocol(r);
        r.rotate();
        mc.rotationExitProtocol(r);

    }

    public String show() throws InterruptedException {
        Show s = new Show(this);

        mc.showEntryProtocol();
        String showResult = s.show();
        mc.showExitProtocol();

        return showResult;
    }


}