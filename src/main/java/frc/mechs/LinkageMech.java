package frc.mechs;

import edu.wpi.first.wpilibj.Solenoid;
import frc.gen.BIGData;

public class LinkageMech implements Mech {
    private Solenoid sol;

    public LinkageMech() {
        sol = new Solenoid(9, BIGData.getInt("linkage_sol_id"));
        BIGData.requestLinkageState(false);
    }

    @Override
    public void update() {
        sol.set(BIGData.getLinkageState());
    }
}