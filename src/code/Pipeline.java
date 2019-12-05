package code;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;


/**
 * The Pipeline class has methods for all the major components of the
 * rendering pipeline.
 */
public class Pipeline {

	public static float maxMag = 0;


	/**
	 * Returns true if the given polygon is facing away from the camera (and so
	 * should be hidden), and false otherwise.
	 */
	public static boolean isHidden(Scene.Polygon poly) {

		return poly.getNormal().z > 0;
	}

	/**
	 * only calculates ambient portion of shading, this is because we don't want to spend time calculating pixel by pixel
	 * shading until we know the polygon isn't overlapped.
	 *
	 *            The color of that directional light.
	 * @param ambientLight
	 *            The ambient light in the scene, i.e. light that doesn't depend
	 *            on the direction.
	 */
	public static Color getAmbientShading(Scene.Polygon poly, Color ambientLight) {

		int r,g,b;
		r = ambientLight.getRed()* poly.getReflectance().getRed()/255;
		g = ambientLight.getGreen()* poly.getReflectance().getGreen()/255;
		b = ambientLight.getBlue()* poly.getReflectance().getBlue()/255;

		//cap rgb values at 255
		r = Math.min(r,255);
		g = Math.min(g,255);
		b = Math.min(b,255);


		return new Color(r,g,b);
	}
	/**
	 * This method rotates the polygons and light such that the viewer is
	 * looking down the Z-axis. The idea is that it returns an entirely new
	 * Scene object, filled with new Polygons, that have been rotated.
	 * 
	 * @param scene
	 *            The original Scene.
	 * @param xRot
	 *            An angle describing the viewer's rotation in the YZ-plane (i.e
	 *            around the X-axis).
	 * @param yRot
	 *            An angle describing the viewer's rotation in the XZ-plane (i.e
	 *            around the Y-axis).
	 * @return A new Scene where all the polygons and the light source have been
	 *         rotated accordingly.
	 */
	public static Scene rotateScene(Scene scene, float xRot, float yRot) {
		List<Scene.Polygon> rotatedPoly = new ArrayList<>();

		Transform t = Transform.newXRotation(xRot);
		Transform t2 = Transform.newYRotation(yRot);
		Transform t3 = t.compose(t2);

		for(Scene.Polygon p: scene.getPolygons()){
		    Vector3D[] v = p.getVertices();

			Vector3D tv1 = t3.multiply(v[0]);
			Vector3D tv2 = t3.multiply(v[1]);
			Vector3D tv3 = t3.multiply(v[2]);
			rotatedPoly.add(new Scene.Polygon(tv1,tv2,tv3,p.reflectance));
		}

		Vector3D lightPos = t3.multiply(scene.getLight());


		return new Scene(rotatedPoly,lightPos);


	}

	/**
	 * This translates the scene by the appropriate amount.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene translateScene(Scene scene) {

		//find the maximum magnitude of the vectors, this value is used to ensure the model always fits in canvas
		List<Scene.Polygon> polygons = scene.getPolygons();
		for(Scene.Polygon p: polygons){
			for(Vector3D v:p.getVertices()){
				if(v.mag>maxMag) maxMag = v.mag;
			}
		}


		List<Scene.Polygon> transPolygons= new ArrayList<>();
		Transform transform = Transform.newTranslation(maxMag,maxMag,0);
		for(Scene.Polygon p:polygons){
			Vector3D[] v = p.getVertices();
			transPolygons.add(new Scene.Polygon(transform.multiply(v[0]),transform.multiply(v[1]),transform.multiply(v[2]),p.getReflectance()));
		}
		return new Scene(transPolygons,scene.getLight());

	}

	/**
	 * This scales the scene to fit inside the GUI window.
	 *
	 * @param scene
	 * @return
	 */
	public static Scene scaleScene(Scene scene) {
		List<Scene.Polygon> polygons = scene.getPolygons();

		List<Scene.Polygon> transPolygons= new ArrayList<>();

		float scale = (GUI.CANVAS_WIDTH-1)/maxMag/2;


		Transform transform = Transform.newScale(scale,scale,scale);
		for(Scene.Polygon p:polygons){
			Vector3D[] v = p.getVertices();
			transPolygons.add(new Scene.Polygon(transform.multiply(v[0]),transform.multiply(v[1]),transform.multiply(v[2]),p.reflectance));
		}

		return new Scene(transPolygons,scene.getLight());

	}

