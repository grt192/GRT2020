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
    // array of cameras
    private UsbCamera[] cameras;
    // index of the camera that we are currently streaming
    private int curCameraIndex;
    private VideoSink server;
    public DriverCameras() {
      cameras = new UsbCamera[2];
      cameras[0] = CameraServer.getInstance().startAutomaticCapture(0);
      cameras[1] = CameraServer.getInstance().startAutomaticCapture(1);
      CameraServer.getInstance().addServer("DRIVER CAMERA");
      server = CameraServer.getInstance().getServer();
    }

    public void update() {
        if (BIGData.getBoolean("request_camera_switch")) {
            BIGData.put("request_camera_switch", false);
            curCameraIndex = (curCameraIndex+1) % cameras.length;
            server.setSource(cameras[curCameraIndex]);
        }
    }
}