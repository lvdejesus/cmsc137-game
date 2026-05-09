package client.systems;

import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import client.components.TransformComponent;
import client.components.CollisionComponent;
import client.components.MovementComponent;
import client.components.bullet.BulletComponent;
import client.rendering.Camera;

public class WallCollisionSystem extends EntitySystem<Context> {
    private ComponentMapper<TransformComponent> transformM;
    private ComponentMapper<CollisionComponent> collisionM;
    private ComponentMapper<MovementComponent> movementM;
    private Camera camera;

    public WallCollisionSystem(Camera camera) {
        super(TransformComponent.class, CollisionComponent.class);
        this.camera = camera;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.transformM = engine.getMapper(TransformComponent.class);
        this.collisionM = engine.getMapper(CollisionComponent.class);
        this.movementM = engine.getMapper(MovementComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        TransformComponent transform = transformM.get(id);
        CollisionComponent collision = collisionM.get(id);

        float minX = -collision.boundingBox.minX();
        float maxX = camera.width - collision.boundingBox.maxX();
        float minY = -collision.boundingBox.minY();
        float maxY = camera.height - collision.boundingBox.maxY();

        boolean hitLeft = transform.position.x < minX;
        boolean hitRight = transform.position.x > maxX;
        boolean hitTop = transform.position.y < minY;
        boolean hitBottom = transform.position.y > maxY;

        if (!hitLeft && !hitRight && !hitTop && !hitBottom) {
            return;
        }

        if (hitLeft) transform.position.x = minX;
        if (hitRight) transform.position.x = maxX;
        if (hitTop) transform.position.y = minY;
        if (hitBottom) transform.position.y = maxY;

        long bitsets[] = getBitsets();
        int bulletIndex = getComponentIndex(BulletComponent.class);
        long bulletMask = 1L << bulletIndex;

        if ((bitsets[id] & bulletMask) == bulletMask) {
            engine.destroyEntity(id);
            return;
        }

        int movementIndex = getComponentIndex(MovementComponent.class);
        long movementMask = 1L << movementIndex;
        if ((bitsets[id] & movementMask) == movementMask) {
            MovementComponent movement = movementM.get(id);
            if (hitLeft || hitRight) movement.velocity.x = 0;
            if (hitTop || hitBottom) movement.velocity.y = 0;
        }
    }
}