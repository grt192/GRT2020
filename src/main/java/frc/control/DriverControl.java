package frc.control;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import frc.control.input.Input;
import frc.control.input.JoystickProfile;
import frc.gen.BIGData;

class DriverControl extends Mode {
    private int pov = -1;
    private int lastPov;

    private boolean useCameraCenter, useLidarCenter;

    @Override
    public boolean loop() {
        JoystickProfile.updateProfilingPoints();
        driveSwerve();
        driveShooterMech();
        driveIntakeMech();
        driveStorageMech();
        driveWinchMech();
        driveLinkageMech();
        driveSpinnerMech();
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
        double rotate = JoystickProfile.applyProfile(-(Math.abs(rTrigger) - Math.abs(lTrigger)));
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

        // center with vision if the a button is being held
        double cameraAzimuth = BIGData.getDouble("camera_azimuth");
        if (Input.SWERVE_XBOX.getAButtonPressed()) {
            useCameraCenter = true;
        }
        if (Input.SWERVE_XBOX.getAButtonReleased()) {
            useCameraCenter = false;
            BIGData.setPIDFalse();
        }
        if (useCameraCenter) {
            // set the angle to rotate to
            BIGData.setAngle(cameraAzimuth + BIGData.getGyroAngle());
        }

        BIGData.requestDrive(x, y, rotate);
    }

    private void driveIntakeMech() {
        // if left trigger is pressed, run intake motor in reverse
        // if right trigger is pressed, run intake motor in forwards
        double lTriggerMech = Input.MECH_XBOX.getTriggerAxis(Hand.kLeft);
        double rTriggerMech = Input.MECH_XBOX.getTriggerAxis(Hand.kRight);
        double mechTriggerSum = JoystickProfile.applyDeadband(Math.abs(rTriggerMech) - Math.abs(lTriggerMech));
        BIGData.put("intake_speed", mechTriggerSum);
        // if x button is released, toggle the intake position
        if (Input.MECH_XBOX.getXButtonReleased()) {
            boolean currState = BIGData.getIntakeState();
            BIGData.requestIntakeState(!currState);
        }
    }

    private void driveShooterMech() {
        // if mech b button is pressed, toggle the hood
        if (Input.MECH_XBOX.getBButtonReleased()) {
            boolean shooterUp = BIGData.getBoolean("shooter_up");
            shooterUp = !shooterUp;
            BIGData.put("shooter_up", shooterUp);
        }
        // if mech a button is pressed, automatic control for shooter
        BIGData.putShooterState(Input.MECH_XBOX.getAButton(), "mech");

        // put in requested shooter manual speed
        double rJoystickMech = Input.MECH_XBOX.getY(Hand.kRight);
        rJoystickMech = JoystickProfile.applyProfile(rJoystickMech);
        BIGData.put("shooter_manual", rJoystickMech);

        // if the bumpers are pressed, change the base offset
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
    }

    private void driveStorageMech() {
        // if y is being held, set to manual control (aka false)
        BIGData.putStorageState(!Input.MECH_XBOX.getYButton());
        // set the speed of manual control
        double lJoystickMech = Input.MECH_XBOX.getY(Hand.kLeft);
        lJoystickMech = JoystickProfile.applyProfile(lJoystickMech);
        BIGData.requestManualStorageSpeed(lJoystickMech);
    }

    private void driveWinchMech() {
        // only allow the winch to be driven when the swerve y button is being pressed
        BIGData.putWinchState(Input.SWERVE_XBOX.getYButton());
        // read motor speed from right joystick of swerve controller
        double rJoystickSwerve = Input.SWERVE_XBOX.getY(Hand.kRight);
        rJoystickSwerve = JoystickProfile.applyProfile(rJoystickSwerve);
        // set the speed to spin at
        BIGData.requestWinchSpeed(rJoystickSwerve);
    }

    private void driveLinkageMech() {
        // if the x button is released, toggle linkage state
        if (Input.SWERVE_XBOX.getXButtonReleased()) {
            boolean linkageUp = BIGData.getLinkageState();
            linkageUp = !linkageUp;
            BIGData.requestLinkageState(linkageUp);
        }
        // if the b button is released, toggle the hook solenoid
        if (Input.SWERVE_XBOX.getBButtonReleased()) {
            BIGData.requestHookState(!BIGData.getHookState());
        }
    }

    private void driveSpinnerMech() {
        // check if we need to toggle spinner state (up/down)
        boolean spinnerUp = BIGData.getSpinnerState();
        if (Input.SWERVE_XBOX.getBackButtonReleased()) {
            BIGData.putSpinnerState(!spinnerUp);
        }
        // check if we should be in automatic control of the color wheel
        if (Input.MECH_XBOX.getStartButtonReleased()) {
            BIGData.setUseManualSpinner(false);
        }
        // Use the POV on MECH_XBOX to set the speed
        int mechPOV = Input.MECH_XBOX.getPOV();
        //System.out.println(mechPOV);
        // if POV is being pressed, we should use the manual control
        if (mechPOV >= 0) {
            BIGData.setUseManualSpinner(true);
        }
        switch (mechPOV) {
            case 0: BIGData.setManualSpinnerSpeed(0); break;
            case 45: BIGData.setManualSpinnerSpeed(BIGData.getDouble("slow_spinner_speed")); break;
            case 90: BIGData.setManualSpinnerSpeed(BIGData.getDouble("fast_spinner_speed")); break;
            case 270: BIGData.setManualSpinnerSpeed(-BIGData.getDouble("fast_spinner_speed")); break;
            case 315: BIGData.setManualSpinnerSpeed(-BIGData.getDouble("slow_spinner_speed")); break;
            default: BIGData.setManualSpinnerSpeed(0); break;
        }
        //System.out.println(BIGData.getManualSpinnerSpeed());
    }

}
