package ass2.spec;

/**
 * A collection of useful math methods.
 * Updated to function for a 3D world.
 *
 * @author malcolmr
 */
public class MathUtil {
  
  /**
   * Normalise an angle to the range [-180, 180)
   *
   * @param angle input angle
   * @return normalised angle
   */
  static public double normaliseAngle(double angle) {
    return ((angle + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
  }
  
  /**
   * Clamp a value to the given range
   *
   * @param value value to be clamped
   * @param min minimum value
   * @param max maximum value
   * @return clamped value within given range
   */
  
  public static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
  
  /**
   * Multiply two matrices (for 3D)
   *
   * @param p A 4x4 matrix
   * @param q A 4x4 matrix
   * @return A resulting 4x4 matrix
   */
  public static double[][] multiply(double[][] p, double[][] q) {
    
    double[][] m = new double[4][4];
    
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        m[i][j] = 0;
        for (int k = 0; k < 4; k++) {
          m[i][j] += p[i][k] * q[k][j];
        }
      }
    }
    
    return m;
  }
  
  /**
   * Multiply a vector by a matrix (for 3D)
   *
   * @param m A 4x4 matrix
   * @param v A 4x1 vector
   * @return resulting multiplied vector
   */
  public static double[] multiply(double[][] m, double[] v) {
    
    double[] u = new double[4];
    
    for (int i = 0; i < 4; i++) {
      u[i] = 0;
      for (int j = 0; j < 4; j++) {
        u[i] += m[i][j] * v[j];
      }
    }
    
    return u;
  }
  
  
  
  // ===========================================
  // COMPLETE THE METHODS BELOW
  // ===========================================
  
  
  /**
   * A 2D translation matrix for the given offset vector
   *
   * @param v offset vector
   * @return 3x3 array of doubles representing 2D translation matrix
   */
  public static double[][] translationMatrix(double[] v) {
    double[][] m = {
      { 1, 0, v[0]},
      { 0, 1, v[1]},
      { 0, 0, 1}
    };
    
    return m;
  }
  
  /**
   * A 2D rotation matrix for the given angle
   *
   * @param angle in degrees
   * @return 3x3 array of doubles representing 2D rotation matrix
   */
  public static double[][] rotationMatrix(double angle) {
    double rAngle = Math.toRadians(angle);  //convert to radians for Math functions
    
    double[][] m = {
      { Math.cos(rAngle), -Math.sin(rAngle), 0},
      { Math.sin(rAngle), Math.cos(rAngle), 0},
      { 0, 0, 1}
    };
    
    return m;
  }
  
  /**
   * A 3D scale matrix that scales both axes by the same factor
   *
   * @param scale scale factor
   * @return 4x4 array of doubles representing 3D scale matrix
   */
  public static double[][] scaleMatrix(double scale) {
    double[][] m = {
      {scale, 0, 0, 0},
      { 0, scale, 0, 0},
      { 0, 0, scale, 0},
      { 0, 0, 0, 1}
    };
    
    return m;
  }
  
  /**
   * Calculate the cross product between two vectors u and v.
   * Source: COMP3421 lecture example code
   *
   * @param u input u vector
   * @param v input v vector
   * @return Cross product result
   */
  public static double [] crossProduct(double u [], double v[]){
    double crossProduct[] = new double[3];
    crossProduct[0] = u[1]*v[2] - u[2]*v[1];
    crossProduct[1] = u[2]*v[0] - u[0]*v[2];
    crossProduct[2] = u[0]*v[1] - u[1]*v[0];
    
    return crossProduct;
  }
  
  
  /**
   * Get the normal vector of 3 points that lie on a plane.
   * Note: Assumes correct winding order.
   *
   * Source: COMP3421 lecture example code
   *
   * @param p0 first point on plane
   * @param p1 second point on plane
   * @param p2 third point on plane
   * @return Normal vector (perpendicular) to plane face
   */
  public static double [] getNormal(double[] p0, double[] p1, double[] p2){
    double u[] = {p1[0] - p0[0], p1[1] - p0[1], p1[2] - p0[2]};
    double v[] = {p2[0] - p0[0], p2[1] - p0[1], p2[2] - p0[2]};
    
    return crossProduct(u,v);
  }
  
  /**
   * Find the unit vector of given vector by dividing by the magnitude.
   * Source: http://gamedev.stackexchange.com/a/76082
   *
   * @param u input vector to be converted
   * @return input vector as a unit vector
   */
  public static double [] getUnitVector(double[] u) {
    double unitVector[] = new double[4];
    
    double magnitude = Math.sqrt(u[0] * u[0] + u[1] * u[1] + u[2] * u[2]);
    unitVector[0] = u[0] / magnitude;
    unitVector[1] = u[1] / magnitude;
    unitVector[2] = u[2] / magnitude;
    unitVector[3] = 1;
    
    return unitVector;
  }
}
