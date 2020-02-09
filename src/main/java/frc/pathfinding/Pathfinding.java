package frc.pathfinding;

import java.util.HashSet;
import java.util.PriorityQueue;

import frc.gen.BIGData;
import frc.pathfinding.fieldmap.*;
import frc.pathfinding.fieldmap.geometry.*;

public class Pathfinding {

    private double ROBOT_RADIUS;
    private Vector target;
    private FieldMap field;

    private HashSet<Node> nodes;
    private HashSet<Node> bezier_nodes;
    private Node targetNode;

    public Pathfinding() {
        target = new Vector(0, 0);
        field = new FieldMap();
        initNodes();
        initBezierNodes();
        targetNode = new Node(new Vector(0, 0));
        ROBOT_RADIUS = Math.max(BIGData.getDouble("robot_width"), BIGData.getDouble("robot_height")) / 2;
    }

    public void searchAStar(Vector pos) {
        setTargetNode(Target.getTarget());
        HashSet<Node> closed = new HashSet<>();
        PriorityQueue<Node> open = new PriorityQueue<>();
        cleanTree();
        Node start = new Node(pos);
        addNode(start);
        start.calcH(targetNode);
        open.add(start);
        while (!open.isEmpty()) {
            Node current = open.poll();
            closed.add(current);
            if (current.equals(targetNode)) {
                Target.put(current.pos);
                Node parent;
                while ((parent = current.parent) != null) {
                    Target.put(0, parent.pos);
                    current = parent;
                }
                open.clear();
            } else {
                for (Node node : current.neighbors) {
                    if (closed.contains(node)) {
                        continue;
                    }
                    if (!open.contains(node)) {
                        open.add(node);
                    }
                    node.update(current);
                }
            }
        }
        for (int i = 0; i < Target.size(); i++) {
            System.out.println("x: " + Target.get(i).x + " y: " + Target.get(i).y);
        }
        removeNode(start);
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

        // blue initiation line
        addNode(new Node(new Vector(120, 297.25)));
        addNode(new Node(new Vector(120, 271.25)));
        addNode(new Node(new Vector(120, 161.625)));
        addNode(new Node(new Vector(120, 52)));
        addNode(new Node(new Vector(120, 26)));

        // red initiation line
        addNode(new Node(new Vector(509.25, 297.25)));
        addNode(new Node(new Vector(509.25, 271.25)));
        addNode(new Node(new Vector(509.25, 161.625)));
        addNode(new Node(new Vector(509.25, 52)));
        addNode(new Node(new Vector(509.25, 26)));

        //middle trench runs
        addNode(new Node(new Vector(206.625, 25)));
        addNode(new Node(new Vector(249.825, 25)));
        addNode(new Node(new Vector(293.025, 25)));
        addNode(new Node(new Vector(422.625, 25)));

        addNode(new Node(new Vector(206.625, 298.25)));
        addNode(new Node(new Vector(336.225, 298.25)));
        addNode(new Node(new Vector(379.425, 298.25)));
        addNode(new Node(new Vector(422.625, 298.25)));

        //outer red trench run
        addNode(new Node(new Vector(206.625, 271.25)));
        addNode(new Node(new Vector(249.825, 271.25)));
        addNode(new Node(new Vector(293.025, 297.25)));
        addNode(new Node(new Vector(422.625, 271.25)));

        //outer blue trench run
        addNode(new Node(new Vector(206.625, 52)));
        addNode(new Node(new Vector(336.225, 52)));
        addNode(new Node(new Vector(379.425, 52)));
        addNode(new Node(new Vector(422.625, 52)));

        // target and loading zones
        addNode(new Node(new Vector(30, 100.65)));
        addNode(new Node(new Vector(30, 229.531)));
        addNode(new Node(new Vector(599.25, 100.65)));
        addNode(new Node(new Vector(599.25, 229.531)));

        // middle of target and loading zones
        addNode(new Node(new Vector(30, 161.625)));
        addNode(new Node(new Vector(599.25, 161.625)));
    }

    private void initBezierNodes() {
        bezier_nodes = new HashSet<>();

        addNode(new Node(new Vector(120, 53.875)));
        addNode(new Node(new Vector(509.25, 53.875)));

        modulateY(53.875, 53.875, 5);
        modulateX(509.25, 30, 3);
        modulateX(509.25, -30, 2);
        modulateX(120, 30, 2);
        modulateX(120, -30, 3);
        
        // blue initiation line
        addNode(new Node(new Vector(120, 297.25)));
        addNode(new Node(new Vector(120, 271.25)));
        addNode(new Node(new Vector(120, 161.625)));
        addNode(new Node(new Vector(120, 52)));
        addNode(new Node(new Vector(120, 26)));

        // red initiation line
        addNode(new Node(new Vector(509.25, 297.25)));
        addNode(new Node(new Vector(509.25, 271.25)));
        addNode(new Node(new Vector(509.25, 161.625)));
        addNode(new Node(new Vector(509.25, 52)));
        addNode(new Node(new Vector(509.25, 26)));

        //middle trench runs
        addNode(new Node(new Vector(206.625, 25)));
        addNode(new Node(new Vector(206.625, 298.25)));
        modulateX(206.625, 43.2, 8);
        modulateX(206.625, -43.2, 3);

        //outer red trench run
        addNode(new Node(new Vector(206.625, 271.25)));
        addNode(new Node(new Vector(249.825, 271.25)));
        addNode(new Node(new Vector(293.025, 297.25)));
        addNode(new Node(new Vector(422.625, 271.25)));

        //outer blue trench run
        addNode(new Node(new Vector(206.625, 52)));
        addNode(new Node(new Vector(336.225, 52)));
        addNode(new Node(new Vector(379.425, 52)));
        addNode(new Node(new Vector(422.625, 52)));

        // target and loading zones
        addNode(new Node(new Vector(30, 100.65)));
        addNode(new Node(new Vector(30, 229.531)));
        addNode(new Node(new Vector(599.25, 100.65)));
        addNode(new Node(new Vector(599.25, 229.531)));

        // middle of target and loading zones
        addNode(new Node(new Vector(30, 161.625)));
        addNode(new Node(new Vector(599.25, 161.625)));
    }

    private void modulateX(double comparison, double n, int times) {
        HashSet<Node> newNodes = new HashSet<>();
        for (Node node : bezier_nodes) {
            if (node.pos.x == comparison) {
                for (int i = 1; i <= times; i++) {
                    newNodes.add(new Node(new Vector(comparison + n * i, node.pos.y)));
                }
            }
        }
        for (Node node : newNodes) {
            addNode(node);
        }
    }

    private void modulateY(double comparison, double n, int times) {
        HashSet<Node> newNodes = new HashSet<>();
        for (Node node : bezier_nodes) {
            if (node.pos.y == comparison) {
                for (int i = 0; i < times; i++) {
                    newNodes.add(new Node(new Vector(node.pos.x, comparison + n * i)));
                }
            }
        }
        for (Node node : newNodes) {
            addNode(node);
        }
    }

    private void constrainedAdd(Node n) {
        for (Node node : nodes) {
            if (field.lineOfSightConstrained(n.pos, node.pos)) {
                node.neighbors.add(n);
                n.neighbors.add(node);
            }
        }
        nodes.add(n);
    } 

    private void addNode(Node n) {
        constrainedAdd(n);
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

    private void cleanTree() {
        for (Node node : nodes) {
            node.g = Double.POSITIVE_INFINITY;
        }
    }

    public HashSet<Node> getNodes() {
        return nodes;
    }
}
