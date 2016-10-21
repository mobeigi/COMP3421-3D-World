package ass2.spec;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;

import java.nio.FloatBuffer;

/**
 * Enemy object.
 *
 * Defined in scene language like so:
 *   "enemies" : [
 *    { "x" : 3.0, "z" : 3.0 , "rotation" : 45.0} ]
 *
 * where (x,z) is the coordinates in the world and rotation is initial direction for enemy to face.
 *
 */
public class Enemy {
  
  private Terrain myTerrain;
  
  //VBO arrays
  private FloatBuffer sphereVertexBuffer;
  private FloatBuffer sphereNormalBuffer;
  private FloatBuffer sphereTextureBuffer;
  private float[] sphereVertexArray;
  private float[] sphereNormalArray;
  private float[] sphereTextureArray;
  
  //VBO indexes
  private int vertexVboId;
  private int normalVboId;
  private int textureVboId;
  
  //Enemy location
  private double[] myPos;
  private double myYPosition;
  private double myRotation;
  
  //For bouncing animation
  private double bounceTime;
  public static double MIN_ENEMY_ALTITUDE = 0.18;
  
  //Setup
  private boolean setupComplete;
  
  public Enemy(Terrain terrain, double x, double z, double rotation) {
    this.myTerrain = terrain;
    this.myPos = new double[]{x, z};
    this.myRotation = rotation;
  
    this.bounceTime = 0;
    
    this.setupComplete = false;
  }
  
  //Getters and setters
  public double[] getMyPos() {
    return myPos;
  }
  
  public double getMyRotation() {
    return myRotation;
  }
  
  
  public void setMyRotation(double myRotation) {
    this.myRotation = myRotation;
  }
  
  public void setMyPos(double[] myPos) {
    this.myPos = myPos;
  }
  
  public double getMyYPosition() {
    return myYPosition;
  }
  
