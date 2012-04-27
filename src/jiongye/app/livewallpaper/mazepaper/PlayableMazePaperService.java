package jiongye.app.livewallpaper.mazepaper;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class PlayableMazePaperService extends BaseLiveWallpaperService implements IAccelerationListener {
	Maze maze;

	private int rows = 10;
	private int columns = 7;

	private static int CAMERA_WIDTH = 480;
	private static int CAMERA_HEIGHT = 720;

	private Camera camera;
	private PhysicsWorld physicsWorld;
	
	Rectangle playerRectangle;
	
	BitmapTextureAtlasSource bitmapTextureAtlasSource;
	BitmapTextureAtlas bitmapTextureAtlas;
	TextureRegion bitmapTextureRegion;
	
	FixtureDef wallFixtureDef;
	FixtureDef playerFixtureDef;
	
	TextureRegion textureRegion;
	
	@Override
	public void onCreate(){
//		 android.os.Debug.waitForDebugger();
		super.onCreate();
	}
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
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
		maze.player = new Player();
		maze.player.radius = 5;
		maze.createMaze();
		
		this.wallFixtureDef = PhysicsFactory.createFixtureDef(.5f, .1f, .5f);
		this.playerFixtureDef = PhysicsFactory.createFixtureDef(.5f, .1f, .5f);
		
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
		Scene scene = new Scene();
		scene.setBackground(new Background(0, 0, 0));
		
		this.physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		
		if (maze != null) {
			double distanceLimit = 300;
			int lineCount = 0;
			int cWidth = CAMERA_WIDTH;
			int cHeight = CAMERA_HEIGHT;
			int cellSize = cWidth < cHeight ? cWidth / maze.rows : cHeight / maze.rows;
			int verticalOffset = cHeight > cellSize * maze.rows ? (cHeight - (cellSize * maze.rows)) / 2 : 40;
			int horizontalOffset = cWidth > cellSize * maze.columns ? (cWidth - (cellSize * maze.columns)) / 2 : 0;
			final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();

			for (int i = 0; i < maze.rows; i++) {
				for (int j = 0; j < maze.columns; j++) {
					Cell cell = maze.getCell(i, j);
					
					if (cell != null) {
						Point topLeft = new Point(j * cellSize + horizontalOffset, i * cellSize + verticalOffset);
						Point topRight = new Point((j + 1) * cellSize + horizontalOffset, i * cellSize + verticalOffset);
						Point bottomLeft = new Point(j * cellSize + horizontalOffset, (i + 1) * cellSize + verticalOffset);
						Point bottomRight = new Point((j + 1) * cellSize + horizontalOffset, (i + 1) * cellSize + verticalOffset);
						
						// determine which wall gets drawn
						if (cell.walls.get(CellNeighbor.TOP)) {
							if(distanceToPlayer(topLeft, topRight, cellSize, horizontalOffset, verticalOffset) < distanceLimit)
								drawWall(scene, vertexBufferObjectManager, topLeft, topRight, maze.cellPaint, CellNeighbor.TOP);
							
						}
						
						if (cell.walls.get(CellNeighbor.LEFT)) {
							if(distanceToPlayer(topLeft, bottomLeft, cellSize, horizontalOffset, verticalOffset) < distanceLimit)
								drawWall(scene, vertexBufferObjectManager, topLeft, bottomLeft, maze.cellPaint, CellNeighbor.LEFT);
						}

						if (cell.walls.get(CellNeighbor.RIGHT)) {
							if(distanceToPlayer(topRight, bottomRight, cellSize, horizontalOffset, verticalOffset) < distanceLimit)
								drawWall(scene, vertexBufferObjectManager, topRight, bottomRight, maze.cellPaint, CellNeighbor.RIGHT);
						}

						if (cell.walls.get(CellNeighbor.BOTTOM)) {
							if(distanceToPlayer(bottomLeft, bottomRight, cellSize, horizontalOffset, verticalOffset) < distanceLimit)
								drawWall(scene, vertexBufferObjectManager, bottomLeft, bottomRight, maze.cellPaint, CellNeighbor.BOTTOM);
						}
						
						if(cell.isStart){
							this.playerRectangle = new Rectangle(topLeft.x + cellSize/4, topLeft.y + cellSize/4, cellSize/2, cellSize/2, vertexBufferObjectManager);
							this.playerRectangle.setColor(Color.RED);
							
							Body playerBody = PhysicsFactory.createCircleBody(this.physicsWorld, this.playerRectangle, BodyType.DynamicBody, this.playerFixtureDef);
							this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(this.playerRectangle, playerBody));
							
							scene.attachChild(this.playerRectangle);
						}
					}
				}
			}			
		}
		scene.registerUpdateHandler(this.physicsWorld);
		
		pOnCreateSceneCallback.onCreateSceneFinished(scene);
	}
		
	@Override
	public void onPopulateScene(Scene scene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
	
	public double distanceToPlayer(Point a, Point b, int cellSize, int hOffset, int vOffset){
		float playerX = 0;
		float playerY = 0;
		
		if(playerRectangle != null){
			playerX = playerRectangle.getX() + playerRectangle.getWidth()/2;
			playerY = playerRectangle.getY() + playerRectangle.getHeight()/2;
		}else{
			playerX = maze.startPoint.x * cellSize + hOffset + cellSize/2;
			playerY = maze.startPoint.y * cellSize + vOffset + cellSize/2;
		}
		
		return Math.sqrt(Math.pow(playerY - a.y, 2) + Math.pow(playerX - a.x, 2));
	}
	
	public void drawWall(Scene scene, VertexBufferObjectManager vertexBufferObjectManager, Point a, Point b, Paint p, CellNeighbor position) {
		Rectangle wall = null;
		Body body = null;
		
		//create line
		switch (position) {
			case TOP:
			case BOTTOM:
				wall = new Rectangle(a.x, a.y, b.x - a.x, p.getStrokeWidth(), vertexBufferObjectManager);
				wall.setColor(Color.WHITE);
				
				body = PhysicsFactory.createBoxBody(this.physicsWorld, wall, BodyType.StaticBody, this.wallFixtureDef);
				this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(wall, body));
				break;
			case LEFT:
			case RIGHT:
				wall = new Rectangle(a.x, a.y, p.getStrokeWidth(), b.y - a.y, vertexBufferObjectManager);
				wall.setColor(Color.WHITE);
				
				body = PhysicsFactory.createBoxBody(this.physicsWorld, wall, BodyType.StaticBody, this.wallFixtureDef);
				this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(wall, body));
				break;
		}
		
		if(wall!=null)		
			scene.attachChild(wall);
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
