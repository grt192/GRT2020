package frc.control;

import frc.gen.BIGData;
import frc.pathfinding.*;
import frc.pathfinding.fieldmap.geometry.*;

public class GeneralControl extends Mode {

    private boolean newLoop = true;
    private boolean returnBool = true;

    private double startAngle;
    private double targetAngle;

    private static double SPEED = 0.2;
    private double d;

    private Vector velocity;
    private Vector currentPosition;
    private Vector targetPosition;

    private boolean intakeState;

    private double cameraAzimuth;
    private double cameraRange;
    private double lemonCount;
    private double rotate;

    public GeneralControl() {
    }

    @Override
    public boolean loop() {
        Target.Actions action = Target.getAction();
        switch (action) {
        case DRIVETO:
            runDriveTo();
            break;
        case INTAKE:
            runIntake();
            break;
        case SCORE:
            runScore();
            break;
        case TURN:
            runTurn();
            break;
        case SHOOT:
            runShoot();
            break;
        case HOOD:
            runHood();
            break;
        }
        if (!returnBool)
            Target.removeAction();
        return returnBool;
    }

    private void runDriveTo() {
        BIGData.put("robot_centric", false);
        if (newLoop) {
            targetPosition = Target.getTarget();
            newLoop = false;
        }
        currentPosition = BIGData.getPosition("curr");
        System.out.println("x: " + currentPosition.x + " y: " + currentPosition.y);
        d = targetPosition.distanceTo(currentPosition);
        velocity = currentPosition.subtract(targetPosition).multiply(1 / d).multiply(SPEED);

        if (d < 2) {
            newLoop = true;
            returnBool = false;
        } else {
            BIGData.requestDrive(velocity.y, -velocity.x, 0);
        }
    }

    private void runIntake() {
        intakeState = Target.getIntakeState();
        BIGData.requestIntakeState(intakeState);
        returnBool = false;
    }

    private void runScore() {
        cameraAzimuth = BIGData.getDouble("camera_azimuth");
        cameraRange = BIGData.getDouble("camera_range");
        lemonCount = BIGData.getInt("lemon_count");

        if (Math.abs(cameraAzimuth) > 1) {
            rotate = (-0.01 * cameraAzimuth);
        } else {
            rotate = 0;
        }
        if (Math.abs(cameraAzimuth) < 1)
            BIGData.putShooterState(true);
        returnBool = true;
        if (lemonCount < 1)
            returnBool = false;
        BIGData.requestDrive(0, 0, rotate);
    }

    private void runTurn() {
        startAngle = BIGData.getGyroAngle();
        targetAngle = Target.getAngle() + startAngle;
        BIGData.setAngle(targetAngle);
        returnBool = false;
    }

    private void runShoot() {
        BIGData.put("auton_manual_shooter", true);
        if (lemonCount < 1) {
            BIGData.put("auton_manual_shooter", false);
            returnBool = false;
        } else {
            returnBool = true;
        }
    }

    private void runHood() {
        BIGData.put("shooter_up", Target.getHoodState());
        returnBool = false;
    }
}