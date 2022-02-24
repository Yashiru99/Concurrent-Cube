package concurrentcube;

import java.util.stream.IntStream;
import static concurrentcube.Side.*;

public class Rotation {

    private final Cube cube;
    private final int size;
    private final Side side;
    private final int layer;
    private int group;

    private static int simplifyLayer(Side side, int layer, int size) {
        int temporaryLayer = layer;
        if (side == right || side == bottom || side == back) temporaryLayer = size - 1 - temporaryLayer;
        assert (temporaryLayer >= 0);
        return temporaryLayer;

    }

    private static Color[][] rotateMatrix(Color[][] matrixToBeRotated) {
        for (int i = 0; i < matrixToBeRotated.length / 2; i++) {
            for (int j = i; j < matrixToBeRotated.length - i - 1; j++) {

                Color temp = matrixToBeRotated[i][j];
                matrixToBeRotated[i][j] = matrixToBeRotated[matrixToBeRotated.length - 1 - j][i];
                matrixToBeRotated[matrixToBeRotated.length - 1 - j][i] = matrixToBeRotated[matrixToBeRotated.length - 1 - i][matrixToBeRotated.length - 1 - j];
                matrixToBeRotated[matrixToBeRotated.length - 1 - i][matrixToBeRotated.length - 1 - j] = matrixToBeRotated[j][matrixToBeRotated.length - 1 - i];
                matrixToBeRotated[j][matrixToBeRotated.length - 1 - i] = temp;
            }
        }
        return matrixToBeRotated;
    }

    public Rotation(Cube cube, Side side, int layer) {
        this.cube = cube;
        this.side = side;
        this.size = cube.getSize();
        this.layer = layer;

        if (side == top || side == bottom) this.group = 0;
        else if (side == left || side == right) this.group = 1;
        else if (side == front || side == back) this.group = 2;
    }

    public int getGroup() {
        return group;
    }

    public Side getSide() {
        return side;
    }

    public Integer getLayer() {
        return simplifyLayer(side, layer, size);
    }

    public Integer getRealLayer() {
        return layer;
    }

    private static Color[] getColumn(Color[][] array, int index) {
        Color[] column = new Color[array[0].length];
        IntStream.range(0, column.length).forEach(i -> column[i] = array[i][index]);
        return column;
    }

    private static Color[] reverseArray(Color[] arr) {
        Color[] result = new Color[arr.length];
        for (int i = 0; i < arr.length; ++i)
            result[i] = arr[arr.length - 1 - i];
        return result;
    }

    private static void setColumn(Color[][] array, int index, Color[] column) {
        IntStream.range(0, column.length).forEach(i -> array[i][index] = column[i]);
    }

    public void rotate() {
        Color[][][] cubeMatrix = cube.getCubeMatrix();
        int size = cube.getSize();
        Color[] previousLayer;

        cube.getBeforeRotation().accept(sideToInt(side), layer);
        if (layer == 0) {
            cubeMatrix[Side.sideToInt(side)] = rotateMatrix(cubeMatrix[Side.sideToInt(side)]);
        }
        switch (side) {
            case top:
                if (layer == size - 1) {
                    for (int i = 0; i < 3; i++)
                        rotateMatrix(cubeMatrix[Side.sideToInt(bottom)]);
                }
                previousLayer = cubeMatrix[1][layer];
                cubeMatrix[1][layer] = cubeMatrix[2][layer];
                cubeMatrix[2][layer] = cubeMatrix[3][layer];
                cubeMatrix[3][layer] = cubeMatrix[4][layer];
                cubeMatrix[4][layer] = previousLayer;
                break;
            case left:
                if (layer == size - 1) {
                    for (int i = 0; i < 3; i++)
                        rotateMatrix(cubeMatrix[Side.sideToInt(right)]);
                }
                previousLayer = getColumn(cubeMatrix[4], size - 1 - layer);
                setColumn(cubeMatrix[4], size - 1 - layer, reverseArray(getColumn(cubeMatrix[5], layer)));
                setColumn(cubeMatrix[5], layer, getColumn(cubeMatrix[2], layer));
                setColumn(cubeMatrix[2], layer, getColumn(cubeMatrix[0], layer));
                setColumn(cubeMatrix[0], layer, reverseArray(previousLayer));
                break;
            case front:
                if (layer == size - 1) {
                    for (int i = 0; i < 3; i++)
                        rotateMatrix(cubeMatrix[Side.sideToInt(back)]);
                }
                previousLayer = cubeMatrix[0][size - layer - 1];
                cubeMatrix[0][size - layer - 1] = reverseArray(getColumn(cubeMatrix[1], size - 1 - layer));
                setColumn(cubeMatrix[1], size - 1 - layer, cubeMatrix[5][layer]);
                cubeMatrix[5][layer] = reverseArray(getColumn(cubeMatrix[3], layer));
                setColumn(cubeMatrix[3], layer, previousLayer);
                break;
            case right:
                if (layer == size - 1) {
                    for (int i = 0; i < 3; i++)
                        rotateMatrix(cubeMatrix[Side.sideToInt(left)]);
                }
                previousLayer = getColumn(cubeMatrix[2], size - 1 - layer);
                setColumn(cubeMatrix[2], size - 1 - layer, getColumn(cubeMatrix[5], size - 1 - layer));
                setColumn(cubeMatrix[5], size - 1 - layer, reverseArray(getColumn(cubeMatrix[4], layer)));
                setColumn(cubeMatrix[4], layer, reverseArray(getColumn(cubeMatrix[0], size - 1 - layer)));
                setColumn(cubeMatrix[0], size - 1 - layer, previousLayer);
                break;
            case back:
                if (layer == size - 1) {
                    for (int i = 0; i < 3; i++)
                        rotateMatrix(cubeMatrix[Side.sideToInt(front)]);
                }
                previousLayer = cubeMatrix[0][layer];
                cubeMatrix[0][layer] = getColumn(cubeMatrix[3], size - 1 - layer);
                setColumn(cubeMatrix[3], size - 1 - layer, reverseArray(cubeMatrix[5][size - 1 - layer]));
                cubeMatrix[5][size - 1 - layer] = getColumn(cubeMatrix[1], layer);
                setColumn(cubeMatrix[1], layer, reverseArray(previousLayer));
                break;
            case bottom:
                if (layer == size - 1) {
                    for (int i = 0; i < 3; i++)
                        rotateMatrix(cubeMatrix[Side.sideToInt(top)]);
                }
                previousLayer = cubeMatrix[1][size - 1 - layer];
                cubeMatrix[1][size - 1 - layer] = cubeMatrix[4][size - 1 - layer];
                cubeMatrix[4][size - 1 - layer] = cubeMatrix[3][size - 1 - layer];
                cubeMatrix[3][size - 1 - layer] = cubeMatrix[2][size - 1 - layer];
                cubeMatrix[2][size - 1 - layer] = previousLayer;
                break;
            default:
                break;
        }
        cube.getAfterRotation().accept(sideToInt(side), layer);
    }
}

