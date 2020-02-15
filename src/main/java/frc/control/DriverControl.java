package frc.control;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import frc.control.input.Input;
import frc.control.input.JoystickProfile;
import frc.gen.BIGData;

class DriverControl extends Mode {
    private int pov = -1;
    private int lastPov;

    private boolean centeringCamera = false;

    @Override
    public boolean loop() {
        JoystickProfile.updateProfilingPoints();
        driveSwerve();
        driveMechs();
        return true;
    }

    private void driveSwerve() {
        // zero swerve gyro if start button (menu button) is pressed
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
        if (Input.MECH_XBOX.getStartButtonReleased()) {
            BIGData.put("reset_lemon_count", true);
        }
        if (Input.SWERVE_XBOX.getAButtonReleased()) {
            boolean currState = BIGData.getLinkageState();
            BIGData.requestLinkageState(!currState);
        }

        if (Input.SWERVE_XBOX.getXButtonReleased()) {
            boolean currState = BIGData.getSpinnerState();
            BIGData.putSpinnerState(!currState);
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

        // if x button is released, toggle the intake position
        if (Input.MECH_XBOX.getXButtonReleased()) {
            boolean currState = BIGData.getIntakeState();
            BIGData.requestIntakeState(!currState);
        }

        if (Input.MECH_XBOX.getAButtonReleased()) {
            BIGData.put("Spinner?", !BIGData.getBoolean("Spinner?"));
            BIGData.put("firstTime?", true);
        }

        // if left trigger is pressed, run intake motor in reverse
        // if right trigger is pressed, run intake motor in forwards
        //TODO TEST IF INTAKE WORKS AS EXPECTED!
        double lTriggerMech = Input.MECH_XBOX.getTriggerAxis(Hand.kLeft);
        double rTriggerMech = Input.MECH_XBOX.getTriggerAxis(Hand.kRight);
        double mechTriggerSum = JoystickProfile.applyDeadband(Math.abs(rTriggerMech) - Math.abs(lTriggerMech));
        BIGData.put("intake_speed", mechTriggerSum);

    }

}
