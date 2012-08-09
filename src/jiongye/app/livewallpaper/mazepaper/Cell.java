package jiongye.app.livewallpaper.mazepaper;

import java.util.HashMap;

import android.graphics.Point;

public class Cell {
	//row and column position of cell
	public Point pos;
	
	public Point coord;
	
	//use for creating maze and removing walls
	public boolean visited;
	public boolean cpuVisited;
	
	//information about surrouding 4 walls
	public HashMap<CellNeighbor, Boolean> walls;
	public HashMap<CellNeighbor, Wall> wallBodies;
	
	//entry and exit markers
	public boolean isStart = false;
	public boolean isEnd = false;
		
	public Cell(int _row, int _col){
		this.pos = new Point(_row,_col);
		
		this.visited = false;
		this.cpuVisited = false;
		
		this.walls = new HashMap<CellNeighbor, Boolean>();
		this.walls.put(CellNeighbor.TOP, true);
		this.walls.put(CellNeighbor.RIGHT, true);
		this.walls.put(CellNeighbor.BOTTOM, true);
		this.walls.put(CellNeighbor.LEFT, true);
		
		this.wallBodies = new HashMap<CellNeighbor, Wall>();
	}

	public void setCoord(Point point) {
		this.coord = new Point(point.x,point.y);
	}
	
	public void reset(){
		this.visited = false;
		this.cpuVisited = false;
		
		this.walls.put(CellNeighbor.TOP, true);
		this.walls.put(CellNeighbor.RIGHT, true);
		this.walls.put(CellNeighbor.BOTTOM, true);
		this.walls.put(CellNeighbor.LEFT, true);
	}
}
