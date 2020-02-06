package frc.positiontracking;

import frc.pathfinding.fieldmap.geometry.*;

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