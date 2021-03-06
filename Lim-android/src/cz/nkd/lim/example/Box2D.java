package cz.nkd.lim.example;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

/**
 * @author Michal NkD Nikodim (michal.nikodim@gmail.com)
 */
public class Box2D extends AndroidApplication {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        //cfg.useGL20 = true;
        cfg.useAccelerometer = true;
        cfg.useCompass = false;
        cfg.useWakelock = true;
        initialize(new ExampleBox2D(), cfg);
    }

}