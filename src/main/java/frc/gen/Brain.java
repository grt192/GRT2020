package frc.gen;

import edu.wpi.first.wpilibj.Notifier;
import frc.control.ShuffleboardCommands;
import frc.mechs.MechCollection;
import frc.swerve.Swerve;
import frc.targettracking.JetsonCamera;
import frc.targettracking.Lidar;

public class Brain implements Runnable {
    public static Swerve swerve;
    public static MechCollection mechs;
    public static ShuffleboardCommands shuffleboardCommands;
    public static Lidar lidar;
    public static JetsonCamera camera;

    Notifier notif;

    public Brain() {
        swerve = new Swerve();
        mechs = new MechCollection();
        shuffleboardCommands = new ShuffleboardCommands();
        lidar = new Lidar();
        camera = new JetsonCamera();
        notif = new Notifier(this);
        notif.startPeriodic(0.02);
    }

    public void run() {
        swerve.update();
        mechs.update();
        shuffleboardCommands.update();
    }
}