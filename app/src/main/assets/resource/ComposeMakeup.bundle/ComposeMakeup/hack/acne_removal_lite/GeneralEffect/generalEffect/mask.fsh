precision highp float;
varying vec2 textureCoordinate;
uniform sampler2D inputImageMaskTexture;

void main()
{
    lowp vec4 maskColor = texture2D(inputImageMaskTexture, textureCoordinate);
    gl_FragColor = vec4(maskColor.rgb, 1.0);
}