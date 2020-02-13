package frc.gen;

import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import frc.control.ShuffleboardCommands;
import frc.mechs.MechCollection;
import frc.swerve.Swerve;

public class Brain implements Runnable {
    public static Swerve swerve;
    public static MechCollection mechs;
    public static PowerDistributionPanel pdp;
    public static ShuffleboardCommands shuffleboardCommands;

    Notifier notif;

    public Brain() {
        swerve = new Swerve();
        mechs = new MechCollection();
        //pdp = new PowerDistributionPanel(0);
        shuffleboardCommands = new ShuffleboardCommands();
        notif = new Notifier(this);
        notif.startPeriodic(0.02);
    }

    public void run() {
        swerve.update();
        mechs.update();
        shuffleboardCommands.update();
        
        // brownout prevention
        // if (pdp.getVoltage() < 8) 
        //     BIGData.put("stage_1_disabled", true);
        // if (pdp.getVoltage() < 7.5)
        //     BIGData.put("stage_2_disabled", true);
    }
}