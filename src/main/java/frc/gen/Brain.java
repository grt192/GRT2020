package frc.gen;

import edu.wpi.first.wpilibj.Notifier;
import frc.control.ShuffleboardCommands;
import frc.swerve.Swerve;

public class Brain implements Runnable {
    public static Swerve swerve;
    public static ShuffleboardCommands shuffleboardCommands;

    Notifier notif;
    public Brain() {
        swerve = new Swerve();
        shuffleboardCommands = new ShuffleboardCommands();
        notif = new Notifier(this);
        notif.startPeriodic(0.02);
    }
    public void run() {
        swerve.update();
        shuffleboardCommands.update();
    }
}