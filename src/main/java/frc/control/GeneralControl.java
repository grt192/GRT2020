package frc.control;

import frc.gen.BIGData;
import frc.pathfinding.Target;
import frc.pathfinding.fieldmap.geometry.Vector;

public class GeneralControl extends Mode {

    private boolean newLoop = true;
    private boolean returnBool = true;

    private double startAngle;
    private double currentAngle;
    private double targetAngle;

    private static double SPEED = 0.1;
    private double d;

    private Vector velocity;
    private Vector currentPosition;
    private Vector targetPosition;

    private boolean intakeState;

    public GeneralControl() {
    }

    @Override
    public boolean loop() {
        Target.Actions action = Target.getAction();
        switch (action) {
        case TURN:
            runTurn();
            break;
        case DRIVETO:
            runDriveTo();
            break;
        case INTAKE:
            runIntake();
            break;
        }
        return returnBool;
    }

    private void runTurn() {
        if (newLoop) {
            startAngle = BIGData.getGyroAngle();
            targetAngle = Target.getAngle() + startAngle;
            newLoop = false;
        }
        currentAngle = BIGData.getGyroAngle();
        if (Math.abs(targetAngle - currentAngle) < 2) {
            newLoop = true;
            returnBool = false;
        } else {
            BIGData.setAngle(targetAngle);
            returnBool = true;
        }

    }

    private void runDriveTo() {
        if (newLoop) {
            targetPosition = Target.getTarget();
            newLoop = false;
        }
        currentPosition = BIGData.getPosition("curr");
        d = targetPosition.distanceTo(currentPosition);
        velocity = targetPosition.subtract(currentPosition).multiply(1 / d).multiply(SPEED);

        if (d < 2) {
            newLoop = true;
            returnBool = false;
        } else {
            BIGData.requestDrive(velocity.x, velocity.y, 0);
        }
    }

    private void runIntake() {
        intakeState = Target.getIntakeState();
        BIGData.requestIntakeState(intakeState);
        returnBool = false;
    }
}