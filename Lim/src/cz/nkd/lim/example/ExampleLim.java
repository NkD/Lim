package cz.nkd.lim.example;

import java.util.Iterator;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import cz.nkd.veced.VecEdLoader;
import cz.nkd.veced.VecEdRenderer;
import cz.nkd.veced.geom.FixturePolygon;
import cz.nkd.veced.geom.Tex;
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
    private Color cGreen = new Color(0, 1, 0, 0.6f);

    private Vector3 touchTestPoint = new Vector3();
    private Vector2 touchTarget = new Vector2();
    private Body touchedBody = null;
    private Body groundBody;

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

        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("lim_test/export.atlas"));

        loader = new VecEdLoader(Gdx.files.internal("lim_test/export.json"), atlas);
        world = loader.world;
        world.setAutoClearForces(false);
        BodyDef bd = new BodyDef();
        bd.type = BodyType.StaticBody;
        groundBody = world.createBody(bd);

        final QueryCallback callback = new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                if (fixture.getBody().getType() == BodyType.StaticBody) return true;
                if (fixture.testPoint(touchTestPoint.x, touchTestPoint.y)) {
                    touchedBody = fixture.getBody();
                    return false;
                } else
                    return true;
            }
        };

        Gdx.input.setInputProcessor(new InputProcessor() {

            private MouseJoint mouseJoint;

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                //camera.unproject(touchTestPoint.set(screenX, screenY, 0));
                System.out.println("touchDown");
                if (button == Input.Buttons.LEFT) {
                    Ray pickRay = camera.getPickRay(screenX, screenY);
                    touchTestPoint.set(pickRay.origin.x * W2B, pickRay.origin.y * W2B, 0);
                    System.out.println(pickRay.origin.x + ", " + pickRay.origin.y);
                    world.QueryAABB(callback, touchTestPoint.x - 0.1f, touchTestPoint.y - 0.1f, touchTestPoint.x + 0.1f, touchTestPoint.y + 0.1f);
                    if (touchedBody != null) {
                        MouseJointDef def = new MouseJointDef();
                        def.bodyA = groundBody;
                        def.bodyB = touchedBody;
                        def.collideConnected = true;
                        def.target.set(touchTestPoint.x, touchTestPoint.y);
                        def.maxForce = 200.0f * touchedBody.getMass();
                        mouseJoint = (MouseJoint) world.createJoint(def);
                        touchedBody.setAwake(true);
                    }
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (mouseJoint != null) {
                    Ray pickRay = camera.getPickRay(screenX, screenY);
                    touchTestPoint.set(pickRay.origin.x * W2B, pickRay.origin.y * W2B, 0);
                    mouseJoint.setTarget(touchTarget.set(touchTestPoint.x, touchTestPoint.y));
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    if (mouseJoint != null) {
                        if (!Gdx.input.isButtonPressed(Buttons.RIGHT)) world.destroyJoint(mouseJoint);
                        mouseJoint = null;
                    }
                    touchedBody = null;
                }
                return false;
            }

            @Override
            public boolean scrolled(int amount) {
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }

            @Override
            public boolean keyUp(int keycode) {
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                return false;
            }
        });

        currentTime = System.nanoTime();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);
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
            for (FixturePolygon fp : loader.fixturePolygons) {
                fp.updatePosAlongBox2D(B2W, W2B);
            }
            for (Tex tex : loader.texs) {
                tex.updatePosAlongBox2D(B2W, W2B);
            }
            accumulator -= BOX_STEP_NANO;
        }

        if (Gdx.app.getType() == ApplicationType.Android) {
            if (sWidth > sHeight) {
                gravity.set(Gdx.input.getAccelerometerY(), -Gdx.input.getAccelerometerX());
            } else {
                gravity.set(-Gdx.input.getAccelerometerX(), -Gdx.input.getAccelerometerY());
            }
            if (gravityMem.dst(gravity) > 0.3f) {
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
        for (FixturePolygon fp : loader.fixturePolygons) {
            for (Triangle t : fp.getTriangles()) {
                ren.triangleFill(t);
            }
        }
        for (Tex tex : loader.texs) {
            tex.draw(spriteBatch);
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
