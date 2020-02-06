package frc.mechs;

import com.ctre.phoenix.motorcontrol.ControlMode;
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

    private double storageVelocity;

    // private final int range = 400;
    private boolean intakingLemon = false;

    private int lemonCount = 0;
    private int conveyerCount = 0;

    private boolean lemonInTop, lemonInMiddle, lemonInBottom, intakeSeen, shooterSeen = false;

    public StorageMech() {
        intake = new AnalogInput(BIGData.getInt("intake_analog"));
        top = new AnalogInput(BIGData.getInt("top_analog"));
        middle = new AnalogInput(BIGData.getInt("middle_analog"));
        bottom = new AnalogInput(BIGData.getInt("bottom_analog"));
        this.motor = new TalonSRX(BIGData.getInt("storage_motor"));
        storageVelocity = BIGData.getStorageSpeedAuto();
        ultrasonicRange = BIGData.getInt("ultrasonic_range");
        revRange = BIGData.getInt("rev_range");
        IRRange = BIGData.getInt("ir_range");
        BIGData.put("lemon_count", 3);
    }

    public void update() {
        boolean state = BIGData.getStorageState();
        if (state) {
            automaticControl();
        } else {
            double speed = BIGData.getStorageSpeed();
            motor.set(ControlMode.PercentOutput, speed);
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

        // when lemon leave through shooter, must have been counted and in conveyor
        if (shooterSeen) {
            lemonCount--;
            conveyerCount--;
        }

        // only room for two lemons in conveyor
        if (lemonInBottom && conveyerCount < 2) {
            conveyerCount++;
        }

        // if (bottom.getValue() > IRRange) {
        // conveyerSeenLemon = true;
        // }

        // if only one lemon in conveyor, try to get to middle storage spot
        if (conveyerCount == 1 && !lemonInMiddle) {
            motor.set(ControlMode.PercentOutput, storageVelocity);
        } else {
            motor.set(ControlMode.PercentOutput, 0.0);
        }

        // if two lemons in conveyor, try to get them in top and middle spots
        if (conveyerCount == 2 && !lemonInMiddle && !lemonInTop) {
            motor.set(ControlMode.PercentOutput, storageVelocity);
        } else {
            motor.set(ControlMode.PercentOutput, 0.0);
        }

        // means that one lemon got too far, reverse to maintain consistency
        if (conveyerCount == 2 && lemonInTop && !lemonInMiddle) {
            motor.set(ControlMode.PercentOutput, -storageVelocity);
        } else {
            motor.set(ControlMode.PercentOutput, 0.0);
        }

        BIGData.put("lemon_count", lemonCount);
        SmartDashboard.putNumber("Lemon Count", BIGData.getInt("lemon_count"));
    }
}