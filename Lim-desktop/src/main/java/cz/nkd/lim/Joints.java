package cz.nkd.lim;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import cz.nkd.lim.example.ExampleJoints;

/**
 * @author Michal NkD Nikodim (michal.nikodim@gmail.com)
 */
public class Joints {
    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Lim";
        //cfg.useGL20 = true;
        cfg.width = 1650;
        cfg.height = 1050;
        cfg.vSyncEnabled = false;

        new LwjglApplication(new ExampleJoints(), cfg);
    }
}
