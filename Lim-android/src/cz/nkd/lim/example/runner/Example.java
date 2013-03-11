package cz.nkd.lim.example.runner;

import cz.nkd.lim.example.Box2DWithLight;
import android.app.Activity;

/**
 * @author NkD
 *
 */
public enum Example {

    BOX2D_WITH_LIGHT(Box2DWithLight.class),
    D1(null),
    D2(null),
    D3(null),
    
    ;
    
    
    private final Class<? extends Activity> activityClass;

    private Example(final Class<? extends Activity> activityClass){
        this.activityClass = activityClass;
    }
    
    public Class<? extends Activity> getActivityClass(){
        return activityClass;
    }
    
}
