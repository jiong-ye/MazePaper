package jiongye.app.livewallpaper.mazepaper;

import java.util.Random;
import java.util.Stack;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class MazePaperService extends WallpaperService {
	private final Handler mHandler = new Handler();
	public static final String SHARED_PREFS_NAME = "mazepaper_settings";
	
	@Override
	public void onCreate() {
		super.onCreate();
//		 android.os.Debug.waitForDebugger();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Engine onCreateEngine() {
		return new MazePaperEngine();
	}

	private class MazePaperEngine extends Engine 
		implements SharedPreferences.OnSharedPreferenceChangeListener {
		
		private SharedPreferences preferences;
		
		private int mazeRows = 5;
		private int mazeCols = 5;
		private Maze maze;
		
		private String solveSpeed;

		private int rowDrawNumber = 1;

		private final Runnable mdrawMaze = new Runnable() {
			public void run() {
				drawFrame();
			}
		};
		private boolean mVisible;

		MazePaperEngine() {
			preferences = MazePaperService.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
			preferences.registerOnSharedPreferenceChangeListener(this);
			onSharedPreferenceChanged(preferences,null);
		}
		
		public void onSharedPreferenceChanged(SharedPreferences pref, String key){
			try{
				this.mazeRows = Integer.parseInt(pref.getString("maze_rows", "10"));
				this.mazeCols = Integer.parseInt(pref.getString("maze_cols", "10"));
				this.solveSpeed = pref.getString("maze_solve_speed", "slow");
				
			}
			catch(Exception exp){
				this.mazeRows = 10;
				this.mazeCols = 10;
				this.solveSpeed = "slow";
			}
			generateMaze();
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			mHandler.removeCallbacks(mdrawMaze);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
			if (visible) {
				drawFrame();
			} else {
				mHandler.removeCallbacks(mdrawMaze);
			}
		}

		void drawFrame() {
			final SurfaceHolder holder = getSurfaceHolder();
			int speed = 750;
			
			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					drawMaze(c);
				}
			} finally {
				if (c != null)
					holder.unlockCanvasAndPost(c);
			}

			// Reschedule the next redraw
			mHandler.removeCallbacks(mdrawMaze);
			if (mVisible) {
				if(this.solveSpeed.equals("crazy_fast"))
					speed = 5;
				else if(this.solveSpeed.equals("really_fast"))
					speed = 50;
				else if(this.solveSpeed.equals("fast"))
					speed = 150;
				else if(this.solveSpeed.equals("medium"))
					speed = 500;
				
				mHandler.postDelayed(mdrawMaze, speed);
			}
		}
		
		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			super.onSurfaceCreated(holder);
			generateMaze();
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			mVisible = false;
			mHandler.removeCallbacks(mdrawMaze);
		}

		void generateMaze() {
			maze = new Maze(mazeRows, mazeCols);
			maze.player = new Player();

			Cell curCell = maze.getCell(0, 0);
			curCell.isEnd = true;
			int cellsRemain = mazeRows * mazeCols;
			Stack<Point> track = new Stack<Point>();

			curCell.visited = true;
			while (cellsRemain > 0) {
				if (curCell.visited)
					cellsRemain--;

				curCell.visited = true;

				// check which neighbor hasn't been visited
				Stack<Point> neighbors = new Stack<Point>();

				// top neighbor
				if (curCell.pos.x > 0 && !maze.getNeighbor(curCell.pos, CellNeighbor.TOP).visited)
					neighbors.push(new Point(curCell.pos.x - 1, curCell.pos.y));

				// right neighbor
				if (curCell.pos.y < mazeCols - 1 && !maze.getNeighbor(curCell.pos, CellNeighbor.RIGHT).visited)
					neighbors.push(new Point(curCell.pos.x, curCell.pos.y + 1));

				// bottom neighbor
				if (curCell.pos.x < mazeRows - 1 && !maze.getNeighbor(curCell.pos, CellNeighbor.BOTTOM).visited)
					neighbors.push(new Point(curCell.pos.x + 1, curCell.pos.y));

				// left neighbor
				if (curCell.pos.y > 0 && !maze.getNeighbor(curCell.pos, CellNeighbor.LEFT).visited)
					neighbors.push(new Point(curCell.pos.x, curCell.pos.y - 1));

				// there are unvisited neighbors
				if (neighbors.size() > 0) {
					Random randomNeighbor = new Random();
					int neighborIndex = randomNeighbor.nextInt(neighbors.size());
					Point neighborPos = null;

					// get a random neighbor
					neighborPos = neighbors.get(neighborIndex);

					if (neighborPos != null) {
						Cell newCell = maze.getCell(neighborPos);

						if (newCell != null) {
							// check neighbor's position relative to current
							// cell
							// top cell
							if (newCell.pos.x < curCell.pos.x) {
								newCell.walls.put(CellNeighbor.BOTTOM, false);
								curCell.walls.put(CellNeighbor.TOP, false);
							}

							// right
							if (newCell.pos.y > curCell.pos.y) {
								newCell.walls.put(CellNeighbor.LEFT, false);
								curCell.walls.put(CellNeighbor.RIGHT, false);
							}

							// bottom
							if (newCell.pos.x > curCell.pos.x) {
								newCell.walls.put(CellNeighbor.TOP, false);
								curCell.walls.put(CellNeighbor.BOTTOM, false);
							}

							// left cell
							if (newCell.pos.y < curCell.pos.y) {
								newCell.walls.put(CellNeighbor.RIGHT, false);
								curCell.walls.put(CellNeighbor.LEFT, false);
							}

							track.push(new Point(curCell.pos.x, curCell.pos.y));
							curCell = newCell;
						}
					}
				} else {
					if (track.size() > 0) {
						Point previousCell = track.pop();
						curCell = maze.getCell(previousCell);
					}
				}
			}
		}

		void drawMaze(Canvas c) {
			int cWidth = c.getWidth();
			int cHeight = c.getHeight();
			int cellSize = cWidth < cHeight ? cWidth / mazeRows : cHeight / mazeRows;
			int verticalOffset = cHeight > cellSize * mazeRows ? (cHeight - (cellSize * mazeRows)) / 2 : 40;
			int horizontalOffset = cWidth > cellSize * mazeCols ? (cWidth - (cellSize * mazeCols)) / 2 : 0;
			Point playerPos = null;

			maze.player.setRadius(cellSize);

			c.save();
			c.drawColor(0xff000000);

			rowDrawNumber = mazeRows;

			// draw maze cells
			for (int i = 0; i < mazeRows && i < rowDrawNumber; i++) {
				for (int j = 0; j < mazeCols; j++) {
					Cell cell = maze.getCell(i, j);
					if (cell != null) {
						Point topLeft = new Point(j * cellSize + horizontalOffset, i * cellSize + verticalOffset);
						Point topRight = new Point((j + 1) * cellSize + horizontalOffset, i * cellSize + verticalOffset);
						Point bottomLeft = new Point(j * cellSize + horizontalOffset, (i + 1) * cellSize + verticalOffset);
						Point bottomRight = new Point((j + 1) * cellSize + horizontalOffset, (i + 1) * cellSize + verticalOffset);

						// determine which wall gets drawn
						if (cell.walls.get(CellNeighbor.TOP)) {
							drawLine(c, topLeft, topRight, maze.cellPaint);
						}

						if (cell.walls.get(CellNeighbor.LEFT)) {
							drawLine(c, topLeft, bottomLeft, maze.cellPaint);
						}

						if (cell.walls.get(CellNeighbor.RIGHT)) {
							drawLine(c, topRight, bottomRight, maze.cellPaint);
						}

						if (cell.walls.get(CellNeighbor.BOTTOM)) {
							drawLine(c, bottomLeft, bottomRight, maze.cellPaint);
						}

						//this cell is the destination
						if (cell.isEnd) {
							c.drawRect(	topLeft.x + maze.cellStrokeWidth, 
										topLeft.y + maze.cellStrokeWidth, 
										bottomRight.x - maze.cellStrokeWidth,
										bottomRight.y - maze.cellStrokeWidth, maze.endCellPaint);
						}
						//a cell visited by the play but not on his track to destination
						else if (cell.playerVisited && !maze.player.track.contains(cell.pos)) {
							c.drawRect(	topLeft.x + maze.cellStrokeWidth + 3, 
										topLeft.y + maze.cellStrokeWidth + 3, 
										bottomRight.x - maze.cellStrokeWidth - 3, 
										bottomRight.y - maze.cellStrokeWidth - 3, 
										maze.playerVisitedCellPaint);
						//set player at the starting cell
						} else if (cell.isStart && maze.player != null && maze.player.pos == null) {
							cell.playerVisited = true;
							playerPos = new Point(cell.pos.x, cell.pos.y);
							maze.player.track.push(new Point(cell.pos.x, cell.pos.y));
						}
					}
				}
			}

			// dont try to move player if still drawing cells
			if (rowDrawNumber < mazeRows) {
				rowDrawNumber += 1;
			} else {
				Cell playerCell = maze.getCell(playerPos != null ? playerPos : maze.player.pos);
				if (playerCell != null) {
					maze.player.pos = new Point(playerCell.pos.x, playerCell.pos.y);
					
					// draw player
					c.drawCircle(playerCell.pos.y * cellSize + horizontalOffset + maze.player.offset.x, 
								 playerCell.pos.x * cellSize + verticalOffset + maze.player.offset.y, 
								 maze.player.radius, 
								 maze.player.paint);

					// draw players track;
					if (maze.player.track.size() > 1) {
						for (int i = 0; i < maze.player.track.size() - 1; i++) {
							Point pathFrom = new Point( maze.player.track.get(i).y * cellSize + horizontalOffset + cellSize / 2, 
														maze.player.track.get(i).x * cellSize + verticalOffset + cellSize / 2);
							Point pathTo = new Point(maze.player.track.get(i + 1).y * cellSize + horizontalOffset + cellSize / 2, 
													 maze.player.track.get(i + 1).x * cellSize + verticalOffset + cellSize / 2);

							drawLine(c, pathFrom, pathTo, maze.player.pathPaint);
						}
					}
					if (!maze.solved){
						maze.playerNextMove();
					}
					else {
						generateMaze();
					}
				}
			}

			c.restore();
		}

		void drawLine(Canvas c, Point from, Point to, Paint p) {
			c.drawLine(from.x, from.y, to.x, to.y, p);
		}
	}
}
