package frc.pathfinding.fieldmap;

import java.util.HashSet;
import java.util.Set;

import frc.gen.BIGData;
import frc.pathfinding.fieldmap.geometry.*;
import frc.robot.Robot;

public class FieldMap {

	private static double FIELD_SHORT, FIELD_LONG;
	private static Vector bounds;
	private Polygon wall;
	private static Polygon[] obstacles;
	private static Polygon[] safeZones;
	private static double SHORTEST_SIDE;
	private static double ROBOT_RADIUS;

	public FieldMap() {
		SHORTEST_SIDE = Math.min(BIGData.getDouble("robot_width"), BIGData.getDouble("robot_height"));
		buildMap();
		wall = new Polygon(new Vector(0, 0), new Vector(FIELD_LONG, 0), new Vector(FIELD_LONG, FIELD_SHORT),
						new Vector(0, FIELD_SHORT));
		ROBOT_RADIUS = Math.max(BIGData.getDouble("robot_width"), BIGData.getDouble("robot_height")) / 2;
	}

	private static void buildMap() {
		obstacles = new Polygon[7];
		safeZones = new Polygon[3];
		FIELD_SHORT = 323.25;
		FIELD_LONG = 629.25;
		double TRENCH_WIDTH = 52;
		bounds = new Vector(FIELD_LONG, FIELD_SHORT);

		Polygon RedTrench = new Polygon(new Vector(349.1886, FIELD_SHORT - TRENCH_WIDTH), new Vector(379.1886, FIELD_SHORT - TRENCH_WIDTH), new Vector(349.1886, FIELD_SHORT - TRENCH_WIDTH + 4), new Vector(379.1886, FIELD_SHORT - TRENCH_WIDTH + 4));
		Polygon BlueTrench = new Polygon(new Vector(248.5614, TRENCH_WIDTH), new Vector(278.5614, TRENCH_WIDTH), new Vector(248.5614, TRENCH_WIDTH - 4), new Vector(278.5614, TRENCH_WIDTH - 4));   
		Polygon LeftCornerNear = new Polygon(new Vector(0, 0), new Vector(25.715, 0), new Vector(0, 70.655));
		Polygon RightCornerNear = new Polygon(new Vector(0, 253.531), new Vector(0, FIELD_SHORT), new Vector(25.715, FIELD_SHORT));
		Polygon LeftCornerFar = new Polygon(new Vector(FIELD_LONG, 0), new Vector(604.18, 0), new Vector(FIELD_LONG, 70.655));
		Polygon RightCornerFar = new Polygon(new Vector(FIELD_LONG, 253.531), new Vector(FIELD_LONG, FIELD_SHORT), new Vector(603.545, FIELD_SHORT));
		Polygon middle = new Polygon(new Vector(FIELD_LONG / 2 - 51.063, 52), new Vector(FIELD_LONG / 2 - 115.928, 205.399), new Vector(FIELD_LONG / 2 + 115.928, 117.911), new Vector(FIELD_LONG / 2 + 51.063, 274.509));

		obstacles[0] = RedTrench;
		obstacles[1] = BlueTrench;
		obstacles[2] = LeftCornerFar;
		obstacles[3] = LeftCornerNear;
		obstacles[4] = RightCornerFar;
		obstacles[5] = RightCornerNear;
		obstacles[6] = middle;

		Polygon SafeCloseAlliance = new Polygon(new Vector(0, 192.655 - SHORTEST_SIDE),
						new Vector(0, 252.655 + SHORTEST_SIDE), new Vector(30.72 + SHORTEST_SIDE, 119.655));

		Polygon SafeFarAlliance = new Polygon(new Vector(599.187 - SHORTEST_SIDE, 228.655),
						new Vector(204.655 - SHORTEST_SIDE, 254.535 + SHORTEST_SIDE));

		Polygon SafeTrench = new Polygon(new Vector(206.630, 267.811), new Vector(206.345, FIELD_SHORT),
						new Vector(422.63, FIELD_SHORT), new Vector(422.63, 267.811));

		safeZones[0] = SafeCloseAlliance;
		safeZones[1] = SafeFarAlliance;
		safeZones[2] = SafeTrench;
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

	public boolean lineOfSightConstrained(Vector v1, Vector v2) {
		Vector dif = v2.subtract(v1);
		double d = v1.distanceTo(v2);
		if (d == 0.0)
			return true;
		if (d > 200)
			return false;
		Vector norm = dif.multiply(ROBOT_RADIUS / d).normal();
		Polygon rect = new Polygon(v1.add(norm), v2.add(norm), v2.subtract(norm), v1.subtract(norm));
		Circle startCircle = new Circle(v1, ROBOT_RADIUS);
		Circle endCircle = new Circle(v2, ROBOT_RADIUS);
		if (shapeIntersects(rect))
				return false;
		if (shapeIntersects(startCircle))
				return false;
		if (shapeIntersects(endCircle))
				return false;
		return true;
	}
}