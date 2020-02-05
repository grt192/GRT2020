package frc.mechs;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.revrobotics.ColorSensorV3;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.I2C;
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
    private boolean seenLemon, conveyerSeenLemon, intakingLemon = false;

    private int lemonCount = 0;
    private int conveyerCount = 0;

    private boolean lemonsInCoveyer = false;
    private boolean lemonInTop, lemonInMiddle, lemonInBottom = false;

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
        BIGData.put("lemon_count", 3);
    }

    private void setSpeeds(double RPM) {
        // System.out.println(aRPM + " " + bRPM);
        this.RPM = rpmToTalonVeloc(RPM);

        motor.set(ControlMode.Velocity, this.RPM);

    }

    public void update() {
        // setSpeeds(BIGData.getDouble("storage_rpm"));
        lemonInTop = top.getValue() < IRRange;
        lemonInMiddle = middle.getValue() < IRRange;
        lemonInBottom = bottom.getValue() < IRRange;

        // SmartDashboard.putNumber("Proximity Storage", storageDistance);
        // SmartDashboard.putNumber("Proximity Shooter", shooterDistance);

        if (intake.getValue() < ultrasonicRange && !intakingLemon) {
            lemonCount++;
            intakingLemon = true;
        }

        if (intake.getValue() > ultrasonicRange) {
            intakingLemon = false;
            // seenLemon = true;
        }
        if (SHOOTER.getProximity() < revRange) {
            lemonCount--;
            conveyerCount--;
        }

        if (lemonInBottom && conveyerCount < 2) {
            conveyerCount++;
        }

        // if (bottom.getValue() > IRRange) {
        // conveyerSeenLemon = true;
        // }

        if (conveyerCount == 0 && !lemonInMiddle) {
            // TODO Spin Motor once
        }

        if (conveyerCount == 1 && !lemonInMiddle && !lemonInTop) {
            // TODO Spin Motor once
        }

        if (conveyerCount == 1 && lemonInTop && !lemonInMiddle) {
            // TODO Spin Motor once backwards
        }
        BIGData.put("lemon_count", lemonCount);
        SmartDashboard.putNumber("Lemon Count", BIGData.getInt("lemon_count"));

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