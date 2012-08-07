package jiongye.app.livewallpaper.mazepaper;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class PlayableMazePaperService extends BaseLiveWallpaperService implements IAccelerationListener {
	Maze maze;
		
	private int rows = 5;
	private int columns = 4;

	private static int CAMERA_WIDTH = 480;
	private static int CAMERA_HEIGHT = 720;

	private Camera camera;
	private PhysicsWorld physicsWorld;

	Human human;
	
	Rectangle endRectangle;

	BitmapTextureAtlasSource bitmapTextureAtlasSource;
	BitmapTextureAtlas bitmapTextureAtlas;
	TextureRegion bitmapTextureRegion;

	final static short WALLCATEGORY = 1;
	final static short DOORCATEGORY = 2;
	final static short PLAYERCATEGORY = 4;

	final static short WALLMASK = WALLCATEGORY + PLAYERCATEGORY;
	final static short DOORMASK = DOORCATEGORY;

	FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(1, 0, 0.2f, false, WALLCATEGORY, WALLMASK, (short) 0);
	FixtureDef doorFixtureDef = PhysicsFactory.createFixtureDef(1, 0, 0.2f, false, DOORCATEGORY, DOORMASK, (short) 0);
	FixtureDef playerFixtureDef = PhysicsFactory.createFixtureDef(10, .2f, .3f, false, WALLCATEGORY, WALLMASK, (short) 0);

	TextureRegion textureRegion;

	@Override
	public void onCreate() {
//		android.os.Debug.waitForDebugger();
		super.onCreate();
	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		wm.getDefaultDisplay().getRotation();
		CAMERA_WIDTH = displayMetrics.widthPixels;
		CAMERA_HEIGHT = displayMetrics.heightPixels;

		this.camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.camera);
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
		maze = new Maze(rows, columns);
		maze.createMaze();
		
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onSurfaceChanged(final GLState pGLState, final int pWidth, final int pHeight) {
		CAMERA_WIDTH = pWidth;
		CAMERA_HEIGHT = pHeight;
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
		this.enableAccelerationSensor(this);
		final Scene scene = new Scene();
		scene.setBackground(new Background(0, 0, 0));

		this.physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

		setUpMazeScene(scene);

		scene.registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void onUpdate(float pSecondsElapsed) {
				if (endRectangle != null) {
					if (human.collidesWith(endRectangle)) {
						endRectangle.setColor(Color.BLUE);					
						
						human.moveTo(maze.getStartCell().coord);
						maze.regenerate();
						regenerateMazeScene(scene);
					}
				}
			}

			@Override
			public void reset() {
			}

		});

		pOnCreateSceneCallback.onCreateSceneFinished(scene);
	}

	@Override
	public void onPopulateScene(Scene scene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	public void setUpMazeScene(Scene scene) {
		if (maze != null) {
			int cWidth = CAMERA_WIDTH;
			int cHeight = CAMERA_HEIGHT;
			int cellSize = cWidth < cHeight ? cWidth / maze.rows : cHeight / maze.rows;
			int verticalOffset = cHeight > cellSize * maze.rows ? (cHeight - (cellSize * maze.rows)) / 2 : 40;
			int horizontalOffset = cWidth > cellSize * maze.columns ? (cWidth - (cellSize * maze.columns)) / 2 : 0;

			Wall tempWall = null;
			Point topLeft, topRight, bottomLeft, bottomRight;
			Cell cell;

			final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();

			for (int i = 0; i < maze.rows; i++) {
				for (int j = 0; j < maze.columns; j++) {
					cell = maze.getCell(i, j);

					if (cell != null) {
						topLeft = new Point(j * cellSize + horizontalOffset, i * cellSize + verticalOffset);
						topRight = new Point((j + 1) * cellSize + horizontalOffset, i * cellSize + verticalOffset);
						bottomLeft = new Point(j * cellSize + horizontalOffset, (i + 1) * cellSize + verticalOffset);
						bottomRight = new Point((j + 1) * cellSize + horizontalOffset, (i + 1) * cellSize + verticalOffset);

						maze.getCell(i, j).setCoord(topLeft);
						
						// determine which wall gets shown
						for (CellNeighbor neighbor : CellNeighbor.values()) {
							tempWall = null;

							switch (neighbor) {
								case TOP:
									tempWall = drawWall(scene, vertexBufferObjectManager, topLeft, topRight, maze.cellPaint, neighbor,
											cell.walls.get(neighbor));
									break;
								case LEFT:
									tempWall = drawWall(scene, vertexBufferObjectManager, topLeft, bottomLeft, maze.cellPaint, neighbor,
											cell.walls.get(neighbor));
									break;
								case RIGHT:
									if (j == maze.columns - 1)
										tempWall = drawWall(scene, vertexBufferObjectManager, topRight, bottomRight, maze.cellPaint, neighbor,
												cell.walls.get(neighbor));
									break;
								case BOTTOM:
									if (i == maze.rows - 1)
										tempWall = drawWall(scene, vertexBufferObjectManager, bottomLeft, bottomRight, maze.cellPaint, neighbor,
												cell.walls.get(neighbor));
									break;
							}

							if (tempWall != null) {
								cell.wallBodies.put(neighbor, tempWall);

								if (cell.walls.get(neighbor))
									cell.wallBodies.get(neighbor).show();
								else
									cell.wallBodies.get(neighbor).hide();
							}
						}

						// this cell is the destination
						if (cell.isEnd) {
							if (endRectangle == null) {
								endRectangle = new Rectangle(topLeft.x + maze.cellStrokeWidth, topLeft.y + maze.cellStrokeWidth, cellSize - maze.cellStrokeWidth, cellSize - maze.cellStrokeWidth, vertexBufferObjectManager);
								endRectangle.setColor(Color.GREEN);
								scene.attachChild(endRectangle);
							}
						}

						if (cell.isStart) {
							if (human == null) {
								human = new Human(new Rectangle(topLeft.x + cellSize / 4, topLeft.y + cellSize / 4, cellSize / 2, cellSize / 2, vertexBufferObjectManager), Color.RED);
								
								human.body = PhysicsFactory.createBoxBody(physicsWorld, human.rectangle, BodyType.DynamicBody, this.playerFixtureDef);
								physicsWorld.registerPhysicsConnector(new PhysicsConnector(human.rectangle, human.body));
								human.rectangle.registerUpdateHandler(physicsWorld);
								scene.attachChild(human.rectangle);
							}
						}
					}
				}
			}
		}
	}

	public void regenerateMazeScene(Scene scene) {
		if(maze.regenerated){
			Cell cell;
			
			for (int i = 0; i < maze.rows; i++) {
				for (int j = 0; j < maze.columns; j++) {
					cell = maze.getCell(i, j);
					
					// determine which wall gets shown
					for (CellNeighbor neighbor : CellNeighbor.values()) {
						Wall wall = cell.wallBodies.get(neighbor);
						if(wall != null){
							if (cell.walls.get(neighbor)){
								wall.show();
//								wall.setBodyFixture(wallFixtureDef);
							} else {
								wall.hide();
//								wall.setBodyFixture(doorFixtureDef);
							}
						}
					}
				}
			}
		}
	}
	
	public Wall drawWall(Scene scene, VertexBufferObjectManager vertexBufferObjectManager, Point a, Point b, Paint p, CellNeighbor position,
			boolean visible) {
		Wall wall = null;

		// create line
		switch (position) {
			case TOP:
			case BOTTOM:
				wall = new Wall(a.x, a.y, b.x - a.x, p.getStrokeWidth(), vertexBufferObjectManager);
				
				wall.body = PhysicsFactory.createBoxBody(this.physicsWorld, wall, BodyType.StaticBody, visible ? wallFixtureDef : doorFixtureDef);
				this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(wall, wall.body));
				break;
			case LEFT:
			case RIGHT:
				wall = new Wall(a.x, a.y, p.getStrokeWidth(), b.y - a.y, vertexBufferObjectManager);

				wall.body = PhysicsFactory.createBoxBody(this.physicsWorld, wall, BodyType.StaticBody, visible ? wallFixtureDef : doorFixtureDef);
				this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(wall, wall.body));
				break;
		}

		if (wall != null) {
			scene.attachChild(wall);
			return wall;
		}
		return null;
	}

	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {

	}

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(), pAccelerationData.getY());
		this.physicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}

}
