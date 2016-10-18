#version 130

// Based on tutorial from: https://www.opengl.org/sdk/docs/tutorials/ClockworkCoders/lighting.php

in vec3 vertexPosition;
in vec3 vertexNormals;
in vec2 vertexTextures;

out vec3 V;
out vec3 N;

void main(void) {
  //Set output vertex position
  gl_Position =  gl_ModelViewProjectionMatrix * vec4(vertexPosition, 1.0);

  //The current vertex position is transformed to eye space
  //These are passed to fragment shader for lighting
  V = vec3(gl_ModelViewMatrix * vec4(vertexPosition, 1.0));
  N = normalize(gl_NormalMatrix * vertexNormals);
  
  //Pass gl colour through
  gl_FrontColor = gl_Color;
  gl_BackColor = gl_Color;
}


