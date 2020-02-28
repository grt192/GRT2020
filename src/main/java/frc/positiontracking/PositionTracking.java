package frc.positiontracking;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.video.KalmanFilter;

import frc.gen.BIGData;
import frc.pathfinding.fieldmap.geometry.*;
import frc.swerve.SwerveData;

public class PositionTracking {

    private static final int TYPE = CvType.CV_64F;
    private static final int STATES = 2;

    private static final double INITIAL_VARIANCE = 4.0;
    private static final double PROCESS_NOISE = 0.07;
    private static final double MEASUREMENT_NOISE = 0.5;

    private double FIELD_HEIGHT = 629.25;
    private double FIELD_WIDTH = 323.25;

    private long lastUpdate;
    private long temp, ticks;
    private KalmanFilter kf;
    private double dt;
    private double speed;
    private double cachedX, cachedY;
    private double tempX, tempY;

    private Vector relativeEstimate;
    private Vector absoluteEstimate;
    private Vector closestTarget;

    private Mat Q, R, U, Z, state, error;

    private SwerveData data;

    private ArrayList<Vector> visionTargets;

    public PositionTracking() {
        initVisionTargets();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        kf = new KalmanFilter(STATES, STATES, STATES, TYPE);
        kf.set_transitionMatrix(Mat.eye(STATES, STATES, TYPE));
        kf.set_measurementMatrix(Mat.eye(STATES, STATES, TYPE));

        // set measurement noise covariance
        R = new Mat(STATES, STATES, TYPE);
        R.put(0, 0, MEASUREMENT_NOISE, 0, 0, MEASUREMENT_NOISE);
        kf.set_measurementNoiseCov(R);

        // default position is (0, 0)
        set(0, 0);
    }

    public void set(double x, double y) {
        state = new Mat(STATES, 1, TYPE);
        state.put(0, 0, x, y);
        kf.set_statePre(state);
        kf.set_statePost(state);
        error = new Mat(STATES, STATES, TYPE);
        error.put(0, 0, INITIAL_VARIANCE, 0, 0, INITIAL_VARIANCE);
        kf.set_errorCovPre(error);
        kf.set_errorCovPost(error);
        lastUpdate = System.currentTimeMillis();
        cachedX = x;
        cachedY = y;
    }

    public double getX() {
        try {
            return kf.get_statePost().get(0, 0)[0];
        } catch (CvException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public double getY() {
        try {
            return kf.get_statePost().get(1, 0)[0];
        } catch (CvException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public void update() {
        manualSetPos();

        // time since last call
        temp = System.currentTimeMillis();
        ticks = (temp - lastUpdate);
        lastUpdate = temp;
        dt = ticks / 1000.0;

        // calculate speed using swerve data
        data = BIGData.getSwerveData();
        speed = Math.sqrt(data.encoderVX * data.encoderVX + data.encoderVY * data.encoderVY);

        // set process noise covariance
        Q = new Mat(STATES, STATES, TYPE);
        Q.put(0, 0, PROCESS_NOISE * dt * speed, 0, 0, PROCESS_NOISE * dt * speed);
        kf.set_processNoiseCov(Q);
        kf.get_controlMatrix().put(0, 0, dt, 0, 0, dt);

        // predict using control input u
        U = new Mat(STATES, 1, TYPE);
        U.put(0, 0, data.encoderVX, data.encoderVY);
        kf.predict(U);

        // get relative position estimate from the camera
        relativeEstimate = BIGData.getCameraPos();
        closestVisionTarget(new Vector(tempX, tempY));
        if (relativeEstimate != null && BIGData.getBoolean("correct_with_camera")){

            // find absolute position from relative position
            absoluteEstimate = closestTarget.subtract(relativeEstimate);
            
            // correct using measurement input z
            Z = new Mat(STATES, 1, TYPE);
            Z.put(0, 0, absoluteEstimate.x, absoluteEstimate.y);
            kf.correct(Z);
        }

        tempX = getX();
        tempY = getY();

        if (tempX < (-1 * FIELD_HEIGHT) || tempX > (2 * FIELD_HEIGHT) || tempY < (-1 * FIELD_WIDTH)
                || tempY > (2 * FIELD_WIDTH)) {
            System.out.println("An error occured, resetting to last position");
            set(cachedX, cachedY);
        } else {
            cachedX = tempX;
            cachedY = tempY;
        }
        Vector curr_pos = new Vector(tempX, tempY);
        // System.out.println("x: " + curr_pos.x + " y: " + curr_pos.y);
        BIGData.setPosition(curr_pos, "curr");
    }

    /**
     * initialize vision targets on field (there are only 2 that our camera can see)
     */
    private void initVisionTargets() {
        visionTargets = new ArrayList<>();
        visionTargets.add(new Vector(0, 229.531));
        visionTargets.add(new Vector(FIELD_HEIGHT, 100.65));
    }

    /** find the closest vision target to the estimated robot position */
    private void closestVisionTarget(Vector v) {
        closestTarget = (v.distanceTo(visionTargets.get(0)) > v.distanceTo(visionTargets.get(1))) ? visionTargets.get(0)
                : visionTargets.get(1);
    }

    /** manually set the current position */
    private void manualSetPos() {
        if (BIGData.getBoolean("manual_change_pos")) {
            Vector manualPos = BIGData.getManualPos();
            set(manualPos.x, manualPos.y);
        }
    }
}
