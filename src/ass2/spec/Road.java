package ass2.spec;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;

import java.util.ArrayList;
import java.util.List;

/**
 * COMMENT: Comment Road 
 *
 * @author malcolmr
 */
public class Road {
  
  private List<Double> myPoints;
  private double myWidth;
  private Terrain myTerrain;
  
  //Constants
  private static final double DIVISION_FACTOR = 100; //the higher this is the more accurate/smooth road is
                                                      //but also increases computation cost
  private static final double ALTITUDE_OFFSET = 0.015; //to combat 'Z-fighting' of terrain and road
  
  /**
   * Create a new road starting at the specified point
   */
  public Road(double width, double x0, double y0) {
    myWidth = width;
    myPoints = new ArrayList<Double>();
    myPoints.add(x0);
    myPoints.add(y0);
  }
  
  /**
   * Create a new road with the specified spine
   *
   * @param width
   * @param spine
   */
  public Road(double width, double[] spine, Terrain terrain) {
    myWidth = width;
    myTerrain = terrain;
    myPoints = new ArrayList<Double>();
    for (int i = 0; i < spine.length; i++) {
      myPoints.add(spine[i]);
    }
  }
  
  /**
   * The width of the road.
   *
   * @return
   */
  public double width() {
    return myWidth;
  }
  
  /**
   * Add a new segment of road, beginning at the last point added and ending at (x3, y3).
   * (x1, y1) and (x2, y2) are interpolated as bezier control points.
   *
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @param x3
   * @param y3
   */
  public void addSegment(double x1, double y1, double x2, double y2, double x3, double y3) {
    myPoints.add(x1);
    myPoints.add(y1);
    myPoints.add(x2);
    myPoints.add(y2);
    myPoints.add(x3);
    myPoints.add(y3);
  }
  
  /**
   * Get the number of segments in the curve
   *
   * @return
   */
  public int size() {
    return myPoints.size() / 6;
  }
  
  /**
   * Get the specified control point.
   *
   * @param i
   * @return
   */
  public double[] controlPoint(int i) {
    double[] p = new double[2];
    p[0] = myPoints.get(i*2);
    p[1] = myPoints.get(i*2+1);
    return p;
  }
  
  /**
   * Get a point on the spine. The parameter t may vary from 0 to size().
   * Points on the kth segment take have parameters in the range (k, k+1).
   *
   * @param t
   * @return
   */
  public double[] point(double t) {
    int i = (int)Math.floor(t);
    t = t - i;
    
    i *= 6;
    
    double x0 = myPoints.get(i++);
    double y0 = myPoints.get(i++);
    double x1 = myPoints.get(i++);
    double y1 = myPoints.get(i++);
    double x2 = myPoints.get(i++);
    double y2 = myPoints.get(i++);
    double x3 = myPoints.get(i++);
    double y3 = myPoints.get(i++);
    
    double[] p = new double[2];
    
    p[0] = b(0, t) * x0 + b(1, t) * x1 + b(2, t) * x2 + b(3, t) * x3;
    p[1] = b(0, t) * y0 + b(1, t) * y1 + b(2, t) * y2 + b(3, t) * y3;
    
    return p;
  }
  
  /**
   * Calculate the Bezier coefficients
   *
   * @param i
   * @param t
   * @return
   */
  private double b(int i, double t) {
    
    switch(i) {
      
      case 0:
        return (1-t) * (1-t) * (1-t);
      
      case 1:
        return 3 * (1-t) * (1-t) * t;
      
      case 2:
        return 3 * (1-t) * t * t;
      
      case 3:
        return t * t * t;
    }
    
    // this should never happen
    throw new IllegalArgumentException("" + i);
  }
  
