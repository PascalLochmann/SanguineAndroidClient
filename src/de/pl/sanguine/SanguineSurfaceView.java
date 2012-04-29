package de.pl.sanguine;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

class SanguineSurfaceView extends GLSurfaceView {

	private final int OGL_ES_VERSION = 2;

	private final float TOUCH_SCALE_FACTOR = 180.0f / 320;

	private SanguineRenderer mRenderer;

	// private float mPreviousX;
	// private float mPreviousY;

	public SanguineSurfaceView(Context context) {
		super(context);

		// Create an OpenGL ES 2.0 context
		setEGLContextClientVersion(OGL_ES_VERSION);

		// Set the Renderer for drawing on the GLSurfaceView
		mRenderer = new SanguineRenderer();
		setRenderer(mRenderer);

		// Render the view only when there is a change
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		// MotionEvent reports input details from the touch screen
		// and other input controls. In this case, you are only
		// interested in events where the touch position changed.

		float x = e.getX();
		float y = e.getY();

		switch (e.getAction()) {
		case MotionEvent.ACTION_MOVE:

			/*
			 * float dx = x - mPreviousX; float dy = y - mPreviousY;
			 */
			// mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;

			// mRenderer.mX = x / 1000;
			// mRenderer.mY = y / 1000;

			requestRender();
		}

		// mPreviousX = x;
		// mPreviousY = y;
		return true;
	}

	public void repaintTest(int x, int y) {

		mRenderer.setTestData(x, y);

		requestRender();
	}

	// @Override
	// public boolean onTouchEvent(MotionEvent e) {
	// // MotionEvent reports input details from the touch screen
	// // and other input controls. In this case, you are only
	// // interested in events where the touch position changed.
	//
	// float x = e.getX();
	// float y = e.getY();
	//
	// switch (e.getAction()) {
	// case MotionEvent.ACTION_MOVE:
	//
	// float dx = x - mPreviousX;
	// float dy = y - mPreviousY;
	//
	// // reverse direction of rotation above the mid-line
	// if (y > getHeight() / 2) {
	// dx = dx * -1;
	// }
	//
	// // reverse direction of rotation to left of the mid-line
	// if (x < getWidth() / 2) {
	// dy = dy * -1;
	// }
	//
	// mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;
	// requestRender();
	// }
	//
	// mPreviousX = x;
	// mPreviousY = y;
	// return true;
	// }

}