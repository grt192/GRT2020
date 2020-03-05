package frc.gen;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoSink;
import edu.wpi.cscore.VideoSource.ConnectionStrategy;
import edu.wpi.first.cameraserver.CameraServer;

public class DriverCameras {
    // the camera we are currently using
    private int curCameraIndex;
    // the camera that the driver sees
    private VideoSink switchedCamera;

    // cameras we can switch between
    private UsbCamera[] cameras;

    private static final int VIDEO_WIDTH = 640;
    private static final int VIDEO_HEIGHT = 480;

    public DriverCameras(int numCameras) {
        if (numCameras > 0) {
            // initialize each camera
            cameras = new UsbCamera[numCameras];
            for (int i = 0; i < numCameras; i++) {
                cameras[i] = CameraServer.getInstance().startAutomaticCapture(i);
                cameras[i].setResolution(VIDEO_WIDTH, VIDEO_HEIGHT);
            }
            // set up virtual camera for switching between camera streams
            curCameraIndex = 0;
            switchedCamera = CameraServer.getInstance().addSwitchedCamera("DRIVER CAMERA");
            // arbitrarily set the starting camera to the first camera
            switchedCamera.setSource(cameras[0]);
        } else {
            cameras = null;
        }
    }

    public void update() {
        if (cameras == null) {
            return;
        } else if (BIGData.getBoolean("request_camera_switch")) {
            BIGData.put("request_camera_switch", false);
            curCameraIndex = (curCameraIndex+1) % cameras.length;
            switchedCamera.setSource(cameras[curCameraIndex]);
        }
    }
}