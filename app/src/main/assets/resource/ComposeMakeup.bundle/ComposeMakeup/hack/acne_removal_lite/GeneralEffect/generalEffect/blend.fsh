precision mediump float;

uniform sampler2D inputImageTexture;
uniform sampler2D blurImageTexture;
uniform sampler2D faceSkinMaskTexture;
uniform sampler2D lutImageTexture;

varying highp vec2 textureCoordinate; 
uniform float intensity;

vec4 LUT8x8(vec4 inColor, sampler2D lutImageTexture)
{
    highp float blueColor = inColor.b * 63.0;
    
    highp vec2 quad1;
    quad1.y = floor(floor(blueColor) / 8.0);
    quad1.x = floor(blueColor) - (quad1.y * 8.0);
    highp vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 8.0);
    quad2.x = ceil(blueColor) - (quad2.y * 8.0);
    
    highp vec2 texPos1;
    texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * inColor.r);
    texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * inColor.g);
    highp vec2 texPos2;
    texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * inColor.r);
    texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * inColor.g);
    
    lowp vec4 newColor2_1 = texture2D(lutImageTexture, texPos1);
    lowp vec4 newColor2_2 = texture2D(lutImageTexture, texPos2);

    lowp vec4 newColor22 = mix(newColor2_1, newColor2_2, fract(blueColor));

    return newColor22;
}

void main()
{
    lowp float dark_threshold_Factor = 28.86751;

    mediump vec4 srcColor = texture2D(inputImageTexture, textureCoordinate); 
    lowp float mask = texture2D(faceSkinMaskTexture, textureCoordinate).b; 

    vec4 dstColor = srcColor;

    if(mask > 0.0002) {            
        lowp vec4 blurColor = texture2D(blurImageTexture, textureCoordinate);

        float empColor = srcColor.b - blurColor.b + 0.5;

        empColor = empColor < 0.5 ? (2.0 * empColor * empColor) : (1.0 - 2.0 * (1.0 - empColor) * (1.0 - empColor));
        empColor = empColor < 0.5 ? (2.0 * empColor * empColor) : (1.0 - 2.0 * (1.0 - empColor) * (1.0 - empColor));
        empColor = empColor < 0.5 ? (2.0 * empColor * empColor) : (1.0 - 2.0 * (1.0 - empColor) * (1.0 - empColor));

        // if (empColor < 0.43) {
        //     vec3 diffColor = clamp(blurColor.rgb - srcColor.rgb, 0.0, 0.3);
        //     diffColor = min(srcColor.rgb + diffColor.rgb, 1.0);

        //     dstColor = vec4(mix(srcColor.rgb, diffColor.rgb, mask), 1.0);
        // }
        // 选取非白色区域，做曲线提亮 + 去黄红
        if (empColor < 0.43) {
            vec4 brightColor = LUT8x8(srcColor, lutImageTexture);
            dstColor = mix(srcColor, brightColor, intensity * 0.3 * mask); 
        }
        gl_FragColor = dstColor;
    }
    else{
        gl_FragColor = dstColor; 
    }
    
}