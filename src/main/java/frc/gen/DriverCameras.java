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

public class DriverCameras extends Thread {
    // the camera we are currently using
    private int curCameraIndex;
    // the sink that we are currently using
    private CvSink curCvSink;

    // cameras we can switch between
    private UsbCamera[] cameras;

    private static final int VIDEO_WIDTH = 640;
    private static final int VIDEO_HEIGHT = 480;

    public DriverCameras(int numCameras) {
        if (numCameras > 0) {
            cameras = UsbCamera[numCameras];
            for (int i = 0; i < numCameras; i++) {
                cameras[i] = CameraServer.getInstance.startAutomaticCapture(i);
                cameras[i].setResolution(VIDEO_WIDTH, VIDEO_HEIGHT);
            }
            curCameraIndex = 0;
            curCvSink = CameraServer.getInstance().getVideo(cameras[0]);
        } else {
            cameras = null;
        }
    }

    public void run() {
        CvSource outputStream = CameraServer.getInstance().putVideo("DRIVER CAMERA", VIDEO_WIDTH, VIDEO_HEIGHT);

        Mat source = new Mat();
        Mat output = new Mat();
        Size size = new Size(VIDEO_WDITH, VIDEO_HEIGHT);

        while (!Thread.interrupted()) {
            if (curCvSink.grabFrame(source) == 0) {
                continue;
            }
            Imgproc.resize(source, output, size);
            outputStream.putFrame(output);
        }

    }

    public void update() {
        if (cameras == null) {
            return;
        } else if (BIGData.getBoolean("request_camera_switch")) {
            BIGData.put("request_camera_switch", false);
            curCvSink.close();
            curCameraIndex = (curCameraIndex+1) % cameras.length;
            curCvSink = CameraServer.getInstance().getVideo(cameras[curCameraIndex]);
        }
    }
}