package testingcube;

import concurrentcube.Cube;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Thread.sleep;

public class CubeTest {
    private static final int smallCube = 4;
    private static final int numberOfWalls = 6;
    private static final int smallNumberOfRotations = 10;
    private static final int cyclicNumber = 12;
    private static final int largeNumberOfRotations = 100000;
    private final Random rd = new Random();

    /* Non concurrent tests, just testing rotations */
    @Test
    void exampleTest(){

        var counter = new Object() { int value = 0; };
        Cube testedCube = new Cube(smallCube,
                (x, y) -> { ++counter.value; },
                (x, y) -> { ++counter.value; },
                () -> { ++counter.value; },
                () -> { ++counter.value; });

        try {
            testedCube.rotate(2, 0);
            testedCube.rotate(5, 1);
            assert(counter.value == 4);
            String state = testedCube.show();
            assert(counter.value == 6);
            assert(state.equals(ExpectedViews.EXPECTED_1));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void threeDifferentRotationsTestBackTopLeft(){

        var counter = new Object() { int value = 0; };
        Cube testedCube = new Cube(4,
                (x, y) -> { ++counter.value; },
                (x, y) -> { ++counter.value; },
                () -> { ++counter.value; },
                () -> { ++counter.value; });

        try {
            testedCube.rotate(4, 0);
            testedCube.rotate(0, 1);
            testedCube.rotate(1, 1);
            assert(counter.value == 6);
            String state = testedCube.show();
            assert(counter.value == 8);
            assert(state.equals(ExpectedViews.EXPECTED_2));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    void threeDifferentRotationsTestFrontBottomRight(){
        var counter = new Object() { int value = 0; };
        Cube testedCube = new Cube(smallCube,
                (x, y) -> { ++counter.value; },
                (x, y) -> { ++counter.value; },
                () -> { ++counter.value; },
                () -> { ++counter.value; });
        try {
            testedCube.rotate(2, 2);
            testedCube.rotate(5, 1);
            testedCube.rotate(3, 3);
            assert(counter.value == 6);
            String state = testedCube.show();
            assert(counter.value == 8);
            assert(state.equals(ExpectedViews.EXPECTED_3));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sixDifferentRotationsOnSmallCube(){
        var counter = new Object() { int value = 0; };
        Cube testedCube = new Cube(smallCube,
                (x, y) -> { ++counter.value; },
                (x, y) -> { ++counter.value; },
                () -> { ++counter.value; },
                () -> { ++counter.value; });
        try {
            testedCube.rotate(2, 2);
            testedCube.rotate(0, 3);
            testedCube.rotate(1, 0);
            testedCube.rotate(5, 1);
            testedCube.rotate(3, 0);
            testedCube.rotate(4, 0);
            String state = testedCube.show();
            assert(state.equals(ExpectedViews.EXPECTED_4) && counter.value == 14);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void lastNonConcurrent(){

        var counter = new Object() { int value = 0; };
        Cube testedCube = new Cube(smallCube,
                (x, y) -> { ++counter.value; },
                (x, y) -> { ++counter.value; },
                () -> { ++counter.value; },
                () -> { ++counter.value; });

        try {
            testedCube.rotate(2, 0);
            testedCube.rotate(3, 0);
            testedCube.rotate(4, 0);
            testedCube.rotate(5, 0);
            String state = testedCube.show();
            assert(state.equals(ExpectedViews.EXPECTED_5));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* Concurrent Tests */
    public class applyingRotation implements Runnable{

        private int layer;
        private int side;
        private Cube cube;

        public applyingRotation(int layer, int side, Cube cube) {
            this.layer = layer;
            this.side = side;
            this.cube = cube;
        }

        @Override
        public void run() {
            try {
                cube.rotate(side, layer);
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public class applyingShow implements Runnable{

        private Cube cube;

        public applyingShow(Cube cube){
            this.cube = cube;
        }

        @Override
        public void run() {
            try{
                cube.show();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    /* Number of colors is invariant in the cube, we're testing that here for small cube*/
    @Test
    void TestingSecurityForRotations(){
        var counter = new Object() { int value = 0; };
        Cube testedCube = new Cube(smallCube,
                (x, y) -> { ++counter.value; },
                (x, y) -> { ++counter.value; },
                () -> { ++counter.value; },
                () -> { ++counter.value; });
        try {
            ArrayList<Thread> t = new ArrayList(10000000);
            for(int i = 0; i < smallNumberOfRotations; i++){
                t.add(i, new Thread(new applyingRotation(rd.nextInt(smallCube), rd.nextInt(numberOfWalls), testedCube)));
                t.get(i).start();
            }

            for(int i = 0; i < smallNumberOfRotations; i++)
                t.get(i).join();


            assert(testedCube.isValid());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /* Testing invariance of color for many readers and writers */
    @Test
    public void testingManyReadersAndRotationsConcurrently() throws InterruptedException {
        var counter = new Object() { int value = 0; };
        Cube testedCube = new Cube(smallCube,
                (x, y) -> { ++counter.value; },
                (x, y) -> { ++counter.value; },
                () -> { ++counter.value; },
                () -> { ++counter.value; });

        ArrayList<Thread> shows = new ArrayList(10);
        ArrayList<Thread> rotations = new ArrayList(10);

        for(int i = 0; i < 10; i++){
            shows.add(i, new Thread(new applyingShow(testedCube)));
            rotations.add(i, new Thread(new applyingRotation(rd.nextInt(smallCube), rd.nextInt(numberOfWalls), testedCube)));
            shows.get(i).start();
            rotations.get(i).start();
        }

        for(int i = 0; i < 10; i++){
            shows.get(i).join();
            rotations.get(i).join();
        }

        assert(testedCube.isValid());
    }


    /* We can test whether cube after certain number of cyclic rotations is solved */
    @Test
    public void testedCube() throws InterruptedException {
        var counter = new Object() { int value = 0; };
        Cube testedCube = new Cube(smallCube,
                (x, y) -> { ++counter.value; },
                (x, y) -> { ++counter.value; },
                () -> { ++counter.value; },
                () -> { ++counter.value; });

        Thread[] threads = new Thread[cyclicNumber];
        Thread[] counterThreads = new Thread[cyclicNumber];
        for(int i = 0; i < cyclicNumber; i++){
            threads[i] = new Thread(new applyingRotation(i % 4, 1, testedCube));
            counterThreads[i] = new Thread(new applyingRotation(i % 4, 3, testedCube));

            counterThreads[i].start();
            threads[i].start();
        }

        for(Thread t : threads)
            t.join();

        assert(testedCube.isSolved() && testedCube.isValid());
    }

    /* Testing if we interrupt all threads cube will be solved */
    @Test
    public void testStopThreads(){
        var counter = new Object() { int value = 0; };
        Cube testedCube = new Cube(smallCube,
                (x, y) -> { ++counter.value; },
                (x, y) -> { ++counter.value; },
                () -> { ++counter.value; },
                () -> { ++counter.value; });
        Thread[] threads = new Thread[cyclicNumber];
        for(Thread t : threads){
            t = new Thread(new applyingRotation(rd.nextInt(smallCube), rd.nextInt(numberOfWalls), testedCube));
            t.start();
            t.interrupt();
        }
        assert(testedCube.isSolved());
    }

}
