package frc.mechs;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import frc.gen.BIGData;

public class TwoWheelShooter {
    private int TICKS_PER_ROTATION;
    private int REDUCTION;
    private TalonSRX wheelAMotor;
    private TalonSRX wheelBMotor;

    private double aRPM;
    private double bRPM;

    public TwoWheelShooter() {
        wheelAMotor = new TalonSRX(BIGData.getInt("two_wheel_motor_a"));
        wheelBMotor = new TalonSRX(BIGData.getInt("two_wheel_motor_b"));
        configTalons(wheelAMotor);
        configTalons(wheelBMotor);
        TICKS_PER_ROTATION = BIGData.getInt("two_wheel_ticks_per_rotation");
        REDUCTION = BIGData.getInt("two_wheel_reduction");
    }

    public void updateSpeeds() {
        setSpeeds(BIGData.getDouble("wheel_a_rpm"), BIGData.getDouble("wheel_b_rpm"));
    }

    private void setSpeeds(double aRPM, double bRPM) {
        this.aRPM = rpmToTalonVeloc(aRPM);
        this.bRPM = rpmToTalonVeloc(bRPM);
        System.out.println(this.aRPM + " " + this.bRPM);
        wheelAMotor.set(ControlMode.Velocity, this.aRPM);
        wheelBMotor.set(ControlMode.Velocity, this.bRPM);
    }

    public void configTalons(TalonSRX tal) {
        tal.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);
        tal.config_kP(0, 0.04);
        tal.config_kI(0, 0);
        tal.config_kD(0, 0);
        tal.config_kF(0, .009);
        tal.selectProfileSlot(0, 0);
    }

    public double rpmToTalonVeloc(double rpm) {
        return rpm * REDUCTION * TICKS_PER_ROTATION / (60 * 10);
    }
}