package ass2.spec;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;



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
  private GLU glu;
  
  //Constants
  private static final double FIELD_OF_VIEW = 60.0; //field of view to use in world
  private static final double MAXIMUM_FIELD_OF_VIEW = 180.0; //max possible value for field of view
  private static final double ALTITUDE_OFFSET = 0.5; //camera offset from ground so world is visible
  private static final double WALKING_SPEED = 0.1; //speed at which player (camera) moves at
  private static final double CAMERA_ROTATION_STEP = 10; //number of degrees to rotate camera by
  
  
  public Game(Terrain terrain) {
    super("Assignment 2");
    
    //Default values
    myTerrain = terrain;
    cameraPosition = new double[]{0,0};
    cameraRotation = 45.0;
    width = 800;
    height = 600;
    glu = new GLU();
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
    
    // Add an animator to call 'display' at 60fps
    FPSAnimator animator = new FPSAnimator(60);
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
    
    //Draw terrain
    myTerrain.draw(gl);
    
    
    
  }
  
  @Override
  public void dispose(GLAutoDrawable drawable) {
    // TODO Auto-generated method stub
    // TODO: Probably do not need
  }
  
  @Override
  public void init(GLAutoDrawable drawable) {
    GL2 gl = drawable.getGL().getGL2();
    
    //Background colour
    gl.glClearColor(0.529411f, 0.807843f, 0.980392f, 1.0f); //Sky Blue, RGB: 135-206-250
    
    //Configure required options
    gl.glEnable(GL2.GL_DEPTH_TEST); //need Z axis taken into account
    gl.glEnable(GL2.GL_NORMALIZE); //automatically normalise our normals as we scale
    
    /*
    gl.glEnable(GL2.GL_CULL_FACE);  //optimization to get rid of various fragments
    gl.glEnable(GL2.GL_BACK);
    */
    
    //gl.glEnable(GL2.GL_LIGHTING); //enable lighting
    //gl.glEnable(GL2.GL_LIGHT1); //skip light0 and use first standard light
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
    
    glu.gluLookAt(//Eye is the camera location with interpolated altitude
      cameraPosition[0],
      myTerrain.altitude(cameraPosition[0], cameraPosition[1]) + ALTITUDE_OFFSET,
      cameraPosition[1],
      
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
      default:
        break;
    }
  }
  
  @Override
  public void keyReleased(KeyEvent e) {
  }
}
