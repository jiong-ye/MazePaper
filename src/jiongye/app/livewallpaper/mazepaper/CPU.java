package jiongye.app.livewallpaper.mazepaper;

import java.util.Random;
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
	
	public boolean deadend;
	
	public CPU() {
		this.radius = 5;
		this.track = new Stack<Point>();
		this.offset = new Point();
		
		Random rand = new Random();
		int r = rand.nextInt(255);
		int g = rand.nextInt(255);
		int b = rand.nextInt(255);
		
		this.paint = new Paint();
		this.paint.setARGB(255, r, g, b);
		
		this.pathPaint = new Paint();
		this.pathPaint.setARGB(200, r, g, b);
		this.pathPaint.setStrokeWidth(1f);
		this.pathPaint.setStrokeCap(Cap.ROUND);
		this.pathPaint.setAntiAlias(true);
		
		this.deadend = false;
	}
	
	public void setRadius(int cellSize){
		this.radius = (int) (cellSize/3 - cellSize*.1);
		this.offset.x = (int) (this.radius + cellSize*.3);
		this.offset.y = this.offset.x;
	}
}
