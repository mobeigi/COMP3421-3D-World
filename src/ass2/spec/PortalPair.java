package ass2.spec;

import com.jogamp.opengl.GL2;

/**
 * PortalPair object.
 * Contains two portals which act as gateways to each other.
 *
 * Defined in scene language like so:
 *   "portals" : [
 *    { "firstX" : 1.0, "firstZ" : 4.0 , "firstRotation" : 135.0, "secondX" : 1.0, "secondZ" : 1.0 , "secondRotation" : 45.0} ]
 *
 */
public class PortalPair {
  private Portal first;
  private Portal second;
  
  public PortalPair(Terrain terrain, double firstX, double firstZ, double firstRotation,
                    double secondX, double secondZ, double secondRotation) {
    this.first = new Portal(terrain, firstX, firstZ, firstRotation, 1);
    this.second = new Portal(terrain, secondX, secondZ, secondRotation, 2);
  }
  
  public void draw(GL2 gl, TexturePack texturePack) {
    first.draw(gl, texturePack);
    second.draw(gl, texturePack);
  }
  
  public Portal getFirst() {
    return first;
  }
  
  public Portal getSecond() {
    return second;
  }
}
