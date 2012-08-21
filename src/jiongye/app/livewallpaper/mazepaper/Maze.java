package jiongye.app.livewallpaper.mazepaper;

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
	public Stack<CPU> cpuStack;
	
	public boolean isMultiCPU;
	
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
		this.cells[_end.x][_end.y].possibleNeighbor = 4;
		
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
		
		this.isMultiCPU = false;
		this.cpuStack = new Stack<CPU>();
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
		
		Cell cell = null;
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				cell = this.getCell(i,j);
				if(cell!=null && !cell.isEnd){
					cell.possibleNeighbor += !cell.walls.get(CellNeighbor.TOP) ? 1:0;
					cell.possibleNeighbor += !cell.walls.get(CellNeighbor.RIGHT) ? 1:0;
					cell.possibleNeighbor += !cell.walls.get(CellNeighbor.BOTTOM) ? 1:0;
					cell.possibleNeighbor += !cell.walls.get(CellNeighbor.LEFT) ? 1:0;
				}
			}
		}
	}
	
	public void reset() {
		this.solved = false;
		
		for(int i=0;i<this.cpuStack.size();i++){
			CPU cpu = this.cpuStack.get(i);
			cpu.track.clear();
			cpu.pos = new Point(this.startPoint.x, this.startPoint.y);
		}
		
		
		Cell cell = null;
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				cell = this.getCell(i,j);
				if(cell!=null && !cell.isEnd){
					cell.possibleNeighbor += !cell.walls.get(CellNeighbor.TOP) ? 1:0;
					cell.possibleNeighbor += !cell.walls.get(CellNeighbor.RIGHT) ? 1:0;
					cell.possibleNeighbor += !cell.walls.get(CellNeighbor.BOTTOM) ? 1:0;
					cell.possibleNeighbor += !cell.walls.get(CellNeighbor.LEFT) ? 1:0;
					cell.cpuVisited = false;
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

	public void cpuNextMove(CPU cpu) {
		if (this.getCell(cpu.pos).isEnd) {
			this.solved = true;
		}		
				
		if (!this.solved) {
			// get all neighbors that havent been visited before
			Stack<Point> neighbors = new Stack<Point>();
			Cell curCell = this.getCell(cpu.pos);
			Cell neighborCell = null;
			Point neightPoint = null;
			Point nextMove = null;
			
			for (CellNeighbor neighbor : CellNeighbor.values()) {
				//if there is no wall
				if(!curCell.walls.get(neighbor)){
					neighborCell = this.getNeighbor(curCell.pos, neighbor);
					if(neighborCell != null){
						if(neighborCell.possibleNeighbor > 0 && !cpu.track.contains(neighborCell.pos)){
							neightPoint = new Point(neighborCell.pos);
							neighbors.push(neightPoint);
						}
					}
				}
			} 
			
			curCell.possibleNeighbor = neighbors.size();
			Point trackPoint = null;
			
			//determine which neighbor to visit
			switch (neighbors.size()) {
				//0 choices, back track
				case 0:
					if(!this.isMultiCPU) {
						if (cpu.track.size() > 0)
							nextMove = cpu.track.pop();
						
						//always start the track at the starting point
						if(cpu.track.size() == 0){
							trackPoint = new Point(this.startPoint.x, this.startPoint.y); 
						}						
					}
					
					curCell.possibleNeighbor = 0;
					
					break;
				//1 choice, go to it
				case 1:
					int nextX = neighbors.get(0).x;
					int nextY = neighbors.get(0).y;
					
					nextMove = new Point(nextX, nextY);
					this.cells[nextX][nextY].cpuVisited = true;
					this.cells[nextX][nextY].possibleNeighbor--;
						
					trackPoint = new Point(nextX,nextY);
					
					curCell.possibleNeighbor = 0;
					
					break;
				//more than 1 choice, pick randomly
				default:
					if(!this.isMultiCPU) {
						Random rand = new Random();
						int randomNeighborIndex = rand.nextInt(neighbors.size());
						int randX = neighbors.get(randomNeighborIndex).x;
						int randY = neighbors.get(randomNeighborIndex).y;
						
						nextMove = new Point(randX, randY);
						this.cells[randX][randY].cpuVisited = true;
						this.cells[randX][randY].possibleNeighbor--;
						
						//push new point to track
						trackPoint = new Point(randX,randY);
																
						cpu.track.push(trackPoint);
						
						//change possible neighbor count
						if(curCell.possibleNeighbor > 2) {
							curCell.possibleNeighbor--;
						} else {
							// if we are on a straight hallwall (top and bottom walls open or left and right walls open),
							// possible neighbor count is really the same as 1 choice
							// because you can only go back by back track
							if( (curCell.walls.get(CellNeighbor.TOP) && curCell.walls.get(CellNeighbor.BOTTOM)) || 
								(curCell.walls.get(CellNeighbor.LEFT) && curCell.walls.get(CellNeighbor.RIGHT))) {
								curCell.possibleNeighbor = 0;
							} else {
								curCell.possibleNeighbor--;
							}
						}
						
					} else {
						for (int i = 0; i < neighbors.size(); i++) {
							CPU childCpu = new CPU();
							childCpu.pos = new Point(neighbors.get(i).x, neighbors.get(i).y);
							childCpu.track.push(new Point(curCell.pos));
							childCpu.track.push(new Point(childCpu.pos));
							this.cpuStack.push(childCpu);
							
							curCell.possibleNeighbor--;
						}
						cpu.deadend = true;
					}
					
					
					
					break;
			}
			
			if(nextMove != null) 
				cpu.pos = new Point(nextMove);
			
			if(trackPoint != null){
				//cornering fix
				if(cpu.track.size() > 0){
					Point lastPoint = cpu.track.peek();
					if(lastPoint.x != trackPoint.x && lastPoint.y != trackPoint.y){
						Point pointA = new Point(lastPoint.x,trackPoint.y);
						Point pointB = new Point(trackPoint.x, lastPoint.y);
						Point cornerPoint = null;
												
						if(!this.wallBetween(pointA, lastPoint) && !this.wallBetween(pointA, trackPoint)) {
							cornerPoint = pointA;
						} else if(!this.wallBetween(pointB, lastPoint) && !this.wallBetween(pointB, trackPoint)) {
							cornerPoint = pointB;
						}
						
						if(cornerPoint != null) 
							cpu.track.push(cornerPoint);
					}
				}
				
				if(!cpu.track.contains(trackPoint))
					cpu.track.push(trackPoint);
			}
		}
	} 
		
	public void setCpuRadius(int radius) {
		for(int i=0;i<this.cpuStack.size();i++) {
			this.cpuStack.get(i).setRadius(radius);
		}
	}
	
	public boolean cellOnCpuTrack(Point p) {
		for(int i=0;i<this.cpuStack.size();i++) {
			if(this.cpuStack.get(i).track.contains(p))
				return true;
		}
		
		return false;
	}
	
	public boolean wallBetween(Point pointA, Point pointB){
		Cell cellA = this.getCell(pointA);
		Cell cellB = this.getCell(pointB);
		
		if(cellA != null && cellB != null) {
			//A is above B
			if(cellA.pos.x < cellB.pos.x)
				return cellA.walls.get(CellNeighbor.BOTTOM) || cellB.walls.get(CellNeighbor.TOP);
			
			//A is below B
			if(cellA.pos.x > cellB.pos.x)
				return cellA.walls.get(CellNeighbor.TOP) || cellB.walls.get(CellNeighbor.BOTTOM);
			
			//A is to the left of B
			if(cellA.pos.y < cellB.pos.y)
				return cellA.walls.get(CellNeighbor.RIGHT) || cellB.walls.get(CellNeighbor.LEFT);
			
			//A is to the right of B
			if(cellA.pos.y > cellB.pos.y)
				return cellA.walls.get(CellNeighbor.LEFT) || cellB.walls.get(CellNeighbor.RIGHT);
		}
		
		return true;
	}
}
