package frc.pathfinding.fieldmap.geometry;

public class Circle {

    private double radius;
    private Vector position;

    public Circle(Vector pos, double radius) {
        this.radius = radius;
        position = pos;
    }

    public boolean intersects(Polygon other) {
        Vector firstAxis = other.getClosestVertex(position).subtract(position);
        double min1 = getMin(firstAxis);
        double max1 = getMax(firstAxis);
        double min2 = other.getMin(firstAxis);
        double max2 = other.getMax(firstAxis);
        if (!(max1 >= min2 && max2 >= min1)) {
            return false;
        }
        Vector[] otherAxes = other.getAxes();
        for (int i = 0; i < otherAxes.length; ++i) {
            min1 = getMin(otherAxes[i]);
            max1 = getMax(otherAxes[i]);
            min2 = other.getMin(otherAxes[i]);
            max2 = other.getMax(otherAxes[i]);
            if (!(max1 >= min2 && max2 >= min1)) {
                return false;
            }
        }
        return true;
    }

    public double getMin(Vector axis) {
        return position.dot(axis) - radius * axis.magnitude();
    }

    public double getMax(Vector axis) {
        return position.dot(axis) + radius * axis.magnitude();
    }

    public boolean outsideBounds(Vector bounds) {
        return (position.x - radius <= 0 || position.x + radius >= bounds.x || position.y - radius <= 0
                || position.y + radius >= bounds.y);
    }
}
