package code;


import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import code.Scene.Polygon;

/**
 * Creates the GUI for this program using swing.
 */

public class Renderer extends GUI {
    private Scene scene;
    private float xRot;
    private float yRot;
    private boolean loaded;
    Vector3D secondLight;
    Vector3D thirdLight;
    Vector3D fourthLight;

	/**
	 * Loads a data file containing the triangle vertices used to renfer the model.
	 * @param file The data file name.
	 */
	@Override
	protected void onLoad(File file) {
	    try {
	    	loaded = true;
            List<Polygon> polygons = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            String[] lightVec = line.split(" ");
            Vector3D lightPos = new Vector3D(Float.parseFloat(lightVec[0]),Float.parseFloat(lightVec[1]),Float.parseFloat(lightVec[2]));
            line = br.readLine();
            while(line!=null){
                String[] polyData = line.split(" ");
                float[] points = new float[9];
                int[] color = new int[3];
                for(int i = 0; i < 9; i++){
                    points[i] = Float.parseFloat(polyData[i]);
                }
                for(int i = 0; i < 3; i++){
                   color[i] = Integer.parseInt(polyData[i+9]);
                }

                polygons.add(new Polygon(points,color));

                line = br.readLine();
            }
            this.scene = new Scene(polygons,lightPos);
            this.scene = Pipeline.shiftAxis(scene);

            Vector3D lightZY = new Vector3D(0,scene.getLight().y,scene.getLight().z);
            Vector3D lightXZ = new Vector3D(scene.getLight().x,0,scene.getLight().z);
            float initialXRot = lightZY.cosTheta(new Vector3D(0,0,1));
            float initialYRot = lightXZ.cosTheta(new Vector3D(0,0,1));

            this.scene = Pipeline.rotateScene(scene,-initialXRot,-initialYRot);
            secondLight = Transform.newYRotation((float)Math.PI*2/3).multiply(scene.getLight());
            thirdLight = Transform.newYRotation((float)Math.PI*4/3 + yRot).multiply(scene.getLight());
            fourthLight = Transform.newXRotation((float)Math.PI*2/3 + xRot).multiply(scene.getLight());

            xRot = yRot = 0;
            Pipeline.maxMag = 0;//reset maxMag so it will be calculated for this new object

        }
        catch(IOException e){
	        e.printStackTrace();
        }

	}

	/**
	 * Hot keys for rotating the model.
	 * @param ev
	 */

	@Override
	protected void onKeyPress(KeyEvent ev) {
		if(ev.getKeyChar()=='a') yRot += Math.PI/10;
		if(ev.getKeyChar()=='d') yRot -= Math.PI/10;
		if(ev.getKeyChar()=='w') xRot -= Math.PI/10;
		if(ev.getKeyChar()=='s') xRot += Math.PI/10;

	}



	@Override
	protected BufferedImage render() {
		if(!loaded) return null; //prevents rendering before an image is loaded
		List<Color> colors = new ArrayList<>();
		List<EdgeList> edgeLists = new ArrayList<>();

		// we create a new copy of scene so we can translate/scale without losing the original data for future rotations
		 scene = Pipeline.rotateScene(scene,xRot,yRot);


        Scene sceneCopy = Pipeline.translateScene(scene);
		sceneCopy = Pipeline.scaleScene(sceneCopy);

		List<Polygon> visiblePolys = sceneCopy.getPolygons().stream().filter(p->!Pipeline.isHidden(p)).collect(Collectors.toList());

		HashMap<Vector3D,Vector3D> vertexIntensities = Pipeline.buildVertexNormalMap(visiblePolys);


		for(Polygon p:visiblePolys){
			colors.add(Pipeline.getAmbientShading(p,new Color(getAmbientLight()[0],getAmbientLight()[1],getAmbientLight()[2])));
			edgeLists.add(Pipeline.computeEdgeList(p,vertexIntensities));
		}


		Color[][] zbuffer = new Color[CANVAS_HEIGHT+50][CANVAS_WIDTH+50];
		float[][] zdepth = new float[CANVAS_HEIGHT+50][CANVAS_WIDTH+50];
		for(int i = 0;i < zbuffer.length; i++)
			for(int j = 0; j<zbuffer[0].length;j++)
				zbuffer[i][j] = Color.GRAY;
		for(int i = 0;i < zdepth.length; i++)
			for(int j = 0; j<zdepth[0].length;j++)
				zdepth[i][j] = Float.MAX_VALUE;
		//create map to store light positions and colors
		HashMap<Vector3D,Color> lights = new HashMap<>();
        //create light vectors and get colors from sliders in GUI
		secondLight = Transform.newYRotation(yRot).multiply(secondLight);
        secondLight = Transform.newXRotation(xRot).multiply(secondLight);

		thirdLight = Transform.newYRotation(yRot).multiply(thirdLight);
        thirdLight = Transform.newXRotation(xRot).multiply(thirdLight);

        fourthLight = Transform.newXRotation(xRot).multiply(fourthLight);
		fourthLight = Transform.newYRotation(yRot).multiply(fourthLight);

		Color firLightCol = new Color(getIncidentLight()[0]/255f,getIncidentLight()[1]/255f,getIncidentLight()[2]/255f);
		Color secLightCol = new Color(getIncidentLight2()[0]/255f,getIncidentLight2()[1]/255f,getIncidentLight2()[2]/255f);
        Color thiLightCol = new Color(getIncidentLight3()[0]/255f,getIncidentLight3()[1]/255f,getIncidentLight3()[2]/255f);
        Color fouLightCol = new Color(getIncidentLight4()[0]/255f,getIncidentLight4()[1]/255f,getIncidentLight4()[2]/255f);
        //add lights and colors to map
        lights.put(sceneCopy.getLight(),firLightCol);
        lights.put(secondLight,secLightCol);
        lights.put(thirdLight,thiLightCol);
        lights.put(fourthLight,fouLightCol);

		for(int i = 0; i < colors.size();i++){
			Pipeline.computeZBuffer(zbuffer,zdepth,edgeLists.get(i),colors.get(i),visiblePolys.get(i),lights);
		}

        xRot = yRot = 0;

		return convertBitmapToImage(zbuffer);

	}

	/**
	 * Converts a 2D array of Colors to a BufferedImage. Assumes that bitmap is
	 * indexed by column then row and has imageHeight rows and imageWidth
	 * columns. Note that image.setRGB requires x (col) and y (row) are given in
	 * that order.
	 */
	private BufferedImage convertBitmapToImage(Color[][] bitmap) {
		BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				image.setRGB(x, y, bitmap[x][y].getRGB());
			}
		}
		return image;
	}

	public static void main(String[] args) {
		new Renderer();
	}
}

// code for comp261 assignments
