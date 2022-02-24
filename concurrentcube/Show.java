package concurrentcube;

public class Show {

    private final Cube cube;

    public Show(Cube cube) {
        this.cube = cube;
    }

    public String show() {
        String result;

        cube.getBeforeShowing().run();
        result = cube.toString();
        cube.getAfterShowing().run();

        return result;
    }

}
