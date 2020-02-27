package frc.control;

import java.util.ArrayList;

import frc.gen.BIGData;
import frc.pathfinding.fieldmap.geometry.*;
import frc.pathfinding.*;

public class BezierControl extends Mode {

    private static final double SPEED = 0.3;
    private static final double e = 4;

    private static Vector velocity;
    private static Vector currentPos;
    private static Vector targetPos;

    private static boolean newSpline;

    private static double d;
    private static double distance;
    private static double dist;
    private static int counter;

    private static ArrayList<Vector> control_pts;

    public static Bezier spline;

    public BezierControl() {
        control_pts = new ArrayList<>();
        counter = 0;
        newSpline = true;
    }

    @Override
    public boolean loop() {
        currentPos = BIGData.getPosition("curr");
        targetPos = Target.getTarget();
        control_pts = Target.getBezier();
        runBezier();
        return check();
    }

    private static boolean check() {
        if (distance < e) {
            BIGData.requestDrive(0, 0, 0);
            Target.removeAction();
            return false;
        }

        BIGData.requestDrive(velocity.y, -velocity.x, 0);
        return true;
    }

    private static void runBezier() {
        if (newSpline) {
            spline = new Bezier(currentPos, control_pts.get(0), control_pts.get(1), control_pts.get(2));
            newSpline = false;
            counter++;
        }
        distance = currentPos.distanceTo(targetPos);
        d = currentPos.distanceTo(spline.getNext(counter));
        velocity = currentPos.subtract(spline.getNext(counter)).multiply(1 / d).multiply(SPEED);
        
        // TODO: test the value
        while ((dist = currentPos.distanceTo(spline.getNext(counter))) <= 3 && counter < spline.size() - 1) {
            counter++;
        }
    }
}