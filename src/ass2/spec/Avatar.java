package ass2.spec;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;

public class Avatar {
  private Terrain myTerrain;
  private double[] cameraPosition;
  private double cameraRotation;
  
  //Constants
  private static final double AVATAR_ALTITUDE_OFFSET = 0.08; //avatar offset from ground so sits on terrain
  
  //Constructor
  public Avatar(Terrain terrain, double[] cameraPosition, double cameraRotation) {
    this.myTerrain = terrain;
    this.cameraPosition = cameraPosition;
    this.cameraRotation = cameraRotation;
  }
  
  /*********************** My Code *********************/
  public void draw(GL2 gl, TexturePack texturePack) {
    gl.glPushMatrix();
    gl.glPushAttrib(GL2.GL_LIGHTING); //to preserve ambient, etc values
  
    //Set position
    gl.glTranslated(cameraPosition[0], myTerrain.altitude(cameraPosition[0], cameraPosition[1]) + AVATAR_ALTITUDE_OFFSET, cameraPosition[1]);
    gl.glRotated(-cameraRotation, 0, 1, 0); //make teapot face outwards (like a face)
  
    //Set material for teapot (metallic look)
    //Golden (aka metallic) values borrowed from week 5 LightExample.java
    float[] ambient = {0.24725f, 0.1995f, 0.0745f}; //gold ambient
    float[] diffuse = {0.75164f,0.60648f,0.22648f}; //gold defuse
    float[] specular = {0.628281f,0.555802f,0.366065f}; //gold specular
  
    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, ambient, 0);
    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffuse, 0);
    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, specular, 0);
  
    //Textures
    gl.glEnable(GL2.GL_TEXTURE_GEN_S);
    gl.glEnable(GL2.GL_TEXTURE_GEN_T);
    Texture avatar = texturePack.getAvatar();
    avatar.enable(gl);
    avatar.bind(gl);
  
    GLUT glut = new GLUT();
  
    // The builtin teapot is back-to-front
    // https://developer.apple.com/library/mac/documentation/Darwin/Reference/ManPages/man3/glutSolidTeapot.3.html
    gl.glFrontFace(GL2.GL_CW);
    glut.glutSolidTeapot(0.08f);
    gl.glFrontFace(GL2.GL_CCW);
  
    gl.glDisable(GL2.GL_TEXTURE_GEN_S);
    gl.glDisable(GL2.GL_TEXTURE_GEN_T);
  
    gl.glPopAttrib();
    gl.glPopMatrix();
  }
}



