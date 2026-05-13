package client.entities;

import framework.engine.Engine;
import client.systems.client.Context;
import client.components.*;
import client.components.bullet.BulletComponent;
import client.rendering.Texture;
import client.rendering.TextureAtlas;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Bullet extends Prefab {
    int networkId;
    float x;
    float y;
    float pvx;
    float pvy;
    float angleDegrees;
    boolean isEnemy;

    public Bullet(Engine<Context> engine, int networkId, float x, float y, float pvx, float pvy, float angleDegrees,
            boolean isEnemy) {
        super(engine);

        this.networkId = networkId;
        this.x = x;
        this.y = y;
        this.pvx = pvx;
        this.pvy = pvy;
        this.angleDegrees = angleDegrees;
        this.isEnemy = isEnemy;
    }

    @Override
    public void spawn() {
        TextureAtlas atlas = TextureAtlas.get();
        String textureName = isEnemy ? "bullets/enemy_bullets.png" : "bullets/friend_bullets.png";
        Texture bulletTexture = atlas.getRegion(textureName);
        entity.addComponent(new RenderComponent(bulletTexture));

        spawnServer();
    }

    @Override
    public void spawnServer() {
        float angleRadians = (float) Math.toRadians(angleDegrees);
        float speed = isEnemy ? 300.0f : 700.0f;
        Vector2f dir = new Vector2f((float) Math.cos(angleRadians), (float) Math.sin(angleRadians));
        float inherited = (pvx * dir.x + pvy * dir.y);
        float inhertedStr = 1f;

        float vx = dir.x * speed + dir.x * inherited * inhertedStr;
        float vy = dir.y * speed + dir.y * inherited * inhertedStr;

        entity.addComponent(new TransformComponent(new Vector2f(x, y), new Vector2f(0.5f, 0.5f)));
        entity.addComponent(new MovementComponent(0, 0, 0, new Vector2f(vx, vy)));

        BulletComponent bulletComp = new BulletComponent();
        bulletComp.isEnemy = isEnemy;
        bulletComp.lifetime = isEnemy ? 4.0f : 2.0f;
        entity.addComponent(bulletComp);

        entity.addComponent(new CollisionComponent(new AABBf(
                new Vector3f(-4.0f, -4.0f, 0.0f),
                new Vector3f(4.0f, 4.0f, 0.1f))));
        entity.addComponent(new NetworkIdComponent(networkId));
    }

    public static Bullet deserialize(Engine<Context> engine, int networkId, ByteBuffer bytes) throws IOException {
        float x = bytes.getFloat();
        float y = bytes.getFloat();
        float pvx = bytes.getFloat();
        float pvy = bytes.getFloat();
        float angleDegrees = bytes.getFloat();
        boolean isEnemy = bytes.get() == 1;

        return new Bullet(engine, networkId, x, y, pvx, pvy, angleDegrees, isEnemy);
    }

    public static byte[] serialize(float x, float y, float pvx, float pvy, float angle, boolean isEnemy) {
        ByteBuffer bytes = ByteBuffer.allocate(21);

        bytes.putFloat(x);
        bytes.putFloat(y);
        bytes.putFloat(pvx);
        bytes.putFloat(pvy);
        bytes.putFloat(angle);
        bytes.put((byte) (isEnemy ? 1 : 0));

        return bytes.array();
    }
}
