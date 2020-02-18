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

import static frc.gen.BIGData.downRPMMap;
import static frc.gen.BIGData.upRPMMap;

public class ShooterMech implements Mech {
    
    private static final int DEFAULT_RPM = 3500;

    private CANSparkMax motor_lead;
    private CANSparkMax motor_follow;
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
        this.motor_lead = new CANSparkMax(BIGData.getInt("one_wheel_shooter_lead"), MotorType.kBrushless);
        // motor_lead.setSmartCurrentLimit(10);
        // motor_lead.setSecondaryCurrentLimit(15);
        motor_lead.setIdleMode(IdleMode.kCoast);
        this.motor_follow = new CANSparkMax(BIGData.getInt("one_wheel_shooter_follow"), MotorType.kBrushless);
        motor_follow.follow(motor_lead);
        // motor_follow.setSmartCurrentLimit(10);
        // motor_follow.setSecondaryCurrentLimit(15);
        motor_follow.setIdleMode(IdleMode.kCoast);
        smff = new SimpleMotorFeedforward(0.162, 0.13, 0.0225);
        this.encoder = motor_lead.getEncoder();
        BIGData.putShooterState(false);
        this.shooterUp = BIGData.getBoolean("shooter_up");
        this.hood = new Solenoid(9, BIGData.getInt("one_wheel_hood"));
        // currentSpike = BIGData.getDouble("current_spike");
    }

    public void update() {
        // Hood Toggle
        shooterUp = BIGData.getBoolean("shooter_up");
        hood.set(shooterUp);

        boolean mode = BIGData.getShooterState();
        // mode being false means shooter is off
        // SSystem.out.println(mode);
        if (!mode) {
            motor_lead.set(BIGData.getDouble("shooter_manual"));
            // mode being true means it's in automatic control, speed calculated based on
            // distance to vision target
        } else {
            double range = BIGData.getDouble("camera_range");
            double rpm = calcSpeed((int)range);
            int offset = BIGData.getInt("shooter_offset_change");
            double newSpeed = rpm + offset;
            newSpeed = 5500;
            rpm = BIGData.getDouble("shooter_speed");
            // put current rpm in BIGData so driver can to adjust speed based off that
            BIGData.put("shooter_auto", rpm);
            motor_lead.setVoltage(smff.calculate(rpm / 60));
            // System.out.println("smff voltage: " + smff.calculate(rpm / 60));
            // System.out.println("speed: " + getSpeed());
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

    public double getSpeed() {
        return encoder.getVelocity();
    }
}