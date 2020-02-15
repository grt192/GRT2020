package frc.mechs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
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
    // lookup table rpms for shooting in increments of 1 foot.
    // rpmTable[i] is the rpm to shoot at, i feet away from target (horizontal distance)
    private int[] upRPMTable;
    private int[] downRPMTable;
    private static final int DEFAULT_MIN_RPM = 2000;
    private static final int DEFAULT_MAX_RPM = 6000;

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
        // smff = new SimpleMotorFeedforward(-0.124, 0.14, 0.0798);
        smff = new SimpleMotorFeedforward(-0.101, 0.138, 0.149);
        // TODO: improve PID
        // configPID();
        this.encoder = motor.getEncoder();
        BIGData.putShooterState(false);
        this.shooterUp = BIGData.getBoolean("shooter_up");
        this.hood = new Solenoid(1, BIGData.getInt("one_wheel_hood"));
        // currentSpike = BIGData.getDouble("current_spike");

        initRPMTable();
    }

    private void initRPMTable() {
        upRPMTable = new int[50];
        downRPMTable = new int[50];
        boolean loadingDown = true;
        String rpmFileName = BIGData.getString("shooter_rpm_file");
        SortedMap<Integer, Integer> upRPMMap = new TreeMap<Integer, Integer>();
        SortedMap<Integer, Integer> downRPMMap = new TreeMap<Integer, Integer>();
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
                        System.out.println("loaded shooter point: dist(ft)=" + a + ",rpm=" + b + ", down=" + loadingDown);
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
        } catch (FileNotFoundException e) {
            System.out.println("UNABLE TO LOAD SHOOTER VALUES! SHOOTER WILL BE BAD!");
        } catch (Exception e) {
            System.out.println("something bad happened in initRPMTable!");
        } finally {
            // put edge rpms into map if absent
            upRPMMap.putIfAbsent(0, DEFAULT_MIN_RPM);
            upRPMMap.putIfAbsent(upRPMTable.length-1, DEFAULT_MAX_RPM);
            
            downRPMMap.putIfAbsent(0, DEFAULT_MIN_RPM);
            downRPMMap.putIfAbsent(downRPMTable.length-1, DEFAULT_MAX_RPM);
        }
        
        // linear interpolation
        int prevIndex = 0;
        int prevNum = downRPMMap.get(prevIndex);
        for (Map.Entry<Integer, Integer> e : downRPMMap.entrySet()) {
            if (e.getKey() < downRPMTable.length) {
                interpolate(downRPMTable, prevIndex, e.getKey(), prevNum, e.getValue());
                prevIndex = e.getKey();
                prevNum = e.getValue();
            }
        }
        
        // linear interpolation, pt 2
        prevIndex = 0;
        prevNum = upRPMMap.get(prevIndex);
        for (Map.Entry<Integer, Integer> e : upRPMMap.entrySet()) {
            if (e.getKey() < upRPMTable.length) {
                interpolate(upRPMTable, prevIndex, e.getKey(), prevNum, e.getValue());
                prevIndex = e.getKey();
                prevNum = e.getValue();
            }
        }
    }

    private void interpolate(int[] output, int startIndex, int endIndex, int startNum, int endNum) {
        if (startIndex == endIndex) {
            output[startIndex] = startNum;
            return;
        }
        for (int i = startIndex; i <= endIndex; i++) {
            output[i] = startNum + ((i - startIndex) * (endNum - startNum)) / (endIndex - startIndex);
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
            double rpm = calcSpeed(range);
            int offset = BIGData.getInt("shooter_offset_change");
            double newSpeed = rpm + offset;
            newSpeed = 3200;
            rpm = 3200;
            // put current rpm in BIGData so driver can to adjust speed based off that
            BIGData.put("shooter_auto", rpm);
            motor.setVoltage(smff.calculate(rpm / 60));
            System.out.println("smff voltage: " + smff.calculate(rpm / 60));
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
     * @param range
     *                  the distance from the target in inches
     * @return the rpm to run the shooter at
     */
    public double calcSpeed(double range) {
        if (shooterUp) {
            return upRPMTable[(int)(range/12)];
        } else {
            return downRPMTable[(int)(range/12)];
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
        double distanceRPM = calcSpeed(range);

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