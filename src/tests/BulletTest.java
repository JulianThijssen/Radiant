package tests;

import radiant.engine.core.math.Vector3f;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;

public class BulletTest {
	public static void main(String[] args) {
		BroadphaseInterface bi = new DbvtBroadphase();
		
		DefaultCollisionConfiguration colconf = new DefaultCollisionConfiguration();
		CollisionDispatcher coldisp = new CollisionDispatcher(colconf);
		
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
		
		DiscreteDynamicsWorld world = new DiscreteDynamicsWorld(coldisp, bi, solver, colconf);
		
		world.setGravity(new Vector3f(0f, -10f, 0f));
	}
}
