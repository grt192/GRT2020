package frc.mechs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.gen.BIGData;

public class StorageMech implements Mech {

    private AnalogInput intake, top, bottom, middle;
    private TalonSRX motor;

    private int IRRange, IRIntakeRange, IRBotRange;

    /** percent motor output for the conveyer when it is loading balls */
    private final double loadStorageVelocity;
    /** percent motor output for the conveyer when it is shooting balls */
    private final double shootStorageVelocity;

    /** whether in this cycle of balls, we've shot already. 
     * if we've shot already in this cycle of balls, just run the conveyer at
     * shooting speed, even if the motor speed is not completely accurate
     */
    private boolean shotInLoad = false;

    // private final int range = 400;
    private boolean intakingLemon, waitingLemon = true;
    private boolean topWaiting = false;

    private int lemonCount = 0;
    private int conveyerCount = 0;

    private boolean lemonInTop, lemonInMiddle, lemonInBottom, intakeSeen = false;

    private double topMedVal, midMedVal, botMedVal, intakeMedVal;
    private double[] topArr, midArr, botArr, inArr;
    // index of the running median arrays
    private int count;

    public StorageMech() {

        intake = new AnalogInput(BIGData.getInt("intake_analog"));
        top = new AnalogInput(BIGData.getInt("top_analog"));
        middle = new AnalogInput(BIGData.getInt("middle_analog"));
        bottom = new AnalogInput(BIGData.getInt("bottom_analog"));

        this.motor = new TalonSRX(BIGData.getInt("storage_motor"));
        motor.setNeutralMode(NeutralMode.Brake);
        motor.configContinuousCurrentLimit(10, 0);
        motor.configPeakCurrentLimit(15, 0);
        motor.configPeakCurrentDuration(100, 0);
        motor.enableCurrentLimit(true);

        lemonCount = BIGData.getInt("initial_total_lemon_count");
        conveyerCount = BIGData.getInt("initial_conveyer_lemon_count");

        loadStorageVelocity = BIGData.getStorageSpeedAuto();
        shootStorageVelocity = BIGData.getDouble("storage_speed_shoot");
        
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

        BIGData.put("lemon_count", lemonCount);
    }

    public void update() {
        // reset count if it is requested
        if (BIGData.getBoolean("reset_lemon_count")) {
            resetCount();
            BIGData.put("reset_lemon_count", false);
        }
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
        // System.out.println("lemon count: " + lemonCount);
        // System.out.println("conveyor count: " + conveyerCount);

        findMed();
        lemonInTop = topMedVal > IRRange;
        lemonInMiddle = midMedVal > IRRange;
        lemonInBottom = botMedVal > IRBotRange;
        intakeSeen = intakeMedVal > IRIntakeRange;

        // only count a new lemon if not intaking one currently
        if (intakeSeen && !intakingLemon) {
            lemonCount++;
            intakingLemon = true;
        }

        // if no lemon is being intaked, set to false so new one can be counted
        if (!intakeSeen) {
            intakingLemon = false;
        }

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
            System.out.println("BALL LEFT SYSTEM");
            waitingLemon = false;
            lemonCount--;
            conveyerCount--;
            topWaiting = false;
        }

        // what the auton code wants the shooter to run at 
        double requestedShooterSpeed = BIGData.getDouble("shooter_auto");
        // what the shooter is actually running at
        double actualShooterSpeed = BIGData.getDouble("shooter_current_rpm");
        // conveyer will run automatically when shooter is at correct rpm or if we have already shot in this cycle
        // if we've shot in this load already, make the rpm requirement less strict
        if ((shotInLoad && Math.abs(actualShooterSpeed - requestedShooterSpeed) < requestedShooterSpeed/2)
            || ((Math.abs(actualShooterSpeed - requestedShooterSpeed) < 50)
                && Math.abs(BIGData.getDouble("shooter_auto")) > 0)) {
            motor.set(ControlMode.PercentOutput, shootStorageVelocity);
            shotInLoad = true;
            return;
        } else {
            shotInLoad = false;
        }

        if ((conveyerCount == 1 && !lemonInMiddle) || (conveyerCount == 2 && !lemonInTop)) {
            motor.set(ControlMode.PercentOutput, loadStorageVelocity);
        } else {
            motor.set(ControlMode.PercentOutput, 0.0);
        }

        if (lemonInBottom && !lemonInTop) {
            motor.set(ControlMode.PercentOutput, storageVelocity);
        } else {
            motor.set(ControlMode.PercentOutput, 0.0);
        }

        correctValues();

        // System.out.println("shooter diff: "
        // + Math.abs(BIGData.getDouble("shooter_current_rpm") -

        System.out.println("Top sensor " + lemonInTop);
        System.out.println("Bot sensor " + lemonInBottom);
        System.out.println("Mid sensor " + lemonInMiddle);
        System.out.println("In sensor " + intakeSeen);
        System.out.println("waitingLemon " + waitingLemon);

        // System.out.println("Top sensor " + topMedVal);
        // System.out.println("Bot sensor " + botMedVal);
        // System.out.println("Mid sensor " + midMedVal);
        // System.out.println("In sensor: " + intakeMedVal);

        updateBigData();
    }

    private void correctValues() {
        int newConveyorCount = 0;
        int newLemonCount = 0;
        if (lemonInBottom) newLemonCount++;
        if (lemonInMiddle) {
            newLemonCount++;
            newConveyorCount++;
        }
        if (lemonInTop) {
            newLemonCount++;
            newConveyorCount++;
        }
        if (intakeSeen && newLemonCount == 3) {
            newLemonCount = 5;
        } else {
            newLemonCount = lemonCount;
        }
        conveyerCount = newConveyorCount;
        lemonCount = newLemonCount;
    }

    public void disable() {
        motor.set(ControlMode.Current, 0);
    }

    public void resetCount() {
        lemonCount = 0;
        conveyerCount = 0;
        updateBigData();
    }

    public void setCount(int lemonCount, int conveyerCount) {
        this.lemonCount = lemonCount;
        this.conveyerCount = conveyerCount;
        updateBigData();
    }

    public void updateBigData() {
        BIGData.put("lemon_count", lemonCount);
        SmartDashboard.putNumber("Lemon Count", lemonCount);
        SmartDashboard.putNumber("Conveyer Lemon Count", conveyerCount);
    }
}