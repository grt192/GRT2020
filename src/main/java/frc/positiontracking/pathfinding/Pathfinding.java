package frc.positiontracking.pathfinding;

import java.util.HashSet;
import java.util.PriorityQueue;

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

    public Vector bezier(double t) {
        int size = Target.size();
        if (size >= 3) {
            int k = size - 1;
            double x = 0;
            double y = 0;
            for (int i = k; i >= 0; i--) {
                x += choose(k, i) * Target.get(i).x * Math.pow(t, k - i) * Math.pow((1 - t), i);
                y += choose(k, i) * Target.get(i).y * Math.pow(t, k - i) * Math.pow((1 - t), i);
            }
            return new Vector(x, y);
        }
        return null;
    }

    //TODO: make the factorial calculation faster
    private double factorial(int n) {
        double num = 1;
        for (int i = 1; i <= n; i++) {
            num *= n;
        }
        return num;
    }

    private double choose(int n, int r) {
        return (factorial(n) / (factorial(r) * factorial(n - r)));
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
        while(!open.isEmpty()) {
            Node current = open.poll();
            closed.add(current);
            if (current.equals(targetNode)) {
                Target.put(current.pos);
                Node parent;
                while((parent = current.parent) != null) {
                    Target.put(0, parent.pos);
                    current = parent;
                }
                open.clear();
            } else {
                for(Node node : current.neighbors) {
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
        for(int i = 0; i < Target.size(); i++) {
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
        //blue initiation line
        addNode(new Node(new Vector(120, 297.25)));
        addNode(new Node(new Vector(120, 26)));
        addNode(new Node(new Vector(120, 161.625)));

        //red initiation line
        addNode(new Node(new Vector(509.25, 297.25)));
        addNode(new Node(new Vector(509.25, 26)));
        addNode(new Node(new Vector(509.25, 297.25)));

        //red trench run
        addNode(new Node(new Vector(206.625, 297.25)));
        addNode(new Node(new Vector(422.625, 297.25)));
        addNode(new Node(new Vector(314.625, 297.25)));

        //blue trech run
        addNode(new Node(new Vector(206.625, 26)));
        addNode(new Node(new Vector(422.625, 26)));
        addNode(new Node(new Vector(314.625, 26)));

        //target and loading zones
        addNode(new Node(new Vector(30, 100.65)));
        addNode(new Node(new Vector(30, 229.531)));
        addNode(new Node(new Vector(599.25, 100.65)));
        addNode(new Node(new Vector(599.25, 229.531)));
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

    private void cleanTree() {
        for (Node node : nodes) {
            node.g = Double.POSITIVE_INFINITY;
        }
    }
}
