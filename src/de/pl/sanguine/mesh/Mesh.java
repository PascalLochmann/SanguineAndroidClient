package de.pl.sanguine.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Mesh {

	/** How many bytes per float. */
	private static final int BYTES_PER_FLOAT = 4;

	/** Position of the vertices in the data buffer **/
	public static final int DATA_POSITION_VERTICES = 0;

	/** Position of the colors in the data buffer **/
	public static final int DATA_POSITION_COLORS = 3;

	/** Position of the vertices in the data buffer **/
	public static final int DATA_SIZE_VERTICES = 3;

	/** Position of the colors in the data buffer **/
	public static final int DATA_SIZE_COLORS = 4;

	/**
	 * chunk size; number of bytes for one data row; How many elements per
	 * vertex.
	 **/
	public static final int DATA_STRIDE_BYTES = (DATA_SIZE_VERTICES + DATA_SIZE_COLORS) * BYTES_PER_FLOAT;

	public FloatBuffer data;

	public float x;

	public float y;

	public float z;

	public int numberOfVertecies;

	public Mesh[] subMeshs = null;

	private Mesh() {

	}

	protected static Mesh create(Point[] points, Vertex origin) {
		Mesh mesh = new Mesh();

		mesh.x = origin.x;
		mesh.y = origin.y;
		mesh.z = origin.z;

		mesh.data = ByteBuffer.allocateDirect(points.length * DATA_STRIDE_BYTES).order(ByteOrder.nativeOrder())
				.asFloatBuffer();

		mesh.numberOfVertecies = points.length / 3;

		for (int i = 0; i < points.length; i++) {
			// vertex data
			mesh.data.put(points[i].vertex.x);
			mesh.data.put(points[i].vertex.y);
			mesh.data.put(points[i].vertex.z);

			// color data
			mesh.data.put(points[i].color.red);
			mesh.data.put(points[i].color.green);
			mesh.data.put(points[i].color.blue);
			mesh.data.put(points[i].color.alpha);
		}
		return mesh;

	}
}
