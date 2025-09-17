precision highp float;  
varying highp vec2 textureCoordinate;

// uniform sampler2D faceSkinMaskTexture; 
uniform sampler2D inputImageTexture;
uniform float hue_intensity;
uniform float satu_intensity;
uniform float light_intensity;


vec3 pixel_adjust(float h, float hue, float saturation, float brightness, float left_left, float left, float right, float right_right, vec3 delta_hsb) {
    if (left_left < left && left > right && right < right_right) {
        if (h >= left && h <= 360.) {
            delta_hsb.x += hue;
            delta_hsb.y += saturation;
            delta_hsb.z += brightness;
            return delta_hsb;
        }
        if (h >= 0. && h <= right) {
            delta_hsb.x += hue;
            delta_hsb.y += saturation;
            delta_hsb.z += brightness;
            return delta_hsb;
        }
        if (h >= left_left && h <= left) {
            delta_hsb.x += hue * (h - left_left) / (left - left_left);
            delta_hsb.y += saturation * (h - left_left) / (left - left_left);
            delta_hsb.z += brightness * (h - left_left) / (left - left_left);
            return delta_hsb;
        }
        if (h >= right && h <= right_right) {
            delta_hsb.x += hue * (right_right - h) / (right_right - right);
            delta_hsb.y += saturation * (right_right - h) / (right_right - right);
            delta_hsb.z += brightness * (right_right - h) / (right_right - right);
            return delta_hsb;
        }
    }
    if (left_left > left && left < right && right < right_right) {
        if (h >= left && h <= right) {
            delta_hsb.x += hue;
            delta_hsb.y += saturation;
            delta_hsb.z += brightness;
            return delta_hsb;
        }
        if (h >= 0. && h <= left) {
            delta_hsb.x += hue * (h + 360. - left_left) / (left + 360. - left_left);
            delta_hsb.y += saturation * (h + 360. - left_left) / (left + 360. - left_left);
            delta_hsb.z += brightness * (h + 360. - left_left) / (left + 360. - left_left);
            return delta_hsb;
        }
        if (h >= left_left && h <= 360.) {
            delta_hsb.x += (hue * (h - left_left) / (left + 360. - left_left));
            delta_hsb.y += (saturation * (h - left_left) / (left + 360. - left_left));
            delta_hsb.z += (brightness * (h - left_left) / (left + 360. - left_left));
            return delta_hsb;
        }
        if (h >= right && h <= right_right) {
            delta_hsb.x += hue * (right_right - h) / (right_right - right);
            delta_hsb.y += (saturation * (right_right - h) / (right_right - right));
            delta_hsb.z += (brightness * (right_right - h) / (right_right - right));
            return delta_hsb;
        }
    }
    if (left_left <= left && left < right && right <= right_right) {

        if (h >= left && h <= right) {
            delta_hsb.x += hue;
            delta_hsb.y += saturation;
            delta_hsb.z += brightness;
            return delta_hsb;
        }
        if (h >= left_left && h <= left) {
            delta_hsb.x += hue * (h - left_left) / (left - left_left);
            delta_hsb.y += (saturation * (h - left_left) / (left - left_left));
            delta_hsb.z += (brightness * (h - left_left) / (left - left_left));
            return delta_hsb;
        }
        if (h >= right && h <= right_right) {
            delta_hsb.x += hue * (right_right - h) / (right_right - right);
            delta_hsb.y += (saturation * (right_right - h) / (right_right - right));
            delta_hsb.z += (brightness * (right_right - h) / (right_right - right));
            return delta_hsb;
        }
    }
    if (left_left < left && left < right && right > right_right) {
        if (h >= left && h <= right) {
            delta_hsb.x += hue;
            delta_hsb.y += saturation;
            delta_hsb.z += brightness;
            return delta_hsb;
        }
        if (h >= left_left && h <= left) {
            delta_hsb.x += hue * (h - left_left) / (left - left_left);
            delta_hsb.y += saturation * (h - left_left) / (left - left_left);
            delta_hsb.z += brightness * (h - left_left) / (left - left_left);
            return delta_hsb;
        }
        if (h >= right && h <= 360.) {
            delta_hsb.x += hue * (right_right + 360. - h) / (right_right + 360. - right);
            delta_hsb.y += saturation * (right_right + 360. - h) / (right_right + 360. - right);
            delta_hsb.z += brightness * (right_right + 360. - h) / (right_right + 360. - right);
            return delta_hsb;
        }
        if (h >= 0. && h <= right_right) {
            delta_hsb.x += hue * (right_right - h) / (right_right + 360. - right);
            delta_hsb.y += saturation * (right_right - h) / (right_right + 360. - right);
            delta_hsb.z += brightness * (right_right - h) / (right_right + 360. - right);
            return delta_hsb;
        }
    }
    return delta_hsb;
}

