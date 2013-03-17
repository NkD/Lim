package cz.nkd.lim.example;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Vector3;

/**
 * @author Michal NkD Nikodim
 *
 */
public class ExampleDrawPolyLine implements ApplicationListener {

    private int sWidth;
    private int sHeight;
    private OrthographicCamera camera;

    private List<Vector3> points = new ArrayList<Vector3>();
    private BitmapFont font;
    private StringBuilder info;
    private SpriteBatch batch;
    private ImmediateModeRenderer20 ren;
    private Mesh lineMesh;
    private float[] lineVertices;
    private int vertexIndex = 0;
    private int MAX_LINES = 1000;
    private Mesh pointMesh;

    @Override
    public void create() {
        sWidth = Gdx.graphics.getWidth();
        sHeight = Gdx.graphics.getHeight();

        font = new BitmapFont();
        info = new StringBuilder();
        batch = new SpriteBatch();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        lineMesh = new Mesh(false, MAX_LINES * 2, 0, new VertexAttribute(Usage.Position, 2, "a_pos"));
        
        lineVertices = new float[MAX_LINES * 2 * 2];

        pointMesh = new Mesh(false, 8, 0, new VertexAttribute(Usage.Position, 2, "a_pos"));
        
        lineVertices[0] = 0;
        lineVertices[1] = 0;
        
        lineVertices[2] = 5;
        lineVertices[3] = 0;
        
        lineVertices[4] = 5;
        lineVertices[5] = 5;
        
        lineVertices[6] = 0;
        lineVertices[7] = 5;
        
        pointMesh.setVertices(lineVertices, 0, 8);
        
        
        Gdx.input.setInputProcessor(new InputProcessor() {

            private Vector3 point;
            private boolean flagStart = false;
            private boolean flagDragged;

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == Buttons.LEFT && !flagDragged) {
                    point = new Vector3(screenX, screenY, 0);
                    camera.unproject(point);
                    lineVertices[vertexIndex++] = point.x;
                    lineVertices[vertexIndex++] = point.y;
                    lineMesh.setVertices(lineVertices, 0, vertexIndex);
                    flagStart = true;
                    return true;
                } else if (button == Buttons.LEFT && flagDragged) {
                    flagDragged = false;
                    flagStart = true;
                } else if (button == Buttons.RIGHT && flagDragged) {
                    flagDragged = false;
                    flagStart = false;
                    vertexIndex -= 2;
                    lineMesh.setVertices(lineVertices, 0, vertexIndex);
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {

                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (flagStart) {
                    point = new Vector3(screenX, screenY, 0);
                    camera.unproject(point);
                    lineVertices[vertexIndex++] = point.x;
                    lineVertices[vertexIndex++] = point.y;
                    lineMesh.setVertices(lineVertices, 0, vertexIndex);
                    flagStart = false;
                    flagDragged = true;
                    return true;
                } else if (flagDragged) {
                    point = new Vector3(screenX, screenY, 0);
                    camera.unproject(point);
                    lineVertices[vertexIndex - 2] = point.x;
                    lineVertices[vertexIndex - 1] = point.y;
                    lineMesh.setVertices(lineVertices, 0, vertexIndex);
                    return true;
                }
                return false;
            }

            @Override
            public boolean scrolled(int amount) {
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                touchDragged(screenX, screenY, 0);
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
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        //camera.update();
        camera.apply(Gdx.gl10);
        if (vertexIndex >= 4) lineMesh.render(GL10.GL_LINE_STRIP);
        pointMesh.render(GL10.GL_LINE_LOOP);
        
        batch.begin();
        info.setLength(0);
        info.append("FPS: ").append(Gdx.graphics.getFramesPerSecond());
        font.draw(batch, info.toString(), 5, camera.viewportHeight - 5);

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
        // TODO Auto-generated method stub

    }

}
