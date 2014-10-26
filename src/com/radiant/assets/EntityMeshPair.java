package com.radiant.assets;

import com.radiant.entities.Entity;

/** Temporary class to test rendering without refactoring */

public class EntityMeshPair {
	public Entity entity;
	public Mesh mesh;
	
	public EntityMeshPair(Entity entity, Mesh mesh) {
		this.entity = entity;
		this.mesh = mesh;
	}
}
