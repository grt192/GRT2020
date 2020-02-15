package frc.mechs;

import edu.wpi.first.wpilibj.VictorSP;
import frc.gen.BIGData;

public class LightRingMech implements Mech {
    private VictorSP ring;

    public LightRingMech() {
        ring = new VictorSP(BIGData.getInt("ring_id"));
        ring.setVoltage(6);
    }

    @Override
    public void update() {

    }
}