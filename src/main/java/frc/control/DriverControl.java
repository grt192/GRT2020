/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.control;

import com.fasterxml.jackson.databind.deser.std.NumberDeserializers.BigDecimalDeserializer;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import frc.gen.BIGData;
import frc.swerve.SwerveData;
import frc.control.input.Input;
import frc.control.input.JoystickProfile;
import frc.gen.BIGData;

class DriverControl extends Mode {
    private int pov = -1;
    private int lastPov;

    private boolean centeringCamera, centeringCameraLidar = false;

    @Override
    public boolean loop() {
        JoystickProfile.updateProfilingPoints();
        driveSwerve();
        driveMechs();
        return true;
    }

    private void driveSwerve() {
        double x = Input.SWERVE_XBOX.getX(Hand.kLeft);
        // negativize y so that up is forward
        double y = -Input.SWERVE_XBOX.getY(Hand.kLeft);
        x = JoystickProfile.applyProfile(x);
        y = JoystickProfile.applyProfile(y);
        // rotate the robot
        double lTrigger = Input.SWERVE_XBOX.getTriggerAxis(Hand.kLeft);
        double rTrigger = Input.SWERVE_XBOX.getTriggerAxis(Hand.kRight);
        double rotate = 0;

        boolean buttonPressed = false;
        if (pov == -1) {
            buttonPressed = true;
        }
        pov = Input.SWERVE_XBOX.getPOV();
        if (Input.SWERVE_XBOX.getBumperPressed(Hand.kLeft)) {
            pov = lastPov - 45;
        }
        if (Input.SWERVE_XBOX.getBumperPressed(Hand.kRight)) {
            pov = lastPov + 45;
        }
        if (buttonPressed) {
            if (pov == -1) {
            } else {
                BIGData.setAngle(pov);
                // System.out.println("pov: " + pov);
                lastPov = pov;
            }
        }

        if (lTrigger + rTrigger > 0.05) {
            BIGData.setPIDFalse();
            rotate = -(rTrigger * rTrigger - lTrigger * lTrigger);
        } else {
            rotate = 0;
        }

        if (Input.SWERVE_XBOX.getAButtonPressed()) {
            centeringCamera = true;
        }

        if (Input.SWERVE_XBOX.getAButtonReleased()) {
            centeringCamera = false;
            BIGData.setPIDFalse();
        }

        double cameraAzimuth = BIGData.getDouble("camera_azimuth");
        // System.out.println(azimuth);
        if (centeringCamera && Math.abs(cameraAzimuth) > 1) {
            rotate = calcPID(cameraAzimuth);
        }

        if (Input.SWERVE_XBOX.getXButtonPressed()) {
            centeringCameraLidar = true;
        }

        if (Input.SWERVE_XBOX.getXButtonReleased()) {
            centeringCameraLidar = false;
            BIGData.setPIDFalse();
        }

        double lidarAzimuth = BIGData.getDouble("lidar_azimuth");
        double lidarRange = BIGData.getDouble("lidar_range");
        // System.out.println(azimuth);
        System.out.println(Math.toDegrees(lidarAzimuth) + "," + lidarRange);
        if (centeringCameraLidar && Math.abs(Math.toDegrees(lidarAzimuth)) > 1) {
            BIGData.setAngle(-Math.toDegrees(lidarAzimuth) + BIGData.getGyroAngle());
        }
        BIGData.requestDrive(x, y, rotate);

    }

    public double calcPID(double azimuthDeg) {
        return azimuthDeg * .005;
    }

    private void driveMechs() {
        
    }

}