  /*********************** My Code *********************/
  public void draw(GL2 gl, TexturePack texturePack) {
    gl.glPushMatrix();
    gl.glPushAttrib(GL2.GL_LIGHTING);
    
    double step = (myPoints.size() / 6.0) / DIVISION_FACTOR; //determine how many quadrants to cut road into
                                                             //the smaller the step the more intermediate 't' values
    
    //Compute road distance (from t - roadDistance).
    //Cannot use size() here instead of myPoints.size() / 6.0 as double precision is required
    double roadDistance = (myPoints.size() / 6.0) - (1.0/3.0) - (2 * step);
    double startingY = myTerrain.altitude(point(0.0)[0], point(0.0)[1]) + ALTITUDE_OFFSET;
  
    //Get terrain texture
    Texture road = texturePack.getRoad();
    road.enable(gl);
    road.bind(gl);
    TextureCoords textureCoords = road.getImageTexCoords();
    
    //Set road material (matte kind of look)
    float[] ambient = {0.2f, 0.2f, 0.2f, 1.0f};
    float[] diffuse = {0.4f, 0.4f, 0.4f, 1.0f};
    float[] specular = {0.5f, 0.5f, 0.5f, 1.0f};
  
    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, ambient, 0);
    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffuse, 0);
    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, specular, 0);
    
    
    gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
    
    for (double t = 0.0; t < roadDistance; t += step) {
      //Get current point
      double[] currentPoint = point(t);
      double[] currentPointVertex = {currentPoint[0], startingY, currentPoint[1]};
      
      //Get next point
      double[] nextPoint = point(t + step);
      double[] nextPointVertex = {nextPoint[0], startingY, nextPoint[1]};
      
      //Get next next point
      double[] nextNextPoint = point(t + (2 * step));
  
      //Obtain line segments vectors
      //These are lines segments connecting currentPoint to nextPoint, and nextPoint to nextNextPoint
      //The 'y' axis does not matter here as altitude will be based on terrain, so we set it to 0
      double[] currentToNextLine = {nextPoint[0] - currentPoint[0], 0, nextPoint[1] - currentPoint[1], 1};
      double[] nextToNextNextLine = {nextNextPoint[0] - nextPoint[0], 0, nextNextPoint[1] - nextPoint[1], 1};
  
      
      //Compute perpendicular of these vectors (using cross product method).
      //The normal vector here is simply pointing upwards in y axis
      //As all roads will be flat rather than 'melting' into the terrain
      //We also want these results as unit vectors so both vectors are comparable
      double[] normalVector = {0, 1, 0, 1};
      double[] currentToNextLinePerp = MathUtil.getUnitVector(MathUtil.crossProduct(normalVector, currentToNextLine));
      double[] nextToNextNextLinePerp = MathUtil.getUnitVector(MathUtil.crossProduct(normalVector, nextToNextNextLine));
  
      //Scale these vectors to match provided myWidth
      //We scale by myWidth/2 as we need to go outwards from the central line segments
      //In both directions to match the total width required.
      double[] currentToNextLineFinal = MathUtil.multiply(MathUtil.scaleMatrix(myWidth / 2), currentToNextLinePerp);
      double[] nextToNextNextLineFinal = MathUtil.multiply(MathUtil.scaleMatrix(myWidth / 2), nextToNextNextLinePerp);
  
      //At this stage we can generate the 4 vertexes that will be used to draw this portion of the road
      //We will use 2 triangles to represent the polygon (4 points)
      double[] currentLeft = {currentPointVertex[0] - currentToNextLineFinal[0], currentPointVertex[1] - currentToNextLineFinal[1], currentPointVertex[2] - currentToNextLineFinal[2]};
      double[] currentRight = {currentPointVertex[0] + currentToNextLineFinal[0], currentPointVertex[1] + currentToNextLineFinal[1], currentPointVertex[2] + currentToNextLineFinal[2]};
      double[] nextLeft = {nextPointVertex[0] - nextToNextNextLineFinal[0], nextPointVertex[1] - nextToNextNextLineFinal[1], nextPointVertex[2] - nextToNextNextLineFinal[2]};
      double[] nextRight = {nextPointVertex[0] + nextToNextNextLineFinal[0], nextPointVertex[1] + nextToNextNextLineFinal[1], nextPointVertex[2] + nextToNextNextLineFinal[2]};
      
      //Draw first triangle
      gl.glBegin(GL2.GL_TRIANGLES);
      {
        gl.glColor3f(0.9f, 0.9f, 0.9f); //Black road (does nothing if lighting enabled)
        gl.glNormal3dv(normalVector, 0); //Roads are always facing directly upwards
        
        double[] currentLeftTexture = {textureCoords.left(), textureCoords.bottom()};
        double[] currentRightTexture = {textureCoords.right(), textureCoords.bottom()};
        double[] nextRightTexture = {textureCoords.left(), textureCoords.top()};
  
        gl.glTexCoord2dv(currentLeftTexture, 0);
        gl.glVertex3dv(currentLeft, 0);
        gl.glTexCoord2dv(currentRightTexture, 0);
        gl.glVertex3dv(currentRight, 0);
        gl.glTexCoord2dv(nextRightTexture, 0);
        gl.glVertex3dv(nextRight, 0);
      }
      gl.glEnd();
      
      //Draw second triangle
      gl.glBegin(GL2.GL_TRIANGLES);
      {
        gl.glColor3f(0.9f, 0.9f, 0.9f); //Black road (does nothing if lighting enabled)
        gl.glNormal3dv(normalVector, 0); //Roads are always facing directly upwards
  
        double[] currentLeftTexture = {textureCoords.left(), textureCoords.bottom()};
        double[] nextRightTexture = {textureCoords.left(), textureCoords.top()};
        double[] nextLeftTexture = {textureCoords.right(), textureCoords.top()};
        
        gl.glTexCoord2dv(currentLeftTexture, 0);
        gl.glVertex3dv(currentLeft, 0);
        gl.glTexCoord2dv(nextRightTexture, 0);
        gl.glVertex3dv(nextRight, 0);
        gl.glTexCoord2dv(nextLeftTexture, 0);
        gl.glVertex3dv(nextLeft, 0);
      }
      gl.glEnd();
    }
  
    road.disable(gl);
    gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
    
    gl.glPopAttrib();
    gl.glPopMatrix();
  }
  
}
