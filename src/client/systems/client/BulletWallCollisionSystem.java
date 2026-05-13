package client.systems.client;

import client.components.CollisionComponent;
import client.components.MovementComponent;
import client.components.NetworkIdComponent;
import client.components.TransformComponent;
import client.components.bullet.BulletComponent;
import client.entities.Bullet;
import client.network.NetworkSpawnManager;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BulletWallCollisionSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<TransformComponent> transformM;
    private ComponentMapper<CollisionComponent> collisionM;

    private NetworkSpawnManager nsm;

    public BulletWallCollisionSystem(NetworkSpawnManager nsm) {
        super(BulletComponent.class);
        this.nsm = nsm;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.transformM = engine.getMapper(TransformComponent.class);
        this.collisionM = engine.getMapper(CollisionComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        TransformComponent transform = transformM.get(id);
        CollisionComponent collision = collisionM.get(id);

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

        if (!hitLeft && !hitRight && !hitTop && !hitBottom) {
            return;
        }

        ComponentMapper<BulletComponent> bm = engine.getMapper(BulletComponent.class);
        if (bm.get(id)!= null) {
            NetworkIdComponent bulletnic = engine.getMapper(NetworkIdComponent.class).get(id);
            nsm.despawn(id, bulletnic.networkId);
        }
    }
}