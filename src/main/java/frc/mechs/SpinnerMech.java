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
    /** //motor to drive the winch */
    private TalonSRX motor;
    private Solenoid sol;
    private final I2C.Port i2cPort = I2C.Port.kOnboard;
    private final ColorSensorV3 sensor = new ColorSensorV3(i2cPort);
    /** speed from -1.0 to 1.0 */

    private Map<Character, Color> colorMap = new HashMap<>();

    private final Color kBlueTarget = ColorMatch.makeColor(0.143, 0.427, 0.429);
    private final Color kGreenTarget = ColorMatch.makeColor(0.197, 0.561, 0.240);
    private final Color kRedTarget = ColorMatch.makeColor(0.561, 0.232, 0.114);
    private final Color kYellowTarget = ColorMatch.makeColor(0.361, 0.524, 0.113);

    private int stage = 2;
    private int counter = 0;
    private Color reqColor;

    private boolean firstTime = true;
    private boolean unSeen;
    private Color firstSeen = Color.kBlack;

    private final ColorMatch colorMatcher = new ColorMatch();

    private ColorMatchResult firstSeenResult;

    private int IRRange = 400;

    public SpinnerMech() {
        // System.out.print(BIGData.getInt("spinner_id"));
        motor = new TalonSRX(BIGData.getInt("spinner_id"));
        sol = new Solenoid(9, BIGData.getInt("spinner_sol"));
        // BIGData.put("Spinner?", false);
        // BIGData.put("firstTime?", true);

        colorMap.put('B', kBlueTarget);
        colorMap.put('G', kGreenTarget);
        colorMap.put('R', kRedTarget);
        colorMap.put('Y', kYellowTarget);

        colorMatcher.addColorMatch(kBlueTarget);
        colorMatcher.addColorMatch(kGreenTarget);
        colorMatcher.addColorMatch(kRedTarget);
        colorMatcher.addColorMatch(kYellowTarget);

        BIGData.putSpinnerState(false);
    }

    public void update() {
        String gameData = DriverStation.getInstance().getGameSpecificMessage();

        if (gameData.length() > 0) {
            // in stage 3
            reqColor = colorMap.get(gameData.charAt(0));
            stage = 3;
        } else {
            // in stage 2
            stage = 2;
        }

        boolean spinnerEnabled = BIGData.getSpinnerState();
        sol.set(spinnerEnabled);
        // make sure spinner is actually under the wheel
        // System.out.println(counter);
        Color detectedColor = sensor.getColor();
        ColorMatchResult result = colorMatcher.matchClosestColor(detectedColor);
        if (spinnerEnabled && sensor.getProximity() < IRRange) {

            switch (stage) {
            case 2:

                if (firstTime) {
                    System.out.println("first time seen");
                    firstTime = false;
                    firstSeen = detectedColor;
                    firstSeenResult = colorMatcher.matchClosestColor(firstSeen);
                    unSeen = false;
                } else {

                    if (!(result.color == firstSeenResult.color)) {
                        System.out.println("unseen");
                        unSeen = true;
                    }

                    if (result.color == firstSeenResult.color && unSeen) {
                        counter++;
                        firstTime = true;
                        unSeen = false;
                    }
                }

                if (counter <= 7) {
                    motor.set(ControlMode.PercentOutput, BIGData.getDouble("spinner_speed"));

                } else {
                    motor.set(ControlMode.PercentOutput, 0.0);
                    stage = 3;
                    firstTime = true;
                    BIGData.putSpinnerState(false);
                    counter = 0;
                    sol.set(false);
                }
                break;
            case 3:
                if (result.color == (reqColor)) {
                    motor.set(ControlMode.PercentOutput, 0.0);
                } else {
                    motor.set(ControlMode.PercentOutput, BIGData.getDouble("spinner_speed"));
                }
                break;
            }

        }

    }
}