package frc.mechs;

public class MechCollection {
    private StorageMech storage;
    private ShooterMech shooter;
    public MechCollection() {
        this.shooter = new ShooterMech();
        this.storage = new StorageMech();

    }

    public void update() {
        shooter.update();
        storage.update();
    }
}