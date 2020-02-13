package frc.mechs;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.ControlType;

import edu.wpi.first.wpilibj.Solenoid;
import frc.gen.BIGData;

public class ShooterMech implements Mech {

    private CANSparkMax motor;
    private CANEncoder encoder;
    private CANPIDController pid;
    private double kP, kI, kFF, kMaxOutput, kMinOutput;
    private Solenoid hood;
    private boolean shooterUp;

    public ShooterMech() {
        System.out.println(BIGData.getInt("one_wheel_shooter"));
        this.motor = new CANSparkMax(BIGData.getInt("one_wheel_shooter"), MotorType.kBrushless);
        motor.setSmartCurrentLimit(10);
        motor.setSecondaryCurrentLimit(15);
        this.pid = motor.getPIDController();
        // TODO: improve PID
        configPID();
        this.encoder = motor.getEncoder();
        BIGData.putShooterState(false);
        this.shooterUp = BIGData.getBoolean("shooter_up");
        this.hood = new Solenoid(1, BIGData.getInt("one_wheel_hood"));
    }

    public void update() {
        // Hood Toggle
        shooterUp = BIGData.getBoolean("shooter_up");
        hood.set(shooterUp);

        boolean mode = BIGData.getShooterState();
        // mode being false means shooter is off
        // SSystem.out.println(mode);
        boolean disabled = BIGData.getDisabled(2);
        if (disabled) {
            disable();
        } else if (!mode) {
            motor.set(BIGData.getDouble("shooter_manual"));
            // motor.set(BIGData.getDouble("shooter_manual"));
            // mode being true means it's in automatic control, speed calculated based on
            // distance to vision target
        } else {
            double range = BIGData.getDouble("camera_range");
            double rpm = calcSpeed(range);
            int offset = BIGData.getInt("shooter_offset_change");
            double newSpeed = rpm + offset;
            newSpeed = -5000;
            rpm = -5000;
            // put current rpm in BIGData so driver can to adjust speed based off that
            BIGData.put("shooter_auto", rpm);
            pid.setReference(newSpeed, ControlType.kVelocity);
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

    public void disable() {
        motor.close();
    }
}