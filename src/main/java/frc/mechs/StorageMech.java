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

    private AnalogInput intake, intakeBack, top, bottom, middle;
    private TalonSRX motor;

    private int IRRange, IRIntakeRange, IRBotRange;

    /** percent motor output for the conveyer when it is loading balls */
    private final double loadStorageVelocity;
    /** percent motor output for the conveyer when it is shooting balls */
    private final double shootStorageVelocity;

    /**
     * whether in this cycle of balls, we've shot already. if we've shot already in
     * this cycle of balls, just run the conveyer at shooting speed, even if the
     * motor speed is not completely accurate
     */
    private boolean shotInLoad = false;

    // private final int range = 400;
    private boolean intakingLemon, waitingLemon = true;
    private boolean topWaiting = false;

    private int lemonCount = 0;
    private int conveyerCount = 0;

    private boolean lemonIntake, lemonTop, lemonMiddle, lemonBottom, lemonIntakeBack;

    private double intakeMedVal, topMedVal, middleMedVal, bottomMedVal, intakeBackMedVal;
    private double[] intakeArr, topArr, middleArr, bottomArr, intakeBackArr;
    // index of the running median arrays
    private int count;

    public StorageMech() {

        intake = new AnalogInput(BIGData.getInt("intake_analog"));
        top = new AnalogInput(BIGData.getInt("top_analog"));
        middle = new AnalogInput(BIGData.getInt("middle_analog"));
        bottom = new AnalogInput(BIGData.getInt("bottom_analog"));
        intakeBack = new AnalogInput(BIGData.getInt("intake_back_analog"));

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
        IRBotRange = 1500;
        IRRange = 1300;

        count = 0;
        intakeMedVal = 0;
        topMedVal = 0;
        middleMedVal = 0;
        bottomMedVal = 0;
        intakeBackMedVal = 0;
        intakeArr = new double[5];
        topArr = new double[5];
        middleArr = new double[5];
        bottomArr = new double[5];
        intakeBackArr = new double[5];

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
        intakeArr[count % 5] = top.getValue();
        topArr[count % 5] = middle.getValue();
        middleArr[count % 5] = bottom.getValue();
        bottomArr[count % 5] = intake.getValue();
        intakeBackArr[count % 5] = intakeBack.getValue();

        Arrays.sort(intakeArr);
        Arrays.sort(topArr);
        Arrays.sort(middleArr);
        Arrays.sort(bottomArr);
        Arrays.sort(intakeBackArr);

        intakeMedVal = intakeArr[2];
        topMedVal = topArr[2];
        middleMedVal = middleArr[2];
        bottomMedVal = bottomArr[2];
        intakeBackMedVal = intakeBackArr[2];

        count += 1;
    }

    public void automaticControl() {
        System.out.println("lemon count: " + lemonCount);
        System.out.println("conveyor count: " + conveyerCount);

        findMed();

        lemonIntake = intakeMedVal > IRRange;
        lemonTop = topMedVal > IRRange;
        lemonMiddle = middleMedVal > IRBotRange;
        lemonBottom = bottomMedVal > IRIntakeRange;
        lemonIntakeBack = intakeBackMedVal > IRIntakeRange;

        // what the auton code wants the shooter to run at
        double requestedShooterSpeed = BIGData.getDouble("shooter_auto");
        // what the shooter is actually running at
        double actualShooterSpeed = BIGData.getDouble("shooter_current_rpm");
        // conveyer will run automatically when shooter is at correct rpm or if we have
        // already shot in this cycle
        // if we've shot in this load already, make the rpm requirement less strict
        if ((shotInLoad && Math.abs(actualShooterSpeed - requestedShooterSpeed) < 100)
                || ((Math.abs(actualShooterSpeed - requestedShooterSpeed) < 50)
                        && Math.abs(BIGData.getDouble("shooter_auto")) > 0)) {
            motor.set(ControlMode.PercentOutput, shootStorageVelocity);
            shotInLoad = true;
            return;
        } else {
            shotInLoad = false;
        }

        if (!lemonMiddle && !lemonTop && lemonBottom)
            motor.set(ControlMode.PercentOutput, loadStorageVelocity);
        else if (lemonMiddle && !lemonTop && lemonBottom)
            motor.set(ControlMode.PercentOutput, loadStorageVelocity);
        else if (!lemonMiddle && lemonTop)
            motor.set(ControlMode.PercentOutput, -loadStorageVelocity);
        else
            motor.set(ControlMode.PercentOutput, 0.0);

        correctValues();

        // System.out.println("shooter diff: "
        // + Math.abs(BIGData.getDouble("shooter_current_rpm") -

        // System.out.println("Top sensor " + lemonTop);
        // System.out.println("Bot sensor " + lemonBottom);
        // System.out.println("Mid sensor " + lemonMiddle);
        // System.out.println("In sensor " + lemonIntake);
        // System.out.println("In Back sensor " + lemonIntakeBack);
        // System.out.println("In sensor " + intakeSeen);
        // System.out.println("waitingLemon " + waitingLemon);

        System.out.println("Top sensor " + top.getValue());
        System.out.println("Bot sensor " + bottom.getValue());
        System.out.println("Mid sensor " + middle.getValue());
        System.out.println("In sensor: " + intake.getValue());
        System.out.println("In Back sensor: " + intakeBack.getValue());

        updateBigData();
    }

    private void correctValues() {
        int newConveyorCount = 0;
        int newLemonCount = 0;
        if (lemonIntake)
            newLemonCount++;
        if (lemonBottom)
            newLemonCount++;
        if (lemonIntakeBack)
            newLemonCount++;
        if (lemonMiddle) {
            newLemonCount++;
            newConveyorCount++;
        }
        if (lemonTop) {
            newLemonCount++;
            newConveyorCount++;
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