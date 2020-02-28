/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.control.Mode;
import frc.control.input.Input;
import frc.control.input.JoystickProfile;
import frc.gen.BIGData;
import frc.gen.Brain;
import frc.pathfinding.Target;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
    private NetworkTableEntry mode;
    private Autonomous autonomous;

    public static BIGData data;
    public static Brain brain;
    public static Target target;

    public static double ROBOT_WIDTH;
    public static double ROBOT_HEIGHT;
    public static double ROBOT_RADIUS;
    private boolean overridden;

    /**
     * This function is run when the robot is first started up and should be used
     * for any initialization code.
     */
    @Override
    public void robotInit() {
        BIGData.start();
        BIGData.put("gyro_ang", 0.0);
        JoystickProfile.init();
        ROBOT_HEIGHT = BIGData.getDouble("robot_height");
        ROBOT_WIDTH = BIGData.getDouble("robot_width");
        ROBOT_RADIUS = Math.max(ROBOT_WIDTH, ROBOT_HEIGHT) / 2;

        autonomous = new Autonomous(this);
        Mode.initModes();
        mode = NetworkTableInstance.getDefault().getTable("Robot").getEntry("mode");
        mode.setNumber(0);
        CommandScheduler.getInstance().enable();

        brain = new Brain();
        target = new Target();
    }

    private void loop() {
        autonomous.loop();
        int i = mode.getNumber(0).intValue();
        if (manualOverride()) {
            autonomous.kill();
            mode.setNumber(0);
            i = 0;
        }
        if (!Mode.getMode(i).loop()) {
            autonomous.modeFinished();
            mode.setNumber(0);
        }
    }

    /**
     * This function is called periodically during test mode.
     */
    @Override
    public void testPeriodic() {

        // TESTING INVOLVING VISION

        boolean centeringCamera = false;
        boolean centeringLidar = false;
        double cameraAzimuth = BIGData.getDouble("camera_azimuth");
        double x = Input.SWERVE_XBOX.getX(Hand.kLeft);
        double y = -Input.SWERVE_XBOX.getY(Hand.kLeft);
        double lTrigger = Input.SWERVE_XBOX.getTriggerAxis(Hand.kLeft);
        double rTrigger = Input.SWERVE_XBOX.getTriggerAxis(Hand.kRight);
        double rotate = JoystickProfile.applyProfile(-(rTrigger * rTrigger - lTrigger * lTrigger));

        if (Input.SWERVE_XBOX.getAButtonPressed()) {
            centeringCamera = true;
            BIGData.setAngle(cameraAzimuth + BIGData.getGyroAngle()); // TODO does not work
        }

        if (Input.SWERVE_XBOX.getAButtonReleased()) {
            centeringCamera = false;
            BIGData.setPIDFalse();
        }

        // TODO test centering robot to target using camera

        double lidarAzimuth = BIGData.getDouble("lidar_azimuth");
        double lidarRange = BIGData.getDouble("lidar_range");
        if (Input.SWERVE_XBOX.getXButtonPressed()) {
            centeringLidar = true;
            BIGData.setAngle(-Math.toDegrees(lidarAzimuth) + BIGData.getGyroAngle());
        }

        if (Input.SWERVE_XBOX.getXButtonReleased()) {
            centeringLidar = false;
            BIGData.setPIDFalse();
        }

        // TODO: test azimuth angle thresholds
        if ((centeringLidar || centeringCamera) && (lidarAzimuth < 2 || cameraAzimuth < 2)) {
            BIGData.putShooterState(true, "swerve");
        } else {
            BIGData.putShooterState(false, "swerve");
        }

        BIGData.requestDrive(x, y, rotate);
    }

    @Override
    public void disabledInit() {
        BIGData.requestDrive(0, 0, 0);
        BIGData.setPIDFalse();
    }

    public void setMode(int i) {
        mode.setNumber(i);
    }

    /**
     * This function is called every robot packet, no matter the mode. Use this for
     * items like diagnostics that you want ran during disabled, autonomous,
     * teleoperated and test.
     *
     * <p>
     * This runs after the mode specific periodic functions, but before LiveWindow
     * and SmartDashboard integrated updating.
     */
    @Override
    public void robotPeriodic() {
        CommandScheduler.getInstance().run();

    }

    private boolean manualOverride() {
        double x = JoystickProfile.applyProfile(Input.SWERVE_XBOX.getY(Hand.kLeft));
        double y = JoystickProfile.applyProfile(-Input.SWERVE_XBOX.getX(Hand.kLeft));
        boolean temp = !(x == 0 && y == 0);
        if (temp && !overridden) {
            overridden = temp;
            return true;
        }
        overridden = temp;
        return false;
    }

    /**
     * This autonomous (along with the chooser code above) shows how to select
     * between different autonomous modes using the dashboard. The sendable chooser
     * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
     * remove all of the chooser code and uncomment the getString line to get the
     * auto name from the text box below the Gyro
     *
     * <p>
     * You can add additional auto modes by adding additional comparisons to the
     * switch structure below with additional strings. If using the SendableChooser
     * make sure to add them to the chooser code above as well.
     */
    @Override
    public void autonomousInit() {
        BIGData.putZeroGyroRequest(true);
        BIGData.put("in_teleop", false);
        BIGData.put("auton_started", true);
        autonomous.init("bezier1.txt");
    }

    /**
     * This function is called periodically during autonomous.
     */
    @Override
    public void autonomousPeriodic() {
        loop();
    }

    @Override
    public void teleopInit() {
        BIGData.put("in_teleop", true);
    }

    /**
     * This function is called periodically during operator control.
     */
    @Override
    public void teleopPeriodic() {
        Mode.getMode(0).loop();
    }

}
