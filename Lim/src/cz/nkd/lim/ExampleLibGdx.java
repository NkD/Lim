package cz.nkd.lim;

import java.util.Iterator;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/**
 * @author Michal NkD Nikodim
 *
 */
public class ExampleLibGdx implements ApplicationListener {

    static final float MAX_BOX_STEP = 1 / 60f;
    static final int BOX_VELOCITY_ITERATIONS = 6;
    static final int BOX_POSITION_ITERATIONS = 2;
    static final float W2B = 0.01f;
    static final float B2W = 100f;

    private World world;
    private OrthographicCamera camera;
    private boolean mouseLeft;
    private BitmapFont font;
    private SpriteBatch batch;
    private StringBuilder info;
    private long nano = -1;
    private float step = 1 / 60f;

    private Sprite sprite;
    private boolean mouseMiddle = true;
    private int rotGravity = 0;
    private Texture texture;

    @Override
    public void create() {
        font = new BitmapFont();
        info = new StringBuilder();

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        Pixmap pm = new Pixmap(32, 32, Format.RGB888);
        pm.setColor(1, 1, 1, 1);
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                pm.drawPixel(i, j);
            }
        }
        texture = new Texture(pm);
        pm.dispose();
        sprite = new Sprite(texture);

        world = new World(new Vector2(0, -10), true);
        world.setAutoClearForces(true);

        createBody(BodyType.StaticBody, 0, 0, camera.viewportWidth, 2);
        createBody(BodyType.StaticBody, 0, camera.viewportHeight - 2, camera.viewportWidth, camera.viewportHeight);
        createBody(BodyType.StaticBody, 0, 0, 2, camera.viewportHeight);
        createBody(BodyType.StaticBody, camera.viewportWidth - 2, 0, camera.viewportWidth, camera.viewportHeight);
        createBody(BodyType.StaticBody, 10, camera.viewportHeight / 2 - 20, camera.viewportWidth * 0.5f - 5, camera.viewportHeight / 2 + 20);

        float y = 500;
        for (int i = 0; i < 50; i++) {
            createBody(BodyType.DynamicBody, camera.viewportWidth - 50, y - 20, camera.viewportWidth - 30, y);
            y = y - 30;
        }
    }

    private void createBody(BodyType bodyType, float x1, float y1, float x2, float y2) {
        float xb1 = x1 * W2B;
        float yb1 = y1 * W2B;
        float xb2 = x2 * W2B;
        float yb2 = y2 * W2B;
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(new Vector2(xb1 + ((xb2 - xb1) * 0.5f), yb1 + ((yb2 - yb1) * 0.5f)));
        bodyDef.type = bodyType;
        bodyDef.bullet = false;
        Body body = world.createBody(bodyDef);
        PolygonShape box = new PolygonShape();
        box.setAsBox((xb2 - xb1) * 0.5f, (yb2 - yb1) * 0.5f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = 50f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.3f; // Make it bounce a little bit
        body.createFixture(fixtureDef);
        body.setUserData(new float[] { xb2 - xb1, yb2 - yb1, MathUtils.random(0.5f) + 0.5f, MathUtils.random(0.5f) + 0.5f, MathUtils.random(0.5f) + 0.5f });
        box.dispose();
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void render() {
        if (nano == -1) nano = System.nanoTime();

        world.step(step, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);

        long now = System.nanoTime();
        step = (System.nanoTime() - nano) / 1000000000f;
        nano = now;
        if (step > MAX_BOX_STEP) step = MAX_BOX_STEP;

        if (Gdx.app.getType() == ApplicationType.Android) {
            float gX = Gdx.input.getAccelerometerY();
            float gY = -Gdx.input.getAccelerometerX();
            Gdx.app.debug("accel", gX + ", " +gY);
            world.setGravity(new Vector2(gX, gY));
            for (Iterator<Body> iterator = world.getBodies(); iterator.hasNext();) {
                Body body = iterator.next();
                body.setAwake(true);
            }
            
        } else {
            if (mouseMiddle && Gdx.input.isButtonPressed(Buttons.MIDDLE)) {
                float gX = world.getGravity().y;
                float gY = world.getGravity().x;
                rotGravity = 1 - rotGravity;
                if (rotGravity == 0) {
                    gX = -gX;
                    gY = -gY;
                }

                world.setGravity(new Vector2(gX, gY));
                mouseMiddle = false;
                for (Iterator<Body> iterator = world.getBodies(); iterator.hasNext();) {
                    Body body = iterator.next();
                    body.setAwake(true);
                }
            }
            if (!Gdx.input.isButtonPressed(Buttons.MIDDLE)) mouseMiddle = true;
        }
        //step = 1/600f;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        //debugRenderer.render(world, new Matrix4(camera.combined).scale(B2W, B2W, B2W));

        if ((mouseLeft && Gdx.input.isButtonPressed(Buttons.LEFT)) || Gdx.input.isButtonPressed(Buttons.RIGHT)) {
            mouseLeft = Gdx.app.getType() == ApplicationType.Android;
            Ray pickRay = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
            int halfSize = 10;
            if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
                halfSize = 15;
            }

            createBody(BodyType.DynamicBody, pickRay.origin.x - halfSize, pickRay.origin.y - halfSize, pickRay.origin.x + halfSize, pickRay.origin.y + halfSize);
        }
        if (!Gdx.input.isButtonPressed(Buttons.LEFT)) {
            mouseLeft = true;
        }
        info.setLength(0);
        info.append("FPS: ").append(Gdx.graphics.getFramesPerSecond());
        info.append(" Bodies: ").append(world.getBodyCount());
        batch.begin();
        font.draw(batch, info.toString(), 3, camera.viewportHeight - 3);

        for (Iterator<Body> iterator = world.getBodies(); iterator.hasNext();) {
            Body body = iterator.next();
            float[] data = (float[]) body.getUserData();

            if (body.getType() == BodyType.StaticBody) {
                sprite.setColor(1, 0, 0, 1f);
            } else {
                if (!body.isAwake()) {
                    sprite.setColor(0.2f, 0.2f, 0.2f, 1f);
                } else {
                    sprite.setColor(data[2], data[3], data[4], 0.5f);
                }
            }
            sprite.setBounds(0, 0, data[0] * B2W, data[1] * B2W);
            sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
            sprite.setPosition(body.getPosition().x * B2W - sprite.getWidth() / 2, body.getPosition().y * B2W - sprite.getHeight() / 2);
            sprite.setRotation(body.getAngle() * MathUtils.radDeg);
            sprite.draw(batch);

        }

        /*List<Contact> contacts = world.getContactList();
        for (Contact contact : contacts) {
            WorldManifold worldManifold = contact.getWorldManifold();
            //for (Vector2 point : worldManifold.getPoints()) {
            if (worldManifold.getNumberOfContactPoints() > 0){ 
                Vector2 point = worldManifold.getPoints()[0];
                sprite.setColor(1, 1, 1, 1f);
                sprite.setBounds(0, 0, 3, 3);
                sprite.setOrigin(2, 2);
                sprite.setPosition(point.x * B2W, point.y * B2W);
                sprite.setRotation(0);
                sprite.draw(batch);
            }
        }*/
        batch.end();

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
        batch.dispose();
        texture.dispose();
        world.dispose();
    }

}
