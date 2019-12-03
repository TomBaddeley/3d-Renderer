package code;

import java.util.HashMap;

/**
 * EdgeList should store the data for the edge list of a single polygon in your
 * scene. A few method stubs have been provided so that it can be tested, but
 * you'll need to fill in all the details.
 *
 * You'll probably want to add some setters as well as getters or, for example,
 * an addRow(y, xLeft, xRight, zLeft, zRight) method.
 */
public class EdgeList {
	private int startY, endY;
	private HashMap<Integer,Float> leftX = new HashMap<>(),leftZ = new HashMap<>(),rightX = new HashMap<>(),rightZ = new HashMap<>();
	private HashMap<Integer,Vector3D> leftXLight = new HashMap<>(), rightXLight = new HashMap<>();
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
		String result = "";

		for (int y = startY; y < endY; y++) {
			result += "Left: " + getLeftX(y) + " Right: " + getRightX(y) + "\n";
		}
		return result;
	}
}

// code for comp261 assignments
