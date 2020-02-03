package frc.mechs;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Solenoid;
import frc.gen.BIGData;

public class IntakeMech implements Mech {
    private TalonSRX motor;
    private Solenoid sol;

    public IntakeMech() {
        motor = new TalonSRX(BIGData.getInt("intake_talon_id"));
        sol = new Solenoid(BIGData.getInt("intake_sol_id"));
    }

    @Override
    public void update() {
        motor.set(ControlMode.PercentOutput, BIGData.getIntakeSpeed());
        sol.set(BIGData.getIntakeState());
    }
}