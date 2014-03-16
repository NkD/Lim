package cz.nkd.lim;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import cz.nkd.lim.example.ExampleBox2D;

/**
 * @author Michal NkD Nikodim
 *
 */
public class Box2D {
    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Lim";
        //cfg.useGL20 = true;
        cfg.width = 800;
        cfg.height = 480;
        cfg.vSyncEnabled = false;
        
        new LwjglApplication(new ExampleBox2D(), cfg);
    }
}
