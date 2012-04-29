package de.pl.sanguine;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.SystemClock;
import de.pl.sanguine.mesh.Mesh;
import de.pl.sanguine.mesh.MeshFactory;
import de.pl.sanguine.mesh.Point;
import de.pl.sanguine.mesh.Vertex;

public class SanguineRenderer implements Renderer {

	/*
	 * VERTEX SHADER CODE
	 */
	/* A constant representing the combined model/view/projection matrix. */
	final String vertexShaderCode = "" + "uniform mat4 u_MVPMatrix;      \n"

	/* Per-vertex position information we will pass in. */
	+ "attribute vec4 a_Position;     \n"

	/* Per-vertex color information we will pass in. */
	+ "attribute vec4 a_Color;        \n"

	/* This will be passed into the fragment shader. */
	+ "varying vec4 v_Color;          \n"

	/* The entry point for our vertex shader. */
	+ "		void main() {             \n"

	/*
	 * Pass the color through to the fragment shader. It will be interpolated
	 * across the triangle.
	 */
	+ "   v_Color = a_Color;          \n"

	/*
	 * gl_Position is a special variable used to store the final position.
	 * Multiply the vertex by the matrix to get the final point in normalized
	 * screen coordinates
	 */
	+ "   gl_Position = u_MVPMatrix  * a_Position; }";

	/*
	 * FRAGMENT SHADER CODE
	 */
	final String fragmentShaderCode = "precision mediump float;       \n"
	/*
	 * Set the default precision to medium. We don't need as high of a precision
	 * in the fragment shader.
	 */

	/*
	 * This is the color from the vertex shader interpolated across the triangle
	 * per fragment.
	 */
	+ "varying vec4 v_Color;          \n"
	/* The entry point for our fragment shader. */
	+ "void main() {                   \n"
	/* Pass the color directly through the pipeline. */
	+ "   gl_FragColor = v_Color; } ";

	/**
	 * Store the model matrix. This matrix is used to move models from object
	 * space (where each model can be thought of being located at the center of
	 * the universe) to world space.
	 */
	private float[] mModelMatrix = new float[16];

	/**
	 * Store the view matrix. This can be thought of as our camera. This matrix
	 * transforms world space to eye space; it positions things relative to our
	 * eye.
	 */
	private float[] mViewMatrix = new float[16];

	/**
	 * Store the projection matrix. This is used to project the scene onto a 2D
	 * viewport.
	 */
	private float[] mProjectionMatrix = new float[16];

	/**
	 * Allocate storage for the final combined matrix. This will be passed into
	 * the shader program.
	 */
	private float[] mMVPMatrix = new float[16];

	/** This will be used to pass in the transformation matrix. */
	private int mMVPMatrixHandle;

	/** This will be used to pass in model position information. */
	private int mPositionHandle;

	/** This will be used to pass in model color information. */
	private int mColorHandle;

	// touch
	public float mX;
	public float mY;

	private List<Mesh> mListOfMeshs = new ArrayList<Mesh>();

	public void setTestData(int x, int y) {/*
											 * System.out.println(x + " / " +
											 * y); this.x = x; this.y = y;
											 */
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
		// Set the background clear color to gray.
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

		// Position the eye behind the origin.
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 1.5f;

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -5.0f;

		// Set our up vector. This is where our head would be pointing were we
		// holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		// Set the view matrix. This matrix can be said to represent the camera
		// position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination
		// of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices
		// separately if we choose.
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

		int vertexShaderHandle = createShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShaderHandle = createShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

		// Create a program object and store the handle to it.
		int programHandle = GLES20.glCreateProgram();

		if (programHandle != 0) {
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);

			// Bind attributes
			GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
			GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) {
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}

		if (programHandle == 0) {
			throw new RuntimeException("Error creating program.");
		}

		// Set program handles. These will later be used to pass in values to
		// the program.
		mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
		mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
		mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