	/**
	 * Computes the edgelist of a single provided polygon.
	 */
	public static EdgeList computeEdgeList(Scene.Polygon poly, HashMap<Vector3D,Vector3D> vertexIntesnity) {
		ArrayList<ArrayList<Vector3D>> edges = new ArrayList<>();
		edges.add(new ArrayList<>());
		edges.add(new ArrayList<>());
		edges.add(new ArrayList<>());
		edges.get(0).add(poly.getVertices()[0]);
		edges.get(0).add(poly.getVertices()[1]);
		edges.get(1).add(poly.getVertices()[1]);
		edges.get(1).add(poly.getVertices()[2]);
		edges.get(2).add(poly.getVertices()[2]);
		edges.get(2).add(poly.getVertices()[0]);


		List<Float> verticeY = Arrays.stream(poly.getVertices()).map(p -> p.y).collect(Collectors.toList());
		int startY = Math.round(Collections.min(verticeY));
		int endY = Math.round(Collections.max(verticeY));

		EdgeList edgeList = new EdgeList(startY,endY);
		int avgY = (int)(poly.getVertices()[0].y+poly.getVertices()[1].y+poly.getVertices()[2].y)/3;


		for(ArrayList<Vector3D> edge:edges) {
			float slopeX, slopeZ;
			Vector3D a = edge.get(0);
			Vector3D b = edge.get(1);
			Vector3D aNormal = vertexIntesnity.get(a);
			Vector3D bNormal = vertexIntesnity.get(b);
			int yChange = (Math.round(b.y) - Math.round(a.y));
            if(Math.round(b.y)-Math.round(a.y)!=0) {
				slopeX = (b.x - a.x) / yChange;
				slopeZ = (b.z - a.z) / yChange;
			}
			else{
				slopeX = (b.x - a.x);
				slopeZ = (b.z - a.z);
			}

			float z = a.z;
			float x = a.x;
			int y = Math.round(a.y);
			Vector3D normal = aNormal;
			if (a.y <= b.y) {
				while (y < Math.round(b.y)||(y == Math.round(b.y) && y > avgY) ){
					edgeList.addLeftValues(y, x, z, normal);
					x = x + slopeX;
					z = z + slopeZ;
					y++;
					normal = aNormal.mult(1-(y-a.y)/(yChange)).plus(bNormal.mult((y-a.y)/(yChange)));
				}
			} else  {
				while (y >= Math.round(b.y)) {
					edgeList.addRightValues(y, x, z, normal);
					x = x - slopeX;
					z = z - slopeZ;
					y--;
					normal = aNormal.mult(1-(a.y-y)/(a.y-b.y)).plus(bNormal.mult((a.y-y)/(a.y-b.y)));
				}
			}
		}

		return edgeList;
	}

