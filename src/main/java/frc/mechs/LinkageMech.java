package frc.mechs;

import edu.wpi.first.wpilibj.Solenoid;
import frc.gen.BIGData;

public class LinkageMech implements Mech {
    private Solenoid firstSol;
    private Solenoid hookSol;

    public LinkageMech() {
        firstSol = new Solenoid(BIGData.getInt("pcm_id"), BIGData.getInt("linkage_sol_id"));
        hookSol = new Solenoid(BIGData.getInt("pcm_id"), BIGData.getInt("hook_sol_id"));
        BIGData.requestLinkageState(false);
    }

    @Override
    public void update() {
        firstSol.set(BIGData.getLinkageState());
        if (BIGData.getLinkageState()) {
            hookSol.set(BIGData.getHookState());
        }
    }
}