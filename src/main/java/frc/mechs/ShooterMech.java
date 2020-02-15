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

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.controller.SimpleMotorFeedforward;
import frc.gen.BIGData;

public class ShooterMech implements Mech {
    // lookup table rpms for shooting in increments of 1 foot.
    // rpmTable[i] is the rpm to shoot at, i feet away from target (horizontal
    // distance)
    private int[] rpmTable;
    private static final int DEFAULT_MIN_RPM = 2000;
    private static final int DEFAULT_MAX_RPM = 6000;

    private CANSparkMax motor;
    private CANEncoder encoder;
    private CANPIDController pid;
    private SimpleMotorFeedforward smff;
    private double kP, kI, kFF, kMaxOutput, kMinOutput;
    private Solenoid hood;
    private boolean shooterUp;

    public ShooterMech() {
        System.out.println(BIGData.getInt("one_wheel_shooter"));
        this.motor = new CANSparkMax(BIGData.getInt("one_wheel_shooter"), MotorType.kBrushless);
        motor.setSmartCurrentLimit(10);
        motor.setSecondaryCurrentLimit(15);
        this.pid = motor.getPIDController();
        smff = new SimpleMotorFeedforward(0.0669, 0.133, 0.131);
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
        rpmTable = new int[50];
        String rpmFileName = BIGData.getString("shooter_rpm_file");
        SortedMap<Integer, Integer> rpmMap = new TreeMap<Integer, Integer>();
        String directory = "/home/lvuser/deploy";
        try {
            Scanner in = new Scanner(new File(directory, rpmFileName));
            while (in.hasNextLine()) {
                String line = in.nextLine().trim();
                if (line.length() > 0 && line.charAt(0) != '#') {
                    String[] split = line.split(",");
                    try {
                        int a = Integer.parseInt(split[0]);
                        int b = Integer.parseInt(split[1]);
                        System.out.println("loaded two ints: " + a + "," + b);
                        rpmMap.put(a, b);
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
            if (rpmMap.size() < 2) {
                rpmMap.put(0, DEFAULT_MIN_RPM);
                rpmMap.put(rpmTable.length - 1, DEFAULT_MAX_RPM);
            }
        }
        if (!rpmMap.containsKey(0)) {
            rpmMap.put(0, DEFAULT_MIN_RPM);
        }
        if (!rpmMap.containsKey(rpmTable.length - 1)) {
            rpmMap.put(rpmTable.length - 1, DEFAULT_MAX_RPM);
        }
        // linear interpolation
        int prevIndex = 0;
        int prevNum = rpmMap.get(prevIndex);
        for (Map.Entry<Integer, Integer> e : rpmMap.entrySet()) {
            if (e.getKey() < rpmTable.length) {
                interpolate(rpmTable, prevIndex, e.getKey(), prevNum, e.getValue());
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
     * @param range
     *                  the distance from the target in inches
     * @return the rpm to run the shooter at
     */
    public double calcSpeed(double range) {
        return rpmTable[(int) (range / 12)];
    }

    public double getSpeed() {
        return encoder.getVelocity();
    }

    public void disable() {
        motor.close();
    }
}