vec3 rgb2hsl(vec3 rgb) {
    float h = 0., s = 0., l = 0.;
    float r = rgb.r;
    float g = rgb.g;
    float b = rgb.b;
    float cmax = max(r, max(g, b));
    float cmin = min(r, min(g, b));
    float delta = cmax - cmin;
    l = (cmax + cmin) / 2.;
    if (delta < .0000001) {
        s = 0.;
        h = 0.;
    } else {
        if (l <= .5)
            s = delta / (cmax + cmin);
        else
            s = delta / (2. - (cmax + cmin));
        if (cmax  == r) {
            if (g >= b)
                h = 60. * (g - b) / delta;
            else
                h = 60. * (g - b) / delta + 360.;
        } else if (cmax == g) {
            h = 60. * (b - r) / delta + 120.;
        } else {
            h = 60. * (r - g) / delta + 240.;
        }
    }
    return vec3(h, s, l);
}

float hueToRgb(float p, float q, float t) {
    if (t < 0.)
        t += 1.;
    if (t > 1.)
        t -= 1.;
    if (t < 1. / 6.)
        return p + (q - p) * 6. * t;
    if (t < 1. / 2.)
        return q;
    if (t < 2. / 3.)
        return p + (q - p) * (2. / 3. - t) * 6.;
    return p;
}

vec3 hsl2rgb(vec3 hsl) {
    float r, g, b;
    float h = hsl.x / 360.;
    if (hsl.y == 0.) {
        r = g = b = hsl.z; // gray
    } else {
        float q = hsl.z < .5 ? hsl.z * (1. + hsl.y) : (hsl.z + hsl.y - hsl.z * hsl.y);
        float p = 2. * hsl.z - q;
        r = hueToRgb(p, q, h + 1. / 3.);
        g = hueToRgb(p, q, h);
        b = hueToRgb(p, q, h - 1. / 3.);
    }

    return vec3(r, g, b);
}


vec3 HSLadjust(vec3 src, float adj_hue, float adj_saturation, float adj_lightness) {
    //RGB to HSL conversion
    vec3 hsb = rgb2hsl(src);

    //adjust red channel:
    vec3 delta_hsb = vec3(0.);
    delta_hsb = pixel_adjust(hsb.x, adj_hue, adj_saturation, adj_lightness, 13., 14., 17., 18., delta_hsb);

    //adjust hue
    hsb.x = hsb.x + delta_hsb.x;
    while (hsb.x > 360.) {
        hsb.x -= 360.;
    }
    while (hsb.x < 0.) {
        hsb.x += 360.;
    }

    //adjust saturation
    delta_hsb.y = clamp(delta_hsb.y / 100., -1., 1.);
    // if (delta_hsb.y < 0.) {
    //     hsb.y = hsb.y * (1. + delta_hsb.y);
    // } else {
    //     float temp = hsb.y * (1. - delta_hsb.y);
    //     hsb.y = hsb.y + (hsb.y - temp);
    // }
    hsb.y += delta_hsb.y;

    //adjust brightness
    delta_hsb.z = clamp(delta_hsb.z / 100., -1., 1.);
    if (delta_hsb.z < 0.) {
        float radio = hsb.y;
        if (hsb.z >= .5) {
            radio = hsb.y * 1.;
        }
        if (hsb.z < .5) {
            radio = hsb.y * 2. * hsb.z;
        }
        float temp = hsb.z - radio * (1. - hsb.z) * delta_hsb.z;
        hsb.z = hsb.z + (hsb.z - temp);
        hsb.y = hsb.y * (1. + delta_hsb.z);
    } else {
        float radio = hsb.y;
        if (hsb.z >= .5) {
            radio = hsb.y * 1.;
        }
        if (hsb.z < .5) {
            radio = hsb.y * 2. * hsb.z;
        }
        hsb.z = hsb.z + radio * (1. - hsb.z) * delta_hsb.z;
        hsb.y = hsb.y * (1. - delta_hsb.z);
    }
    hsb.y = clamp(hsb.y, 0., 1.);
    hsb.z = clamp(hsb.z, 0., 1.);

    //output
    vec3 out_rgb = hsl2rgb(hsb);

    return out_rgb;
}

