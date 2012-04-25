package jiongye.app.livewallpaper.mazepaper;

import java.util.HashMap;

import android.graphics.Point;

public class Cell {
	//row and column position of cell
	public Point pos;
	
	//use for creating maze and removing walls
	public boolean visited;
	
	public boolean playerVisited;
	
	//information about surrouding 4 walls
	public HashMap<CellNeighbor, Boolean> walls;
	
	//entry and exit markers
	public boolean isStart = false;
	public boolean isEnd = false;
		
	public Cell(int _row, int _col){
		this.pos = new Point(_row,_col);
		
		this.visited = false;
		this.playerVisited = false;
		
		this.walls = new HashMap<CellNeighbor, Boolean>();
		this.walls.put(CellNeighbor.TOP, true);
		this.walls.put(CellNeighbor.RIGHT, true);
		this.walls.put(CellNeighbor.BOTTOM, true);
		this.walls.put(CellNeighbor.LEFT, true);
	}
}
