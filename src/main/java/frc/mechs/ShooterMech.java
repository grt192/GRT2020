package frc.mechs;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.Solenoid;

import com.revrobotics.ControlType;

import frc.gen.BIGData;

public class ShooterMech implements Mech {

    private CANSparkMax motor;
    private CANEncoder encoder;
    private CANPIDController pid;
    private double kP, kI, kFF, kMaxOutput, kMinOutput;
    private Solenoid hood;
    private boolean shooterUp;

    public ShooterMech() {
        this.motor = new CANSparkMax(BIGData.getInt("one_wheel_shooter"), MotorType.kBrushless);
        this.pid = motor.getPIDController();
        // TODO: improve PID
        configPID();
        this.encoder = motor.getEncoder();
        BIGData.putShooterState(0);

        this.shooterUp = BIGData.getBoolean("shooter_up");
        this.hood = new Solenoid(BIGData.getInt("one_wheel_hood"));

    }

    public void update() {

        // Hood Toggle
        shooterUp = BIGData.getBoolean("shooter_up");
        if (shooterUp) {
            hood.set(shooterUp);
        }

        int mode = BIGData.getInt("shooter_state");
        // mode being 0 means shooter is off
        if (mode == 0) {
            setSpeed(0);
            // mode being 1 means it's in manual control, speed in BIGData adjusted by
            // buttons and driver input
        } else if (mode == 1) {
            setSpeed(BIGData.getDouble("one_wheel_shooter"));
            // mode being 2 means it's in automatic control, speed calculated based on
            // distance to vision target
        } else {
            double range = BIGData.getDouble("camera_range");
            double rpm = calcSpeed(range);
            // put current rpm in BIGData so if driver wants to adjust speed based off that
            BIGData.put("one_wheel_shooter", rpm);
            setSpeed(rpm);
        }
    }

    public void setSpeed(double rpm) {
        pid.setReference(rpm, ControlType.kVelocity);
        // System.out.println(getSpeed());
    }

    public double calcSpeed(double range) {
        // TODO: make an actual formula for this
        return range * 10;
    }

    public void configPID() {
        kP = 5e-5;
        kI = 1e-7;
        kFF = 1e-6;
        kMaxOutput = 1;
        kMinOutput = -1;

        pid.setP(kP);
        pid.setI(kI);
        pid.setD(0);
        pid.setIZone(0);
        pid.setFF(kFF);
        pid.setOutputRange(kMinOutput, kMaxOutput);
    }

    public double getSpeed() {
        return encoder.getVelocity();
    }
}