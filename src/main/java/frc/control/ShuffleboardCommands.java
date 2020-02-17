package frc.control;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
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
        joystickLayout = settingsTab.getLayout("Joysticks", "List Layout").withSize(1, 3).withPosition(2, 0);
        wheelZerosLayout = swerveTab.getLayout("Wheel Zeros", "Grid Layout").withSize(2, 2).withPosition(0, 2);
        wheelDriveSpeedsLayout = swerveTab.getLayout("Wheel Drive Speeds", "Grid Layout").withSize(2, 2).withPosition(2,
                2);
        wheelRotateSpeedsLayout = swerveTab.getLayout("Wheel Rotate Speeds", "Grid Layout").withSize(2, 2)
                .withPosition(4, 2);

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
        configMessage = settingsTab.add("config file msg", BIGData.getConfigFileMsg()).withPosition(3, 0).withSize(2, 1)
                .getEntry();

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

        swerveTab.add("zero ALL WHEELS", zeroRotateCommand).withPosition(3, 0).withWidget(BuiltInWidgets.kCommand);
        for (int i = 0; i < 4; i++) {
            wheelZeros[i] = wheelZerosLayout.add(BIGData.getWheelName(i) + " zero:", BIGData.getWheelZero(i))
                    .getEntry();
            wheelDriveSpeeds[i] = wheelDriveSpeedsLayout
                    .add(BIGData.getWheelName(i) + " drive speed", BIGData.getWheelRawDriveSpeed(i)).getEntry();
            wheelRotateSpeeds[i] = wheelRotateSpeedsLayout
                    .add(BIGData.getWheelName(i) + " rotate speed", BIGData.getWheelRawRotateSpeed(i)).getEntry();
        }

        // commands for zeroing individual swerve modules:
        CommandBase zeroModuleFR = new ZeroIndivSwerveCommand(BIGData.FR_WHEEL);
        CommandBase zeroModuleBR = new ZeroIndivSwerveCommand(BIGData.BR_WHEEL);
        CommandBase zeroModuleBL = new ZeroIndivSwerveCommand(BIGData.BL_WHEEL);
        CommandBase zeroModuleFL = new ZeroIndivSwerveCommand(BIGData.FL_WHEEL);
        swerveTab.add("zero FR", zeroModuleFR).withPosition(0, 1);
        swerveTab.add("zero BR", zeroModuleBR).withPosition(0, 2);
        swerveTab.add("zero BL", zeroModuleBL).withPosition(0, 3);
        swerveTab.add("zero FL", zeroModuleFL).withPosition(0, 4);


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
        swerveTab.add("zero swerve GYRO", zeroGyroCommand).withWidget(BuiltInWidgets.kCommand).withPosition(1, 0);
        SmartDashboard.putData("ZERO SWERVE GYRO", zeroGyroCommand);
        gyroAngle = swerveTab.add("Gyro Angle", BIGData.getGyroAngle()).withPosition(0, 0).getEntry();
        gyroRate = swerveTab.add("Gyro Rate of Rotation", BIGData.getGyroW()).withPosition(2, 0).getEntry();

        CommandBase resetLemonCountCommand = new CommandBase() {
            @Override
            public void initialize() {
                System.out.println("setting lemon count to 0");
                BIGData.put("reset_lemon_count", true);
            }
            @Override
            public boolean isFinished() {
                return true;
            }
        };
        SmartDashboard.putData("RESET LEMON COUNT", resetLemonCountCommand);
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
        SmartDashboard.putBoolean("CONNECTED TO CAMERA ON JETSON?", BIGData.getJetsonCameraConnected());
    }

    class ZeroIndivSwerveCommand extends CommandBase {
        int wheelNum;
        public ZeroIndivSwerveCommand(int wheelNum) {
            this.wheelNum = wheelNum;
        }
        @Override 
        public void initialize() {
            System.out.println("ZEROING INDIVIDUAL MODULE: " + BIGData.getWheelName(wheelNum));
            BIGData.putZeroIndivSwerveRequest(wheelNum, true);
        }
        @Override
        public boolean isFinished() {
            return true;
        }

    }
}