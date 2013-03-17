package cz.nkd.lim;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import cz.nkd.lim.example.ExampleDrawPolyLine;

/**
 * @author Michal NkD Nikodim
 *
 */
public class DrawPolyLine {
    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Lim";
        cfg.useGL20 = false;
        cfg.width = 800;
        cfg.height = 480;
        cfg.vSyncEnabled = false;
        
        new LwjglApplication(new ExampleDrawPolyLine(), cfg);
    }
}
