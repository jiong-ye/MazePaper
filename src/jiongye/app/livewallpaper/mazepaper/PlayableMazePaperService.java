package jiongye.app.livewallpaper.mazepaper;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import android.graphics.Paint;
import android.graphics.Point;

public class PlayableMazePaperService extends BaseLiveWallpaperService {
	Maze maze;

	private int rows = 10;
	private int columns = 10;

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	private PhysicsWorld physicsWorld;

	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
		maze = new Maze(rows, columns);
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
		Scene scene = new Scene();
		scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		
		if (maze != null) {
			int cWidth = CAMERA_WIDTH;
			int cHeight = CAMERA_HEIGHT;
			int cellSize = cWidth < cHeight ? cWidth / maze.rows : cHeight / maze.rows;
			int verticalOffset = cHeight > cellSize * maze.rows ? (cHeight - (cellSize * maze.rows)) / 2 : 40;
			int horizontalOffset = cWidth > cellSize * maze.columns ? (cWidth - (cellSize * maze.columns)) / 2 : 0;
			Point playerPos = null;
			final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();

			maze.player.setRadius(cellSize);
			
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
							drawLine(scene, vertexBufferObjectManager, topLeft, topRight, maze.cellPaint);
						}
						
						if (cell.walls.get(CellNeighbor.LEFT)) {
							drawLine(scene, vertexBufferObjectManager, topLeft, bottomLeft, maze.cellPaint);
						}

						if (cell.walls.get(CellNeighbor.RIGHT)) {
							drawLine(scene, vertexBufferObjectManager, topRight, bottomRight, maze.cellPaint);
						}

						if (cell.walls.get(CellNeighbor.BOTTOM)) {
							drawLine(scene, vertexBufferObjectManager, bottomLeft, bottomRight, maze.cellPaint);
						}
					}
				}
			}
			
		}
		
		pOnCreateSceneCallback.onCreateSceneFinished(scene);
	}
		
	@Override
	public void onPopulateScene(Scene scene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
	
	public void drawLine(Scene scene, VertexBufferObjectManager vertexBufferObjectManager, Point a, Point b, Paint p) {
		Line line = new Line(a.x, a.y, b.x, b.y, p.getStrokeWidth(), vertexBufferObjectManager);
		line.setColor(Color.WHITE);
		scene.attachChild(line);
	}

}
