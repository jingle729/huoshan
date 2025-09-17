precision mediump float; 
uniform sampler2D inputImageTexture; 
varying highp vec2 textureCoordinate; 

varying highp vec4 textureShift_1; 
varying highp vec4 textureShift_2; 
varying highp vec4 textureShift_3; 
varying highp vec4 textureShift_4; 

uniform sampler2D faceSkinMaskTexture; 

void main() { 
    mediump vec4 curColor=texture2D(inputImageTexture, textureCoordinate); 
    lowp float mask = texture2D(faceSkinMaskTexture, textureCoordinate).b; 

    float tolerance_factor = 5.2486386;
    
    if(mask > 0.0005) { 
        mediump float sum_weight; 
        mediump vec4 sum; 
        mediump vec4 neighborColor; 
        mediump float color_dist; 
        mediump float sample_weight; 
        sum_weight = 0.18; 
        sum = curColor * 0.18; 

        neighborColor = texture2D(inputImageTexture, textureShift_1.xy); 
        color_dist = min(distance(curColor, neighborColor) * tolerance_factor, 1.0); 
        sample_weight = 0.15 * (1.0 - color_dist); sum_weight += sample_weight; 
        sum += neighborColor * sample_weight; 
        
        neighborColor = texture2D(inputImageTexture, textureShift_1.zw); 
        color_dist = min(distance(curColor, neighborColor) * tolerance_factor, 1.0); 
        sample_weight = 0.15 * (1.0 - color_dist); 
        sum_weight += sample_weight; 
        sum += neighborColor * sample_weight; 
        
        neighborColor = texture2D(inputImageTexture, textureShift_2.xy); 
        color_dist = min(distance(curColor, neighborColor) * tolerance_factor, 1.0); 
        sample_weight = 0.12 * (1.0 - color_dist); 
        sum_weight += sample_weight; 
        sum += neighborColor * sample_weight; 
        
        neighborColor = texture2D(inputImageTexture, textureShift_2.zw); 
        color_dist = min(distance(curColor, neighborColor) * tolerance_factor, 1.0); 
        sample_weight = 0.12 * (1.0 - color_dist); 
        sum_weight += sample_weight; 
        sum += neighborColor * sample_weight; 

        neighborColor = texture2D(inputImageTexture, textureShift_3.xy); 
        color_dist = min(distance(curColor, neighborColor) * tolerance_factor, 1.0); 
        sample_weight = 0.09 * (1.0 - color_dist); 
        sum_weight += sample_weight; 
        sum += neighborColor * sample_weight; 
        
        neighborColor = texture2D(inputImageTexture, textureShift_3.zw); 
        color_dist = min(distance(curColor, neighborColor) * tolerance_factor, 1.0); 
        sample_weight = 0.09 * (1.0 - color_dist); 
        sum_weight += sample_weight; 
        sum += neighborColor * sample_weight; 

        neighborColor = texture2D(inputImageTexture, textureShift_3.xy); 
        color_dist = min(distance(curColor, neighborColor) * tolerance_factor, 1.0); 
        sample_weight = 0.05 * (1.0 - color_dist); 
        sum_weight += sample_weight; 
        
        sum += neighborColor * sample_weight; 
        neighborColor = texture2D(inputImageTexture, textureShift_4.zw); 
        color_dist = min(distance(curColor, neighborColor) * tolerance_factor, 1.0); 
        sample_weight = 0.05 * (1.0 - color_dist); 
        sum_weight += sample_weight; 
        
        sum += neighborColor * sample_weight; 
        if (sum_weight < 0.4) { 
            gl_FragColor = curColor; 
        } 
        else if (sum_weight < 0.5) 
        { 
            gl_FragColor = mix(curColor, sum / sum_weight, (sum_weight - 0.4) / 0.1); 
        } 
        else 
        { 
            gl_FragColor = sum / sum_weight; 
        } 
    } 
    else 
    { 
        gl_FragColor = curColor; 
    } 
}