package cz.nkd.lim.example;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import cz.nkd.lim.rayhandler.PointLight;
import cz.nkd.lim.rayhandler.RayHandler;

import java.util.Iterator;

/**
 * @author Michal NkD Nikodim
 *
 */
public class ExampleBox2DWithLight implements ApplicationListener {

    static final float BOX_STEP = 1 / 60f;
    static final long BOX_STEP_NANO = (1000000000 / 60) + 1; // 1/60 sec in nano
    static final int BOX_VELOCITY_ITERATIONS = 6;
    static final int BOX_POSITION_ITERATIONS = 2;
    static final float W2B = 0.01f;
    static final float B2W = 100;

    private World world;
    private float sWidth, sHeight;
    private OrthographicCamera camera;
    private Sprite sprite;
    private Texture texture;
    private RayHandler rayHandler;
    private SpriteBatch spriteBatch;
    private StringBuilder info;
    private BitmapFont font;
    private boolean flagMouseLeft = true;
    private boolean flagMouseMiddle = true;
    private int rotGravityHelper = 0;
    private Body lightBody;
    private ShapeRenderer shapeRenderer;
    private Vector2 gravity = new Vector2();
    private Vector2 gravityMem = new Vector2();

    private long currentTime = -1;
    private long accumulator = 0;
    
    @Override
    public void create() {
        sWidth = Gdx.graphics.getWidth();
        sHeight = Gdx.graphics.getHeight();

        font = new BitmapFont();
        info = new StringBuilder();
        shapeRenderer = new ShapeRenderer();

        spriteBatch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        Pixmap pm = new Pixmap(1, 1, Format.RGB888);
        pm.setColor(1, 1, 1, 1);
        pm.drawPixel(0, 0);
        texture = new Texture(pm);
        pm.dispose();

        sprite = new Sprite(texture);

        world = new World(new Vector2(0, -10), true);
        world.setAutoClearForces(false);

        createBody(BodyType.StaticBody, 0, 0, sWidth, 2, 1, 0, 0);
        createBody(BodyType.StaticBody, 0, sHeight - 2, sWidth, sHeight, 1, 0, 0);
        createBody(BodyType.StaticBody, 0, 0, 2, sHeight, 1, 0, 0);
        createBody(BodyType.StaticBody, sWidth - 2, 0, sWidth, sHeight, 1, 0, 0);
        createBody(BodyType.StaticBody, 80, sHeight / 2 - 20, sWidth * 0.5f - 5, sHeight / 2 + 20, 1, 0, 0);

        float y = 500;
        for (int i = 0; i < 50; i++) {
            float r = MathUtils.random(0.5f) + 0.5f;
            float g = MathUtils.random(0.5f) + 0.5f;
            float b = MathUtils.random(0.5f) + 0.5f;
            createBody(BodyType.DynamicBody, sWidth - 200, y - 20, sWidth - 180, y, r, g, b);
            y = y - 30;
        }

        rayHandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        rayHandler.setAmbientLight(1f, 1f, 1f, 0.4f);
        PointLight pl = new PointLight(rayHandler, 500, new Color(1, 1, 1, 0.7f), 10, 200, 200, B2W);
        lightBody = createLight(camera.viewportWidth / 2 - 20, 20, 20);
        pl.attachToBody(lightBody, 0, 0);
        
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
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        long newTime = System.nanoTime();
        long frameTime = (newTime - currentTime);
        currentTime = newTime;
        accumulator += frameTime;
        if (accumulator >= BOX_STEP_NANO) {
            world.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
            rayHandler.setCombinedMatrix(camera.combined, camera.position.x, camera.position.y, camera.viewportWidth * camera.zoom, camera.viewportHeight * camera.zoom);
            rayHandler.update();
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

        if ((flagMouseLeft && Gdx.input.isButtonPressed(Buttons.LEFT)) || Gdx.input.isButtonPressed(Buttons.RIGHT)) {
            flagMouseLeft = Gdx.app.getType() == ApplicationType.Android;
            Ray pickRay = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
            int halfSize = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) ? 15 : 10;
            float r = MathUtils.random(0.5f) + 0.5f;
            float g = MathUtils.random(0.5f) + 0.5f;
            float b = MathUtils.random(0.5f) + 0.5f;
            createBody(BodyType.DynamicBody, pickRay.origin.x - halfSize, pickRay.origin.y - halfSize, pickRay.origin.x + halfSize, pickRay.origin.y + halfSize, r, g, b);
        }
        if (!Gdx.input.isButtonPressed(Buttons.LEFT)) flagMouseLeft = true;

       rayHandler.render();
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        Array<Body> bodies = new Array<Body>();
        world.getBodies(bodies);
        for (Iterator<Body> iterator = bodies.iterator(); iterator.hasNext();) {
            Body body = iterator.next();
            if (body != lightBody) {
                float[] data = (float[]) body.getUserData();

                sprite.setBounds(0, 0, data[0] * B2W, data[1] * B2W);
                sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
                sprite.setPosition(body.getPosition().x * B2W - sprite.getWidth() / 2, body.getPosition().y * B2W - sprite.getHeight() / 2);
                sprite.setRotation(body.getAngle() * MathUtils.radDeg);
                float alpha = body.isAwake() || body.getType() == BodyType.StaticBody ? 0.9f : 0.6f;
                sprite.setColor(data[2], data[3], data[4], alpha);
                sprite.setScale(1.0f);
                sprite.draw(spriteBatch);
            }

        }
        spriteBatch.end();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.circle(lightBody.getPosition().x * B2W, lightBody.getPosition().y * B2W, lightBody.getFixtureList().get(0).getShape().getRadius() * B2W);
        shapeRenderer.end();
        
        spriteBatch.begin();
        info.setLength(0);
        info.append("FPS: ").append(Gdx.graphics.getFramesPerSecond());
        info.append(" Bodies: ").append(world.getBodyCount());
        sprite.setBounds(0, 0, 150, 18);
        sprite.setPosition(3, sHeight - 21);
        sprite.setColor(0, 0, 0, 0.6f);
        sprite.setRotation(0);
        sprite.draw(spriteBatch);
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
        shapeRenderer.dispose();
        spriteBatch.dispose();
        texture.dispose();
        world.dispose();
    }

    private Body createBody(BodyType bodyType, float x1, float y1, float x2, float y2, float red, float green, float blue) {
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
        body.setUserData(new float[] { xb2 - xb1, yb2 - yb1, red, green, blue });
        box.dispose();
        return body;
    }

    private Body createLight(float x, float y, float radius) {
        float xb = x * W2B;
        float yb = y * W2B;
        float radiusb = radius * W2B;
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(new Vector2(xb * 0.5f, yb * 0.5f));
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.bullet = true;
        bodyDef.gravityScale = -0.1f;
        Body body = world.createBody(bodyDef);
        CircleShape circle = new CircleShape();
        circle.setRadius(radiusb);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 50000f;
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.3f; // Make it bounce a little bit
        body.createFixture(fixtureDef);
        body.setUserData(new float[] { radiusb, radiusb, 1, 1, 1 });
        circle.dispose();
        return body;
    }
}
