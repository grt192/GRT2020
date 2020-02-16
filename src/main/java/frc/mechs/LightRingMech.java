package frc.mechs;

import com.ctre.phoenix.motorcontrol.ControlMode;

import edu.wpi.first.wpilibj.VictorSP;
import frc.gen.BIGData;

public class LightRingMech implements Mech {
    private VictorSP ring;

    public LightRingMech() {
        ring = new VictorSP(7);
        ring.setVoltage(12);
    }

    @Override
    public void update() {
        System.out.println("setting voltage");
        ring.set(1);
    }
}