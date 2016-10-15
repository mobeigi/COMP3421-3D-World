package ass2.spec;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree {
  
  private double[] myPos;
  private static final double TRUNK_INTERPOLATION_OFFSET = 0.25; //offset to make tree go slightly underneath terrain
  
  public Tree(double x, double y, double z) {
    myPos = new double[3];
    myPos[0] = x;
    myPos[1] = y;
    myPos[2] = z;
  }
  
  public double[] getPosition() {
    return myPos;
  }
  
  
  /*********************** My Code *********************/
  public void draw(GL2 gl) {
    gl.glPushMatrix();
  
    GLUT glut = new GLUT(); //use GLUT to construct tree, benefit is normals set for us
    
    gl.glPushMatrix();
    {
      //First make the cylinder (trunk) of the tree
      gl.glTranslated(myPos[0], myPos[1] - TRUNK_INTERPOLATION_OFFSET, myPos[2]);
      gl.glRotated(-90.0, 1, 0, 0);
      glut.glutSolidCylinder(0.05f, 0.8f, 60, 60); //TODO: optimize divisions
    }
    gl.glPopMatrix();
  
    gl.glPushMatrix();
    {
      //Now make the spherical top of the trees which will sit on top of the cylinder
      gl.glTranslated(myPos[0], myPos[1] + (0.8f - TRUNK_INTERPOLATION_OFFSET), myPos[2]);
      glut.glutSolidSphere(0.25f, 60, 60); //TODO: optimize divisions
    }
    gl.glPopMatrix();
    
    gl.glPopMatrix();
  }
  
}
