package frc.mechs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMax.IdleMode;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.controller.SimpleMotorFeedforward;
import frc.gen.BIGData;

public class ShooterMech implements Mech {
    // RPM map of <distance (inches), RPM> for when the hood is up
    private TreeMap<Integer, Integer> upRPMMap;
    // RPM map of <distance (inches), RPM> for when the hood is down
    private TreeMap<Integer, Integer> downRPMMap;
    
    private static final int DEFAULT_RPM = 3500;

    private CANSparkMax motor;
    private CANEncoder encoder;
    private CANPIDController pid;
    private SimpleMotorFeedforward smff;
    private double kP, kI, kFF, kMaxOutput, kMinOutput;
    private Solenoid hood;
    private boolean shooterUp;

    private final double WHEEL_RADIUS = 2;
    private final double MINUTES_TO_SECONDS = 60;
    private final double SHOOTER_HIGH_ANGLE = BIGData.getDouble("shooter_high_angle") / 180 * Math.PI;
    private final double LOW_HIGH_ANGLE = BIGData.getDouble("low_high_angle") / 180 * Math.PI;
    private double shooterAngle;

    public ShooterMech() {
        System.out.println(BIGData.getInt("one_wheel_shooter"));
        this.motor = new CANSparkMax(BIGData.getInt("one_wheel_shooter"), MotorType.kBrushless);
        motor.setSmartCurrentLimit(10);
        motor.setSecondaryCurrentLimit(15);
        motor.setIdleMode(IdleMode.kCoast);
        this.pid = motor.getPIDController();
        smff = new SimpleMotorFeedforward(-0.124, 0.14, 0.0798);
        // TODO: improve PID
        // configPID();
        this.encoder = motor.getEncoder();
        BIGData.putShooterState(false);
        this.shooterUp = BIGData.getBoolean("shooter_up");
        this.hood = new Solenoid(9, BIGData.getInt("one_wheel_hood"));
        // currentSpike = BIGData.getDouble("current_spike");

        initRPMTable();
    }

    private void initRPMTable() {
        boolean loadingDown = true;
        String rpmFileName = BIGData.getString("shooter_rpm_file");
        upRPMMap = new TreeMap<Integer, Integer>();
        downRPMMap = new TreeMap<Integer, Integer>();
        String directory = "/home/lvuser/deploy";
        try {
            Scanner in = new Scanner(new File(directory, rpmFileName));
            while (in.hasNextLine()) {
                String line = in.nextLine().trim();
                if (line.equalsIgnoreCase("down")) {
                    loadingDown = true;
                    continue;
                } else if (line.equalsIgnoreCase("up")) {
                    loadingDown = false;
                    continue;
                }
                if (line.length() > 0 && line.charAt(0) != '#') {
                    String[] split = line.split(",");
                    try {
                        int a = Integer.parseInt(split[0]);
                        int b = Integer.parseInt(split[1]);
                        System.out.println("loaded shooter point: dist(in)=" + a + 
                                            ",rpm=" + b + ", down=" + loadingDown);
                        if (loadingDown) {
                            downRPMMap.put(a, b);
                        } else {
                            upRPMMap.put(a, b);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("unable to parse line: " + line);
                    } catch (NumberFormatException e) {
                        System.out.println("unable to parse line: " + line);
                    }
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("UNABLE TO LOAD SHOOTER VALUES! SHOOTER WILL BE BAD!");
        } catch (Exception e) {
            System.out.println("something bad happened in initRPMTable!");
        }
    }

    public void update() {
        // Hood Toggle
        shooterUp = BIGData.getBoolean("shooter_up");
        hood.set(shooterUp);

        boolean mode = BIGData.getShooterState();
        // mode being false means shooter is off
        // SSystem.out.println(mode);
        if (!mode) {
            motor.set(BIGData.getDouble("shooter_manual"));
            // mode being true means it's in automatic control, speed calculated based on
            // distance to vision target
        } else {
            double range = BIGData.getDouble("camera_range");
            double rpm = calcSpeed((int)range);
            int offset = BIGData.getInt("shooter_offset_change");
            double newSpeed = rpm + offset;
            newSpeed = 3000;
            rpm = 3000;
            // put current rpm in BIGData so driver can to adjust speed based off that
            BIGData.put("shooter_auto", rpm);
            motor.setVoltage(smff.calculate(rpm / 60, 3000 / 60));
            System.out.println("smff voltage: " + smff.calculate(rpm / 60, 3000 / 60));
            // pid.setReference(newSpeed, ControlType.kVelocity);
        }
        BIGData.put("shooter_current_rpm", getSpeed());
    }

    public void setSpeed(double rpm) {
        pid.setReference(rpm, ControlType.kVelocity);
        // System.out.println(getSpeed());
    }

    /**
     * Takes a distance from the target (horizontal distance, in inches) and returns
     * a speed in rpm to run the shooter at.
     * 
     * @param range the distance from the target in inches
     * @return the rpm to run the shooter at
     */
    public double calcSpeed(int range) {
        Map.Entry<Integer, Integer> floorEntry = shooterUp ? upRPMMap.floorEntry(range) : downRPMMap.floorEntry(range);
        Map.Entry<Integer, Integer> ceilEntry = shooterUp ? upRPMMap.ceilingEntry(range) : downRPMMap.ceilingEntry(range);
        if (floorEntry == null && ceilEntry == null) {
            return DEFAULT_RPM;
        } else if (floorEntry == null) {
            return ceilEntry.getValue();
        } else if (ceilEntry == null) {
            return floorEntry.getValue();
        } else {
            // linear interpolation
            return floorEntry.getValue() + ((ceilEntry.getValue() - floorEntry.getValue()) / (ceilEntry.getKey() - floorEntry.getKey())) * (range - floorEntry.getKey());
        }
    }

    public double calcSpeedWhileMoving(double range) {

        if (shooterUp) {
            shooterAngle = SHOOTER_HIGH_ANGLE;
        } else {
            shooterAngle = LOW_HIGH_ANGLE;
        }

        double wheelV = Math.sqrt(
                Math.pow(BIGData.getDouble("enc_vx") * 39.37, 2) + Math.pow(BIGData.getDouble("enc_vy") * 39.37, 2));
        double distanceRPM = calcSpeed((int)range);

        double distanceV = distanceRPM * MINUTES_TO_SECONDS * WHEEL_RADIUS * Math.cos(shooterAngle);

        double relativeAng = Math.PI / 2 - Math.abs(BIGData.getDouble("lidar_relative"));

        double shooterV = Math
                .sqrt(Math.pow(distanceV, 2) + Math.pow(wheelV, 2) - wheelV * distanceV * Math.cos(relativeAng))
                / Math.cos(shooterAngle);

        double newAzimuth = Math.signum(relativeAng) * Math.asin(Math.sin(-relativeAng) * wheelV / shooterV);

        BIGData.setAngle(newAzimuth);

        return shooterV;
    }

    public void configPID() {
        kP = 5e-5;
        kI = 1e-7;
        kFF = 3e-6;
        kMaxOutput = 1;
        kMinOutput = -1;

        pid.setP(kP);
        pid.setI(kI);
        pid.setD(0);
        pid.setIZone(0);
        pid.setFF(kFF);
        pid.setOutputRange(kMinOutput, kMaxOutput);
    }

    public double getSpeed() {
        return encoder.getVelocity();
    }

    public void disable() {
        motor.close();
    }
}