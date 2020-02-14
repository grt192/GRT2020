package frc.mechs;

import java.util.Arrays;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.revrobotics.ColorSensorV3;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.gen.BIGData;

public class StorageMech implements Mech {
    // private final I2C.Port i2cPort = I2C.Port.kOnboard;
    // private final ColorSensorV3 SHOOTER = new ColorSensorV3(i2cPort);
    private AnalogInput intake, top, bottom, middle;
    private TalonSRX motor;

    private int ultrasonicRange, revRange, IRRange, IRIntakeRange, IRBotRange;

    private double storageVelocity;

    // private final int range = 400;
    private boolean intakingLemon, waitingLemon = false;
    private boolean topWaiting = false;
    private boolean ballShotCounted = false;

    private int lemonCount = 0;
    private int conveyerCount = 0;

    private boolean lemonInTop, lemonInMiddle, lemonInBottom, intakeSeen, shooterSeen = false;

    private double topMedVal, midMedVal, botMedVal, intakeMedVal;
    private double[] topArr, midArr, botArr, inArr;
    private int count;

    public StorageMech() {
        // System.out.println("STARTING STORAGE");
        // System.out.println(BIGData.getInt("intake_analog"));
        intake = new AnalogInput(BIGData.getInt("intake_analog"));
        top = new AnalogInput(BIGData.getInt("top_analog"));
        middle = new AnalogInput(BIGData.getInt("middle_analog"));
        // System.out.println(BIGData.getInt("bottom_analog"));
        bottom = new AnalogInput(BIGData.getInt("bottom_analog"));
        this.motor = new TalonSRX(BIGData.getInt("storage_motor"));
        motor.setNeutralMode(NeutralMode.Brake);
        motor.configContinuousCurrentLimit(10, 0);
        motor.configPeakCurrentLimit(15, 0);
        motor.configPeakCurrentDuration(100, 0);
        motor.enableCurrentLimit(true);
        // System.out.println("GOT PAST IR INITIAL");
        storageVelocity = BIGData.getStorageSpeedAuto();
        ultrasonicRange = BIGData.getInt("ultrasonic_range");
        // revRange = BIGData.getInt("rev_range");
        // IRRange = BIGData.getInt("ir_range");
        IRIntakeRange = 1100;
        IRBotRange = 1300;
        IRRange = 1300;
        count = 0;
        topMedVal = 0;
        botMedVal = 0;
        midMedVal = 0;
        intakeMedVal = 0;
        topArr = new double[5];
        midArr = new double[5];
        botArr = new double[5];
        inArr = new double[5];
        System.out.println(BIGData.getInt("top_analog"));
        BIGData.put("lemon_count", 0);

        System.out.println(lemonCount);
        System.out.println(conveyerCount);
    }

    public void update() {
        boolean state = BIGData.getStorageState();
        boolean disable = BIGData.getDisabled(1);
        if (disable) {
            disable();
        } else if (state) {
            automaticControl();
        } else {
            double speed = BIGData.getStorageSpeed();
            motor.set(ControlMode.PercentOutput, speed);
        }
    }

    private void findMed() {
        topArr[count % 5] = top.getValue();
        midArr[count % 5] = middle.getValue();
        botArr[count % 5] = bottom.getValue();
        inArr[count % 5] = intake.getValue();

        Arrays.sort(topArr);
        Arrays.sort(midArr);
        Arrays.sort(botArr);
        Arrays.sort(inArr);

        topMedVal = topArr[2];
        midMedVal = midArr[2];
        botMedVal = botArr[2];
        intakeMedVal = inArr[2];

        count += 1;
    }

    public void automaticControl() {
        findMed();
        // setSpeeds(BIGData.getDouble("storage_rpm"));
        lemonInTop = topMedVal > IRRange;
        lemonInMiddle = midMedVal > IRRange;
        lemonInBottom = botMedVal > IRBotRange;
        intakeSeen = intakeMedVal > IRIntakeRange;
        // shooterSeen = SHOOTER.getProximity() < revRange;

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
        // if (shooterSeen) {
        // lemonCount--;
        // conveyerCount--;
        // }

        // only room for two lemons in conveyor
        if (lemonInBottom && conveyerCount < 2 && !waitingLemon) {
            conveyerCount++;
            waitingLemon = true;
        }

        if (!lemonInBottom) {
            waitingLemon = false;
        }

        if (conveyerCount > lemonCount) {
            conveyerCount = lemonCount;
        }

        if (lemonInTop) {
            topWaiting = true;
        }

        if (topWaiting && !lemonInTop) {
            lemonCount--;
            conveyerCount--;
            topWaiting = false;
        }

        // if (BIGData.getBoolean("ball_shot") && !ballShotCounted){
        //     lemonCount--;
        //     conveyerCount--;
        //     ballShotCounted = true;
        // } else {
        //     ballShotCounted = false;
        // }

        // if (bottom.getValue() > IRRange) {
        // conveyerSeenLemon = true;
        // }

        // if only one lemon in conveyor, try to get to middle storage spot
        if (conveyerCount == 1 && !lemonInMiddle) {
            motor.set(ControlMode.PercentOutput, storageVelocity);
        } else {
            motor.set(ControlMode.PercentOutput, 0.0);
        }

        if (conveyerCount == 1 && lemonInTop) {
            motor.set(ControlMode.PercentOutput, -storageVelocity);
        }

        // if two lemons in conveyor, try to get them in top and middle spots
        if (conveyerCount == 2 && !lemonInTop) {
            motor.set(ControlMode.PercentOutput, storageVelocity);
        } else if (conveyerCount == 2){
            motor.set(ControlMode.PercentOutput, 0.0);
        }

        // if (conveyerCount == 2 && lemonInTop && !lemonInMiddle) {
        //     motor.set(ControlMode.PercentOutput, -storageVelocity);
        // }

        // means that one lemon got too far, reverse to maintain consistency
        // if (conveyerCount == 2 && lemonInTop && !lemonInMiddle) {
        // motor.set(ControlMode.PercentOutput, -storageVelocity);
        // }

        // System.out.println("count: " + count);

        // System.out.println("lemon count: " + lemonCount);
        // System.out.println("conveyor count: " + conveyerCount);

        // System.out.println("Top sensor " + lemonInTop);
        // System.out.println("Bot sensor " + lemonInBottom);
        // System.out.println("Mid sensor " + lemonInMiddle);
        //System.out.println("Intake sensor: " + intakeMedVal);

        // System.out.println("Top sensor " + topMedVal);
        // System.out.println("Bot sensor " + botMedVal);
        // System.out.println("Mid sensor " + midMedVal);
        // System.out.println("Intake sensor: " + intakeMedVal);
        // System.out.println("Top sensor " + top.getValue());
        // System.out.println("Bot sensor " + bottom.getValue());
        // System.out.println("Mid sensor " + middle.getValue());
        // System.out.println("Intake sensor " + intake.getValue());

        BIGData.put("lemon_count", lemonCount);
        SmartDashboard.putNumber("Lemon Count", BIGData.getInt("lemon_count"));
    }

    public void disable() {
        motor.set(ControlMode.Current, 0);
    }
}