precision highp float;

varying vec2 textureCoods;
varying vec2 singleStepOffset;

const int GAUSSIAN_SAMPLES = 11;

uniform sampler2D inputImageTex;
uniform sampler2D faceSkinMaskTexture; 

void main() {
    lowp float mask = texture2D(faceSkinMaskTexture, textureCoods).b; 
    if (mask > 0.0005){
        int multiplier = 0;
        
        vec2 blurStep;
        vec2 blurCoordinates[GAUSSIAN_SAMPLES];
        for(int i = 0; i < GAUSSIAN_SAMPLES; i++)
        {
            multiplier = (i - ((GAUSSIAN_SAMPLES - 1)/2));
            blurStep = float(multiplier) * singleStepOffset;
            blurCoordinates[i] = textureCoods + blurStep;
        }
        
        float blurWeights[GAUSSIAN_SAMPLES];

        blurWeights[0] = 0.0663;
        blurWeights[1] = 0.0794;
        blurWeights[2] = 0.0914;
        blurWeights[3] = 0.101;
        blurWeights[4] = 0.1072;
        blurWeights[5] = 0.1094;
        blurWeights[6] = 0.1072;
        blurWeights[7] = 0.101;
        blurWeights[8] = 0.0914;
        blurWeights[9] = 0.0794;
        blurWeights[10] = 0.0663; 

        float sum = 0.0;    
        for(int i = 0; i < GAUSSIAN_SAMPLES; ++i) {
            sum += blurWeights[i] * texture2D(inputImageTex, blurCoordinates[i]).b;
        }
        
        gl_FragColor = vec4(sum, sum, sum, 1.0);
    }
    else{
        vec4 curColor=texture2D(inputImageTex, textureCoods); 
        gl_FragColor = curColor;
    }
}
