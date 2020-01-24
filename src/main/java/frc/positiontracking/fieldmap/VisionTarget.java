package frc.positiontracking.fieldmap;

import frc.positiontracking.fieldmap.geometry.Vector;

public class VisionTarget {

    private static final double HIGH_HEIGHT = 39.125;
    private static final double LOW_HEIGHT = 31.5;

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