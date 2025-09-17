precision mediump float;  
varying highp vec2 textureCoordinate;

uniform sampler2D faceSkinMaskTexture; 
uniform sampler2D inputImageTexture;

vec3 rgb2hsv(lowp vec3 c) { 
    lowp vec4 K = vec4(0.0, -0.33333, 0.66667, -1.0); 
    highp vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g)); 
    highp vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r)); 
    highp float d = q.x - min(q.w, q.y); 
    highp float e = 1.0e-10; 
    float s = 0.0; 
    lowp vec3 hsv = vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), s, q.x); 
    return hsv; 
} 

void main() { 
    lowp vec3 faceMask = texture2D(faceSkinMaskTexture, textureCoordinate).rgb; 
    lowp vec3 srcColor = texture2D(inputImageTexture, textureCoordinate).rgb; 

    lowp vec3 resColor; 
    lowp vec3 color; 
    float opacity = 1.0;

    vec3 hsvSpace = rgb2hsv(srcColor.rgb); 
    float hue = hsvSpace.x; 
    float value = hsvSpace.z;
    
    if ((0.18 <= hue && hue <= 0.89) || value <= 0.2) { 
        opacity = 0.0; 
    } if (0.16 < hue && hue < 0.18) { 
        opacity = min(opacity, (hue - 0.16) * 50.0); 
    } if (0.89 < hue && hue < 0.91) { 
        opacity = min(opacity, (0.91 - hue) * 50.0); 
    } if (0.2 < value && value < 0.3) { 
        opacity = min(opacity, (0.3 - value) * 10.0); 
    }

    color = vec3(opacity);    
    resColor = min(color, faceMask);
    
    // gl_FragColor = vec4(resColor, 1.0);
    // return;

    gl_FragColor = vec4(resColor, 1.0); 
}