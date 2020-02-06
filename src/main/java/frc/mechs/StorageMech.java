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
    private boolean lemonInTop, lemonInMiddle, lemonInBottom, intakeSeen, shooterSeen = false;

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
        boolean state = BIGData.getStorageState();
        if (state) {
            automaticControl();
        } else {
            // TODO: figure out reduction and speeds, rpm
            double speed = BIGData.getStorageSpeed();
            setSpeeds(6000 * speed);
        }
    }

    public void automaticControl() {
        // setSpeeds(BIGData.getDouble("storage_rpm"));
        lemonInTop = top.getValue() < IRRange;
        lemonInMiddle = middle.getValue() < IRRange;
        lemonInBottom = bottom.getValue() < IRRange;
        intakeSeen = intake.getValue() < ultrasonicRange;
        shooterSeen = SHOOTER.getProximity() < revRange;

        // SmartDashboard.putNumber("Proximity Storage", storageDistance);
        // SmartDashboard.putNumber("Proximity Shooter", shooterDistance);

        // only count a new lemon if not intaking one currently
        if (intakeSeen && !intakingLemon) {
            lemonCount++;
            intakingLemon = true;
        }

        // if no lemon is being intaked, set to false so new one can be counted
        if (!intakeSeen) {
            intakingLemon = false;
            // seenLemon = true;
        }

        // when balls leave through shooter, must have been counted and in conveyor
        if (shooterSeen) {
            lemonCount--;
            conveyerCount--;
        }

        // only room for two balls in conveyor
        if (lemonInBottom && conveyerCount < 2) {
            conveyerCount++;
        }

        // if (bottom.getValue() > IRRange) {
        // conveyerSeenLemon = true;
        // }

        // if only one ball in conveyor, try to get to middle storage spot
        if (conveyerCount == 1 && !lemonInMiddle) {
            // TODO Spin Motor once
        }

        // if two balls in conveyor, try to get them in top and middle spots
        if (conveyerCount == 2 && !lemonInMiddle && !lemonInTop) {
            // TODO Spin Motor once
        }

        // means that one ball got too far, reverse to maintain consistency
        if (conveyerCount == 2 && lemonInTop && !lemonInMiddle) {
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