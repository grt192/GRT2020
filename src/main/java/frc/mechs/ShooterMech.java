package frc.mechs;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.ControlType;

import frc.gen.BIGData;

public class ShooterMech implements Mech {

    private CANSparkMax motor;
    private CANEncoder encoder;
    private CANPIDController pid;
    private double kP, kI, kFF, kMaxOutput, kMinOutput;

    public ShooterMech() {
        this.motor = new CANSparkMax(BIGData.getInt("one_wheel_shooter"), MotorType.kBrushless);
        this.pid = motor.getPIDController();
        configPID();
        this.encoder = motor.getEncoder();
        BIGData.put("one_wheel_shooter", 0.0);
    }

    public void update() {
        setSpeed(BIGData.getDouble("one_wheel_shooter"));
    }

    public void setSpeed(double rpm) {
        // System.out.println(rpm);
        pid.setReference(rpm, ControlType.kVelocity);
        System.out.println(getSpeed());
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