		// Tell OpenGL to use this program when rendering.
		GLES20.glUseProgram(programHandle);

		GLES20.glEnable(GL10.GL_CULL_FACE);

		initModel();
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

		// Create a new perspective projection matrix. The height will stay the
		// same while the width will vary as per aspect ratio.
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 10.0f;

		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);

		// Position-Vector of eye, look to and up
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 4.0f;

		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -5.0f;

		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		// define a camera view matrix.
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

	}

	@Override
	public void onDrawFrame(GL10 glUnused) {
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

		// Do a complete rotation every 10 seconds.
		long time = SystemClock.uptimeMillis() % 20000L;
		float angleInDegrees = (360.0f / 20000.0f) * ((int) time);

		// Draw the triangle facing straight on.
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
		// drawTriangle(mTriangle1Vertices);

		// Draw one translated a bit down and rotated to be flat on the ground.
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0.0f, -1.0f, 0.0f);
		Matrix.rotateM(mModelMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f);
		Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
		// drawTriangle(mTriangle2Vertices);

		// Draw one translated a bit to the right and rotated to be facing to
		// the left.
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 1.0f, 0.0f, 0.0f);
		Matrix.rotateM(mModelMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f);
		Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
		// drawTriangle(mTriangle3Vertices);

		/* ************************************** TEST ********************** */

		// Draw one translated a bit to the right and rotated to be facing to
		// the left.
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, -1.0f, -1.0f, 0.0f);
		// Matrix.rotateM(mModelMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f);
		// Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
		// drawTriangle(mTestTriangle1Vertices);

		// Draw one translated a bit to the right and rotated to be facing to
		// the left.
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, -1.0f, -1.0f, 0.0f);
		// Matrix.rotateM(mModelMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f);
		// Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
		// drawTriangle(mTestTriangle2Vertices);

		for (Mesh mesh : mListOfMeshs) {
			// Draw one translated a bit to the right and rotated to be facing
			// to
			// the left.
			Matrix.setIdentityM(mModelMatrix, 0);
			// Matrix.translateM(mModelMatrix, 0, 1.0f, 1.0f, 0.0f);
			Matrix.translateM(mModelMatrix, 0, mesh.x, mesh.y, mesh.z);
			// Matrix.rotateM(mModelMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f);
			Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 1.0f, 1.0f);
			drawMesh(mesh);
		}

	}

	/**
	 * Draws a triangle from the given vertex data.
	 * 
	 * @param aTriangleBuffer
	 *            The buffer containing the vertex data.
	 */
	/*
	 * private void drawTriangle(final FloatBuffer aTriangleBuffer) { // Pass in
	 * the position information aTriangleBuffer.position(mPositionOffset);
	 * GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
	 * GLES20.GL_FLOAT, false, mStrideBytes, aTriangleBuffer);
	 * 
	 * GLES20.glEnableVertexAttribArray(mPositionHandle);
	 * 
	 * // Pass in the color information aTriangleBuffer.position(mColorOffset);
	 * GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize,
	 * GLES20.GL_FLOAT, false, mStrideBytes, aTriangleBuffer);
	 * 
	 * GLES20.glEnableVertexAttribArray(mColorHandle);
	 * 
	 * // This multiplies the view matrix by the model matrix, and stores the //
	 * result in the MVP matrix // (which currently contains model * view).
	 * Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
	 * 
	 * // This multiplies the modelview matrix by the projection matrix, and //
	 * stores the result in the MVP matrix // (which now contains model * view *
	 * projection). Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0,
	 * mMVPMatrix, 0);
	 * 
	 * GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
	 * GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3); }
	 */

	/**
	 * Draws a mesh from the given vertex data.
	 * 
	 * @param aTriangleBuffer
	 *            The buffer containing the vertex data.
	 */
	private void drawMesh(final Mesh mesh) {
		// Pass in the position information
		mesh.data.position(Mesh.DATA_POSITION_VERTICES);
		GLES20.glVertexAttribPointer(mPositionHandle, Mesh.DATA_SIZE_VERTICES, GLES20.GL_FLOAT, false,
				Mesh.DATA_STRIDE_BYTES, mesh.data);

		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Pass in the color information
		mesh.data.position(Mesh.DATA_POSITION_COLORS);
		GLES20.glVertexAttribPointer(mColorHandle, Mesh.DATA_SIZE_COLORS, GLES20.GL_FLOAT, false,
				Mesh.DATA_STRIDE_BYTES, mesh.data);

		GLES20.glEnableVertexAttribArray(mColorHandle);

		// This multiplies the view matrix by the model matrix, and stores the
		// result in the MVP matrix
		// (which currently contains model * view).
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

		// This multiplies the modelview matrix by the projection matrix, and
		// stores the result in the MVP matrix
		// (which now contains model * view * projection).
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mesh.numberOfVertecies * 3);

		if (mesh.subMeshs != null) {
			final int numberOfSubMeshs = mesh.subMeshs.length;

			for (int i = 0; i < numberOfSubMeshs; i++) {
				drawMesh(mesh.subMeshs[i]);
			}

		}
	}

	/**
	 * Method to create a handle for an OopenGL ES 2.0 shader for a given shader
	 * type and shader code.
	 * 
	 * @param type
	 *            - OpenGL ES 2.0 shader type, e.g. GLES20.GL_VERTEX_SHADER
	 * @param shaderCode
	 *            - block of OpenGL ES 2.0 shader code
	 * @return handle of shader
	 */
	private int createShader(int type, String shaderCode) {

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shaderHandle = GLES20.glCreateShader(type);

		// handle is zero if shader could not created
		if (shaderHandle != 0) {
			// add the source code to the shader and compile it
			GLES20.glShaderSource(shaderHandle, shaderCode);

			GLES20.glCompileShader(shaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) {
				GLES20.glDeleteShader(shaderHandle);
				shaderHandle = 0;
			}
		}

		if (shaderHandle == 0) {
			throw new RuntimeException("Error creating vertex shader.");
		}

		return shaderHandle;
	}

	/**
	 * Initialize the model data.
	 */

	private void initModel() {

		float offsetX = 0;// -6.5f;
		float offsetY = 0;// 3.5f;

		for (int i = 0; i < 1; i++) {

			for (int j = 0; j < 1; j++) {

				float width = 01.3f;
				float height = 01.4f;
				float x = 0.0f + width * 3 * i + offsetX;
				float y = 0.0f - height * 3 * j + offsetY;
				float z = -1.0f * i * j * 0.02f;

				Vertex origin = new Vertex();
				origin.x = x;
				origin.y = y;
				origin.z = z;
				Point[] points = new Point[12];

				// Triangle 1
				points[0] = new Point(-1.0f * width, 1.0f * height, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f);
				points[1] = new Point(1.0f * width, -1.0f * height, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
				points[2] = new Point(1.0f * width, 1.0f * height, 0.0f, 0.5f, 0.5f, 0.0f, 0.0f);

				// Triangle 2
				points[3] = new Point(-1.0f * width, 1.0f * height, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f);
				points[4] = new Point(-1.0f * width, -1.0f * height, 0.0f, 0.5f, 0.5f, 0.0f, 0.0f);
				points[5] = new Point(1.0f * width, -1.0f * height, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);

				// Triangle 3
				points[6] = new Point(-1.0f * width, 1.0f * height, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
				points[7] = new Point(1.0f * width, 1.0f * height, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f);
				points[8] = new Point(1.0f * width, -1.0f * height, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);

				// Triangle 4
				points[9] = new Point(-1.0f * width, 1.0f * height, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
				points[10] = new Point(1.0f * width, -1.0f * height, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
				points[11] = new Point(-1.0f * width, -1.0f * height, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f);

				Mesh mesh = (new MeshFactory()).create(points, origin);

				mListOfMeshs.add(mesh);
			}
		}

	}
}
