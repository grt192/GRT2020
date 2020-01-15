/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.modes;

import com.fasterxml.jackson.databind.deser.std.NumberDeserializers.BigDecimalDeserializer;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import frc.gen.BIGData;
import frc.input.Input;
import frc.input.JoystickProfile;

class DriverControl extends Mode {

    private boolean centeringCamera = false;

    @Override
    public boolean loop() {

        driveSwerve();
        return true;
    }

    private void driveSwerve() {
        double x = -Input.SWERVE_XBOX.getY(Hand.kLeft);
        double y = Input.SWERVE_XBOX.getX(Hand.kLeft);
        x = JoystickProfile.applyProfile(x);
        y = JoystickProfile.applyProfile(y);
        // rotate the robot
        double lTrigger = Input.SWERVE_XBOX.getTriggerAxis(Hand.kLeft);
        double rTrigger = Input.SWERVE_XBOX.getTriggerAxis(Hand.kRight);
        double rotate = 0;
        if (lTrigger + rTrigger > 0.05) {
            rotate = (rTrigger * rTrigger - lTrigger * lTrigger);
        }

        if (Input.SWERVE_XBOX.getAButtonPressed()) {
            centeringCamera = true;
        }
        if (Input.SWERVE_XBOX.getBButtonPressed()) {
            centeringCamera = false;
        }

        if (BIGData.getDouble("camera_azimuth") < Math.PI / 180) {
        } else if (centeringCamera) {
            // x = 0;
            // y = 0;
            rotate = BIGData.getDouble("camera_azimuth") * Math.PI / 180;
            // if (Math.abs(rotate) > Math.PI / 180) {
            // aButton = false;
            // }
            System.out.println(rotate * 180 / Math.PI + " " + rotate);
        }
        BIGData.setDrive(x, y, rotate);

    }

}
