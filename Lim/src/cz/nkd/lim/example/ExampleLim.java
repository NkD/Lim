package cz.nkd.lim.example;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

import cz.nkd.vbox.VBoxLoader;
import cz.nkd.vbox.scene.VBoxScene;
import cz.nkd.vbox.scene.visitor.VBoxRenderVisitor;
import cz.nkd.vbox.tool.TouchInteractive;
import cz.nkd.vbox.tool.VBoxRenderer;

import java.util.Iterator;

/**
 * @author Michal NkD Nikodim (michal.nikodim@gmail.com)
 */
public class ExampleLim implements ApplicationListener {

    static final float BOX_STEP = 1 / 60f;
    static final long BOX_STEP_NANO = (1000000000 / 60) + 1; // 1/60 sec in nano
    static final int BOX_VELOCITY_ITERATIONS = 10;
    static final int BOX_POSITION_ITERATIONS = 4;
    static final float W2B = 0.02f;
    static final float B2W = 50;

    private float sWidth, sHeight;
    private OrthographicCamera camera;
    private SpriteBatch spriteBatch;
    private StringBuilder info;
    private BitmapFont font;
    private boolean flagMouseMiddle = true;
    private int rotGravityHelper = 0;
    private Vector2 gravity = new Vector2();
    private Vector2 gravityMem = new Vector2();

    private VBoxScene scene;
    private TouchInteractive touchInteractive;
    private VBoxRenderVisitor renderVisitor;

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

        VBoxRenderer veRenderer = new VBoxRenderer(spriteBatch, whitePixel, font);
        renderVisitor = new VBoxRenderVisitor(veRenderer);
        // renderVisitor.renderFixturesDynamic = false;
        // renderVisitor.renderFixturesKinematic = false;
        // renderVisitor.renderFixturesSensors = false;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        scene = new VBoxLoader(Gdx.files.internal("lim_export/lim_atlas.zip")).getScene();
        touchInteractive = new TouchInteractive(scene, camera);
        scene.create();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);
        camera.update();
    }

    //private long counter = 0;

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        /* counter++;
         if (counter == 600) {
             scene.findItem("Head").destroy();
             //System.out.println("DESTROY");
         } else if (counter == 1200) {
             scene.findItem("Head").create();
             //System.out.println("CREATE");
             counter = 0;
         }*/

        if (scene.computeWorldStep()) {
            scene.update();
        }

        if (Gdx.app.getType() == ApplicationType.Android) {
            if (sWidth > sHeight) {
                gravity.set(Gdx.input.getAccelerometerY(), -Gdx.input.getAccelerometerX());
            } else {
                gravity.set(-Gdx.input.getAccelerometerX(), -Gdx.input.getAccelerometerY());
            }
            if (gravityMem.dst(gravity) > 0.3f) {
                gravityMem.set(gravity);
                scene.world.setGravity(gravity);
                Array<Body> bodies = new Array<Body>();
                scene.world.getBodies(bodies);
                for (Iterator<Body> iterator = bodies.iterator(); iterator.hasNext();) {
                    Body body = iterator.next();
                    body.setAwake(true);
                }
            }
        } else {
            if (flagMouseMiddle && Gdx.input.isButtonPressed(Buttons.MIDDLE)) {
                float gX = scene.world.getGravity().y;
                float gY = scene.world.getGravity().x;
                rotGravityHelper = 1 - rotGravityHelper;
                if (rotGravityHelper == 0) {
                    gX = -gX;
                    gY = -gY;
                }

                scene.world.setGravity(new Vector2(gX, gY));
                flagMouseMiddle = false;
                Array<Body> bodies = new Array<Body>();
                scene.world.getBodies(bodies);
                for (Iterator<Body> iterator = bodies.iterator(); iterator.hasNext();) {
                    Body body = iterator.next();
                    body.setAwake(true);
                }
            }
            if (!Gdx.input.isButtonPressed(Buttons.MIDDLE))
                flagMouseMiddle = true;
        }

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        scene.visit(renderVisitor);

        spriteBatch.end();

        spriteBatch.begin();
        info.setLength(0);
        info.append("FPS: ").append(Gdx.graphics.getFramesPerSecond());
        info.append(" Bodies: ").append(scene.world.getBodyCount());
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
        scene.destroy();
        touchInteractive.dispose();
    }

}
