package frc.control;

import java.util.ArrayList;

import frc.gen.BIGData;
import frc.pathfinding.fieldmap.geometry.*;
import frc.pathfinding.*;

public class BezierControl extends Mode {

    private static final double SPEED = 0.05;
    private static final double e = 4;

    private static Vector velocity;
    private static Vector currentPos;
    private static Vector targetPos;

    private static boolean newSpline;

    private static double d;
    private static double distance;
    private static double vx, vy;
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
            return false;
        }
        vx = velocity.y * -1;
        vy = velocity.x;
        BIGData.requestDrive(vx, vy, 0);
        return true;
    }

    private static void runBezier() {
        if (newSpline) {
            spline = new Bezier(currentPos, control_pts.get(0), control_pts.get(1), control_pts.get(2));
            newSpline = false;
            distance = currentPos.distanceTo(targetPos);
            d = currentPos.distanceTo(spline.getNext(counter));
            velocity = currentPos.subtract(spline.getNext(counter)).multiply(1 / d).multiply(SPEED);
        }
        distance = currentPos.distanceTo(targetPos);
        d = currentPos.distanceTo(spline.getNext(counter));
        
        // TODO: test the value
        if (d <= 1 && counter < spline.size()) {
            counter++;
            velocity = currentPos.subtract(spline.getNext(counter)).multiply(1 / d).multiply(SPEED);
        }
        //TODO: remove after debugging
        // System.out.println("counter: " + counter);
        // System.out.println("d: " + d);
        // System.out.println("curr_x: " + currentPos.x + " curr_y: " + currentPos.y);
        // System.out.println("spline_x: " + spline.getNext(counter).x + " spline_y: " + spline.getNext(counter).y);
        // System.out.println("vx: " + velocity.x + " vy: " + velocity.y);
    }
}