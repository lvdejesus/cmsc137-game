package client.entities;

import framework.engine.Entity;
import framework.engine.Engine;
import client.systems.client.Context;
import client.components.*;
import client.components.bullet.BulletComponent;
import client.components.CollisionComponent;
import client.rendering.Texture;
import client.rendering.TextureAtlas;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Bullet extends Prefab {
    public Bullet(Engine<Context> engine, float x, float y, float angleDegrees) {
        this(engine, -1, x, y, angleDegrees, false);
    }

    public Bullet(Engine<Context> engine, int networkId, float x, float y, float angleDegrees, boolean isEnemy) {
        super(engine);

        TextureAtlas atlas = TextureAtlas.get();
        String textureName = isEnemy ? "bullets/enemy_bullets.png" : "bullets/friend_bullets.png";
        Texture bulletTexture = atlas.getRegion(textureName);

        float angleRadians = (float) Math.toRadians(angleDegrees);

        float speed = isEnemy ? 300.0f : 400.0f;
        float vx = (float) Math.cos(angleRadians) * speed;
        float vy = (float) Math.sin(angleRadians) * speed;

        entity.addComponent(new TransformComponent(new Vector2f(x, y), new Vector2f(0.5f, 0.5f)));
        entity.addComponent(new MovementComponent(0, 0, 0, new Vector2f(vx, vy)));
        entity.addComponent(new RenderComponent(bulletTexture));
        
        BulletComponent bulletComp = new BulletComponent();
        bulletComp.isEnemy = isEnemy;
        bulletComp.lifetime = isEnemy ? 4.0f : 2.0f;
        entity.addComponent(bulletComp);
        
        entity.addComponent(new CollisionComponent(new AABBf(
            new Vector3f(-4.0f, -4.0f, 0.0f),
            new Vector3f(4.0f, 4.0f, 0.1f)
        )));
        entity.addComponent(new NetworkIdComponent(networkId));
    }

    public static Bullet deserialize(Engine<Context> engine, int networkId, ByteBuffer bytes) throws IOException {
        float x = bytes.getFloat();
        float y = bytes.getFloat();
        float angleDegrees = bytes.getFloat();
        boolean isEnemy = bytes.get() == 1;

        return new Bullet(engine, networkId, x, y, angleDegrees, isEnemy);
    }

    public static byte[] serialize(float x, float y, float angle, boolean isEnemy) {
        ByteBuffer bytes = ByteBuffer.allocate(13);

        bytes.putFloat(x);
        bytes.putFloat(y);
        bytes.putFloat(angle);
        bytes.put((byte) (isEnemy ? 1 : 0));

        return bytes.array();
    }
}