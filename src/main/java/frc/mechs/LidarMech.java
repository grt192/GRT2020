package frc.mechs;

import edu.wpi.first.wpilibj.PWM;
import frc.gen.BIGData;

public class LidarMech implements Mech {
    private PWM lidar;

    public LidarMech() {
        lidar = new PWM(BIGData.getInt("lidar_pwm"));
    }

    @Override
    public void update() {
        lidar.setSpeed(1);

    }
}