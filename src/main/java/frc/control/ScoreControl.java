package frc.control;

import frc.gen.BIGData;

public class ScoreControl extends Mode {
    
    private double cameraAzimuth;
    private double cameraRange;

    public ScoreControl() {
        cameraAzimuth = 0.0;
        cameraRange = 0.0;
    }

    @Override
    public boolean loop() {
        cameraAzimuth = BIGData.getDouble("camera_azimuth");
        cameraRange = BIGData.getDouble("camera_range");
        if (cameraRange == 0) {
            System.out.println("no vision target found!! turning!!");
            BIGData.requestDrive(0, 0, 0.2);
        } else {
            BIGData.requestDrive(0, 0, 0);
            if (Math.abs(cameraAzimuth) <= 1)
                return false;
            if (Math.abs(cameraAzimuth) > 1)
                BIGData.setAngle(cameraAzimuth);
        }
        return true;
    }

}