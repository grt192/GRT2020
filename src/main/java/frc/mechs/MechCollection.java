package frc.mechs;

public class MechCollection {

    private OneWheelShooter one;
    private TwoWheelShooter two;
    private Storage storage;

    public MechCollection() {
        this.one = new OneWheelShooter();
        this.two = new TwoWheelShooter();
        this.storage = new Storage();

    }

    public void update() {
        one.update();
        two.updateSpeeds();
        storage.update();
    }
}