package frc.positiontracking;

import frc.positiontracking.fieldmap.geometry.Vector;

public class Position {

    public final Vector pos;
    public final double angle;

    public Position(Vector pos, double angle) {
        this.pos = pos;
        this.angle = angle;
    }
}