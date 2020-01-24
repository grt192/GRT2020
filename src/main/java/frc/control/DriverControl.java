/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.control;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import frc.control.input.Input;
import frc.control.input.JoystickProfile;
import frc.gen.BIGData;

class DriverControl extends Mode {

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
        if (lTrigger + rTrigger > 0.05) {
            rotate = -(rTrigger * rTrigger - lTrigger * lTrigger);
        }
        BIGData.requestDrive(x, y, rotate);
    }

    private void driveMechs() {
        double one_l = Input.MECH_XBOX.getTriggerAxis(Hand.kLeft);
        double one_r = -Input.MECH_XBOX.getTriggerAxis(Hand.kRight);
        double one = one_l + one_r;

        double two_a = -Input.MECH_XBOX.getY(Hand.kRight);
        double two_b = two_a;
        if (two_a == 0) {
            // TODO: add stuff
        }
        BIGData.putMechs(one, two_a, two_b);
    }

}