  public void draw(GL2 gl, TexturePack texturePack, int shaderProgram, Game.FRAGMENT_SHADER_MODE fragmentShaderColourMode,
                   boolean curLighting, boolean nightMode, float[] torchPosition) {
    gl.glPushMatrix();
    
    //Setup if it hasn't already happened
    //This is needed to prepare VBO arrays buffers before we can draw the enemy
    if (!setupComplete) {
      createSphereArraysAndVBOs(gl);
      setupComplete = true;
    }
    
    //Computer Y position of enemy based on positive sine wave
    bounceTime = (bounceTime + 0.05) % (180);
    myYPosition = Math.abs(Math.sin(bounceTime));
    myYPosition = Math.max(myYPosition, MIN_ENEMY_ALTITUDE);
    
    //Use shader to process vertices/fragments
    gl.glUseProgram(shaderProgram);
    
    //Set three buffers
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexVboId);
    int vertexPositionID = gl.glGetAttribLocation(shaderProgram, "vertexPosition");
    gl.glEnableVertexAttribArray(vertexPositionID);
    gl.glVertexAttribPointer(vertexPositionID, 3, GL.GL_FLOAT, false, 0, 0);
    
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, normalVboId);
    int vertexNormalID = gl.glGetAttribLocation(shaderProgram, "vertexNormals");
    gl.glEnableVertexAttribArray(vertexNormalID);
    gl.glVertexAttribPointer(vertexNormalID, 3, GL.GL_FLOAT, false, 0, 0);
    
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, textureVboId);
    int vertexTextureID = gl.glGetAttribLocation(shaderProgram, "vertexTextures");
    gl.glEnableVertexAttribArray(vertexTextureID);
    gl.glVertexAttribPointer(vertexTextureID, 2, GL.GL_FLOAT, false, 0, 0);
  
    //Determine if lighting will be used when rendering
    int lightingEnabledID = gl.glGetUniformLocation(shaderProgram, "lightingEnabled");
    gl.glUniform1i(lightingEnabledID, (curLighting) ? 1 : 0);
    
    //Determine if textures will be used or if colours will be used
    int textureModeID = gl.glGetUniformLocation(shaderProgram, "textureMode");
    gl.glUniform1i(textureModeID, (fragmentShaderColourMode == Game.FRAGMENT_SHADER_MODE.TEXTURE)? 1 : 0);
  
    //Setup which texture to use
    int textureID = gl.glGetUniformLocation(shaderProgram, "textureID");
    gl.glActiveTexture(GL.GL_TEXTURE0);
    gl.glUniform1i(textureID, 0); //0 for GL_TEXTURE0
    
    //Set sun position
    int sunID = gl.glGetUniformLocation(shaderProgram, "lightPosition");
    
    //If night mode, the sun is the position of the camera (spotlight)
    if (nightMode)
      gl.glUniform3fv(sunID, 1, torchPosition, 0);
    else
      gl.glUniform3fv(sunID, 1, myTerrain.getSunlight(), 0);
    
    //Set various lighting modes
    float[] ambient = {0.2f, 0.2f, 0.2f, 1.0f};
    float[] diffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    float[] specular = {0.2f, 0.2f, 0.2f, 1.0f};
  
    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, ambient, 0);
    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffuse, 0);
    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, specular, 0);
    
    //Draw the enemy model
    //We offset the y axis by yPosition for the bouncing enemy animation
    gl.glTranslated(myPos[0], myTerrain.altitude(myPos[0], myPos[1]) + myYPosition, myPos[1]);
    gl.glRotated(-myRotation, 0, 1.0, 0);
    
    //Body
    gl.glPushMatrix();
    {
      //Set Texture
      Texture texture = texturePack.getEnemyBody();
      gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getTextureObject());
      
      gl.glColor4f(0.098039f, 0.066666f, 0.12549f, 1.0f);
      
      gl.glScaled(0.5, 0.5, 0.5);
      drawSphereWithDrawArrays(gl);
    }
    gl.glPopMatrix();
    
    //Left Eye
    gl.glPushMatrix();
    {
      Texture texture = texturePack.getEnemyEyes();
      gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getTextureObject());
      
      gl.glColor4f(0.9f, 0.9f, 0.9f, 1.0f);
      
      gl.glTranslated(-0.08, 0.06, 0.15);
      gl.glScaled(0.08, 0.08, 0.08);
      drawSphereWithDrawArrays(gl);
    }
    gl.glPopMatrix();
    
    //Right Eye
    gl.glPushMatrix();
    {
      Texture texture = texturePack.getEnemyEyes();
      gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getTextureObject());
      
      gl.glColor4f(0.9f, 0.9f, 0.9f, 1.0f);
      
      gl.glTranslated(0.08, 0.06, 0.15);
      gl.glScaled(0.08, 0.08, 0.08);
      
      drawSphereWithDrawArrays(gl);
    }
    gl.glPopMatrix();
    
    //Mouth
    gl.glPushMatrix();
    {
      //Set Texture
      Texture texture = texturePack.getEnemyMouth();
      gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getTextureObject());
      
      gl.glColor4f(0.9f, 0.2f, 0.2f, 1.0f);
      
      gl.glTranslated(0.0, -0.06, 0.18);
      gl.glRotated(-20.0, 1.0, 0.0, 0.0);
      gl.glScaled(0.15, 0.15, 0.05);
      drawSphereWithDrawArrays(gl);
    }
    gl.glPopMatrix();
    
    
    //Cleanup and reset bindings
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, GL.GL_ZERO);
    gl.glUseProgram(GL.GL_ZERO);
    gl.glBindTexture(GL.GL_TEXTURE_2D, GL.GL_ZERO);
    
    gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
    gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
    gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
    
    gl.glPopMatrix();
  }
  
  /**
   * Creates the vertex coordinate and normal vectors for a sphere.
   * The data is stored in the FloatBuffers sphereVertexBuffer and
   * sphereNormalBuffer.  In addition, VBOs are created to hold
   * the data and the data is copied from the FloatBuffers into
   * the VBOs.  (Note: The VBOs are used for render mode 4; the
   * FloatBuffers are used for render mode 3.)
   *
   * Code source: HWS Math and CS
   *
   * @see <a href="http://math.hws.edu/graphicsbook/source/jogl/ColorCubeOfSpheres.java">Code Source</a>
   */
  public void createSphereArraysAndVBOs(GL2 gl) {
    double radius = 0.4;
    int stacks = 16;
    int slices = 32;
    int size = stacks * (slices+1) * 2 * 3;
    
    sphereVertexBuffer = GLBuffers.newDirectFloatBuffer(size);
    sphereNormalBuffer = GLBuffers.newDirectFloatBuffer(size);
    sphereTextureBuffer = GLBuffers.newDirectFloatBuffer(size);
    
    sphereVertexArray = new float[size];
    sphereNormalArray = new float[size];
    sphereTextureArray = new float[size];
    
    for (int j = 0; j < stacks; j++) {
      double latitude1 = (Math.PI/stacks) * j - Math.PI/2;
      double latitude2 = (Math.PI/stacks) * (j+1) - Math.PI/2;
      double sinLat1 = Math.sin(latitude1);
      double cosLat1 = Math.cos(latitude1);
      double sinLat2 = Math.sin(latitude2);
      double cosLat2 = Math.cos(latitude2);
      for (int i = 0; i <= slices; i++) {
        double longitude = (2*Math.PI/slices) * i;
        double sinLong = Math.sin(longitude);
        double cosLong = Math.cos(longitude);
        double x1 = cosLong * cosLat1;
        double y1 = sinLong * cosLat1;
        double z1 = sinLat1;
        double x2 = cosLong * cosLat2;
        double y2 = sinLong * cosLat2;
        double z2 = sinLat2;
        sphereNormalBuffer.put( (float)x2 );
        sphereNormalBuffer.put( (float)y2 );
        sphereNormalBuffer.put( (float)z2 );
        sphereVertexBuffer.put( (float)(radius*x2) );
        sphereVertexBuffer.put( (float)(radius*y2) );
        sphereVertexBuffer.put( (float)(radius*z2) );
        
        sphereTextureBuffer.put((float)(1.0/slices * i)); //from uvSphere function from source
        sphereTextureBuffer.put((float)(1.0/stacks * (j+1)));
        
        sphereNormalBuffer.put( (float)x1 );
        sphereNormalBuffer.put( (float)y1 );
        sphereNormalBuffer.put( (float)z1 );
        sphereVertexBuffer.put( (float)(radius*x1) );
        sphereVertexBuffer.put( (float)(radius*y1) );
        sphereVertexBuffer.put( (float)(radius*z1) );
        
        sphereTextureBuffer.put((float)(1.0/slices * i)); //from uvSphere function from source
        sphereTextureBuffer.put((float)(1.0/stacks * j));
      }
    }
    
    for (int i = 0; i < size; i++) {
      sphereVertexArray[i] = sphereVertexBuffer.get(i);
      sphereNormalArray[i] = sphereNormalBuffer.get(i);
      sphereTextureArray[i] = sphereTextureBuffer.get(i);
    }
    
    sphereVertexBuffer.rewind();
    sphereNormalBuffer.rewind();
    sphereTextureBuffer.rewind();
    
    int[] bufferIDs = new int[3];
    gl.glGenBuffers(3, bufferIDs, 0);
    vertexVboId = bufferIDs[0];
    normalVboId = bufferIDs[1];
    textureVboId = bufferIDs[2];
    
    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexVboId);
    gl.glBufferData(GL2.GL_ARRAY_BUFFER, size*4, sphereVertexBuffer, GL2.GL_STATIC_DRAW);
    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalVboId);
    gl.glBufferData(GL2.GL_ARRAY_BUFFER, size*4, sphereNormalBuffer, GL2.GL_STATIC_DRAW);
    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, textureVboId);
    gl.glBufferData(GL2.GL_ARRAY_BUFFER, size*4, sphereTextureBuffer, GL2.GL_STATIC_DRAW);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
  }
  
  /**
   * Draw one sphere.  The VertexPointer and NormalPointer must already
   * be set to point to the data for the sphere, and they must be enabled.
   *
   * Code source: HWS Math and CS
   *
   * @see <a href="http://math.hws.edu/graphicsbook/source/jogl/ColorCubeOfSpheres.java">Code Source</a>
   */
  private void drawSphereWithDrawArrays(GL2 gl) {
    int slices = 32;
    int stacks = 16;
    int vertices = (slices+1)*2;
    for (int i = 0; i < stacks; i++) {
      int pos = i*(slices+1)*2;
      gl.glDrawArrays(GL2.GL_QUAD_STRIP, pos, vertices);
    }
  }
}
