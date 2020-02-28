package frc.control;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.gen.BIGData;

/**
 * Miscellaneous commands from the shuffleboard
 */
public class ShuffleboardCommands {

    private ShuffleboardTab settingsTab;
    private ShuffleboardLayout configLayout;
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
    // shooter
    private ShuffleboardTab shooterTab;
    private NetworkTableEntry shooterRange;
    private NetworkTableEntry shooterRPM;
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
        shooterTab = Shuffleboard.getTab("Shooter");
        configLayout = settingsTab.getLayout("Config File", "List Layout").withSize(2, 2).withPosition(0, 0);
        wheelZerosLayout = swerveTab.getLayout("Wheel Zeros", "Grid Layout").withSize(2, 2).withPosition(0, 2);
        wheelDriveSpeedsLayout = swerveTab.getLayout("Wheel Drive Speeds", "Grid Layout").withSize(2, 2).withPosition(2,
                2);
        wheelRotateSpeedsLayout = swerveTab.getLayout("Wheel Rotate Speeds", "Grid Layout").withSize(2, 2)
                .withPosition(4, 2);

        CommandBase updateLocalConfigFileCommand = new CommandBase() {
            @Override
            public void initialize() {
                System.out.println("updating the local config file (that contains swerve zeroes, etc.)");
                BIGData.updateLocalConfigFile();
            }

            @Override
            public boolean isFinished() {
                return true;
            }
        };
        configLayout.add("update CONFIG file", updateLocalConfigFileCommand).withWidget(BuiltInWidgets.kCommand);

        CommandBase updateLocalRPMConfigFileCommand = new CommandBase() {
            @Override
            public void initialize() {
                System.out.println("updating the RPM file with the current RPM mappings");
                BIGData.updateLocalRPMConfigFile();
            }

            @Override
            public boolean isFinished() {
                return true;
            }
        };
        configLayout.add("update RPM file", updateLocalRPMConfigFileCommand).withWidget(BuiltInWidgets.kCommand);

        CommandBase resetLocalRPMConfigFileCommand = new CommandBase() {
            @Override
            public void initialize() {
                System.out.println("resetting the RPM file with the deploy time mappings");
                BIGData.resetLocalRPMConfigFile();
            }

            @Override
            public boolean isFinished() {
                return true;
            }
        };
        configLayout.add("reset RPM file", resetLocalRPMConfigFileCommand).withWidget(BuiltInWidgets.kCommand);

        CommandBase resetLocalConfigFileCommand = new CommandBase() {
            @Override
            public void initialize() {
                System.out.println("resetting the LOCAL CONFIG file");
                BIGData.resetLocalConfigFile();
            }

            @Override
            public boolean isFinished() {
                return true;
            }
        };
        configLayout.add("reset CONFIG file", resetLocalConfigFileCommand).withWidget(BuiltInWidgets.kCommand);

        configMessage = settingsTab.add("config file msg", BIGData.getConfigFileMsg()).withPosition(3, 0).withSize(2, 1)
                .getEntry();

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
        swerveTab.add("zero BR", zeroModuleBR).withPosition(1, 1);
        swerveTab.add("zero BL", zeroModuleBL).withPosition(2, 1);
        swerveTab.add("zero FL", zeroModuleFL).withPosition(3, 1);

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

        shooterRange = shooterTab.add("shooter range", 0).getEntry();
        shooterRPM = shooterTab.add("shooter rpm", 0).getEntry();
        CommandBase updateShooter = new CommandBase() {
            @Override
            public void initialize() {
                System.out.println("updating shooter range and rpm");
                BIGData.put("range_testing", shooterRange.getDouble(0));

                BIGData.put("shooter_rpm", shooterRPM.getDouble(0));
            }

            @Override
            public boolean isFinished() {
                return true;
            }
        };
        shooterTab.add("update shooter", updateShooter);

    }

    public void update() {
        String newMsg = BIGData.getConfigFileMsg();
        if (!newMsg.equals(configMessage.getString(""))) {
            configMessage.forceSetString(newMsg);
        }
        gyroAngle.forceSetDouble(((180 / Math.PI) * BIGData.getGyroAngle()) % 360);
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