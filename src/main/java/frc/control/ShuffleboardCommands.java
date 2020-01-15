package frc.control;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import frc.gen.BIGData;
import frc.util.GRTUtil;

/**
 * Miscellaneous commands from the shuffleboard
 */
public class ShuffleboardCommands implements Runnable {

    private ShuffleboardTab settingsTab;
    private ShuffleboardLayout configLayout;
    // joysticks
    private ShuffleboardLayout joystickLayout;
    private NetworkTableEntry joystickX1;
    private NetworkTableEntry joystickY1;
    private NetworkTableEntry joystickX2;
    private NetworkTableEntry joystickY2;
    // swerve
    private ShuffleboardTab swerveTab;
    private NetworkTableEntry frZero;
    private NetworkTableEntry brZero;
    private NetworkTableEntry blZero;
    private NetworkTableEntry flZero;
    private NetworkTableEntry gyroAngle;
    private NetworkTableEntry gyroRate;
    // config
    private NetworkTableEntry configMessage;


    private Notifier notifier;

    public ShuffleboardCommands() {
        init();
    }

    public void init() {
        settingsTab = Shuffleboard.getTab("Settings");
        swerveTab = Shuffleboard.getTab("Swerve");
        configLayout = settingsTab.getLayout("Config File", "List Layout");
        joystickLayout = settingsTab.getLayout("Joysticks", "List Layout");

        CommandBase resetTempConfigCommand = new CommandBase() {
            @Override
            public void initialize() {
                System.out.println("resetting temp config file...");
                BIGData.resetTempConfigFile();
            }
            @Override
            public boolean isFinished() {
                return true;
            }
        };
        configLayout.add("reset temp config file", resetTempConfigCommand).withWidget(BuiltInWidgets.kCommand);
        
        CommandBase useDeployCommand = new CommandBase() {
            @Override
            public void initialize() {
                System.out.println("next restart, deploy time file will be used");
                BIGData.changeStartupConfigFile(true);
            }
            @Override
            public boolean isFinished() {
                return true;
            }
        };
        configLayout.add("use deploy time config file", useDeployCommand).withWidget(BuiltInWidgets.kCommand);

        CommandBase useTempCommand = new CommandBase() {
            @Override
            public void initialize() {
                System.out.println("next restart, temp config file will be used");
                BIGData.changeStartupConfigFile(false);
            }
            @Override
            public boolean isFinished() {
                return true;
            }
        };
        configLayout.add("use temporary config file", useTempCommand).withWidget(BuiltInWidgets.kCommand);
        configMessage = settingsTab.add("config file msg", BIGData.getConfigFileMsg()).getEntry();

        joystickX1 = joystickLayout.add("original pt1", BIGData.getDouble("joystick_x1")).getEntry();
        joystickY1 = joystickLayout.add("new pt1", BIGData.getDouble("joystick_y1")).getEntry();
        joystickX2 = joystickLayout.add("original pt2", BIGData.getDouble("joystick_x2")).getEntry();
        joystickY2 = joystickLayout.add("new pt2", BIGData.getDouble("joystick_y2")).getEntry();
        CommandBase setJoystickCommand = new CommandBase() {
            @Override
            public void initialize() {
                double x1 = joystickX1.getDouble(42);
                double y1 = joystickY1.getDouble(42);
                double x2 = joystickX2.getDouble(42);
                double y2 = joystickY2.getDouble(42);
                if (GRTUtil.inRange(-1.0, x1, 1.0) && GRTUtil.inRange(-1.0, y1, 1.0) && GRTUtil.inRange(-1.0, x2, 1.0)
                        && GRTUtil.inRange(-1.0, y2, 1.0)) {
                    BIGData.setJoystickX1(x1);
                    BIGData.setJoystickY1(y1);
                    BIGData.setJoystickX2(x2);
                    BIGData.setJoystickY2(y2);
                    BIGData.updateConfigFile();
                    System.out.println("set new joystick profiles...");
                }
            }
            @Override
            public boolean isFinished() {
                return true;
            }
        };
        joystickLayout.add("set joystick profile", setJoystickCommand).withWidget(BuiltInWidgets.kCommand);

        CommandBase zeroRotateCommand = new CommandBase() {
            @Override
            public void initialize() {
                BIGData.setZeroSwerveRequest(true);
            }
            @Override
            public boolean isFinished() {
                return true;
            }
        };
        
        swerveTab.add("zero swerve WHEELS", zeroRotateCommand).withWidget(BuiltInWidgets.kCommand);
        frZero = swerveTab.add("fr zero:", BIGData.getFrZero()).getEntry();
        brZero = swerveTab.add("br zero", BIGData.getBrZero()).getEntry();
        blZero = swerveTab.add("bl zero", BIGData.getBlZero()).getEntry();
        flZero = swerveTab.add("fl zero", BIGData.getFlZero()).getEntry();

        CommandBase zeroGyroCommand = new CommandBase() {
            @Override
            public void initialize() {
                System.out.println("zeroing the gyro...");
                BIGData.setZeroGyroRequest(true);
            }
            @Override
            public boolean isFinished() {
                return true;
            }
        };
        swerveTab.add("zero swerve GYRO", zeroGyroCommand).withWidget(BuiltInWidgets.kCommand);
        gyroAngle = swerveTab.add("Gyro Angle", BIGData.getGyroAngle()).getEntry();
        gyroRate = swerveTab.add("Gyro Rate of Rotation", BIGData.getGyroW()).getEntry();

        notifier = new Notifier(this);
        notifier.startPeriodic(0.02);
    }

    @Override
    public void run() {
        String newMsg = BIGData.getConfigFileMsg();
        if (!newMsg.equals(configMessage.getString(""))) {
            configMessage.forceSetString(newMsg);
        }
        gyroAngle.forceSetDouble(BIGData.getGyroAngle());
        gyroRate.forceSetDouble(BIGData.getGyroW());

        // update zeros on dashboard
        frZero.setDouble(BIGData.getFrZero());
        brZero.setDouble(BIGData.getBrZero());
        blZero.setDouble(BIGData.getBlZero());
        flZero.setDouble(BIGData.getFlZero());

    }

}