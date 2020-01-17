package frc.position;

import frc.gen.BIGData;

public class DeadReckPosition {

    private double enc_x, enc_y;
    private double t_prev;

    public DeadReckPosition() {
        enc_x = 0;
        enc_y = 0;
        t_prev = System.currentTimeMillis();
    }

    public void update() {
        long t_curr = System.currentTimeMillis();
        double dt = (t_curr - t_prev) / 1000.0;
        enc_x += BIGData.getDouble("enc_vx") * dt;
        enc_y += BIGData.getDouble("enc_vy") * dt;

        BIGData.setEstPos(enc_x, enc_y);
        t_prev = t_curr;
    }
}