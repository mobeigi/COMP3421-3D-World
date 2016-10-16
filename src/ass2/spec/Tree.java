package ass2.spec;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;

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
  public void draw(GL2 gl, TexturePack texturePack) {
    gl.glPushMatrix();
  
    GLU glu = new GLU(); //use GLU to construct tree, benefit is normals set for us, also supports texturing
    
    gl.glPushMatrix();
    {
      //Get texture
      Texture treeTrunk = texturePack.getTreeTrunk();
      treeTrunk.enable(gl);
      treeTrunk.bind(gl);
      
      //First make the cylinder (trunk) of the tree
      gl.glTranslated(myPos[0], myPos[1] - TRUNK_INTERPOLATION_OFFSET, myPos[2]);
      gl.glRotated(-90.0, 1, 0, 0);
      
      GLUquadric gluQuadratic = glu.gluNewQuadric();
      glu.gluQuadricTexture(gluQuadratic, true);
      glu.gluQuadricNormals(gluQuadratic, GLU.GLU_SMOOTH);
      glu.gluCylinder(gluQuadratic, 0.05f, 0.05f, 0.8f, 60, 60); //TODO: optimize divisions
  
      treeTrunk.disable(gl);
    }
    gl.glPopMatrix();
  
    gl.glPushMatrix();
    {
      //Get texture
      Texture treeLeaves = texturePack.getTreeLeaves();
      treeLeaves.enable(gl);
      treeLeaves.bind(gl);
      
      //Now make the spherical top of the trees which will sit on top of the cylinder
      gl.glTranslated(myPos[0], myPos[1] + (0.8f - TRUNK_INTERPOLATION_OFFSET), myPos[2]);
  
      GLUquadric gluQuadratic = glu.gluNewQuadric();
      glu.gluQuadricTexture(gluQuadratic, true);
      glu.gluQuadricNormals(gluQuadratic, GLU.GLU_SMOOTH);
      glu.gluSphere(gluQuadratic, 0.25f, 60, 60); //TODO: optimize divisions
  
      treeLeaves.disable(gl);
    }
    gl.glPopMatrix();
    
    gl.glPopMatrix();
  }
  
}
