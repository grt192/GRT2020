package frc.pathfinding;

import java.util.ArrayList;
import java.util.List;

import frc.pathfinding.fieldmap.geometry.*;

public class Bezier {

    private static double size = 50;

    private Vector[] vector;

    private double[] xvals;
    private double[] yvals;

    private static List<Double> xnodes;
    private static List<Double> ynodes;

    public Bezier(Vector start, Vector control1, Vector control2, Vector end) {
        vector = new Vector[4];
        vector[0] = start;
        vector[1] = control1;
        vector[2] = control2;
        vector[3] = end;

        // init
        xvals = new double[4]; 
        yvals = new double[4];

        xnodes = new ArrayList<>();
        ynodes = new ArrayList<>();

        put();
        find();
    }

    public void put(){
        Vector n;
        for (int i = 0; i < 4; i++) {
            n = vector[i];
            xvals[i] = n.x;
            yvals[i] = n.y;
        }
    }

    public ArrayList<Node> find(){
        ArrayList<Node> nodes = new ArrayList<>();
		double xval;
		double yval;
		xnodes.clear();
		ynodes.clear();

		for (double x = 0; x <= 1; x += 1 / size) {
			xval = xFunc(xvals[0], xvals[1], xvals[2], xvals[3], x);
			yval = yFunc(yvals[0], yvals[1], yvals[2], yvals[3], x);
			xnodes.add(xval);
			ynodes.add(yval);
			nodes.add(new Node(new Vector(xval, yval)));
		}

		return nodes;
    }

    private double xFunc(double x0, double x1, double x2, double x3, double counter) {
        double cx = 3 * (x1 - x0);
		double bx = 3 * (x2 - x1) - cx;
		double ax = x3 - x0 - cx - bx;
		double xVal = ax * Math.pow(counter, 3) + bx * Math.pow(counter, 2) + cx * counter + x0;

		return xVal;
    }

    private double yFunc(double y0, double y1, double y2, double y3, double counter) {
        double cy = 3 * (y1 - y0);
		double by = 3 * (y2 - y1) - cy;
		double ay = y3 - y0 - cy - by;
		double yVal = ay * Math.pow(counter, 3) + by * Math.pow(counter, 2) + cy * counter + y0;

		return yVal;
    }

    public Vector getNext(int i) {
        return new Vector(xnodes.get(i), ynodes.get(i));
    }

    public double size() {
        return size;
    }
}