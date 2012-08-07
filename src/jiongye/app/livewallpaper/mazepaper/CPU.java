package jiongye.app.livewallpaper.mazepaper;

import java.util.Stack;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Cap;

public class CPU {
	public int radius;
	
	public Point pos;
	public Point offset;
	
	public Paint paint;
	public Paint pathPaint;
	
	public Stack<Point> track;
	
	public CPU() {
		this.radius = 5;
		this.track = new Stack<Point>();
		this.offset = new Point();
		
		this.paint = new Paint();
		this.paint.setColor(0xffff0000);
		
		this.pathPaint = new Paint();
		this.pathPaint.setColor(0xfffff000);
		this.pathPaint.setStrokeCap(Cap.ROUND);
		this.pathPaint.setAntiAlias(true);
	}
	
	public void setRadius(int cellSize){
		this.radius = (int) (cellSize/3 - cellSize*.1);
		this.offset.x = (int) (this.radius + cellSize*.3);
		this.offset.y = this.offset.x;
	}
}
