package frc.mechs;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Ultrasonic;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.revrobotics.ColorSensorV3;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import frc.gen.BIGData;

public class StorageMech implements Mech {
    private final I2C.Port i2cPort = I2C.Port.kOnboard;
    private final ColorSensorV3 SHOOTER = new ColorSensorV3(i2cPort);
    private AnalogInput intake, top, bottom, middle;
    private TalonSRX motor;

    private int ultrasonicRange, revRange, IRRange;

    private int TICKS_PER_ROTATION;
    private int REDUCTION;
    private double RPM;

    // private final int range = 400;
    private boolean seenBall, conveyerSeenBall = false;

    private int lemonCount = 0;
    private int conveyerCount = 0;

    private boolean ballsInCoveyer = false;

    public StorageMech() {
        intake = new AnalogInput(BIGData.getInt("intake_analog"));
        top = new AnalogInput(BIGData.getInt("top_analog"));
        middle = new AnalogInput(BIGData.getInt("middle_analog"));
        bottom = new AnalogInput(BIGData.getInt("bottom_analog"));
        this.motor = new TalonSRX(BIGData.getInt("storage_motor"));
        configTalons(motor);
        TICKS_PER_ROTATION = BIGData.getInt("storage_ticks_per_rotation");
        REDUCTION = BIGData.getInt("storage_reduction");
        BIGData.put("storage_rpm", 0);
        ultrasonicRange = BIGData.getInt("ultrasonic_range");
        revRange = BIGData.getInt("rev_range");
        IRRange = BIGData.getInt("ir_range");
        BIGData.put("ball_count", 3);
    }

    private void setSpeeds(double RPM) {
        // System.out.println(aRPM + " " + bRPM);
        this.RPM = rpmToTalonVeloc(RPM);

        motor.set(ControlMode.Velocity, this.RPM);

    }

    public void update() {
        setSpeeds(BIGData.getDouble("storage_rpm"));

        // SmartDashboard.putNumber("Proximity Storage", storageDistance);
        // SmartDashboard.putNumber("Proximity Shooter", shooterDistance);

        if (intake.getValue() < ultrasonicRange && !seenBall) {
            lemonCount++;
        }

        if (intake.getValue() > ultrasonicRange) {
            seenBall = true;
        }
        if (SHOOTER.getProximity() < revRange) {
            lemonCount--;
            conveyerCount--;
        }

        if (bottom.getValue() < IRRange && !conveyerSeenBall) {
            conveyerCount++;
        }
        if (bottom.getValue() > IRRange) {
            conveyerSeenBall = true;
        }
        if (conveyerCount == 0 && middle.getValue() > IRRange) {
            // TODO Spin Motor once
        } else if (conveyerCount == 1 && middle.getValue() < IRRange && top.getValue() > IRRange) {
            // TODO Spin Motor once
        }

        BIGData.put("ball_count", lemonCount);
        SmartDashboard.putNumber("Lemon Count", BIGData.getInt("ball_count"));

    }

    public void configTalons(TalonSRX tal) {
        tal.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);
        tal.config_kP(0, 0.04);
        tal.config_kI(0, 0);
        tal.config_kD(0, 0);
        tal.config_kF(0, .009);
        tal.selectProfileSlot(0, 0);
    }

    public double rpmToTalonVeloc(double rpm) {
        return rpm * REDUCTION * TICKS_PER_ROTATION / (60 * 10);
    }

}