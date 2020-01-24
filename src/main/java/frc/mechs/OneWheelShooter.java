package frc.mechs;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import frc.gen.BIGData;

public class OneWheelShooter {

    private CANSparkMax motor;
    private CANEncoder encoder;

    public OneWheelShooter() {
        this.motor = new CANSparkMax(BIGData.getInt("one_wheel_shooter"), MotorType.kBrushless);
        encoder = motor.getEncoder();
    }

    public void update() {
        motor.set(BIGData.getDouble("one_wheel_shooter"));
    }

    public double getSpeed() {
        return encoder.getVelocity();
    }
}