	/**
	 *
	 * 
	 * @param zbuffer
	 *            A double array of colours representing the Color at each pixel
	 *            so far.
	 * @param zdepth
	 *            A double array of floats storing the z-value of each pixel
	 *            that has been coloured in so far.
	 * @param polyEdgeList
	 *            The edgelist of the polygon to add into the zbuffer.
	 * @param polyColor
	 *            The colour of the polygon to add into the zbuffer.
	 */
	public static void computeZBuffer(Color[][] zbuffer, float[][] zdepth, EdgeList polyEdgeList, Color polyColor, Scene.Polygon p,
									  HashMap<Vector3D,Color> lights) {
		for(int i = polyEdgeList.getStartY(); i < polyEdgeList.getEndY(); i++){
			float slope = (polyEdgeList.getRightZ(i)-polyEdgeList.getLeftZ(i))/(polyEdgeList.getRightX(i)-polyEdgeList.getLeftX(i));
			int leftX = Math.round(polyEdgeList.getLeftX(i));
			int rightX = Math.round(polyEdgeList.getRightX(i));
			int x = Math.round(polyEdgeList.getLeftX(i));


			Vector3D normal = polyEdgeList.getLeftXLight(i);
			Vector3D leftNormal =  polyEdgeList.getLeftXLight(i);
			Vector3D rightNormal = polyEdgeList.getRightXLight(i);

			float z = polyEdgeList.getLeftZ(i) + slope * (x -leftX);
			while(x <= rightX){
				if(z < zdepth[x][i]){
					zdepth[x][i] = z;

					float redIntensity = 0f,greenIntensity = 0f,blueIntensity = 0f;
					//sums the products of cosTheta by color intensity for all the lights
					for(Vector3D v: lights.keySet()){
						redIntensity += Math.max(0,normal.cosTheta(v)) * lights.get(v).getRed()/255f;
						greenIntensity += Math.max(0,normal.cosTheta(v)) * lights.get(v).getGreen()/255f;
						blueIntensity += Math.max(0,normal.cosTheta(v)) * lights.get(v).getBlue()/255f;
					}

					int r = polyColor.getRed() + (int)(p.getReflectance().getRed()*redIntensity);
					int g = polyColor.getGreen() + (int)(p.getReflectance().getGreen()*greenIntensity);
					int b = polyColor.getBlue() +(int)(p.getReflectance().getBlue()*blueIntensity);
					r = Math.min(r,255);
					g = Math.min(g,255);
					b = Math.min(b,255);
					r = Math.max(r,0);
					g = Math.max(g,0);
					b = Math.max(b,0);
					zbuffer[x][i] = new Color(r,g,b);
				}
				z = z + slope;
				x++;
				normal = leftNormal.mult(1f-(float)(x-leftX)/(rightX-leftX)).plus(rightNormal.mult((float)(x-leftX)/(rightX-leftX)));
			}
		}
	}

	/**
	 * Shifts the axis so 0,0,0 becomes the centre of the object
	 * @param scene
	 * @return
	 */

	public static Scene shiftAxis(Scene scene){

		float sumX =0,sumY = 0,sumZ = 0, count =0;
		for(Scene.Polygon p: scene.getPolygons()){
			for(Vector3D v: p.getVertices()){
				sumX += v.x;
				sumY += v.y;
				sumZ += v.z;
				count++;
			}
		}
		Transform t = Transform.newTranslation(-sumX/count,-sumY/count,-sumZ/count);
		List<Scene.Polygon> polygons = new ArrayList<>();
		for(Scene.Polygon p:scene.getPolygons()){
			Vector3D[] v = p.getVertices();
			Vector3D tv1 = t.multiply(v[0]);
			Vector3D tv2 = t.multiply(v[1]);
			Vector3D tv3 = t.multiply(v[2]);
			polygons.add(new Scene.Polygon(tv1,tv2,tv3,p.getReflectance()));
		}

		Vector3D light = Transform.newScale(10000,10000,10000).multiply(scene.getLight());


		return new Scene(polygons,light);
	}


	/**
	 *
	 *
	 * @param polygons
	 * @return
	 */

	public static HashMap<Vector3D,Vector3D> buildVertexNormalMap(List<Scene.Polygon> polygons){
		//builds a vertice map that contains all the adjacent polygons of each vertex.
		HashMap<Vector3D,Set<Scene.Polygon>> verticeMap = new HashMap<>();
		for(Scene.Polygon p: polygons){
			for(Vector3D v:p.getVertices()){
				if(verticeMap.get(v)==null){
					HashSet<Scene.Polygon> polys = new HashSet<>();
					verticeMap.put(v,polys);
				}
				verticeMap.get(v).add(p);
			}
		}

        HashMap<Vector3D,Vector3D> vectorNormals = new HashMap<>();
		//average the normals of the adjacent polygons
		for(Vector3D v: verticeMap.keySet()){
			Vector3D v3 = new Vector3D(0,0,0);
			for(Scene.Polygon p:verticeMap.get(v)){
				v3 = v3.plus(p.getNormal());
			}

			v3.mult(1f/(float)verticeMap.get(v).size());
			vectorNormals.put(v,v3);
		}

		return  vectorNormals;
	}
}

