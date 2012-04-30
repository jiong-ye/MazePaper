package jiongye.app.livewallpaper.mazepaper;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Wall extends Rectangle {
	public boolean shouldDraw = true;

	public Wall(float x, float y, float width, float height, VertexBufferObjectManager vertexBufferObjectManager) {
		super(x, y, width, height, vertexBufferObjectManager);
	}

	@Override
	protected void preDraw(final GLState pGLState, final Camera pCamera) {
		if (this.shouldDraw) {
			super.preDraw(pGLState, pCamera);

			this.mRectangleVertexBufferObject.bind(pGLState, this.mShaderProgram);
		}
	}
}
