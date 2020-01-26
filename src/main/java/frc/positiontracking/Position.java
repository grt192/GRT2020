package frc.positiontracking;

import frc.positiontracking.fieldmap.geometry.Vector;

public class Position {

    private final Vector pos;
    private final double angle;

    public Position(Vector pos, double angle) {
        this.pos = pos;
        this.angle = angle;
    }

    public Vector getPos() {
        return pos;
    }

    public double getAngle() {
        return angle;
    }
}