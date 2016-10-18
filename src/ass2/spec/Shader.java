package ass2.spec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import com.jogamp.opengl.GL2;


/**
 * COMMENT: Comment Shader
 *
 * Code from Week 6 Lecture code
 *
 * @author
 */
public class Shader {
  private String[] mySource;
  private int myType;
  private int myID;
  
  //read file into a string
  public Shader(int type, File sourceFile) throws IOException {
    myType = type;
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader( new FileInputStream(sourceFile)));
      StringWriter writer = new StringWriter();
      mySource = new String[1];
      String line = reader.readLine();
      while (line != null) {
        writer.write(line);
        writer.write("\n");
        line = reader.readLine();
      }
      reader.close();
      mySource[0] = writer.getBuffer().toString();
      
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public int getID() {
    return myID;
  }
  
  public void printShader(){
    int i =0;
    for(i=0; i < mySource.length;i++){
      System.out.println(mySource[i]);
    }
  }
  
  public void compile(GL2 gl) {
    printShader(); //debugging
    myID = gl.glCreateShader(myType);
    gl.glShaderSource(myID, 1, mySource, new int[] { mySource[0].length() }, 0);
    gl.glCompileShader(myID);
    
    //Check compile status.
    int[] compiled = new int[1];
    gl.glGetShaderiv(myID, GL2.GL_COMPILE_STATUS, compiled, 0);
    if (compiled[0] == 0) {
      int[] logLength = new int[1];
      gl.glGetShaderiv(myID, GL2.GL_INFO_LOG_LENGTH, logLength, 0);
      
      byte[] log = new byte[logLength[0]];
      gl.glGetShaderInfoLog(myID, logLength[0], (int[]) null, 0, log, 0);
      
      throw new CompilationException("Error compiling the shader: "
        + new String(log));
    }
    
  }
  
  public static int initShaders(GL2 gl, String vs, String fs) throws Exception {
    Shader vertexShader = new Shader(GL2.GL_VERTEX_SHADER, new File(vs));
    vertexShader.compile(gl);
    Shader fragmentShader = new Shader(GL2.GL_FRAGMENT_SHADER, new File(fs));
    fragmentShader.compile(gl);
    
    //Each shaderProgram must have
    //one vertex shader and one fragment shader.
    int shaderprogram = gl.glCreateProgram();
    gl.glAttachShader(shaderprogram, vertexShader.getID());
    gl.glAttachShader(shaderprogram, fragmentShader.getID());
    
    gl.glLinkProgram(shaderprogram);
    
    
    int[] error = new int[2];
    gl.glGetProgramiv(shaderprogram, GL2.GL_LINK_STATUS, error, 0);
    if (error[0] != GL2.GL_TRUE) {
      int[] logLength = new int[1];
      gl.glGetProgramiv(shaderprogram, GL2.GL_INFO_LOG_LENGTH, logLength, 0);
      
      byte[] log = new byte[logLength[0]];
      gl.glGetProgramInfoLog(shaderprogram, logLength[0], (int[]) null, 0, log, 0);
      
      System.out.printf("Failed to link shader! %s\n", new String(log));
      throw new CompilationException("Error linking the shader: "
        + new String(log));
    }
    
    gl.glValidateProgram(shaderprogram);
    
    gl.glGetProgramiv(shaderprogram, GL2.GL_VALIDATE_STATUS, error, 0);
    if (error[0] != GL2.GL_TRUE) {
      System.out.printf("Failed to validate shader!\n");
      throw new Exception("program failed to validate");
    }
    
    
    return shaderprogram;
  }
  
  static public class CompilationException extends RuntimeException {
    
    public CompilationException(String message) {
      super(message);
    }
    
  }
}
