package frc.control;

import frc.gen.BIGData;
import frc.positiontracking.fieldmap.geometry.Vector;
import frc.positiontracking.pathfinding.Target;

public class PathfindingControl extends Mode {

    public static final double SPEED = 0.3;

    public PathfindingControl() {
    }

    @Override
    public boolean loop() {
        if (Target.length() > 0) {
            Vector endPos = Target.get(0);
            Vector pos = BIGData.getPosition("curr");
            double d = pos.distanceTo(endPos);
            Vector velocity = endPos.subtract(pos).multiply(1 / d);
            //TODO: Remove this after debugging
            //System.out.println("d: " + d + " vx: " + velocity.x + " vy: " + velocity.y);
            double speed = SPEED * Math.min(d / 36.0, 1);
            velocity = velocity.multiply(speed);
            if (d < 5) {
                BIGData.requestDrive(0, 0, 0);
                Target.remove(0);
            }
            BIGData.requestDrive(-1 * velocity.y, velocity.x, 0);
            return true;
        } else {
            return false;
        }
    }

}