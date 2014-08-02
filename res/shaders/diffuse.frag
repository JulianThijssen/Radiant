#version 330 core

struct Light {
	vec4 position;
	vec3 color;
	float constantAtt;
	float linearAtt;
	float quadraticAtt;
};

uniform mat4 modelMatrix;

uniform sampler2D diffuse;
uniform vec2 tiling;

uniform Light lights[100];
uniform int numLights;

in vec4 pass_position;
in vec2 pass_texCoord;
in vec3 pass_normal;
in vec3 pass_tangent;

out vec4 out_Color;

void main(void) {
	vec3 light = vec3(0, 0, 0);
	
	//Calculate the location of this fragment (pixel) in world coordinates
    vec4 position = modelMatrix * pass_position;
    
    for(int i = 0; i < numLights; i++) {
	    //Calculate the vector from this pixels surface to the light source
	    vec3 lightDir = lights[i].position.xyz - position.xyz;
	    float length = length(lightDir);
	    vec3 lightColor = lights[i].color;
	    
	    //Calculate the cosine of the angle of incidence (brightness)
	    float fDiffuse = dot(normalize(pass_normal), normalize(lightDir));
	    float fAttTotal = 1 / (lights[i].constantAtt + lights[i].linearAtt * length + lights[i].quadraticAtt * length * length);
	    light.r += lightColor.r * fAttTotal * fDiffuse;
	    light.g += lightColor.g * fAttTotal * fDiffuse;
	    light.b += lightColor.b * fAttTotal * fDiffuse;
	}

    out_Color = texture(diffuse, pass_texCoord * tiling) * vec4(light, 1);
}
