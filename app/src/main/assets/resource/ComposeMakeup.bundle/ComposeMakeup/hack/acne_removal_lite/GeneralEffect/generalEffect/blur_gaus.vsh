precision highp float;

attribute vec3 attPosition;
attribute vec2 attUV;
varying vec2 textureCoods;

uniform float texelWidthOffset;
uniform float texelHeightOffset;
varying vec2 singleStepOffset;

void main() {
    gl_Position = vec4(attPosition, 1.0);
    textureCoods = attUV;

    singleStepOffset = vec2(texelWidthOffset, texelHeightOffset); 
}
