package frc.control;

import java.util.Set;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.NetworkButton;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
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

    private ShuffleboardTab swerveTab;
    private NetworkTableEntry frZero;
    private NetworkTableEntry brZero;
    private NetworkTableEntry blZero;
    private NetworkTableEntry flZero;

    private Notifier notifier;

    public ShuffleboardCommands() {
        init();
    }

    public void init() {
        settingsTab = Shuffleboard.getTab("Settings");
        swerveTab = Shuffleboard.getTab("Swerve");
        configLayout = settingsTab.getLayout("Config File", "List Layout");
        joystickLayout = settingsTab.getLayout("Joysticks", "List Layout");

        Command resetTempConfigCommand = new Command("reset temp config file") {
            private boolean done = false;
            @Override
            void execute() {
                BIGData.resetTempConfigFile();
                done = true;
            }

            boolean isFinished() {
                return done;
            }

            @Override
            public Set<Subsystem> getRequirements() {
                // TODO Auto-generated method stub
                return null;
            }
        };
        configLayout.add("reset temp config file", resetTempConfigCommand);
        
        Command useDeployCommand = new Command("use deploy time config file") {
            private boolean done = false;
            @Override
            default void execute() {
                BIGData.changeStartupConfigFile(true);
                done = true;
            }

            protected boolean isFinished() {
                return done;
            }
        };
        configLayout.add("use deploy time config file", useDeployCommand);

        Command useTempCommand = new Command("use temporary config file") {
            private boolean done = false;

            protected void execute() {
                BIGData.changeStartupConfigFile(false);
                done = true;
            }

            protected boolean isFinished() {
                return done;
            }
        };
        configLayout.add("use temporary config file", useTempCommand);

        joystickX1 = joystickLayout.add("original pt1", BIGData.getDouble("joystick_x1")).getEntry();
        joystickY1 = joystickLayout.add("new pt1", BIGData.getDouble("joystick_y1")).getEntry();
        joystickX2 = joystickLayout.add("original pt2", BIGData.getDouble("joystick_x2")).getEntry();
        joystickY2 = joystickLayout.add("new pt2", BIGData.getDouble("joystick_y2")).getEntry();
        Command setJoystickCommand = new Command("set joystick profile") {
            private boolean done = false;

            protected void execute() {
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
                }
                done = true;
            }

            protected boolean isFinished() {
                return done;
            }
        };
        joystickLayout.add("set joystick profile", setJoystickCommand);

        Command zeroRotateCommand = new Command("zero swerve WHEELS") {
            private boolean done = false;

            protected void initialize() {
                System.out.println("requesting swerve zero...");
                BIGData.setZeroSwerveRequest(true);
                done = true;
            }

            protected boolean isFinished() {
                System.out.println("in isfinished of swerve zeroing requester");
                return done;
            }
        };
        swerveTab.add("zero swerve WHEELS", zeroRotateCommand);
        frZero = swerveTab.add("fr zero:", BIGData.getFrZero()).getEntry();
        brZero = swerveTab.add("br zero", BIGData.getBrZero()).getEntry();
        blZero = swerveTab.add("bl zero", BIGData.getBlZero()).getEntry();
        flZero = swerveTab.add("fl zero", BIGData.getFlZero()).getEntry();

        Command zeroGyroCommand = new Command("zero swerve GYRO") {
            private boolean done = false;

            protected void execute() {
                System.out.println("requesting swerve gyro zero...");
                BIGData.setZeroGyroRequest(true);
                done = true;
            }

            protected boolean isFinished() {
                return done;
            }
        };
        swerveTab.add("zero swerve GYRO", zeroGyroCommand);

        notifier = new Notifier(this);
        notifier.startPeriodic(0.02);

        CommandScheduler.schedule(true, resetTempConfigCommand);

        CommandScheduler.schedule(true, resetTempConfigCommand, useDeployCommand, useTempCommand, setJoystickCommand, zeroRotateCommand, zeroGyroCommand);
    }

    @Override
    public void run() {
        CommandScheduler.getInstance().run();
        // update zeros on dashboard
        frZero.setDouble(BIGData.getFrZero());
        brZero.setDouble(BIGData.getBrZero());
        blZero.setDouble(BIGData.getBlZero());
        flZero.setDouble(BIGData.getFlZero());
    }

}