package jiongye.app.livewallpaper.mazepaper;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

public class Wall extends Rectangle {
	public boolean shouldDraw = true;

	public Wall(float x, float y, float width, float height, VertexBufferObjectManager vertexBufferObjectManager) {
		super(x, y, width, height, vertexBufferObjectManager);
		this.setColor(Color.WHITE);
	}
	
	public void hide() {
		this.setVisible(false);
	}
	
	public void show() {
		this.setVisible(true);
	}
}
