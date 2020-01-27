package frc.positiontracking.pathfinding;

import java.util.ArrayList;

import frc.positiontracking.fieldmap.geometry.Vector;

public class Target {

    private static ArrayList<Vector> targets;

    public Target() {
        targets = new ArrayList<Vector>();
    }

    public static void putTarget(Vector v) {
        targets.add(v);
    }

    public static Vector get(int i) {
        return targets.get(i);
    }

    public static void remove(int i) {
        targets.remove(0);
    }

    public static int length() {
        return targets.size();
    }

    public static ArrayList<Vector> getTargets() {
        return targets;
    }

}