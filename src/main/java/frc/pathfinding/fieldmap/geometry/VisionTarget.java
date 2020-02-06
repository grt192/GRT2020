package frc.pathfinding.fieldmap.geometry;

import frc.pathfinding.fieldmap.geometry.*;

public class VisionTarget {

    private static final double HIGH_HEIGHT = 98.188;
    private static final double LOW_HEIGHT = 16.5;

    public final Vector pos;
    public final double angle;
    public final double height;

    public VisionTarget(Vector pos, double angle, boolean high) {
        this.pos = pos;
        this.angle = angle;
        if (high) {
            height = HIGH_HEIGHT;
        } else {
            height = LOW_HEIGHT;
        }
    }
}