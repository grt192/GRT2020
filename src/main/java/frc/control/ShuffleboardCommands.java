package frc.control;

import edu.wpi.first.networktables.NetworkTableEntry;
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
public class ShuffleboardCommands {

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
    private ShuffleboardLayout wheelZerosLayout;
    private NetworkTableEntry wheelZeros[];
    private ShuffleboardLayout wheelDriveSpeedsLayout;
    private NetworkTableEntry wheelDriveSpeeds[];
    private ShuffleboardLayout wheelRotateSpeedsLayout;
    private NetworkTableEntry wheelRotateSpeeds[];
    private NetworkTableEntry gyroAngle;
    private NetworkTableEntry gyroRate;
    // config
    private NetworkTableEntry configMessage;

    private ShuffleboardTab shooterTab;
    private NetworkTableEntry wheelARPM;
    private NetworkTableEntry wheelBRPM;

    public ShuffleboardCommands() {
        wheelZeros = new NetworkTableEntry[4];
        wheelDriveSpeeds = new NetworkTableEntry[4];
        wheelRotateSpeeds = new NetworkTableEntry[4];
        init();
    }

    public void init() {
        settingsTab = Shuffleboard.getTab("Settings");
        swerveTab = Shuffleboard.getTab("Swerve");
        shooterTab = Shuffleboard.getTab("2 wheel shooter");
        configLayout = settingsTab.getLayout("Config File", "List Layout").withSize(2, 2).withPosition(0, 0);
        joystickLayout = settingsTab.getLayout("Joysticks", "List Layout").withSize(1,3).withPosition(2, 0);
        wheelZerosLayout = swerveTab.getLayout("Wheel Zeros", "Grid Layout").withSize(2,2).withPosition(0, 2);
        wheelDriveSpeedsLayout = swerveTab.getLayout("Wheel Drive Speeds", "Grid Layout").withSize(2,2).withPosition(2,2);
        wheelRotateSpeedsLayout = swerveTab.getLayout("Wheel Rotate Speeds", "Grid Layout").withSize(2,2).withPosition(4,2);

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
        configMessage = settingsTab.add("config file msg", BIGData.getConfigFileMsg()).withPosition(3, 0).withSize(2, 1).getEntry();

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
                BIGData.putZeroSwerveRequest(true);
            }
            @Override
            public boolean isFinished() {
                return true;
            }
        };
        
        swerveTab.add("zero swerve WHEELS", zeroRotateCommand).withWidget(BuiltInWidgets.kCommand);
        for (int i = 0; i < 4; i++) {
            wheelZeros[i] = wheelZerosLayout.add(BIGData.getWheelName(i) + " zero:", BIGData.getWheelZero(i)).getEntry();
            wheelDriveSpeeds[i] = wheelDriveSpeedsLayout.add(BIGData.getWheelName(i) + " drive speed", BIGData.getWheelRawDriveSpeed(i)).getEntry();
            wheelRotateSpeeds[i] = wheelRotateSpeedsLayout.add(BIGData.getWheelName(i) + " rotate speed", BIGData.getWheelRawRotateSpeed(i)).getEntry();
        }

        CommandBase zeroGyroCommand = new CommandBase() {
            @Override
            public void initialize() {
                System.out.println("zeroing the gyro...");
                BIGData.putZeroGyroRequest(true);
            }
            @Override
            public boolean isFinished() {
                return true;
            }
        };
        swerveTab.add("zero swerve GYRO", zeroGyroCommand).withWidget(BuiltInWidgets.kCommand);
        gyroAngle = swerveTab.add("Gyro Angle", BIGData.getGyroAngle()).getEntry();
        gyroRate = swerveTab.add("Gyro Rate of Rotation", BIGData.getGyroW()).getEntry();

        //TODO ONLY FOR TESTING PURPOSES, DELETE LATER
        wheelARPM = shooterTab.add("wheel a rpm", 0).getEntry();
        wheelBRPM = shooterTab.add("wheel b rpm", 0).getEntry();
        CommandBase updateTwoWheelShooter = new CommandBase() {
            @Override
            public void initialize() {
                System.out.println("updating two wheel shooter speeds");
                BIGData.put("wheel_a_rpm", wheelARPM.getDouble(0));
                BIGData.put("wheel_b_rpm", wheelBRPM.getDouble(0));
            }
            @Override
            public boolean isFinished() {
                return true;
            }
        };
        shooterTab.add("update two wheel shooter", updateTwoWheelShooter);
    }

    public void update() {
        String newMsg = BIGData.getConfigFileMsg();
        if (!newMsg.equals(configMessage.getString(""))) {
            configMessage.forceSetString(newMsg);
        }
        gyroAngle.forceSetDouble(BIGData.getGyroAngle());
        gyroRate.forceSetDouble(BIGData.getGyroW());

        // update zeros on dashboard
        for (int i = 0; i < 4; i++) {
            wheelZeros[i].forceSetDouble(BIGData.getWheelZero(i));
            wheelDriveSpeeds[i].forceSetDouble(BIGData.getWheelRawDriveSpeed(i));
            wheelRotateSpeeds[i].forceSetDouble(BIGData.getWheelRawRotateSpeed(i));

        }
    }

}