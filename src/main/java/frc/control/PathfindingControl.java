package frc.control;

import frc.gen.BIGData;
import frc.positiontracking.fieldmap.geometry.Vector;
import frc.positiontracking.pathfinding.Bezier;
import frc.positiontracking.pathfinding.Pathfinding;
import frc.positiontracking.pathfinding.Target;

public class PathfindingControl extends Mode {

    private static final double SPEED = 0.2;
    private static final double e = 4;

    private Vector velocity;
    private Vector currentPos;
    private Vector nextPos;
    private Vector targetPos;

    private boolean withSpline;
    private boolean newSpline;

    private double d;
    private int counter;

    public Pathfinding path;
    public Bezier spline;

    public PathfindingControl() {
        path = new Pathfinding();
        counter = 0;
        newSpline = true;
    }

    @Override
    public boolean loop() {
        currentPos = BIGData.getPosition("curr");
        targetPos = BIGData.getTarget();

        if (Target.size() <= 0)
            path.searchAStar(currentPos);
        
        withSpline = (BIGData.getBoolean("with_spline") && Target.size() >= 4);
        nextPos = Target.getNext();

        if (nextPos == null)
            runPFP();
        else if (withSpline)
            runBezier();
        else
            runAStar();

        return check();
    }

    private boolean check() {
        if (d < e) {
            BIGData.requestDrive(0, 0, 0);
            Target.remove(0);
            if (withSpline) {
                counter = 0;
                newSpline = true;
                Target.remove(1);
                Target.remove(2);
            }
            return false;
        }
        BIGData.requestDrive(-1 * velocity.y, velocity.x, 0);
        return true;
    }

    private void runBezier() {
        if (newSpline) {
            spline = new Bezier(Target.get(0), Target.get(1), Target.get(2), Target.get(3));
            newSpline = false;
        }
        d = currentPos.distanceTo(spline.getNext(counter));
        // TODO: test the value
        if (d <= 1 && counter < spline.size()) {
            counter++;
            velocity = currentPos.subtract(spline.getNext(counter)).multiply(1 / d).multiply(SPEED);
        }
    }

    private void runPFP() {
        d = currentPos.distanceTo(targetPos);
        velocity = path.searchPFP(currentPos).multiply(SPEED);
    }

    private void runAStar() {
        d = currentPos.distanceTo(nextPos);
        velocity = nextPos.subtract(currentPos).multiply(1 / d).multiply(SPEED);
    }
}