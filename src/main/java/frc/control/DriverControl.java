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
    private int pov = -1;
    private int lastPov;

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

    private void driveMechs() {
        if (Input.SWERVE_XBOX.getAButtonReleased()) {
            boolean currState = BIGData.getLinkageState();
            BIGData.requestLinkageState(!currState);
        }

        BIGData.putWinchState(Input.SWERVE_XBOX.getYButton());
        double rJoystickSwerve = Input.SWERVE_XBOX.getY(Hand.kRight);
        rJoystickSwerve = JoystickProfile.applyProfile(rJoystickSwerve);
        BIGData.requestWinchSpeed(rJoystickSwerve);

        BIGData.putShooterState(Input.MECH_XBOX.getAButton());

        if (Input.MECH_XBOX.getBumperReleased(Hand.kLeft)) {
            int offsetChange = BIGData.getInt("shooter_offset_change");
            int currOffset = BIGData.getInt("shooter_auto_offset");
            BIGData.put("shooter_auto_offset", currOffset - offsetChange);
        }

        if (Input.MECH_XBOX.getBumperReleased(Hand.kRight)) {
            int offsetChange = BIGData.getInt("shooter_offset_change");
            int currOffset = BIGData.getInt("shooter_auto_offset");
            BIGData.put("shooter_auto_offset", currOffset + offsetChange);
        }

        BIGData.putStorageState(!Input.MECH_XBOX.getYButton());

        double lJoystickMech = Input.MECH_XBOX.getY(Hand.kLeft);
        lJoystickMech = JoystickProfile.applyProfile(lJoystickMech);
        BIGData.requestStorageSpeed(lJoystickMech);

        double rJoystickMech = Input.MECH_XBOX.getY(Hand.kRight);
        rJoystickMech = JoystickProfile.applyProfile(rJoystickMech);
        BIGData.put("shooter_manual", rJoystickMech);

        if (Input.MECH_XBOX.getBButtonReleased()) {
            boolean shooterUp = BIGData.getBoolean("shooter_up");
            shooterUp = !shooterUp;
            BIGData.put("shooter_up", shooterUp);
        }

        if (Input.MECH_XBOX.getXButtonReleased()) {
            boolean currState = BIGData.getIntakeState();
            BIGData.requestIntakeState(!currState);
        }

        double lTriggerMech = Input.MECH_XBOX.getTriggerAxis(Hand.kLeft);
        double rTriggerMech = Input.MECH_XBOX.getTriggerAxis(Hand.kRight);
        if (lTriggerMech > 0.8) {
            BIGData.put("roller_mode", 1);
        } else if (rTriggerMech > 0.8) {
            BIGData.put("roller_mode", 2);
        } else {
            BIGData.put("roller_mode", 0);
        }

    }

}
