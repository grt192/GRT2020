package frc.mechs;

import java.util.ArrayList;

public class MechCollection {
    private ArrayList<Mech> mechs;

    public MechCollection() {
        mechs = new ArrayList<>();
        mechs.add(new IntakeMech());
        mechs.add(new ShooterMech());
        mechs.add(new StorageMech());
        mechs.add(new WinchMech());
        // mechs.add(new LidarMech());
        mechs.add(new LinkageMech());
        mechs.add(new SpinnerMech());
        mechs.add(new LightRingMech());
    }

    public void update() {
        for (Mech m : mechs) {
            m.update();
        }
    }
}