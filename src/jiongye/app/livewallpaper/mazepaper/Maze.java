package jiongye.app.livewallpaper.mazepaper;

import java.util.Random;
import java.util.Stack;

import android.graphics.Paint;
import android.graphics.Point;

public class Maze {
	public int width;
	public int height;
	public int rows;
	public int columns;

	public Cell[][] cells;
	public Player player;

	public Paint cellPaint;
	public Paint endCellPaint;
	public Paint playerVisitedCellPaint;
	
	public int cellStrokeWidth;

	public boolean solved;

	public Maze(int _rows, int _columns) {
		this.solved = false;

		this.rows = _rows;
		this.columns = _columns;

		this.cells = new Cell[_rows][_columns];

		for (int i = 0; i < _rows; i++) {
			for (int j = 0; j < _columns; j++) {
				this.cells[i][j] = new Cell(i, j);
			}
		}

		this.cells[0][0].isEnd = true;
		this.cells[_rows - 1][_columns - 1].isStart = true;

		this.cellStrokeWidth = 2;

		this.cellPaint = new Paint();
		this.cellPaint.setColor(0xff838383);
		this.cellPaint.setStrokeWidth(this.cellStrokeWidth);
		this.cellPaint.setStrokeCap(Paint.Cap.ROUND);
		this.cellPaint.setStyle(Paint.Style.STROKE);

		this.endCellPaint = new Paint();
		this.endCellPaint.setColor(0xff00ff00);
		this.endCellPaint.setAntiAlias(true);
		this.endCellPaint.setStyle(Paint.Style.FILL);
		
		this.playerVisitedCellPaint = new Paint();
		this.playerVisitedCellPaint.setColor(0x66c3c3c3);
		this.playerVisitedCellPaint.setStyle(Paint.Style.FILL);
	}
	
	public void createMaze() {
		Cell curCell = this.getCell(0, 0);
		curCell.isEnd = true;
		int cellsRemain = this.rows * this.columns;
		Stack<Point> track = new Stack<Point>();

		curCell.visited = true;
		while (cellsRemain > 0) {
			if (curCell.visited)
				cellsRemain--;

			curCell.visited = true;

			// check which neighbor hasn't been visited
			Stack<Point> neighbors = new Stack<Point>();

			// top neighbor
			if (curCell.pos.x > 0 && !this.getNeighbor(curCell.pos, CellNeighbor.TOP).visited)
				neighbors.push(new Point(curCell.pos.x - 1, curCell.pos.y));

			// right neighbor
			if (curCell.pos.y < this.columns - 1 && !this.getNeighbor(curCell.pos, CellNeighbor.RIGHT).visited)
				neighbors.push(new Point(curCell.pos.x, curCell.pos.y + 1));

			// bottom neighbor
			if (curCell.pos.x < this.rows - 1 && !this.getNeighbor(curCell.pos, CellNeighbor.BOTTOM).visited)
				neighbors.push(new Point(curCell.pos.x + 1, curCell.pos.y));

			// left neighbor
			if (curCell.pos.y > 0 && !this.getNeighbor(curCell.pos, CellNeighbor.LEFT).visited)
				neighbors.push(new Point(curCell.pos.x, curCell.pos.y - 1));

			// there are unvisited neighbors
			if (neighbors.size() > 0) {
				Random randomNeighbor = new Random();
				int neighborIndex = randomNeighbor.nextInt(neighbors.size());
				Point neighborPos = null;

				// get a random neighbor
				neighborPos = neighbors.get(neighborIndex);

				if (neighborPos != null) {
					Cell newCell = this.getCell(neighborPos);

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
					curCell = this.getCell(previousCell);
				}
			}
		}
	}

	public Cell getCell(int row, int column) {
		return this.cells[row][column] != null ? this.cells[row][column] : null;
	}

	public Cell getCell(Point p) {
		return this.cells[p.x][p.y] != null ? this.cells[p.x][p.y] : null;
	}

	public Cell getNeighbor(Point p, CellNeighbor position) {
		switch (position) {
			case TOP:
				if (p.x > 0)
					return this.cells[p.x - 1][p.y];
			case RIGHT:
				if (p.y < this.columns - 1)
					return this.cells[p.x][p.y + 1];
			case BOTTOM:
				if (p.x < this.rows - 1)
					return this.cells[p.x + 1][p.y];
			case LEFT:
				if (p.y > 0)
					return this.cells[p.x][p.y - 1];
		}
		return null;
	}

	public void playerNextMove() {
		if (this.getCell(this.player.pos).isEnd) {
			this.solved = true;
		}

		if (!this.solved) {
			// get all neighbors that havent been visited before
			Stack<Point> neighbors = new Stack<Point>();
			Cell curCell = this.getCell(this.player.pos);

			if (!curCell.walls.get(CellNeighbor.TOP) && !this.getNeighbor(this.player.pos, CellNeighbor.TOP).playerVisited){
				neighbors.push(new Point(this.player.pos.x - 1, this.player.pos.y));
			}

			if (!curCell.walls.get(CellNeighbor.RIGHT) && !this.getNeighbor(this.player.pos, CellNeighbor.RIGHT).playerVisited){
				neighbors.push(new Point(this.player.pos.x, this.player.pos.y + 1));
			}

			if (!curCell.walls.get(CellNeighbor.BOTTOM) && !this.getNeighbor(this.player.pos, CellNeighbor.BOTTOM).playerVisited){
				neighbors.push(new Point(this.player.pos.x + 1, this.player.pos.y));
			}
			
			if (!curCell.walls.get(CellNeighbor.LEFT) && !this.getNeighbor(this.player.pos, CellNeighbor.LEFT).playerVisited){
				neighbors.push(new Point(this.player.pos.x, this.player.pos.y-1));
			}
			
			switch (neighbors.size()) {
				case 0:
					if (this.player.track.size() > 0)
						this.player.pos = this.player.track.pop();
					break;
				case 1:
					int nextX = neighbors.get(0).x;
					int nextY = neighbors.get(0).y;
					
					this.player.pos = new Point(nextX, nextY);
					this.cells[nextX][nextY].playerVisited = true;
					this.player.track.push(new Point(nextX,nextY));
					break;
				default:
					Random rand = new Random();
					int randomNeighborIndex = rand.nextInt(neighbors.size());
					int randX = neighbors.get(randomNeighborIndex).x;
					int randY = neighbors.get(randomNeighborIndex).y;
					
					this.player.pos = new Point(randX, randY);
					this.cells[randX][randY].playerVisited = true;
					this.player.track.push(new Point(randX,randY));
					break;
			}
		}
	}

}
