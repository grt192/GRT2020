package frc.gen;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import frc.control.FieldGUI;
import frc.control.ShuffleboardCommands;
import frc.mechs.MechCollection;
import frc.positiontracking.PositionTracking;
import frc.swerve.Swerve;
import frc.targettracking.JetsonCamera;
import frc.targettracking.Lidar;

public class Brain implements Runnable {
    public static Swerve swerve;
    public static MechCollection mechs;
    public static PositionTracking tracking;
    public static ShuffleboardCommands shuffleboardCommands;
    public static Lidar lidar;
    public static JetsonCamera camera, intakeCamera;
    public static FieldGUI fieldGUI;
    public static PowerDistributionPanel pdp;
    public static DriverCameras cameras;

    Notifier notif;

    public Brain() {
        swerve = new Swerve();
        tracking = new PositionTracking();
        mechs = new MechCollection();
        // pdp = new PowerDistributionPanel(0);
        shuffleboardCommands = new ShuffleboardCommands();
        // lidar = new Lidar();
        camera = new JetsonCamera(1337);
        intakeCamera = new JetsonCamera(1992);
        // fieldGUI = new FieldGUI("10.1.92.151", 5000);

        cameras = new DriverCameras(2);

        notif = new Notifier(this);
        notif.startPeriodic(0.02);
    }

    public void run() {
        swerve.update();
        mechs.update();
        shuffleboardCommands.update();
        // fieldGUI.update();
        cameras.update();

        if (!BIGData.getBoolean("in_teleop"))
            tracking.update();

        // brownout prevention
        // if (pdp.getVoltage() < 8)
        // BIGData.put("stage_1_disabled", true);
        // if (pdp.getVoltage() < 7.5)
        // BIGData.put("stage_2_disabled", true);
    }
}