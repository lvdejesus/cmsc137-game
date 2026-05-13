package client.systems.client;

import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import client.components.TransformComponent;
import client.components.CollisionComponent;
import client.components.MovementComponent;

public class WallCollisionSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<TransformComponent> transformM;
    private ComponentMapper<CollisionComponent> collisionM;
    private ComponentMapper<MovementComponent> movementM;

    public WallCollisionSystem() {
        super(TransformComponent.class, CollisionComponent.class);
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

        // Play area boundaries (matching map_1.png scaled to 800x600)
        float mapWidth = 800.0f;
        float mapHeight = 600.0f;
        
        float paddingX = 15.0f;
        float paddingBottom = 15.0f;
        float paddingTop = 60.0f;
        
        float minX = paddingX - collision.boundingBox.minX();
        float maxX = (mapWidth - paddingX) - collision.boundingBox.maxX();
        float minY = paddingTop - collision.boundingBox.minY();
        float maxY = (mapHeight - paddingBottom) - collision.boundingBox.maxY();

        boolean hitLeft = transform.position.x < minX;
        boolean hitRight = transform.position.x > maxX;
        boolean hitTop = transform.position.y < minY;
        boolean hitBottom = transform.position.y > maxY;

        if (hitLeft) transform.position.x = minX;
        else if (hitRight) transform.position.x = maxX;

        if (hitTop) transform.position.y = minY;
        else if (hitBottom) transform.position.y = maxY;

        if (!hitLeft && !hitRight && !hitTop && !hitBottom) return;
        if (movementM.get(id) != null) {
            MovementComponent movement = movementM.get(id);
            if (hitLeft || hitRight) movement.velocity.x = 0;
            if (hitTop || hitBottom) movement.velocity.y = 0;
        }
    }
}