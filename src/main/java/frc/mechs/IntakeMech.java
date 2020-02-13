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

        System.out.println(BIGData.getInt("intake_sol_id"));

        sol = new Solenoid(1, BIGData.getInt("intake_sol_id"));
    }

    @Override
    public void update() {
        boolean state = BIGData.getIntakeState();
        System.out.println(BIGData.getDouble("intake_speed"));

        if (state) {
            switch (BIGData.getInt("roller_mode")) {
            case 0:
                motor.set(ControlMode.PercentOutput, 0);
                break;
            case 1:
                motor.set(ControlMode.PercentOutput, BIGData.getDouble("intake_speed"));
                break;
            case 2:
                motor.set(ControlMode.PercentOutput, -BIGData.getDouble("intake_speed"));
                break;
            }
        } else {
            motor.set(ControlMode.PercentOutput, 0);
        }
        sol.set(state);
    }
}