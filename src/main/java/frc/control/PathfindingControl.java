package frc.control;

import frc.gen.BIGData;
import frc.pathfinding.fieldmap.geometry.*;
import frc.pathfinding.*;

public class PathfindingControl extends Mode {

    private static final double SPEED = 0.3;
    private static final double e = 4;

    private static Vector velocity;
    private static Vector currentPos;
    private static Vector nextPos;
    private static Vector targetPos;

    private static double d;
    private static double dtoNext;

    public static Pathfinding path;
    public static Bezier spline;

    public PathfindingControl() {
        path = new Pathfinding();
    }

    @Override
    public boolean loop() {
        BIGData.put("robot_centric", false);
        currentPos = BIGData.getPosition("curr");
        targetPos = Target.getTarget();

        if (Target.size() <= 0)
            path.searchAStar(new Vector(120, 161.625));
        
        if (Target.size() < 1) 
            return false;

        nextPos = Target.getNext();
        d = currentPos.distanceTo(targetPos);

        if (nextPos == null)
            runPFP();
        else
            runAStar();
        return check();
    }

    private static boolean check() {
        d = currentPos.distanceTo(targetPos);
        if (d < e) {
            BIGData.requestDrive(0, 0, 0);
            Target.clear();
            return false;
        } else if (dtoNext < e) {
            BIGData.requestDrive(0, 0, 0);
            Target.remove();
            return true;
        } else {
            // don't change this, the swerve field coordinates are different from the actual field coordinates
            BIGData.requestDrive(-1 * velocity.y, velocity.x, 0);
            return true;
        }
    }

    private static void runPFP() {
        velocity = path.searchPFP(currentPos).multiply(SPEED);
    }

    private static void runAStar() {
        dtoNext = currentPos.distanceTo(nextPos);
        velocity = nextPos.subtract(currentPos).multiply(1 / d).multiply(SPEED);
    }
}