package frc.gen;

import edu.wpi.first.wpilibj.Notifier;
import frc.control.ShuffleboardCommands;
import frc.mechs.TwoWheelShooter;
import frc.swerve.Swerve;

public class Brain implements Runnable {
    public static Swerve swerve;
    public static ShuffleboardCommands shuffleboardCommands;
    public static TwoWheelShooter shoot;

    Notifier notif;
    public Brain() {
        swerve = new Swerve();
        shuffleboardCommands = new ShuffleboardCommands();
        shoot = new TwoWheelShooter();
        notif = new Notifier(this);
        notif.startPeriodic(0.02);
    }
    public void run() {
        swerve.update();
        shuffleboardCommands.update();
    }
}