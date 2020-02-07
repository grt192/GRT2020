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
    private ShuffleboardLayout swerveEnableLayout;
    private NetworkTableEntry wheelEnable[];
    // modulePositions[i] gives {col, row} of module for pretty shuffleboard positioning
    private int[][] modulePositions = {{1,0}, {1,1}, {0,1}, {0,0}}; 

    // config
    private NetworkTableEntry configMessage;

    public ShuffleboardCommands() {
        wheelZeros = new NetworkTableEntry[4];
        wheelDriveSpeeds = new NetworkTableEntry[4];
        wheelRotateSpeeds = new NetworkTableEntry[4];
        init();
    }

    public void init() {
        settingsTab = Shuffleboard.getTab("Settings");
        swerveTab = Shuffleboard.getTab("Swerve");
        configLayout = settingsTab.getLayout("Config File", "List Layout").withSize(2, 2).withPosition(0, 0);
        joystickLayout = settingsTab.getLayout("Joysticks", "List Layout").withSize(1,3).withPosition(2, 0);
        wheelZerosLayout = swerveTab.getLayout("Wheel Zeros", "Grid Layout").withSize(2,2).withPosition(0, 2);
        wheelDriveSpeedsLayout = swerveTab.getLayout("Wheel Drive Speeds", "Grid Layout").withSize(2,2).withPosition(2,2);
        wheelRotateSpeedsLayout = swerveTab.getLayout("Wheel Rotate Speeds", "Grid Layout").withSize(2,2).withPosition(4,2);
        swerveEnableLayout = swerveTab.getLayout("Wheels Enabled", "Grid Layout").withSize(2,2).withPosition(2,0);

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
        
        swerveTab.add("zero WHEELS", zeroRotateCommand).withWidget(BuiltInWidgets.kCommand).withPosition(0, 1).withSize(1,1);
        for (int i = 0; i < 4; i++) {
            final int finalI = i;
            wheelZeros[i] = wheelZerosLayout.add(BIGData.getWheelName(i) + " zero:", BIGData.getWheelZero(i))
                .withPosition(modulePositions[i][0], modulePositions[i][1]).getEntry();
            wheelDriveSpeeds[i] = wheelDriveSpeedsLayout.add(BIGData.getWheelName(i) + " drive speed", BIGData.getWheelRawDriveSpeed(i))
                .withPosition(modulePositions[i][0], modulePositions[i][1]).getEntry();
            wheelRotateSpeeds[i] = wheelRotateSpeedsLayout.add(BIGData.getWheelName(i) + " rotate speed", BIGData.getWheelRawRotateSpeed(i))
                .withPosition(modulePositions[i][0], modulePositions[i][1]).getEntry();
            CommandBase enableSwerve = new CommandBase() {
                @Override
                public void initialize() { 
                    System.out.println("disabled=" + BIGData.getSwerveDisable(finalI));
                    BIGData.setSwerveDisable(finalI, !BIGData.getSwerveDisable(finalI));
                    this.setName("enabled=" + !BIGData.getSwerveDisable(finalI));
                }
                @Override
                public boolean isFinished() {
                    return true;
                }
            };
            enableSwerve.setName("enabled=" + !BIGData.getSwerveDisable(i));
            swerveEnableLayout.add(BIGData.getWheelName(i) + " enabled", enableSwerve)
                .withPosition(modulePositions[i][0], modulePositions[i][1]);
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
        swerveTab.add("zero GYRO", zeroGyroCommand).withWidget(BuiltInWidgets.kCommand).withPosition(0, 0).withSize(1,1);
        gyroAngle = swerveTab.add("Angle", BIGData.getGyroAngle()).withPosition(1,0).withSize(1,1).getEntry();
        gyroRate = swerveTab.add("Angular Veloc", BIGData.getGyroW()).withPosition(1, 1).withSize(1,1).getEntry();
        
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