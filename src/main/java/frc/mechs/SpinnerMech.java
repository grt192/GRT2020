package frc.mechs;

import java.util.HashMap;
import java.util.Map;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.util.Color;
import frc.gen.BIGData;

public class SpinnerMech implements Mech {
    /** motor to drive the spinner */
    private TalonSRX motor;
    /** solenoid to toggle position of spinner (up or down) */
    private Solenoid sol;

    /** I2C port for the color sensor */
    private final I2C.Port i2cPort;
    /** color sensor for spinner */
    private final ColorSensorV3 sensor;

    /** map of characters ('B', 'G', 'R', 'Y') to their corresponding Colors */
    private Map<Character, Color> colorMap;

    // colors on the wheel
    private final Color kBlueTarget = ColorMatch.makeColor(0.143, 0.427, 0.429);
    private final Color kGreenTarget = ColorMatch.makeColor(0.197, 0.561, 0.240);
    private final Color kRedTarget = ColorMatch.makeColor(0.561, 0.232, 0.114);
    private final Color kYellowTarget = ColorMatch.makeColor(0.361, 0.524, 0.113);

    private Stage stage;
    private int counter = 0;
    private Color reqColor;

    /** Whether we have loaded the first color for rotation control */
    private boolean loadedFirstColor;
    /** Whether we are on the first color that we loaded for rotation control */
    private boolean onFirstColor;
    /**
     * The color that we started position control with. We use the number of times
     * this color passes by to determine the number of rotations we are at
     */
    private Color firstColor;

    private final ColorMatch colorMatcher;

    private int IRRange = 400;

    public SpinnerMech() {
        i2cPort = I2C.Port.kOnboard;
        sensor = new ColorSensorV3(i2cPort);
        motor = new TalonSRX(BIGData.getInt("spinner_motor_id"));
        motor.configFactoryDefault();
        sol = new Solenoid(BIGData.getInt("pcm_id"), BIGData.getInt("spinner_sol"));

        // initialize the color mappings
        colorMap = new HashMap<Character, Color>();
        colorMap.put('B', kBlueTarget);
        colorMap.put('G', kGreenTarget);
        colorMap.put('R', kRedTarget);
        colorMap.put('Y', kYellowTarget);

        // add the colors to the color matcher
        colorMatcher = new ColorMatch();
        colorMatcher.addColorMatch(kBlueTarget);
        colorMatcher.addColorMatch(kGreenTarget);
        colorMatcher.addColorMatch(kRedTarget);
        colorMatcher.addColorMatch(kYellowTarget);

        // initial stage is position control
        stage = Stage.ROTATION_CONTROL;

        // we don't have an starting color for the color wheel position control
        loadedFirstColor = false;
        onFirstColor = false;
        firstColor = Color.kBlack;

        BIGData.putSpinnerState(false);
    }

    public void update() {
        // if the spinner is up; false=up, true=down
        boolean state = BIGData.getSpinnerState();
        sol.set(state);
        boolean useManual = BIGData.getUseManualSpinner();
        if (!state) {
            // if we are up, we are allowed to do stuff
            if (useManual) {
                System.out.println("setting spinner to " + BIGData.getManualSpinnerSpeed());
                motor.set(ControlMode.PercentOutput, BIGData.getManualSpinnerSpeed());
            } else {
                automaticControl();
            }
        }
    }

    private void automaticControl() {
        String gameData = DriverStation.getInstance().getGameSpecificMessage();

        if (gameData.length() > 0) {
            // in stage 3, position control
            reqColor = colorMap.get(gameData.charAt(0));
            stage = Stage.POSITION_CONTROL;
        } else {
            // in stage 2, rotation control
            stage = Stage.ROTATION_CONTROL;
        }

        // make sure spinner is actually under the wheel
        ColorMatchResult result = colorMatcher.matchClosestColor(sensor.getColor());
        if (sensor.getProximity() < IRRange) {
            switch (stage) {
            case ROTATION_CONTROL:
                if (!loadedFirstColor) {
                    // if we have not loaded a first color, load the current color as the first
                    // color
                    System.out.println("loading the first color for rotation control...");
                    loadedFirstColor = true;
                    firstColor = result.color;
                    onFirstColor = true;
                } else {
                    // if we have loaded a first color already
                    if (!result.color.equals(firstColor) && onFirstColor) {
                        // if we just moved off the first color
                        onFirstColor = false;
                    } else if (result.color.equals(firstColor) && !onFirstColor) {
                        // if we just moved onto the first color
                        counter++;
                        onFirstColor = true;
                    }
                }

                if (counter <= 7) {
                    motor.set(ControlMode.PercentOutput, BIGData.getDouble("auto_spinner_speed"));

                } else {
                    motor.set(ControlMode.PercentOutput, 0.0);
                    loadedFirstColor = false;
                    counter = 0;
                    BIGData.setUseManualSpinner(true);
                }
                break;
            case POSITION_CONTROL:
                if (result.color == (reqColor)) {
                    motor.set(ControlMode.PercentOutput, 0.0);
                } else {
                    motor.set(ControlMode.PercentOutput, BIGData.getDouble("auto_spinner_speed"));
                }
                break;
            default:
                motor.set(ControlMode.PercentOutput, 0.0);
            }
        }
    }

    enum Stage {
        /** rotate the control panel 3-5 times */
        ROTATION_CONTROL,
        /** rotate to a given color */
        POSITION_CONTROL;
    }
}