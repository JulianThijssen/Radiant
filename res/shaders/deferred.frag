uniform sampler2D colorTex;
uniform sampler2D normalTex;
uniform sampler2D positionTex;
uniform sampler2D specularTex;

uniform mat4 modelMatrix;
uniform mat4 biasMatrix;

uniform vec3 camPos;

out vec4 out_Color;

void main(void) {
	vec3 color = texture(colorTex, pass_texCoord).rgb;
	vec3 normal = fromColor(texture(normalTex, pass_texCoord)).rgb;
	vec3 position = texture(positionTex, pass_texCoord).rgb;
	vec4 specular = texture(specularTex, pass_texCoord);

	float bias = 0.005;
	float visibility = 1.0;
	vec3 refl = vec3(0, 0, 0);
	
	vec3 camDir = normalize(camPos - position);
	
	if (isPointLight) {
		PointLight light = pointLight;
		vec3 lightDir = light.position - position;
		
		float fAtt = calcPointAtt(light, lightDir);
	    float fDiffuse = calcDiffuse(lightDir, normal);
		refl += color * light.color * light.energy * fDiffuse * fAtt;
		
		// Calculate specular lighting
	    float fPhong = calcSpec(lightDir, camDir, normal, specular.w);
		refl += specular.rgb * light.color * fPhong * fAtt;
		
		visibility = getPointVisibility(bias, lightDir);
	}
	if (isDirLight) {
		DirectionalLight light = dirLight;
		
		vec3 lightDir = -light.direction;
		
	    float fDiffuse = calcDiffuse(lightDir, normal);
		refl += color * light.color * light.energy * fDiffuse;
		
		// Calculate specular lighting
	    float fPhong = calcSpec(lightDir, camDir, normal, specular.w);
		refl += specular.rgb * light.color * fPhong;
		
		shadowCoord = biasMatrix * shadowInfo.projectionMatrix * shadowInfo.viewMatrix * vec4(position, 1);
		visibility = getDirVisibility(0.0025);
	}
	
	out_Color = vec4(refl * visibility, 1);
}
