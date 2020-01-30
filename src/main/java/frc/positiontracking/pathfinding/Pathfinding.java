package frc.positiontracking.pathfinding;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import frc.gen.BIGData;
import frc.positiontracking.fieldmap.FieldMap;
import frc.positiontracking.fieldmap.geometry.Polygon;
import frc.positiontracking.fieldmap.geometry.Vector;

public class Pathfinding {

    private double ROBOT_RADIUS;
    private Vector target;
    private FieldMap field;

    private HashSet<Node> nodes;
    private Node targetNode;

    public Pathfinding() {
        target = new Vector(0, 0);
        field = new FieldMap();
        initNodes();
        targetNode = new Node(new Vector(0, 0));
        ROBOT_RADIUS = Math.sqrt(
                Math.pow(BIGData.getDouble("robot_width"), 2) + Math.pow(BIGData.getDouble("robot_height"), 2)) / 2;
    }

    public void searchA(Vector curr) {
        HashSet<Node> closed = new HashSet<>();
        PriorityQueue<Node> open = new PriorityQueue<>();
        Node startNode = new Node(curr);
        addNode(startNode);
        startNode.calcH(targetNode);
        open.add(startNode);

        while (!open.isEmpty()) {
            Node current = open.poll();
            closed.add(current);

            if (current == targetNode) {
                Node next = current;
                Target.put(next.pos);
                Node parent;
                while ((parent = next.parent) != null) {
                    Target.put(0, parent.pos);
                    next = parent;
                }
            } else {
                for (Node node : current.neighbors) {
                    if (closed.contains(node))
                        continue;
                    if (!open.contains(node))
                        open.add(node);
                    node.update(current);
                }
            }
        }
        removeNode(startNode);
    }

    public Vector searchPFP(Vector curr) {
        double radius = ROBOT_RADIUS + 4.0;
        double smallRadius = ROBOT_RADIUS + 2.0;
        double r2 = radius * radius;
        double sr2 = smallRadius * smallRadius;
        Vector pos = curr;
        Vector velocity = target.subtract(pos).multiply(1 / target.distanceTo(pos));
        Vector emergency = new Vector(0, 0);
        boolean isEmergency = false;
        for (Polygon p : field.getObstacles()) {
            Vector close = p.closestPoint(pos);
            double d2 = close.distanceSquaredTo(pos);
            if (d2 < sr2) {
                emergency = emergency.add(pos.subtract(close).multiply(1 / Math.sqrt(d2)));
                isEmergency = true;
            } else if (d2 < r2) {
                Vector displacement = pos.subtract(close);
                double project = (displacement.dot(velocity) / d2);
                if (project < 0) {
                    Vector force = displacement.multiply(-project);
                    velocity = velocity.add(force);
                }
            }
        }
        Vector close = field.closestWallPoint(pos);
        double d2 = close.distanceSquaredTo(pos);
        if (d2 < sr2) {
            emergency = emergency.add(pos.subtract(close).multiply(1 / Math.sqrt(d2)));
            isEmergency = true;
        } else if (d2 < radius * radius) {
            Vector displacement = pos.subtract(close);
            double project = (displacement.dot(velocity) / d2);
            if (project < 0) {
                Vector force = displacement.multiply(-project);
                velocity = velocity.add(force);
            }
        }
        if (!isEmergency) {
            velocity = velocity.multiply(1 / velocity.magnitude());
        } else {
            velocity = emergency.multiply(1 / emergency.magnitude());
        }
        return velocity;
    }

    private void initNodes() {
        nodes = new HashSet<>();
        Set<Vector> pos = field.generateNodes();
        for (Vector v : pos)
            addNode(new Node(v));
    }

    private void addNode(Node n) {
        for (Node node : nodes) {
            if (field.lineOfSight(n.pos, node.pos)) {
                node.neighbors.add(n);
                n.neighbors.add(node);
            }
        }
        nodes.add(n);
    }

    private void removeNode(Node n) {
        nodes.remove(n);
        for (Node node : nodes) {
            node.neighbors.remove(n);
        }
    }

    public void setTargetNode(Vector v) {
        removeNode(targetNode);
        targetNode = new Node(v);
        addNode(targetNode);
        for (Node node : nodes) {
            node.calcH(targetNode);
        }
    }
}
