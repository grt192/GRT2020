package frc.mechs;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Solenoid;
import frc.gen.BIGData;

public class IntakeMech implements Mech {
    private TalonSRX motor;
    private Solenoid sol;

    private double intakeSpeed;

    public IntakeMech() {
        motor = new TalonSRX(BIGData.getInt("intake_talon_id"));
        motor.configContinuousCurrentLimit(10, 0);
        motor.configPeakCurrentLimit(15, 0);
        motor.configPeakCurrentDuration(100, 0);
        motor.enableCurrentLimit(true);
        sol = new Solenoid(BIGData.getInt("pcm_id"), BIGData.getInt("intake_sol_id"));
    }

    @Override
    public void update() {
        boolean state = BIGData.getIntakeState();
        intakeSpeed = BIGData.getBoolean("in_teleop") ? BIGData.getDouble("intake_speed") : 0.4;
        motor.set(ControlMode.PercentOutput, state ? intakeSpeed : 0);
        sol.set(state);
    }

    public void disable() {
        motor.set(ControlMode.Current, 0);
    }
}