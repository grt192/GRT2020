package frc.positiontracking.fieldmap;

import java.util.HashSet;
import java.util.Set;

import frc.positiontracking.fieldmap.geometry.Circle;
import frc.positiontracking.fieldmap.geometry.Polygon;
import frc.positiontracking.fieldmap.geometry.Vector;
import frc.robot.Robot;

public class FieldMap {

    private static double FIELD_WIDTH, FIELD_HEIGHT;
    private static Vector bounds;
    private Polygon wall;
    private static Polygon[] obstacles;
    private static VisionTarget[] visionTargets;

    public FieldMap() {
        buildMap();
        wall = new Polygon(new Vector(0, 0), new Vector(FIELD_HEIGHT, 0), new Vector(FIELD_HEIGHT, FIELD_WIDTH),
                new Vector(0, FIELD_WIDTH));
    }

    private static void buildMap() {
        obstacles = new Polygon[2];
        visionTargets = new VisionTarget[1];
        FIELD_WIDTH = 323.31;
        FIELD_HEIGHT = 629.25;
        bounds = new Vector(FIELD_HEIGHT, FIELD_WIDTH);

        Polygon RightSideTrench = new Polygon(new Vector(254, 320), new Vector(254, FIELD_WIDTH),
                new Vector(285, FIELD_WIDTH), new Vector(285, 320));
        Polygon LeftSideTrench = new Polygon(new Vector(254, 266), new Vector(285, 266), new Vector(285, 270),
                new Vector(254, 270));

        obstacles[0] = RightSideTrench;
        obstacles[1] = LeftSideTrench;

        VisionTarget farGoal = new VisionTarget(new Vector(FIELD_HEIGHT, 228.655), Math.PI, true);

        visionTargets[0] = farGoal;

    }

    public Vector closestWallPoint(Vector p) {
        return wall.closestPoint(p);
    }

    public Polygon[] getObstacles() {
        return obstacles;
    }

    public Vector getBounds() {
        return bounds;
    }

    public boolean shapeIntersects(Circle c) {
		if (c.outsideBounds(bounds))
			return true;
		for (Polygon poly : obstacles) {
			if (c.intersects(poly))
				return true;
		}
		return false;
    }
    
    public boolean shapeIntersects(Polygon p) {
		if (p.outsideBounds(bounds))
			return true;
		for (Polygon poly : obstacles) {
			if (p.intersects(poly))
				return true;
		}
		return false;
	}

    public Set<Vector> generateNodes() {
		double radius = Robot.ROBOT_RADIUS + 1.0;
		double bigRadius = radius + 0.5;
		Set<Vector> nodeSet = new HashSet<>();
		for (Polygon p : obstacles) {
			Vector[] nodes = p.getPossibleNodes(bigRadius);
			for (Vector v : nodes) {
				Circle c = new Circle(v, radius);
				if (!shapeIntersects(c))
					nodeSet.add(v);
			}
		}
		return nodeSet;
    }
    
    public boolean lineOfSight(Vector v1, Vector v2) {
		Vector dif = v2.subtract(v1);
		double d = v1.distanceTo(v2);
		if (d == 0.0)
			return true;
		Vector norm = dif.multiply(Robot.ROBOT_RADIUS / d).normal();
		Polygon rect = new Polygon(v1.add(norm), v2.add(norm), v2.subtract(norm), v1.subtract(norm));
		Circle startCircle = new Circle(v1, Robot.ROBOT_RADIUS);
		Circle endCircle = new Circle(v2, Robot.ROBOT_RADIUS);
		if (shapeIntersects(rect))
			return false;
		if (shapeIntersects(startCircle))
			return false;
		if (shapeIntersects(endCircle))
			return false;
		return true;
	}
}