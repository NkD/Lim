package cz.nkd.lim.example;

import java.util.Iterator;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import cz.nkd.veced.VecEdLoader;
import cz.nkd.veced.VecEdRenderer;
import cz.nkd.veced.geom.FixturePolygon;
import cz.nkd.veced.geom.Triangle;

/**
 * @author Michal NkD Nikodim
 *
 */
public class ExampleLim implements ApplicationListener {

    static final float BOX_STEP = 1 / 60f;
    static final long BOX_STEP_NANO = (1000000000 / 60) + 1; // 1/60 sec in nano
    static final int BOX_VELOCITY_ITERATIONS = 10;
    static final int BOX_POSITION_ITERATIONS = 4;
    static final float W2B = 0.02f;
    static final float B2W = 50;

    private World world;
    private float sWidth, sHeight;
    private OrthographicCamera camera;
    private SpriteBatch spriteBatch;
    private StringBuilder info;
    private BitmapFont font;
    private boolean flagMouseMiddle = true;
    private int rotGravityHelper = 0;
    private Vector2 gravity = new Vector2();
    private Vector2 gravityMem = new Vector2();

    private long currentTime = -1;
    private long accumulator = 0;
    private VecEdLoader loader;
    private VecEdRenderer ren;
    private Color cGreen = new Color(0,1,0,0.6f);
    
    @Override
    public void create() {
        sWidth = Gdx.graphics.getWidth();
        sHeight = Gdx.graphics.getHeight();

        font = new BitmapFont();
        info = new StringBuilder();
   
        spriteBatch = new SpriteBatch();
        
        Pixmap pm = new Pixmap(1, 1, Format.RGB888);
        pm.setColor(1, 1, 1, 1);
        pm.drawPixel(0, 0);
        TextureRegion whitePixel = new TextureRegion(new Texture(pm));
        pm.dispose();
        
        ren = new VecEdRenderer(spriteBatch, whitePixel, font);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        loader = new VecEdLoader(Gdx.files.internal("lim_test/export.json"),null);
        world = loader.world;
        world.setAutoClearForces(false);
        
        currentTime = System.nanoTime();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set( Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);
        camera.update();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        long newTime = System.nanoTime();
        long frameTime = (newTime - currentTime);
        currentTime = newTime;
        accumulator += frameTime;
        if (accumulator >= BOX_STEP_NANO) {
            world.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
            for (Entry<String, FixturePolygon> entry : loader.fixturePolygons.entries()) {
               entry.value.updatePosAlongBox2D(B2W, W2B);
            }
            accumulator -= BOX_STEP_NANO;
        }

        if (Gdx.app.getType() == ApplicationType.Android) {
            if (sWidth > sHeight) {
                gravity.set(Gdx.input.getAccelerometerY(), -Gdx.input.getAccelerometerX());
            } else {
                gravity.set(-Gdx.input.getAccelerometerX(), -Gdx.input.getAccelerometerY());
            }
            if (gravityMem.dst(gravity) > 0.3f){
                gravityMem.set(gravity);
                world.setGravity(gravity);
                Array<Body> bodies = new Array<Body>();
                world.getBodies(bodies);
                for (Iterator<Body> iterator = bodies.iterator(); iterator.hasNext();) {
                    Body body = iterator.next();
                    body.setAwake(true);
                }
            }
        } else {
            if (flagMouseMiddle && Gdx.input.isButtonPressed(Buttons.MIDDLE)) {
                float gX = world.getGravity().y;
                float gY = world.getGravity().x;
                rotGravityHelper = 1 - rotGravityHelper;
                if (rotGravityHelper == 0) {
                    gX = -gX;
                    gY = -gY;
                }

                world.setGravity(new Vector2(gX, gY));
                flagMouseMiddle = false;
                Array<Body> bodies = new Array<Body>();
                world.getBodies(bodies);
                for (Iterator<Body> iterator = bodies.iterator(); iterator.hasNext();) {
                    Body body = iterator.next();
                    body.setAwake(true);
                }
            }
            if (!Gdx.input.isButtonPressed(Buttons.MIDDLE)) flagMouseMiddle = true;
        }

        
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        ren.setColor(cGreen);
        for (Entry<String, FixturePolygon> entry : loader.fixturePolygons.entries()) {
            FixturePolygon fp = entry.value;
            for (Triangle t : fp.getTriangles()) {
                ren.triangleFill(t);
            }
        }
        
        spriteBatch.end();
                
        spriteBatch.begin();
        info.setLength(0);
        info.append("FPS: ").append(Gdx.graphics.getFramesPerSecond());
        info.append(" Bodies: ").append(world.getBodyCount());
        font.draw(spriteBatch, info.toString(), 5, camera.viewportHeight - 5);
        spriteBatch.end();
        
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        world.dispose();
    }
    
}