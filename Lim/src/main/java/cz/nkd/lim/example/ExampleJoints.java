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

import cz.nkd.vbox.VBoxZipLoader;
import cz.nkd.vbox.scene.VBoxScene;
import cz.nkd.vbox.scene.visitor.VBoxRenderVisitor;
import cz.nkd.vbox.tool.VBoxDragItemsByTouch;
import cz.nkd.vbox.tool.VBoxRenderer;

import java.util.Iterator;

/**
 * @author Michal NkD Nikodim (michal.nikodim@gmail.com)
 */
public class ExampleJoints implements ApplicationListener {

    private OrthographicCamera camera;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private VBoxScene vboxScene;
    private VBoxRenderVisitor vboxRenderVisitor;

    @Override
    public void create() {
        font = new BitmapFont();
        info = new StringBuilder();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteBatch = new SpriteBatch();
        vboxRenderVisitor = new VBoxRenderVisitor(createVBoxRenderer(spriteBatch, font));
        vboxScene = new VBoxZipLoader(Gdx.files.internal("joints.zip")).getScene();
        vboxScene.create();
        VBoxDragItemsByTouch.create(vboxScene, camera, true);
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render() {
        if (vboxScene.computeWorldStep()) {
            vboxScene.update();
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.begin();
            vboxScene.visit(vboxRenderVisitor);
            spriteBatch.end();
            drawInfo();
            inspectDeviceRotation();
        }
    }

    @Override
    public void pause() {
        // nothing

    }

    @Override
    public void resume() {
        // nothing
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        vboxScene.destroy();
    }

    private VBoxRenderer createVBoxRenderer(SpriteBatch sb, BitmapFont font) {
        Pixmap pm = new Pixmap(1, 1, Format.RGB888);
        pm.setColor(1, 1, 1, 1);
        pm.drawPixel(0, 0);
        TextureRegion whitePixel = new TextureRegion(new Texture(pm));
        pm.dispose();
        return new VBoxRenderer(sb, whitePixel, font);
    }

    private boolean flagMouseMiddle = true;
    private int rotGravityHelper = 0;
    private Vector2 gravity = new Vector2();
    private Vector2 gravityMem = new Vector2();

    private void inspectDeviceRotation() {
        if (Gdx.app.getType() == ApplicationType.Android) {
            if (Gdx.graphics.getWidth() > Gdx.graphics.getHeight()) {
                gravity.set(Gdx.input.getAccelerometerY(), -Gdx.input.getAccelerometerX());
            } else {
                gravity.set(-Gdx.input.getAccelerometerX(), -Gdx.input.getAccelerometerY());
            }
            if (gravityMem.dst(gravity) > 0.3f) {
                gravityMem.set(gravity);
                vboxScene.world.setGravity(gravity);
                Array<Body> bodies = new Array<Body>();
                vboxScene.world.getBodies(bodies);
                for (Iterator<Body> iterator = bodies.iterator(); iterator.hasNext();) {
                    Body body = iterator.next();
                    body.setAwake(true);
                }
            }
        } else {
            if (flagMouseMiddle && Gdx.input.isButtonPressed(Buttons.MIDDLE)) {
                float gX = vboxScene.world.getGravity().y;
                float gY = vboxScene.world.getGravity().x;
                rotGravityHelper = 1 - rotGravityHelper;
                if (rotGravityHelper == 0) {
                    gX = -gX;
                    gY = -gY;
                }
                vboxScene.world.setGravity(new Vector2(gX, gY));
                flagMouseMiddle = false;
                Array<Body> bodies = new Array<Body>();
                vboxScene.world.getBodies(bodies);
                for (Iterator<Body> iterator = bodies.iterator(); iterator.hasNext();) {
                    Body body = iterator.next();
                    body.setAwake(true);
                }
            }
            if (!Gdx.input.isButtonPressed(Buttons.MIDDLE)) flagMouseMiddle = true;
        }
    }

    private StringBuilder info;

    private void drawInfo() {
        spriteBatch.begin();
        info.setLength(0);
        info.append("FPS: ").append(Gdx.graphics.getFramesPerSecond());
        info.append(" Bodies: ").append(vboxScene.world.getBodyCount());
        font.draw(spriteBatch, info.toString(), camera.position.x - (camera.viewportWidth / 2), camera.position.y + (camera.viewportHeight / 2));
        spriteBatch.end();
    }

}
