package de.pl.sanguine;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class SanguineActivity extends Activity {

	private GLSurfaceView surfaceView;

	private Timer myTimer;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Create a SurfaceView instance and set it as the ContentView for this
		// Activity
		surfaceView = new SanguineSurfaceView(this);
		setContentView(surfaceView);

		myTimer = new Timer();

		myTimer.scheduleAtFixedRate(new TimerTask() {

			static final int X_MIN = 0;
			static final int X_MAX = 100;
			static final int X_STEP = 1;
			static final int Y_MIN = 0;
			static final int Y_MAX = 1;
			static final int Y_STEP = 1;

			private int x = X_MIN;
			private int y = Y_MIN;

			@Override
			public void run() {

				if (x < X_MAX) {
					x += X_STEP;
				} else if (y < Y_MAX) {
					y += Y_STEP;
					x = X_MIN;
				} else {
					x = X_MIN;
					y = Y_MIN;
				}
				((SanguineSurfaceView) surfaceView).repaintTest(x, y);

			}

		}, 0, 100);

	}

	@Override
	protected void onPause() {
		super.onPause();

		surfaceView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		surfaceView.onResume();
	}

}