package frc.position;

import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.video.KalmanFilter;

import frc.gen.BIGData;

/**
 * Add your docs here.
 */
public class KalmanPosition {

    private static final int TYPE = CvType.CV_64F;
    private static final int STATES = 2;

    private static final double INITIAL_VARIANCE = 4.0;
    private static final double PROCESS_NOISE = 0.07;
    private static final double MEASUREMENT_NOISE = 0.5;

    private long lastUpdate;
    private KalmanFilter kf;

    public KalmanPosition() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        kf = new KalmanFilter(STATES, STATES, STATES, TYPE);
        kf.set_transitionMatrix(Mat.eye(STATES, STATES, TYPE));
        kf.set_measurementMatrix(Mat.eye(STATES, STATES, TYPE));
        Mat Q = new Mat(STATES, STATES, TYPE);
        Q.put(0, 0, MEASUREMENT_NOISE, 0, 0, MEASUREMENT_NOISE);
        kf.set_measurementNoiseCov(Q);
    }

    public void set(double x, double y) {
        Mat state = new Mat(STATES, 1, TYPE);
        state.put(0, 0, x, y);
        kf.set_statePre(state);
        kf.set_statePost(state);
        Mat error = new Mat(STATES, STATES, TYPE);
        error.put(0, 0, INITIAL_VARIANCE, 0, 0, INITIAL_VARIANCE);
        kf.set_errorCovPre(error);
        kf.set_errorCovPost(error);
        lastUpdate = System.currentTimeMillis();
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
        double vx = BIGData.getDouble("enc_vx");
        double vy = BIGData.getDouble("enc_vy");
        long temp = System.currentTimeMillis();
        long ticks = (temp - lastUpdate);
        lastUpdate = temp;
        double dt = ticks / 1000.0;
        double speed = Math.sqrt(vx * vx + vy * vy);
        Mat R = new Mat(STATES, STATES, TYPE);
        R.put(0, 0, PROCESS_NOISE * dt * speed, 0, 0, PROCESS_NOISE * dt * speed);
        kf.set_processNoiseCov(R);
        kf.get_controlMatrix().put(0, 0, dt, 0, 0, dt);
        Mat U = new Mat(STATES, 1, TYPE);
        U.put(0, 0, vx, vy);
        kf.predict(U);
        // double navx_x = BIGData.getDouble("navx_x");
        // double navx_y = BIGData.getDouble("navx_y");

        // Mat Z = new Mat(STATES, 1, TYPE);
        // Z.put(0, 0, navx_x, navx_y);
        // kf.correct(Z);
    }
}