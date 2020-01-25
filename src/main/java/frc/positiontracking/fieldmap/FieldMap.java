package frc.positiontracking.fieldmap;

import frc.gen.BIGData;
import frc.positiontracking.fieldmap.geometry.Polygon;
import frc.positiontracking.fieldmap.geometry.Vector;

public class FieldMap {

    private static double FIELD_WIDTH, FIELD_HEIGHT;
    private static Vector bounds;
    private Polygon wall;
    private static Polygon[] obstacles;
    public VisionTarget[] visionTargets;

    public FieldMap() {
        buildMap();
        wall = new Polygon(new Vector(0, 0), new Vector(FIELD_HEIGHT, 0), new Vector(FIELD_HEIGHT, FIELD_WIDTH),
                new Vector(0, FIELD_WIDTH));
    }

    private static void buildMap() {
        obstacles = new Polygon[2];
        FIELD_WIDTH = 323.31;
        FIELD_HEIGHT = 629.25;
        BIGData.put("field_width", FIELD_WIDTH);
        BIGData.put("field_height", FIELD_HEIGHT);
        bounds = new Vector(FIELD_HEIGHT, FIELD_WIDTH);

        Polygon RightSideTrench = new Polygon(new Vector(254, 320), new Vector(254, FIELD_WIDTH),
                new Vector(285, FIELD_WIDTH), new Vector(285, 320));
        Polygon LeftSideTrench = new Polygon(new Vector(254, 266), new Vector(285, 266), new Vector(285, 270),
                new Vector(254, 270));

        obstacles[0] = RightSideTrench;
        obstacles[1] = LeftSideTrench;

    }

    public Vector closestWallPoint(Vector p) {
        return wall.closestPoint(p);
    }

    public Polygon[] getObstacles() {
        return obstacles;
    }

}