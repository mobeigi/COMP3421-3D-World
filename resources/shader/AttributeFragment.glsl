#version 130

// Based on tutorial from: https://www.opengl.org/sdk/docs/tutorials/ClockworkCoders/lighting.php

uniform vec4 colour;
uniform vec3 sunLight;

in vec3 V;
in vec3 N;

void main (void) {
  //Gather required variables
  vec3 L = normalize(sunLight - V); //normalise light source
  vec3 E = normalize(-V); // we are in Eye Coordinates, so EyePos is (0,0,0)
  vec3 R = normalize(-reflect(L, N));  
  
  //Calculate ambient lighting
  vec4 l_ambient = gl_FrontMaterial.ambient;
  l_ambient = clamp(l_ambient, 0.0, 1.0);
   
  //Calculate diffuse lighting
  vec4 l_diffuse = gl_FrontMaterial.diffuse * max(dot(N,L), 0.0);
  l_diffuse = clamp(l_diffuse, 0.0, 1.0);
  
  //Calculate specular lighting
  vec4 l_specular = gl_FrontMaterial.specular 
                * pow(max(dot(R,E), 0.0), 0.3 * gl_FrontMaterial.shininess);
  l_specular = clamp(l_specular, 0.0, 1.0); 
  
  //Set final colour with all lighting
  gl_FragColor = gl_Color + l_ambient + l_diffuse + l_specular;
}

