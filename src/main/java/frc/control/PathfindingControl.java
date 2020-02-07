package frc.control;

import frc.gen.BIGData;
import frc.pathfinding.fieldmap.geometry.*;
import frc.pathfinding.*;

public class PathfindingControl extends Mode {

    private static final double SPEED = 0.2;
    private static final double e = 4;

    private static Vector velocity;
    private static Vector currentPos;
    private static Vector nextPos;
    private static Vector targetPos;

    private static double d;

    public static Pathfinding path;
    public static Bezier spline;

    public PathfindingControl() {
        path = new Pathfinding();
    }

    @Override
    public boolean loop() {
        currentPos = BIGData.getPosition("curr");
        targetPos = Target.getTarget();

        if (Target.size() <= 0)
            path.searchAStar(currentPos);
        nextPos = Target.getNext();

        if (nextPos == null)
            runPFP();
        else
            runAStar();

        return check();
    }

    private static boolean check() {
        if (d < e) {
            BIGData.requestDrive(0, 0, 0);
            Target.remove(0);
            return false;
        }
        BIGData.requestDrive(-1 * velocity.y, velocity.x, 0);
        return true;
    }

    private static void runPFP() {
        d = currentPos.distanceTo(targetPos);
        velocity = path.searchPFP(currentPos).multiply(SPEED);
    }

    private static void runAStar() {
        d = currentPos.distanceTo(nextPos);
        velocity = nextPos.subtract(currentPos).multiply(1 / d).multiply(SPEED);
    }
}