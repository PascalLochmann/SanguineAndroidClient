package de.pl.sanguine.mesh;

public class Point {
	public Vertex vertex;
	public Color color;

	public Point() {

	}

	public Point(float x, float y, float z, float red, float green, float blue, float alpha) {
		this.vertex = new Vertex();
		this.vertex.x = x;
		this.vertex.y = y;
		this.vertex.z = z;

		this.color = new Color();
		this.color.red = red;
		this.color.green = green;
		this.color.blue = blue;
		this.color.alpha = alpha;

	}
}
