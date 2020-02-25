package frc.control;

import frc.gen.BIGData;
import frc.pathfinding.*;
import frc.pathfinding.fieldmap.geometry.*;

public class GeneralControl extends Mode {

    private boolean newLoop = true;
    private boolean returnBool = true;

    private double startAngle;
    private double currentAngle;
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
        }
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
            BIGData.requestDrive(velocity.x, velocity.y, 0);
        }
    }

    private void runIntake() {
        BIGData.put("auto_intake_speed", 0.4);
        intakeState = Target.getIntakeState();
        BIGData.requestIntakeState(intakeState);
        returnBool = false;
    }

    private void runScore() {
        cameraAzimuth = BIGData.getDouble("camera_azimuth");
        cameraRange = BIGData.getDouble("camera_range");
        lemonCount = BIGData.getInt("lemon_count");

        if (cameraRange == 0) {
            System.out.println("no vision target found!! turning!!");
            BIGData.requestDrive(0, 0, 0.2);
        } else {
            BIGData.requestDrive(0, 0, 0);
            if (lemonCount < 1) {
                returnBool = false;
            } else {
                if (Math.abs(cameraAzimuth) > 2)
                    BIGData.setAngle(cameraAzimuth);
                if (Math.abs(cameraAzimuth) <= 2)
                    BIGData.putShooterState(true);
                returnBool = true;
            }
        }

    }

    private void runTurn() {
        startAngle = BIGData.getGyroAngle();
        targetAngle = Target.getAngle() + startAngle;
        BIGData.setAngle(targetAngle);
        returnBool = false;
    }
}