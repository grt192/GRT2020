package frc.mechs;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import frc.gen.BIGData;

public class LightRingMech implements Mech {
    private VictorSPX ring;

    public LightRingMech() {
        ring = new VictorSPX(BIGData.getInt("ring_id"));
        ring.set(ControlMode.PercentOutput, 1);
    }

    @Override
    public void update() {
        System.out.println("setting voltage");
        ring.set(ControlMode.PercentOutput, 1);
    }
}