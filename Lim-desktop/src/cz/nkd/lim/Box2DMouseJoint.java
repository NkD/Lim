package cz.nkd.lim;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import cz.nkd.lim.example.ExampleBox2DMouseJoint;

/**
 * @author Michal NkD Nikodim (michal.nikodim@gmail.com)
 */
public class Box2DMouseJoint {
    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Lim";
        //cfg.useGL20 = true;
        cfg.width = 800;
        cfg.height = 480;
        cfg.vSyncEnabled = true;
        new LwjglApplication(new ExampleBox2DMouseJoint(), cfg);
    }
}
