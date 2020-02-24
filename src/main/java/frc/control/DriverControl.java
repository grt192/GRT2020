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
        // zero swerve gyro if start button (menu button) is pressed on swerve
        // controller
        if (Input.SWERVE_XBOX.getStartButtonReleased()) {
            BIGData.putZeroGyroRequest(true);
        }

        double x = Input.SWERVE_XBOX.getX(Hand.kLeft);
        // negativize y so that up is forward
        double y = -Input.SWERVE_XBOX.getY(Hand.kLeft);
        x = JoystickProfile.applyProfile(x);
        y = JoystickProfile.applyProfile(y);
        // rotate the robot
        double lTrigger = Input.SWERVE_XBOX.getTriggerAxis(Hand.kLeft);
        double rTrigger = Input.SWERVE_XBOX.getTriggerAxis(Hand.kRight);
        double rotate = JoystickProfile.applyProfile(-(rTrigger * rTrigger - lTrigger * lTrigger));
        if (rotate != 0) {
            BIGData.setPIDFalse();
        }

        // get input for automatically snapping to an angle (in increments of 45deg)
        pov = Input.SWERVE_XBOX.getPOV();
        if (pov != -1) {
            lastPov = pov;
            BIGData.setAngle(pov);
        } else if (Input.SWERVE_XBOX.getBumperPressed(Hand.kLeft)) {
            pov = lastPov - 45;
            lastPov = pov;
            BIGData.setAngle(pov);
        } else if (Input.SWERVE_XBOX.getBumperPressed(Hand.kRight)) {
            pov = lastPov + 45;
            lastPov = pov;
            BIGData.setAngle(pov);
        }

    }

    private void driveMechs() {

        // Mechs on the swerve xbox

        BIGData.putWinchState(Input.SWERVE_XBOX.getYButton());
        double rJoystickSwerve = Input.SWERVE_XBOX.getY(Hand.kRight);
        rJoystickSwerve = JoystickProfile.applyProfile(rJoystickSwerve);
        BIGData.requestWinchSpeed(rJoystickSwerve);

        if (Input.SWERVE_XBOX.getXButtonReleased()) {
            boolean linkageUp = BIGData.getLinkageState();
            linkageUp = !linkageUp;
            BIGData.requestLinkageState(linkageUp);
        }

        // run the hook if the back button (change views button is pressed)
        if (Input.SWERVE_XBOX.getBackButtonReleased()) {
            BIGData.requestHookState(!BIGData.getHookState());
        }

        // Mechs on the mech xbox
        BIGData.putStorageState(!Input.MECH_XBOX.getYButton());

        if (Input.MECH_XBOX.getStartButtonReleased()) {
            // TODO spin spinner
        }

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

        BIGData.putShooterState(Input.MECH_XBOX.getAButton(), "mech");

        if (Input.MECH_XBOX.getBButtonReleased()) {
            boolean shooterUp = BIGData.getBoolean("shooter_up");
            shooterUp = !shooterUp;
            BIGData.put("shooter_up", shooterUp);
        }

        // if x button is released, toggle the intake position
        if (Input.MECH_XBOX.getXButtonReleased()) {
            boolean currState = BIGData.getIntakeState();
            BIGData.requestIntakeState(!currState);
        }

        if (Input.MECH_XBOX.getBackButtonReleased()) {
            boolean state = BIGData.getSpinnerState();
            BIGData.putSpinnerState(!state);
        }
        if (Input.MECH_XBOX.getStartButtonReleased()) {
            BIGData.put("in_spinner_manual", !BIGData.getBoolean("in_spinner_manual"));
        }

        double lJoystickMech = Input.MECH_XBOX.getY(Hand.kLeft);
        lJoystickMech = JoystickProfile.applyProfile(lJoystickMech);
        BIGData.requestStorageSpeed(lJoystickMech);

        double rJoystickMech = Input.MECH_XBOX.getY(Hand.kRight);
        rJoystickMech = JoystickProfile.applyProfile(rJoystickMech);
        BIGData.put("shooter_manual", rJoystickMech);

        BIGData.put("correct_storage_values",
                (Input.MECH_XBOX.getXButtonReleased() && Input.MECH_XBOX.getYButtonReleased()));

        // if left trigger is pressed, run intake motor in reverse
        // if right trigger is pressed, run intake motor in forwards
        double lTriggerMech = Input.MECH_XBOX.getTriggerAxis(Hand.kLeft);
        double rTriggerMech = Input.MECH_XBOX.getTriggerAxis(Hand.kRight);
        double mechTriggerSum = JoystickProfile.applyDeadband(Math.abs(rTriggerMech) - Math.abs(lTriggerMech));

        BIGData.put("intake_speed", mechTriggerSum);

    }

}
