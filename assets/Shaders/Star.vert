uniform mat4 g_WorldViewProjectionMatrix;

attribute vec4 inPosition;
attribute vec2 inTexCoord;

void main() {  
    gl_Position = g_WorldViewProjectionMatrix * inPosition;
}