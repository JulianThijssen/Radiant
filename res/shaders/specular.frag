uniform mat4 modelMatrix;
uniform vec3 cameraPosition;
uniform vec3 camDir;

out vec4 out_Color;

void main(void) {
	// Shadows
	float visibility = 1.0;
	float bias = 0.005;
	
	vec3 refl = vec3(0, 0, 0);
	
	//Calculate the location of this fragment (pixel) in world coordinates
    vec3 position = (modelMatrix * vec4(pass_position, 1)).xyz;
    
    //Normals
    vec3 normal = normalize(pass_normal);//normalize(transpose(inverse(mat3(modelMatrix))) * pass_normal);
    
    if(material.hasNormalMap) {
    	normal = calcNormal(normal, pass_tangent, pass_texCoord);
    }
    
    vec3 camDir = normalize(cameraPosition - position);
	
	// Point lighting
	if (isPointLight) {
		PointLight light = pointLight;
		
    	vec3 lightDir = light.position - position;
    	
	    // Calculate the diffuse contribution
	    float fAtt = calcPointAtt(light, lightDir);
	    float fDiffuse = calcDiffuse(lightDir, normal);
	    vec3 diffuseColor = material.diffuseColor;
	    if (material.hasDiffuseMap) {
	    	diffuseColor *= texture(material.diffuseMap, pass_texCoord * material.tiling).rgb;
	    }
		refl += diffuseColor * light.color * light.energy * fDiffuse * fAtt;
	    
	    // Calculate specular lighting
	    float fPhong = calcSpec(lightDir, camDir, normal, material.hardness);
		refl += material.specularColor * material.specularIntensity * light.color * fPhong * fAtt;
		
		// Shadows
		if (material.receiveShadows && light.castShadows) {
			visibility = getPointVisibility(bias * 20, lightDir);
		}
	}
	
	// Directional lighting
	if (isDirLight) {
		DirectionalLight light = dirLight;
	
		// Calculate the vector from this pixels surface to the light source
		vec3 lightDir = -light.direction;
		
		// Calculate diffuse lighting
		float fDiffuse = calcDiffuse(lightDir, normal);
		vec3 diffuseColor = material.diffuseColor;
	    if (material.hasDiffuseMap) {
	    	diffuseColor *= texture(material.diffuseMap, pass_texCoord * material.tiling).rgb;
	    }
		refl += diffuseColor * light.color * fDiffuse * light.energy;
		
		// Calculate specular lighting
		float fPhong = calcSpec(lightDir, camDir, normal, material.hardness);
		refl += material.specularColor * material.specularIntensity * light.color * fPhong;
		
		// Shadows
		if (material.receiveShadows && light.castShadows) {
			shadowCoord = pass_shadowCoord;
			visibility = getDirVisibility(bias);
		}
	}
	
	out_Color = vec4(refl * visibility, 1);
}
