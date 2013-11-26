package cz.nkd.lim.example.runner;

import android.app.Activity;
import cz.nkd.lim.example.Box2D;
import cz.nkd.lim.example.Box2DFixedTimeStep;
import cz.nkd.lim.example.Box2DMouseJoint;
import cz.nkd.lim.example.Box2DWithLight;
import cz.nkd.lim.example.Lim;


/**
 * @author Michal NkD Nikodim
 *
 */
public enum Example {

    BOX2D(Box2D.class),
    BOX2D_WITH_LIGHT(Box2DWithLight.class),
    BOX2D_MOUSEJOINT(Box2DMouseJoint.class),
    BOX2D_FIXED_TIMESTEP(Box2DFixedTimeStep.class),
    LIM(Lim.class),
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
