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
        // driveMechs();
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

        if (Input.SWERVE_XBOX.getAButtonPressed()) {
            centeringCamera = true;
        }

        if (Input.SWERVE_XBOX.getAButtonReleased()) {
            centeringCamera = false;
        }

        double azimuth = BIGData.getDouble("camera_azimuth");
        // System.out.println(azimuth);
        if (centeringCamera && Math.abs(azimuth) > 1) {
            rotate = 0.5 * azimuth * Math.PI / 180;
        }
        BIGData.requestDrive(x, y, rotate);

        //TODO: remove after debugging
        System.out.println("curr_x" + BIGData.getPosition("curr").x + " curr_y: " + BIGData.getPosition("curr").y);

    }

    // private void driveMechs() {
    // double one_l = Input.MECH_XBOX.getTriggerAxis(Hand.kLeft);
    // double one_r = -Input.MECH_XBOX.getTriggerAxis(Hand.kRight);
    // double one = one_l + one_r;

    // double two_a = -Input.MECH_XBOX.getY(Hand.kRight);
    // double two_b = two_a;
    // if (two_a == 0) {
    // // TODO: add stuff
    // }
    // BIGData.putMechs(one, two_a, two_b);
    // }

}
