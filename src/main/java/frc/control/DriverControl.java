/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.control;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import frc.gen.BIGData;
import frc.control.input.Input;
import frc.control.input.JoystickProfile;

class DriverControl extends Mode {
    private int pov = -1;
    private int lastPov;

    @Override
    public boolean loop() {
        JoystickProfile.updateProfilingPoints();
        driveSwerve();
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
                BIGData.setAngle(Math.toRadians(pov));
                System.out.println("pov: " + pov);
                lastPov = pov;
            }
        }

        if (lTrigger + rTrigger > 0.05) {
            rotate = -(rTrigger * rTrigger - lTrigger * lTrigger);
        }

        BIGData.requestDrive(x, y, rotate);
        
    }

}
