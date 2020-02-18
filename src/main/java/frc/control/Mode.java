package frc.control;

public abstract class Mode {
    private static DriverControl driverControl;
    private static PathfindingControl pathfindingControl;
    private static BezierControl bezierControl;
    private static ScoreControl scoreControl;
    private static Mode[] modes;

    public static void initModes() {
        driverControl = new DriverControl();
        pathfindingControl = new PathfindingControl();
        bezierControl = new BezierControl();
        scoreControl = new ScoreControl();
        modes = new Mode[4];
        modes[0] = driverControl;
        modes[1] = pathfindingControl;
        modes[2] = bezierControl;
        modes[3] = scoreControl;
    }

    public abstract boolean loop();

    public static Mode getMode(int i) {
        return modes[i];
    }

}
