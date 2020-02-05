package frc.control;

import frc.gen.BIGData;
import frc.positiontracking.fieldmap.geometry.Vector;
import frc.positiontracking.pathfinding.Pathfinding;
import frc.positiontracking.pathfinding.Target;

public class PathfindingControl extends Mode {

    public static final double SPEED = 0.3;

    private Vector velocity;
    private double d;
    private boolean bezier;

    public Pathfinding path;

    public PathfindingControl() {
        path = new Pathfinding();
        bezier = false;
    }

    @Override
    public boolean loop() {
        Vector pos = BIGData.getPosition("curr");
        //TODO: change this back to current position
        pos = new Vector(30, 250);
        if (Target.size() <= 0) {
            path.searchAStar(pos);
        }
        Vector endPos = Target.getNext();
        if (endPos == null) {
            d = pos.distanceTo(pos);
            velocity = path.searchPFP(pos);
        } else if (bezier) {
            d = pos.distanceTo(pos);
            velocity = path.bezier(0);
        } else {
            d = pos.distanceTo(endPos);
            velocity = endPos.subtract(pos).multiply(1 / d);
        }
        //TODO: Remove this after debugging
        //System.out.println("d: " + d + " vx: " + velocity.x + " vy: " + velocity.y);
        double speed = SPEED * Math.min(d / 36.0, 1);
        velocity = velocity.multiply(speed);
        if (Target.size() <= 0)
            return false;
        if (d < 5) {
            //BIGData.requestDrive(0, 0, 0);
            Target.remove(0);
        }
        //BIGData.requestDrive(-1 * velocity.y, velocity.x, 0);
        return true;
    }

}