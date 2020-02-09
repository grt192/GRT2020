package frc.gen;

import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import frc.control.FieldGUI;
import frc.control.ShuffleboardCommands;
import frc.mechs.MechCollection;
import frc.positiontracking.PositionTracking;
import frc.swerve.Swerve;

public class Brain implements Runnable {
    public static Swerve swerve;
    public static MechCollection mechs;
    public static PositionTracking tracking;
    public static ShuffleboardCommands shuffleboardCommands;
    public static FieldGUI fieldGUI;
    public static PowerDistributionPanel pdp;

    Notifier notif;

    public Brain() {
        swerve = new Swerve();
        // mechs = new MechCollection();
        tracking = new PositionTracking();
        shuffleboardCommands = new ShuffleboardCommands();
        //fieldGUI = new FieldGUI("10.1.92.147", 5000);
        pdp = new PowerDistributionPanel();
        notif = new Notifier(this);
        notif.startPeriodic(0.02);
    }

    public void run() {
        swerve.update();
        tracking.update();
        // mechs.update();
        shuffleboardCommands.update();
        //fieldGUI.update();
    }
}