// vec3 HSLadjust_new(vec3 src, float delta_hue, float delta_saturation, float delta_lightness)
// {
//     // RGB to HSL
//     float cMax = max(max(src.r, src.g), src.b);
//     float cMin = min(min(src.r, src.g), src.b);
//     float chroma = cMax - cMin;

//     // https://stackoverflow.com/a/39147465
//     float hue = 0.0; // Hue in the range of 0 to 360 
//     if (chroma > 0.0) {
//         if (cMax == src.r) {
//             if(src.g >= src.b)
//                 hue = 60.0 * ((src.g - src.b) / chroma);
//             else
//                 hue = 60.0 * ((src.g - src.b) / chroma) + 360.;
//             // hue = 60.0 * ((src.g - src.b) / chroma);
//         } else if (cMax == src.g) {
//             hue = 60.0 * ((src.b - src.r) / chroma + 2.0);
//         } else {
//             hue = 60.0 * ((src.r - src.g) / chroma + 4.0);
//         }
//     }

//     float lightness = (cMax + cMin) / 2.0;

//     float saturation = 0.;
//     if (cMax == 0. || cMin == 1.) {
//         saturation = 0.;
//     } else {
//         // if(lightness<=.5)
//         //     saturation = chroma / (cMax + cMin);
//         // else
//         //     saturation = chroma / (2. - (cMax + cMax));
//         saturation = (cMax - lightness) / min(lightness, 1. - lightness);
//     }

//     // vec3 hsl_img = rgb2hsl(src);
//     // float hue = hsl_img.x;
//     // float saturation = hsl_img.y;
//     // float lightness = hsl_img.z;




//     if (hue >= 7. && hue <=24.)
//     {
//         // Modify hue and (possibly) saturation values
//         hue += delta_hue;
//         if (hue < 0.) {
//             hue += 360.;
//         } else if (hue > 360.) {
//             hue -= 360.;
//         }

//         // Only over-saturation (i.e. an increase in the level of saturation) happens here if there is any
//         if (delta_saturation >= 0.) {
//             saturation += delta_saturation;
//         }
//         if (saturation < 0. || (src.r == src.g && src.g == src.b)) {
//             saturation = 0.;
//         } else if (saturation > 1.) {
//             saturation = 1.;
//         }
//     }

//     // HSL to RGB
//     vec3 rgb = vec3(0.0);
//     if (saturation == 0.) {
//         rgb = vec3(lightness);
//         src = rgb;
//     } else {
//         chroma = (1. - abs(2. * lightness - 1.)) * saturation;
//         float x = chroma * (1. - abs(mod((hue / 60.), 2.) - 1.));
//         if (hue <= 60.) {
//             rgb = vec3(chroma, x, 0.);
//         } else if (hue <= 120.) {
//             rgb = vec3(x, chroma, 0.);
//         } else if (hue <= 180.) {
//             rgb = vec3(0., chroma, x);
//         } else if (hue <= 240.) {
//             rgb = vec3(0., x, chroma);
//         } else if (hue <= 300.) {
//             rgb = vec3(x, 0., chroma);
//         } else {
//             rgb = vec3(chroma, 0., x);
//         }

//         float m = lightness - chroma / 2.;
//         rgb += m;
//     }

//     // Only desaturating (i.e. a decrease in the level of saturation) happens here, this is done by blending in luma component
//     // Referenced from https://stackoverflow.com/a/20820649
//     // if (delta_saturation < 0.) {
//     //     float L = 0.299 * rgb.r + 0.587 * rgb.g + 0.114 * rgb.b;
//     //     rgb = vec3(rgb.r + (-delta_saturation * (L - rgb.r)), rgb.g + (-delta_saturation * (L - rgb.g)), rgb.b + (-delta_saturation * (L - rgb.b)));
//     // }

//     // Modify lightness, for darkening we simply decrease the RGB values proportionally; for lightening we blend the original image with a white layer (1, 1, 1)
//     // rgb = delta_lightness < 1.0 ? delta_lightness * rgb : (delta_lightness - 1.0) * vec3(1.0) + (2.0 - delta_lightness) * rgb;

//     return rgb;
// }


void main(void) {
    vec4 curColor = texture2D(inputImageTexture, textureCoordinate);
    // if (curColor.a > 0.)
    //     curColor.rgb /= curColor.a;

    vec3 resultColor = HSLadjust(curColor.rgb, hue_intensity, satu_intensity, light_intensity);

    gl_FragColor = vec4(resultColor, 1.) * curColor.a;
}
