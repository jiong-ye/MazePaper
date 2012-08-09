package jiongye.app.livewallpaper.mazepaper;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.util.color.Color;

import android.graphics.Point;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Human {
	private final int PIXEL_TO_METER_RATIO_DEFAULT = 32;
	
	public float width;
	public float height;
	
	public Rectangle rectangle;
	public Body body;
	
	public Human(){
		this.rectangle = null;
		this.body = null;
	}
	
	public Human(Rectangle rectangle, Color color){
		this.rectangle = rectangle;
		this.body = null;
		
		this.rectangle.setColor(color);
		this.width = this.rectangle.getWidth();
		this.height = this.rectangle.getHeight();
	}
		
	public void moveTo(Point point){
		final float widthD2 = this.width / 2;
		final float heightD2 = this.height / 2;
		final Vector2 v2 = Vector2Pool.obtain((point.x + widthD2) / this.PIXEL_TO_METER_RATIO_DEFAULT, (point.y + heightD2) / this.PIXEL_TO_METER_RATIO_DEFAULT);
		this.body.setTransform(v2, 0);
		Vector2Pool.recycle(v2);
	}
	
	public void moveTo(float x, float y){
		final float widthD2 = this.width / 2;
		final float heightD2 = this.height / 2;
		final Vector2 v2 = Vector2Pool.obtain((x + widthD2) / this.PIXEL_TO_METER_RATIO_DEFAULT, (y + heightD2) / this.PIXEL_TO_METER_RATIO_DEFAULT);
		this.body.setTransform(v2, 0);
		Vector2Pool.recycle(v2);
	}
	
	public boolean collidesWith(Rectangle rectangle){
		return this.rectangle.collidesWith(rectangle);
	}
}
