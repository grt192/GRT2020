package frc.mechs;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ColorMatch;
import com.revrobotics.ColorSensorV3;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.util.Color;
import frc.gen.BIGData;

public class SpinnerMech implements Mech {
    /** motor to drive the winch */
    private TalonSRX motor;
    private final I2C.Port i2cPort = I2C.Port.kOnboard;
    private final ColorSensorV3 sensor = new ColorSensorV3(i2cPort);
    /** speed from -1.0 to 1.0 */

    private final Color kBlueTarget = ColorMatch.makeColor(0.143, 0.427, 0.429);
    private final Color kGreenTarget = ColorMatch.makeColor(0.197, 0.561, 0.240);
    private final Color kRedTarget = ColorMatch.makeColor(0.561, 0.232, 0.114);
    private final Color kYellowTarget = ColorMatch.makeColor(0.361, 0.524, 0.113);

    private double speed;
    private int spinCount;
    private int mode = 0;
    private int counter = 0;

    private boolean firstTime = true;
    private Color firstSeen;

    private final ColorMatch colorMatcher = new ColorMatch();

    public SpinnerMech() {
        motor = new TalonSRX(BIGData.getInt("spinner_id"));
        BIGData.put("Spinner?", false);
        BIGData.put("firstTime?", true);
    }

    @Override
    public void update() {

        if (BIGData.getBoolean("Spinner?")) {

            if (BIGData.getBoolean("firstTime?")) {
                firstSeen = sensor.getColor();
                counter = 0;
            }
            Color detectedColor = sensor.getColor();

            switch (mode) {
            case 1:

                if (colorMatcher.matchClosestColor(detectedColor).color == colorMatcher
                        .matchClosestColor(firstSeen).color) {
                    counter++;
                }
                if (counter >= 7) {
                    mode++;
                }
                break;
            case 2:
                break;
            case 3:
                break;
            }

        }

    }
}