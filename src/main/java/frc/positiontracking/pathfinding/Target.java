package frc.positiontracking.pathfinding;

import java.util.ArrayList;

import frc.positiontracking.fieldmap.geometry.Vector;

public class Target {

    private static ArrayList<Vector> targets;
    private static Vector target;

    public Target() {
        targets = new ArrayList<Vector>();
    }

    public static void put(Vector v) {
        targets.add(v);
    }

    public static void put(int i, Vector v) {
        targets.add(i, v);
    }

    public static Vector get(int i) {
        return targets.get(i);
    }

    public static void remove(int i) {
        targets.remove(0);
    }

    public static ArrayList<Vector> getTargets() {
        return targets;
    }

    public static int size() {
        return targets.size();
    }

    public static Vector getNext() {
        if (size() > 0) {
            return targets.get(targets.size() - 1);
        } else {
            return null;
        }
    }

    public static void setTarget(Vector v) {
        target = v;
    }

    public static Vector getTarget() {
        return target;
    }

    public static ArrayList<double[]> convert() {
        ArrayList<double[]> converted = new ArrayList<double[]>();
        for (int i = 0; i < size(); i++) {
            double[] point = {get(i).x, get(i).y};
            converted.add(point);
        }
        return converted;
    }
}