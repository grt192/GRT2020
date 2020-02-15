package frc.mechs;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import frc.gen.BIGData;

public class WinchMech implements Mech {
    /** motor to drive the winch */
    private CANSparkMax motor;
    /** speed from -1.0 to 1.0 */
    private double speed;

    public WinchMech() {
        motor = new CANSparkMax(BIGData.getInt("winch_id"), MotorType.kBrushless);
    }

    @Override
    public void update() {
        boolean state = BIGData.getWinchState();
        speed = BIGData.getWinchSpeed();
        motor.set(state ? speed : 0);
    }
}