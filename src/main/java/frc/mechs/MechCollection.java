package frc.mechs;

public class MechCollection {

    private OneWheelShooter one;
    private TwoWheelShooter two;

    public MechCollection() {
        this.one = new OneWheelShooter();
        this.two = new TwoWheelShooter();
    }

    public void update() {
        one.update();
        two.updateSpeeds();
    }
}