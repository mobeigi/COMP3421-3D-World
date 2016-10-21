package ass2.spec;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;


/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class Game extends JFrame implements GLEventListener, KeyListener {
  
  private Terrain myTerrain;
  
  //Camera
  private int width;
  private int height;
  private double[] cameraPosition;
  private double cameraRotation;
  private boolean thirdPerson;
  private GLU glu;
  
  //Textures
  private TexturePack texturePack;
  private int prevTexturePack; //Used to determine if texture packs have changed (is reload of textures required)
  private int curTexturePack;
  
  //Lighting
  private boolean prevLighting;
  private boolean curLighting;
  
  //Shader
  private int shaderProgram;
  private FRAGMENT_SHADER_MODE fragmentShaderColourMode;
  public enum FRAGMENT_SHADER_MODE {
    COLOUR, TEXTURE
  }
  
  //Night mode
  private boolean nightMode;
  
  //Enemy
  Enemy enemy;
  
  //Constants
  private static final double FIELD_OF_VIEW = 60.0; //field of view to use in world
  private static final double MAXIMUM_FIELD_OF_VIEW = 180.0; //max possible value for field of view
  private static final double ALTITUDE_OFFSET = 0.5; //camera offset from ground so world is visible
  private static final double THIRDPERSON_ALTITUDE_CHANGE = 1.0; //how much to change altitude of camera in third person view
  private static final double WALKING_SPEED = 0.1; //speed at which player (camera) moves at
  private static final double CAMERA_DEFAULT_ROTATION = 45.0; //camera default rotation
  private static final double CAMERA_ROTATION_STEP = 10; //number of degrees to rotate camera by
  private static final double NUM_TEXTURE_PACKS = 2; //total number of texture packs
  private static final String VERTEX_SHADER_GLSL = "/shader/AttributeVertex.glsl"; //path to vertex shader GLSL file
  private static final String FRAGMENT_SHADER_GLSL = "/shader/AttributeFragment.glsl"; //path to fragment shader GLSL file
  
  
  public Game(Terrain terrain) {
    super("Assignment 2");
    
    //Default values
    myTerrain = terrain;
    cameraPosition = new double[]{0,0};
    cameraRotation = CAMERA_DEFAULT_ROTATION;
    width = 800*2;
    height = 600*2;
    glu = new GLU();
    texturePack = new TexturePack();
    curTexturePack = prevTexturePack = 0;
    curLighting = prevLighting = true;
    thirdPerson = false;
    fragmentShaderColourMode = FRAGMENT_SHADER_MODE.COLOUR;
    nightMode = false;
  }
  
  /**
   * Run the game.
   *
   */
  public void run() {
    GLProfile glp = GLProfile.getDefault();
    GLCapabilities caps = new GLCapabilities(glp);
    GLJPanel panel = new GLJPanel();
    panel.addGLEventListener(this);
    panel.addKeyListener(this);
    
    // Add an animator to call 'display' at some interval
    FPSAnimator animator = new FPSAnimator(60); //todo: up this for smoother movements
    animator.add(panel);
    animator.start();
    
    getContentPane().add(panel);
    setSize(width, height);
    setVisible(true);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }
  
  /**
   * Load a level file and display it.
   *
   * @param args - The first argument is a level file in JSON format
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException {
    Terrain terrain = LevelIO.load(new File(args[0]));
    Game game = new Game(terrain);
    game.run();
  }
  
  @Override
  public void display(GLAutoDrawable drawable) {
    GL2 gl = drawable.getGL().getGL2();
    
    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glLoadIdentity();
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    
    //Setup camera
    setupCamera(gl);
    
    //Set lighting if it has changed
    if (prevLighting != curLighting) {
      if (curLighting)
        gl.glEnable(GL2.GL_LIGHTING);
      else
        gl.glDisable(GL2.GL_LIGHTING);
      
      prevLighting = curLighting;
    }
    
    //Setup Sunlight
    setupSun(gl);
    
    //Setup torch if in night mode
    if (nightMode)
      setupTorch(gl);
    
    //Do textures need reloading?
    if (prevTexturePack != curTexturePack) {
      setupTextures();
      prevTexturePack = curTexturePack;
    }
    
    //Draw terrain including enemy
    float[] torchPosition = {(float)cameraPosition[0], (float)(myTerrain.altitude(cameraPosition[0], cameraPosition[1])) + (float)ALTITUDE_OFFSET, (float)cameraPosition[1]};
    myTerrain.draw(gl, texturePack, shaderProgram, fragmentShaderColourMode, curLighting, nightMode, torchPosition);
    
    //Draw avatar
    if (thirdPerson) {
      Avatar avatar = new Avatar(myTerrain, cameraPosition, cameraRotation);
      avatar.draw(gl, texturePack);
    }
  }
  
  
  @Override
  public void dispose(GLAutoDrawable drawable) {
    // TODO Auto-generated method stub
    // TODO: Probably do not need
  }
  
  @Override
  public void init(GLAutoDrawable drawable) {
    GL2 gl = drawable.getGL().getGL2();
    
    //Configure required options
    gl.glEnable(GL2.GL_DEPTH_TEST); //need Z axis taken into account
    gl.glEnable(GL2.GL_NORMALIZE); //automatically normalise our normals as we scale
    
    /*
    gl.glEnable(GL2.GL_CULL_FACE);  //optimization to get rid of various fragments todo: enable
    gl.glEnable(GL2.GL_BACK);
    */
    
    gl.glEnable(GL2.GL_LIGHTING); //enable lighting
    
    gl.glEnable(GL2.GL_TEXTURE_2D); //turn on texture features
    setupTextures(); //read in and load textures file with IO
    gl.glGenerateMipmap(GL2.GL_TEXTURE_2D); //make smaller copies from 512x512 texture for higher quality textures and performance
    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR); //use trilinear filtering with MIN filter

    //Configure shader
    gl.glShadeModel(GL2.GL_SMOOTH); //we will use Gouraud shading
    
    try {
      //Get resource URLS
      URL vertexShader = this.getClass().getResource(VERTEX_SHADER_GLSL);
      URL fragmentShader = this.getClass().getResource(FRAGMENT_SHADER_GLSL);
      shaderProgram = Shader.initShaders(gl, vertexShader.getPath(), fragmentShader.getPath());
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  @Override
  public void reshape(GLAutoDrawable drawable, int x, int y, int width,
                      int height) {
    GL2 gl = drawable.getGL().getGL2();
    
    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glLoadIdentity();
    
    this.width = width;
    this.height = height;
    double aspectRatio = (double)width / (double)height;
    double fieldOfView = Math.min(aspectRatio * FIELD_OF_VIEW, MAXIMUM_FIELD_OF_VIEW);
    
    glu.gluPerspective(fieldOfView, aspectRatio, 0.01d, 10000d);
  }
  
  
  /**
   * Setup camera based on current window size, field of view and camera position.
   *
   * @param gl GL2 object
   */
  private void setupCamera(GL2 gl) {
    gl.glPushMatrix();
    
    gl.glMatrixMode(GL2.GL_PROJECTION); //Set projection matrix mode
    gl.glLoadIdentity();
    
    //We will use gluPerspective to obtain a perspective camera
    double aspectRatio = (double)width / (double)height;
    double fieldOfView = Math.min(aspectRatio * FIELD_OF_VIEW, MAXIMUM_FIELD_OF_VIEW);
    
    //set near,far so they show everything for all sample terrains of size without clipping
    glu.gluPerspective(fieldOfView, aspectRatio, 0.01d, 10000d);
    
    //Default first person offsets
    double xOffset = 0; //changes the eye position in x axis
    double zOffset = 0; //changes the eye position in y axis
    double altitudeThirdpersonOffset = 0; //top-down typical third person view
    
    //If thirdperson mode, change offets above so they affect our eye and target
    //We want to subtract the offsets from the eye so we are looking from further back
    if (thirdPerson) {
      xOffset = Math.cos(Math.toRadians(cameraRotation));
      zOffset = Math.sin(Math.toRadians(cameraRotation));
      altitudeThirdpersonOffset = THIRDPERSON_ALTITUDE_CHANGE;
    }
    
    glu.gluLookAt(//Eye is the camera location with interpolated altitude
      cameraPosition[0] - xOffset,
      myTerrain.altitude(cameraPosition[0], cameraPosition[1]) + ALTITUDE_OFFSET + altitudeThirdpersonOffset,
      cameraPosition[1] - zOffset,
      
      //center of object is vector going away from camera
      cameraPosition[0] + Math.cos(Math.toRadians(cameraRotation)),
      myTerrain.altitude(cameraPosition[0], cameraPosition[1]) + ALTITUDE_OFFSET,
      cameraPosition[1] + Math.sin(Math.toRadians(cameraRotation)),
      
      //standard camera orientation (UP)
      0,
      1,
      0
    );
    
    gl.glPopMatrix();
  }
  
  /**
   * Setup Sun as light source
   *
   * @param gl GL2 object
   */
  private void setupSun(GL2 gl) {
    gl.glPushMatrix();
  
    gl.glEnable(GL2.GL_LIGHT1);
  
    //Background colour
    gl.glClearColor(0.529411f, 0.807843f, 0.980392f, 1.0f); //Sky Blue, RGB: 135-206-250
    
    //Global Ambient light
    float[] globalAmb = {1.0f, 1.0f, 1.0f, 1.0f}; //full intensity
    gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globalAmb, 0);
    
    //Sunlight (LIGHT1)
    float[] sunlightVector = myTerrain.getSunlight();
    float[] finalSunlightVector = new float[4];
    
    finalSunlightVector[0] = sunlightVector[0];
    finalSunlightVector[1] = sunlightVector[1];
    finalSunlightVector[2] = sunlightVector[2];
    finalSunlightVector[3] = 0; //for directional light
    
    float[] diffuseComponent = new float[]{1.0f, 1.0f, 1.0f, 1.0f}; //diffuse all light
    
    gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, diffuseComponent, 0);
    gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, finalSunlightVector, 0);
    
    gl.glPopMatrix();
  }
  
  /**
   * Setup a torch as a light source.
   *
   * @param gl GL2 object
   */
  private void setupTorch(GL2 gl) {
    gl.glPushMatrix();
    
    //Disable sun in night mode
    gl.glDisable(GL2.GL_LIGHT1);
    
    //Enable torch light
    gl.glEnable(GL2.GL_LIGHT2);
  
    //Background colour
    gl.glClearColor(0.0f, 0.09411f, 0.2823f, 1.0f); //Night sky, storm petrel
    
    //Global Ambient light
    float[] globalAmb = {0.05f, 0.05f, 0.05f, 1.0f}; //very low for night mode
    gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globalAmb, 0);
    
    // Light property vectors.
    float lightAmb[] = {0.05f, 0.05f, 0.05f, 1.0f};
    float lightDifAndSpec[] = {1.0f, 1.0f, 1.0f, 1.0f};
  
    // Light properties.
    gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_AMBIENT, lightAmb, 0);
    gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_DIFFUSE, lightDifAndSpec, 0);
    gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_SPECULAR, lightDifAndSpec, 0);
    
    //Set torch position to camera position
    float[] torchPosition = {(float)cameraPosition[0], (float)(myTerrain.altitude(cameraPosition[0], cameraPosition[1])) + (float)ALTITUDE_OFFSET, (float)cameraPosition[1], 1.0f};
    gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_POSITION, torchPosition, 0);
    
    //Set torch direction (facing outwards from avatar)
    float[] torchDirection = {(float)Math.cos(Math.toRadians(cameraRotation)), 0.0f, (float)Math.sin(Math.toRadians(cameraRotation))};
    gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_SPOT_DIRECTION, torchDirection, 0); //direction vector
  
    //Set cut off and attenuation
    gl.glLightf(GL2.GL_LIGHT2, GL2.GL_SPOT_CUTOFF, 45.0f); //cutoff angle
    gl.glLightf(GL2.GL_LIGHT2, GL2.GL_SPOT_EXPONENT, 0.0f); //attenuation
    
    gl.glPopMatrix();
  }
  
  /**
   * Setup and read in textures to be used later.
   * We only need to do this once.
   */
  private void setupTextures() {
    switch(this.curTexturePack) {
      case 0:
        //Default
        try {
          texturePack.setTerrain(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/fifa_grass.jpg"), true, TextureIO.JPG));
          texturePack.setTreeTrunk(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/tree_trunk.jpg"), true, TextureIO.JPG));
          texturePack.setTreeLeaves(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/tree_leaves.jpg"), true, TextureIO.JPG));
          texturePack.setRoad(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/asphalt.png"), true, TextureIO.PNG));
          texturePack.setAvatar(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/circular_gold.jpg"), true, TextureIO.JPG));
          
          texturePack.setEnemyBody(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/enemy_body.jpg"), true, TextureIO.JPG));
          texturePack.setEnemyEyes(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/enemy_eye.jpg"), true, TextureIO.JPG));
          texturePack.setEnemyMouth(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/enemy_mouth.png"), true, TextureIO.PNG));
          
        } catch (IOException e) {
          //File may not exist
          e.printStackTrace();
        }
        break;
      case 1:
        //Molten/Dead/Halloween
        try {
          texturePack.setTerrain(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/lava.jpg"), true, TextureIO.JPG));
          texturePack.setTreeTrunk(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/black_bark.png"), true, TextureIO.PNG));
          texturePack.setTreeLeaves(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/autumn_leaves.jpg"), true, TextureIO.JPG));
          texturePack.setRoad(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/lava_crack.jpg"), true, TextureIO.JPG));
          texturePack.setAvatar(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/circular_gold.jpg"), true, TextureIO.JPG));
  
          texturePack.setEnemyBody(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/enemy_body.jpg"), true, TextureIO.JPG));
          texturePack.setEnemyEyes(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/enemy_eye.jpg"), true, TextureIO.JPG));
          texturePack.setEnemyMouth(TextureIO.newTexture(this.getClass().getResourceAsStream("/textures/enemy_mouth.png"), true, TextureIO.PNG));
        } catch (IOException e) {
          //File may not exist
          e.printStackTrace();
        }
        break;
      default:
        break;
    }
  }
  
  @Override
  public void keyTyped(KeyEvent e) {
  }
  
  @Override
  public void keyPressed(KeyEvent e) {
    switch(e.getKeyCode()){
      case KeyEvent.VK_UP:
      {
        //Calculate new camera location
        double newX = (Math.cos(Math.toRadians(cameraRotation)) * WALKING_SPEED) + cameraPosition[0];
        double newZ = (Math.sin(Math.toRadians(cameraRotation)) * WALKING_SPEED) + cameraPosition[1];
        
        //Update global camera position only if movement doesn't take us 'off the grid'
        if (!(newX < 0 || newX > myTerrain.size().getWidth() - 1 || newZ < 0 || newZ > myTerrain.size().getHeight() - 1)) {
          cameraPosition[0] = newX;
          cameraPosition[1] = newZ;
        }
      }
      break;
      case KeyEvent.VK_DOWN:
      {
        //Calculate new camera location
        double newX = cameraPosition[0] - (Math.cos(Math.toRadians(cameraRotation)) * WALKING_SPEED);
        double newZ = cameraPosition[1] - (Math.sin(Math.toRadians(cameraRotation)) * WALKING_SPEED);
        
        //Update global camera position only if movement doesn't take us 'off the grid'
        if (!(newX < 0 || newX > myTerrain.size().getWidth() - 1 || newZ < 0 || newZ > myTerrain.size().getHeight() - 1)) {
          cameraPosition[0] = newX;
          cameraPosition[1] = newZ;
        }
      }
      break;
      case KeyEvent.VK_LEFT:
      {
        cameraRotation -= CAMERA_ROTATION_STEP;
      }
      break;
      case KeyEvent.VK_RIGHT:
      {
        cameraRotation += CAMERA_ROTATION_STEP;
      }
      break;
      case KeyEvent.VK_G:
      {
        //Cycle through texture packs
        ++curTexturePack;
        curTexturePack %= NUM_TEXTURE_PACKS; //handle overflow
  
        System.out.println("Switching to texture pack #: " + this.curTexturePack);
      }
      break;
      case KeyEvent.VK_L:
      {
        //Toggle lighting
        this.curLighting = !this.curLighting;
  
        System.out.println("Lighting: " + ((this.curLighting) ? "ENABLED" : "DISABLED"));
      }
      break;
      case KeyEvent.VK_T:
      {
        //Toggle third person mode
        this.thirdPerson = !this.thirdPerson;
        System.out.println("Thirdperson View: " + ((this.thirdPerson) ? "ENABLED" : "DISABLED"));
      }
      break;
      case KeyEvent.VK_F:
      {
        //Toggle fragment shader colour mode (colour/texture)
        if (this.fragmentShaderColourMode == FRAGMENT_SHADER_MODE.COLOUR)
          this.fragmentShaderColourMode = FRAGMENT_SHADER_MODE.TEXTURE;
        else
          this.fragmentShaderColourMode = FRAGMENT_SHADER_MODE.COLOUR;
        
        System.out.println("Fragment Colour Mode: " + ((this.fragmentShaderColourMode == FRAGMENT_SHADER_MODE.COLOUR) ? "COLOURS" : "TEXTURES"));
      }
      break;
      case KeyEvent.VK_N:
      {
        //Toggle night mode
        this.nightMode = !this.nightMode;
        
        System.out.println("Night mode: " + ((this.nightMode) ? "ENABLED" : "DISABLED"));
      }
      break;
      default:
        break;
    }
  }
  
  @Override
  public void keyReleased(KeyEvent e) {
  }
}
