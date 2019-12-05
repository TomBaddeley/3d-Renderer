package code;

import java.util.HashMap;

/**
 * EdgeList stores the data for the edge list of a single polygon in your
 * scene. An Edge list stores the coordinates of every pixel in the edge and the
 * color of every pixel at each edge/
 *
 */
public class EdgeList {
	private int startY, endY;
	private HashMap<Integer,Float> leftX = new HashMap<>(),leftZ = new HashMap<>(),rightX = new HashMap<>(),rightZ = new HashMap<>();
	private HashMap<Integer,Vector3D> leftXLight = new HashMap<>(), rightXLight = new HashMap<>();//light values stored for color interpolation
	public EdgeList(int startY, int endY) {
		this.startY = startY;
		this.endY = endY;
	}

	public void addLeftValues(int y,float xLeft,float zLeft,Vector3D light){
		leftX.put(y,xLeft);
		leftZ.put(y,zLeft);
        leftXLight.put(y,light);
	}

	public void addRightValues(int y,float xRight,float zRight,Vector3D light){
		rightX.put(y,xRight);
		rightZ.put(y,zRight);
		rightXLight.put(y,light);
	}

	public Vector3D getLeftXLight(int y){
		return leftXLight.get(y);
	}

	public Vector3D getRightXLight(int y){
		return rightXLight.get(y);
	}

	public int getStartY() {

		return startY;
	}

	public int getEndY() {

		return endY;
	}

	public float getLeftX(int y) {

		return leftX.get(y);
	}

	public float getRightX(int y) {

		return rightX.get(y);
	}

	public float getLeftZ(int y) {

		return leftZ.get(y);
	}

	public float getRightZ(int y) {

		return rightZ.get(y);
	}

	public String toString() {
		StringBuilder result = new StringBuilder();

		for (int y = startY; y < endY; y++) {
			result.append("Left: ").append(getLeftX(y)).append(" Right: ").append(getRightX(y)).append("\n");
		}
		return result.toString();
	}
}

