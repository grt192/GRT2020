/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.control;

import frc.gen.BIGData;
import frc.positiontracking.Position;
import frc.positiontracking.fieldmap.geometry.*;

/**
 * Add your docs here.
 */
public class PathfindingControl extends Mode {

    public static final double SPEED = 0.1;

    public PathfindingControl() {
    }

    @Override
    public boolean loop() {
        Vector pos = BIGData.getPosition("curr");
        Vector endPos = BIGData.getTarget();
        double d = pos.distanceTo(endPos);
        Vector velocity = endPos.subtract(pos).multiply(1 / d);
        System.out.println("x: " + pos.x + " y: " + pos.y);
        System.out.println("d: " + d + " vx: " + velocity.x + " vy: " + velocity.y);
        double speed = SPEED * Math.min(d / 36.0, 1);
        velocity = velocity.multiply(speed);
        if (d < 4) {
            BIGData.requestDrive(0, 0, 0);
            return false;
        }
        BIGData.requestDrive(velocity.x, velocity.y, 0);
        return true;
    }
}
