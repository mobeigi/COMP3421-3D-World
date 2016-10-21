package ass2.spec;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;

public class Portal {
  
  private Terrain myTerrain;
  
  //Portal location
  private double[] myPos;
  private double myRotation;
  private int portalNum; //designates if this is the first or second portal
  
  //Constants
  public static final double PORTAL_ALTITUDE_OFFSET = 0.8; //offset from ground so portal sits on ground
  
  public Portal(Terrain terrain, double x, double z, double rotation, int portalNum) {
    this.myTerrain = terrain;
    this.myPos = new double[]{x, z};
    this.myRotation = rotation;
    this.portalNum = portalNum;
  }
  
  public void draw(GL2 gl, TexturePack texturePack) {
    gl.glPushMatrix();
    
    //Set positions and apply rotations
    gl.glTranslated(myPos[0], myTerrain.altitude(myPos[0], myPos[1]) + PORTAL_ALTITUDE_OFFSET, myPos[1]);
    gl.glRotated(myRotation, 0, 1.0, 0);
    gl.glScaled(0.5, 0.8, 1.0);
  
    //Determine portal colour and texture based on portal number
    //Ie based on if portal is first or second portal defined in portal pair
    Texture portal;
    if (portalNum == 1) {
      gl.glColor3f(0.1176f, 0.5647f, 1.0f); //blue
      portal = texturePack.getBluePortal();
      portal.enable(gl);
      portal.bind(gl);
    }
    else {
      gl.glColor3f(1.0f, 0.2549f, 0.0f); //orange
      portal = texturePack.getOrangePortal();
      portal.enable(gl);
      portal.bind(gl);
    }
    
    //Draw portal as scaled circle
    //Should look flat from side on like in the portal games
    gl.glBegin(GL2.GL_TRIANGLE_FAN);
    {
      //Set material for portals
      float[] ambient = {0.6f, 0.6f, 0.6f};
      float[] diffuse = {0.9f, 0.9f, 0.9f};
      float[] specular = {0.3f, 0.3f, 0.3f};
  
      gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, ambient, 0);
      gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffuse, 0);
      gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, specular, 0);
      
      gl.glNormal3d(0.0, 0.0, 1.0);
      
      for (int i = 0; i <= 300; ++i) {
        double angle = 2 * Math.PI * i / 300;
        double x = Math.cos(angle);
        double y = Math.sin(angle);
        
        gl.glTexCoord2d(x * 0.5 + 0.5, y *0.5 + 0.5);
        gl.glVertex3d(x, y, 0.0);
      }
      
    }
    gl.glEnd();
    
    portal.disable(gl); //disable portal textures
    
    //Draw back side of portal
    gl.glBegin(GL2.GL_TRIANGLE_FAN);
    {
      //Set material for portal backs
      float[] ambient = {0.0f, 0.0f, 0.0f};
      float[] diffuse = {0.0f, 0.0f, 0.0f};
      float[] specular = {0.0f, 0.0f, 0.0f};
  
      gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, ambient, 0);
      gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffuse, 0);
      gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, specular, 0);
      
      
      gl.glColor3f(0.0f, 0.0f, 0.0f);
      gl.glNormal3d(0.0, 0.0, 0.0);
      
      for (int i = 0; i <= 300; ++i) {
        double angle = 2 * Math.PI * i / 300;
        double x = Math.cos(angle);
        double y = Math.sin(angle);
        gl.glVertex3d(x, y, -0.01); //move slightly backwards
      }
    }
    gl.glEnd();
    
    gl.glPopMatrix();
  }
  
  //Getters
  public double[] getMyPos() {
    return myPos;
  }
  
  public double getMyRotation() {
    return myRotation;
  }
}
