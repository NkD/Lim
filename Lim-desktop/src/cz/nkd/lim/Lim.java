package cz.nkd.lim;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import cz.nkd.lim.example.ExampleLim;

/**
 * @author Michal NkD Nikodim
 *
 */
public class Lim {
    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Lim";
        cfg.useGL20 = true;
        cfg.width = 480;
        cfg.height = 800;
        cfg.vSyncEnabled = false;
        
        new LwjglApplication(new ExampleLim(), cfg);
    }
}
