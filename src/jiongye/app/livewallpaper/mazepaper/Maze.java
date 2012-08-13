package jiongye.app.livewallpaper.mazepaper;

import java.io.Console;
import java.util.Random;
import java.util.Stack;

import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

public class Maze {
	public int width;
	public int height;
	public int rows;
	public int columns;

	public Cell[][] cells;
	public CPU cpu;

	public Point startPoint;
	public Point endPoint;
	
	public Paint cellPaint;
	public Paint endCellPaint;
	public Paint cpuVisitedCellPaint;
		
	public int cellStrokeWidth;

	public boolean regenerated;
	public boolean solved;

	public Maze(int _rows, int _columns){
		init(_rows, _columns, new Point(_rows-1,_columns-1), new Point(0,0));
	}
	
	public Maze(int _rows, int _columns, Point _start, Point _end) {
		init(_rows, _columns, _start, _end);
	}
	
	public void init(int _rows, int _columns, Point _start, Point _end){
		this.regenerated = false;
		this.solved = false;

		this.rows = _rows;
		this.columns = _columns;

		this.cells = new Cell[_rows][_columns];

		for (int i = 0; i < _rows; i++) {
			for (int j = 0; j < _columns; j++) {
				this.cells[i][j] = new Cell(i, j);
			}
		}

		this.endPoint = _end;
		this.startPoint = _start;
		this.cells[_end.x][_end.y].isEnd = true;
		this.cells[_start.x][_start.y].isStart = true;

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
		
		this.cpuVisitedCellPaint = new Paint();
		this.cpuVisitedCellPaint.setColor(0x66c3c3c3);
		this.cpuVisitedCellPaint.setStyle(Paint.Style.FILL);
	}
	
	public void regenerate(){
		this.regenerated = true;
		this.solved = false;
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				this.cells[i][j].reset();
			}
		}
		
		createMaze();
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
			if (curCell.pos.x > 0 && !this.getNeighbor(curCell.pos, CellNeighbor.TOP).visited){
				neighbors.push(new Point(curCell.pos.x - 1, curCell.pos.y));
			}
			
			// right neighbor
			if (curCell.pos.y < this.columns - 1 && !this.getNeighbor(curCell.pos, CellNeighbor.RIGHT).visited){
				neighbors.push(new Point(curCell.pos.x, curCell.pos.y + 1));
			}

			// bottom neighbor
			if (curCell.pos.x < this.rows - 1 && !this.getNeighbor(curCell.pos, CellNeighbor.BOTTOM).visited){
				neighbors.push(new Point(curCell.pos.x + 1, curCell.pos.y));
			}

			// left neighbor
			if (curCell.pos.y > 0 && !this.getNeighbor(curCell.pos, CellNeighbor.LEFT).visited){
				neighbors.push(new Point(curCell.pos.x, curCell.pos.y - 1));
			}

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
						// check neighbor's position relative to current cell
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
	
	public Cell getEndCell(){
		return getCell(this.endPoint);
	}
	
	public Cell getStartCell() {
		return getCell(this.startPoint);
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

	public void cpuNextMove() {
		if (this.getCell(this.cpu.pos).isEnd) {
			this.solved = true;
		}

		if (!this.solved) {
			// get all neighbors that havent been visited before
			Stack<Point> neighbors = new Stack<Point>();
			Cell curCell = this.getCell(this.cpu.pos);
			Cell neighbCell = null;
			Point neightPoint = null;
			
			for (CellNeighbor neighbor : CellNeighbor.values()) {
				if(!curCell.walls.get(neighbor) && !this.getNeighbor(this.cpu.pos, neighbor).cpuVisited){
					neighbCell = this.getNeighbor(curCell.pos, neighbor);
					if(neighbCell != null){
						neightPoint = new Point(neighbCell.pos);
						
						if(!this.cpu.track.contains(neightPoint))
							neighbors.push(neightPoint);
					}
				}
			}
			
			curCell.possibleNeighbor = neighbors.size();
			Point trackPoint = null;
			
			//determine which neighbor to visit
			switch (neighbors.size()) {
				//0 choices, back track
				case 0:
					if (this.cpu.track.size() > 0)
						this.cpu.pos = this.cpu.track.pop();
					
					//always start the track at the starting point
					if(this.cpu.track.size() == 0){
						trackPoint = new Point(this.startPoint.x, this.startPoint.y);
					}
					
					break;
				//1 choice, go to it
				case 1:
					int nextX = neighbors.get(0).x;
					int nextY = neighbors.get(0).y;
					
					this.cpu.pos = new Point(nextX, nextY);
					this.cells[nextX][nextY].cpuVisited = true;
					
					trackPoint = new Point(nextX,nextY);
					
					break;
				//more than 1 choice, pick randomly
				default:
					Random rand = new Random();
					int randomNeighborIndex = rand.nextInt(neighbors.size());
					int randX = neighbors.get(randomNeighborIndex).x;
					int randY = neighbors.get(randomNeighborIndex).y;
					
					this.cpu.pos = new Point(randX, randY);
					this.cells[randX][randY].cpuVisited = true;
					
					//push new point to track
					trackPoint = new Point(randX,randY);
															
					this.cpu.track.push(trackPoint);
					
					break;
			}
			
			//add corner point, so track corners are always 90 degree
			if(trackPoint != null){
				if(this.cpu.track.size() > 0){
					Point lastPoint = this.cpu.track.peek();
					
					if(lastPoint != null){
						if(lastPoint.x != trackPoint.x && lastPoint.y != trackPoint.y){
							if(trackPoint.x < lastPoint.x && trackPoint.y < lastPoint.y)
								this.cpu.track.push(new Point(trackPoint.x+1, trackPoint.y));
						}
					}
				}
				this.cpu.track.push(trackPoint);
			}
		}
	}	
}
