package frc.gen;

import edu.wpi.first.wpilibj.Notifier;
import frc.mechs.TwoWheelShooter;
public class Brain implements Runnable {
    TwoWheelShooter shoot;
    Notifier notif;
    public Brain() {
        shoot = new TwoWheelShooter();
        notif = new Notifier(this);
        notif.startPeriodic(0.1);
    }
    public void run() {
        shoot.updateSpeeds();
    }
}