package concurrentcube;

import java.util.concurrent.Semaphore;

public class MonitorCube {

    private final Cube cube;

    public MonitorCube(Cube cube) {
        this.cube = cube;
        int size = cube.getSize();
        axesSemaphores = new Semaphore[numberOfAxes][size];
        waitingAxes = new int[numberOfAxes][size];
        this.workingAxes = new boolean[numberOfAxes][size];

        for (int i = 0; i < numberOfAxes; i++)
            for (int j = 0; j < size; j++) {
                workingAxes[i][j] = false;
                axesSemaphores[i][j] = new Semaphore(0);
            }
    }

    private final Semaphore mutex = new Semaphore(1, true);
    private final Semaphore[][] axesSemaphores;
    private final int[][] waitingAxes;
    private final Semaphore showSemaphore = new Semaphore(0, true);
    private final boolean[][] workingAxes;

    private int lastFinishedGroup;
    private int workingRotations = 0;
    private int waitingRotations = 0;
    private int workingShows = 0;
    private int waitingShows = 0;
    private int workingGroup;
    private final static int numberOfAxes = 3;

    private boolean waitingRotationCondition(Rotation r) {
        return workingShows > 0 || (workingRotations > 0
                && workingGroup != r.getGroup() || workingAxes[r.getGroup()][r.getLayer()]);
    }

    private boolean waitingShowsCondition() {
        return waitingRotations > 0 || workingRotations > 0;
    }

    private boolean letRotationsIn() {
        boolean released = false;
        for (int i = 1; i <= numberOfAxes && !released; i++) {
            for (int j = 0; j < cube.getSize() && !released; j++) {
                if (waitingAxes[(i + lastFinishedGroup) % numberOfAxes][j] > 0) {
                    released = true;
                    axesSemaphores[(i + lastFinishedGroup) % numberOfAxes][j].release();
                }
            }
        }
        return released;
    }

    public void rotationEntryProtocol(Rotation r) throws InterruptedException {
        mutex.acquire();
        try {
            if (waitingRotationCondition(r)) {
                waitingRotations++;
                waitingAxes[r.getGroup()][r.getLayer()]++;
                mutex.release();
                axesSemaphores[r.getGroup()][r.getLayer()].acquire(); /* Critical section inheritance */
                waitingAxes[r.getGroup()][r.getLayer()]--;
                waitingRotations--;
            }
        } catch (InterruptedException e) {
            /* Thread was interrupted on his way to rotate */
            mutex.acquireUninterruptibly();
            waitingAxes[r.getGroup()][r.getLayer()]--;
            waitingRotations--;
            mutex.release();
            throw e;
        }

        assert (!workingAxes[r.getGroup()][r.getLayer()]);
        workingAxes[r.getGroup()][r.getLayer()] = true;
        workingRotations++;
        if (workingRotations == 1) workingGroup = r.getGroup();

        boolean awakaned = false;
        for (int i = 0; i < cube.getSize() && !awakaned; i++) {
            if (waitingAxes[r.getGroup()][i] > 0 && !workingAxes[r.getGroup()][i]) {
                awakaned = true;
                axesSemaphores[r.getGroup()][i].release();
            }
        }
        if (!awakaned) {
            mutex.release();
        }

    }

    public void rotationExitProtocol(Rotation r) {
        /* We want to finish this section nevertheless thread was interrupted */
        mutex.acquireUninterruptibly();
        workingRotations--;
        workingAxes[r.getGroup()][r.getLayer()] = false;
        boolean released = false;
        lastFinishedGroup = r.getGroup();
        if (workingRotations == 0 && waitingShows > 0) {
            released = true;
            showSemaphore.release();
        } else if (workingRotations == 0 && waitingRotations > 0) {
            released = letRotationsIn();
        }
        if (!released) {
            mutex.release();
        }

    }

    public void showEntryProtocol() throws InterruptedException {

        mutex.acquire();

        try {
            if (waitingShowsCondition()) {
                waitingShows++;
                mutex.release();
                showSemaphore.acquire();
                waitingShows--;
            }
        } catch (InterruptedException e) {
            mutex.acquireUninterruptibly();
            waitingShows--;
            mutex.release();
            throw e;
        }

        workingShows++;

        if (waitingShows > 0) {
            showSemaphore.release();
        } else {
            mutex.release();
        }

    }

    public void showExitProtocol() {
        mutex.acquireUninterruptibly();
        workingShows--;

        if (workingShows == 0 && waitingRotations > 0) {
            letRotationsIn();
        } else {
            mutex.release();
        }

    }

}
