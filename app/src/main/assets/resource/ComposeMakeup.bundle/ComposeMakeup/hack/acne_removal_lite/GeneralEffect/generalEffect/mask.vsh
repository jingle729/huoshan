attribute vec3 attPosition;     //vertex coordinate
attribute vec2 attUV;           //uv coordinate
attribute vec2 attStandardUV;   //uv coordinate of standard face

varying vec2 textureCoordinate;

uniform mat4 uSTMatrix;
uniform int inputTextureWidth;
uniform int inputTextureHeight;

void main(){
    gl_Position = vec4(attPosition.x / float(inputTextureWidth) * 2. - 1., 
                       attPosition.y / float(inputTextureHeight) * 2. - 1., 
                       0.0, 
                       1.0);
    textureCoordinate = (uSTMatrix * vec4(attStandardUV, 0., 1.)).xy;
}