package jiongye.app.livewallpaper.mazepaper;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Wall extends Rectangle {
	public Body body;
	
	public Wall(float x, float y, float width, float height, VertexBufferObjectManager vertexBufferObjectManager) {
		super(x, y, width, height, vertexBufferObjectManager);
		this.setColor(new Color(1f, 1f, 1f, 0.5f));
		
		this.body = null;
	}
	
	public void hide() {
		this.setVisible(false);
	}
	
	public void show() {
		this.setVisible(true);
	}
	
	public void setBodyFixture(FixtureDef newFixtureDef) {
		if(this.body != null){
			this.body.destroyFixture(null);
			this.body.createFixture(newFixtureDef);
		}
	}
}
