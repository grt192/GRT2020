package frc.positiontracking.fieldmap;

import java.util.HashSet;
import java.util.Set;

import frc.gen.BIGData;
import frc.positiontracking.fieldmap.geometry.Circle;
import frc.positiontracking.fieldmap.geometry.Polygon;
import frc.positiontracking.fieldmap.geometry.Vector;
import frc.robot.Robot;

public class FieldMap {
        
	private static double FIELD_SHORT, FIELD_LONG;
	private static Vector bounds;
	private Polygon wall;
	private static Polygon[] obstacles;
	private static Polygon[] safeZones;
	private static VisionTarget[] visionTargets;
	private static double SHORTEST_SIDE;

	public FieldMap() {
		SHORTEST_SIDE = Math.min(BIGData.getDouble("robot_width"), BIGData.getDouble("robot_height"));
		buildMap();
		wall = new Polygon(new Vector(0, 0), new Vector(FIELD_LONG, 0), new Vector(FIELD_LONG, FIELD_SHORT),
						new Vector(0, FIELD_SHORT));
	}

	private static void buildMap() {
		obstacles = new Polygon[7];
		safeZones = new Polygon[3];
		visionTargets = new VisionTarget[1];
		FIELD_SHORT = 323.25;
		FIELD_LONG = 629.25;

		double TRENCH_WIDTH = 52;
		bounds = new Vector(FIELD_LONG, FIELD_SHORT);

		Polygon RedTrench = new Polygon(new Vector(349.1886, FIELD_SHORT - TRENCH_WIDTH), new Vector(379.1886, FIELD_SHORT - TRENCH_WIDTH), new Vector(349.1886, FIELD_SHORT - TRENCH_WIDTH + 4), new Vector(379.1886, FIELD_SHORT - TRENCH_WIDTH + 4));
		Polygon BlueTrench = new Polygon(new Vector(248.5614, TRENCH_WIDTH), new Vector(278.5614, TRENCH_WIDTH), new Vector(248.5614, TRENCH_WIDTH - 4), new Vector(278.5614, TRENCH_WIDTH - 4));   
		// Polygon RightSideTrench = new Polygon(new Vector(254, 320), new Vector(254, FIELD_WIDTH),
		// 				new Vector(285, FIELD_WIDTH), new Vector(285, 320));
		// Polygon LeftSideTrench = new Polygon(new Vector(254, 266), new Vector(285, 266), new Vector(285, 270),
		// 				new Vector(254, 270));

		Polygon LeftCornerNear = new Polygon(new Vector(0, 0), new Vector(25.715, 0), new Vector(0, 70.655));
		Polygon RightCornerNear = new Polygon(new Vector(0, 253.531), new Vector(0, FIELD_SHORT),
						new Vector(25.715, FIELD_SHORT));
		Polygon LeftCornerFar = new Polygon(new Vector(FIELD_LONG, 0), new Vector(604.18, 0),
						new Vector(FIELD_LONG, 70.655));
		Polygon RightCornerFar = new Polygon(new Vector(FIELD_LONG, 253.531),
						new Vector(FIELD_LONG, FIELD_SHORT), new Vector(603.545, FIELD_SHORT));

		// Polygon BlueTrench = new Polygon(new Vector(206.630, 0), new Vector(206.630, 55.5),
		// 				new Vector(422.63, 0), new Vector(422.63, 55.5));

		// Polygon RendezvousZoneClose = new Polygon(new Vector(206.556, 121.414), new Vector(217.614, 116.591),
		// 				new Vector(222.366, 127.61), new Vector(211.024, 206.847));

		// Polygon RendezvousOurTrench = new Polygon(new Vector(262.284, 255.531), new Vector(273.072, 251.062),
		// 				new Vector(277.895, 262.121), new Vector(265.924, 267));

		// Polygon RendezvousTheirTrench = new Polygon(new Vector(351.365, 61.19), new Vector(362.22, 56.693),
		// 				new Vector(367.043, 67.684), new Vector(355.957, 72.277));

		// Polygon RendezvousZoneFar = new Polygon(new Vector(407.148, 195.864), new Vector(411.617, 206.652),
		// 				new Vector(422.731, 202.127), new Vector(418.139, 191.041));

		// Polygon SafeCloseOpponent = new Polygon(new Vector(0, 70.655 - ROBOT_WIDTH),
		// 				new Vector(0, 118.655 + ROBOT_WIDTH), new Vector(30.072 + ROBOT_WIDTH, 94.655));

		// Polygon SafeFarOpponent = new Polygon(new Vector(FIELD_HEIGHT, 70.655 - ROBOT_WIDTH),
		// 				new Vector(FIELD_HEIGHT, 130.655 + ROBOT_WIDTH),
		// 				new Vector(599.187 - ROBOT_WIDTH, 100.655));

		// Polygon RendezvousOpponent = new Polygon(new Vector(362.561, 56.563), new Vector(208.711, 121.073),
		// 				new Vector(244.992, 190.5), new Vector(384.288, 132.861));

		Polygon middle = new Polygon(new Vector(198.6974, 205.339), new Vector(365.6886, 212.6886), new Vector(430.5526, 117.911), new Vector(263.5614, 110.5614));

		// Polygon RTPillar = new Polygon(new Vector(356, FIELD_SHORT - TRENCH_WIDTH), new Vector(372, FIELD_SHORT - TRENCH_WIDTH), new Vector(353,FIELD_SHORT - TRENCH_WIDTH - 14), new Vector(372, FIELD_SHORT - TRENCH_WIDTH - 14));
		// Polygon LBPillar = new Polygon(new Vector(280, TRENCH_WIDTH), new Vector(258, TRENCH_WIDTH), new Vector(258, TRENCH_WIDTH + 14), new Vector(280, TRENCH_WIDTH + 14));
		// Polygon RBPillar = new Polygon(new Vector(431, 113), new Vector(431, 134), new Vector(417, 113), new Vector(417, 134));
		// Polygon LTPillar = new Polygon(new Vector(197, 211), new Vector(197, 190), new Vector(211, 211), new Vector(211, 190));
		obstacles[0] = RedTrench;
		obstacles[1] = BlueTrench;

		obstacles[2] = LeftCornerFar;
		obstacles[3] = LeftCornerNear;
		obstacles[4] = RightCornerFar;
		obstacles[5] = RightCornerNear;

		obstacles[6] = middle;

		// obstacles[6] = RTPillar;
		// obstacles[7] = RBPillar;
		// obstacles[8] = LTPillar;
		// obstacles[9] = LBPillar;
		// obstacles[10] = RendezvousZoneClose;

		// obstacles[11] = SafeCloseOpponent;
		// obstacles[12] = SafeFarOpponent;

		// obstacles[13] = RendezvousOpponent;

		Polygon SafeCloseAlliance = new Polygon(new Vector(0, 192.655 - SHORTEST_SIDE),
						new Vector(0, 252.655 + SHORTEST_SIDE), new Vector(30.72 + SHORTEST_SIDE, 119.655));

		Polygon SafeFarAlliance = new Polygon(new Vector(599.187 - SHORTEST_SIDE, 228.655),
						new Vector(204.655 - SHORTEST_SIDE, 254.535 + SHORTEST_SIDE));

		Polygon SafeTrench = new Polygon(new Vector(206.630, 267.811), new Vector(206.345, FIELD_SHORT),
						new Vector(422.63, FIELD_SHORT), new Vector(422.63, 267.811));

		safeZones[0] = SafeCloseAlliance;
		safeZones[1] = SafeFarAlliance;
		safeZones[2] = SafeTrench;

		VisionTarget farGoal = new VisionTarget(new Vector(FIELD_LONG, 228.655), Math.PI, true);

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