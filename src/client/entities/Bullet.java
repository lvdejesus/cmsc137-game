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
    private final int networkId;
    private final float x;
    private final float y;
    private final float px;
    private final float py;
    private final float pvx;
    private final float pvy;
    private final float speed;
    private final int origin;

    public Bullet(Engine<Context> engine, int networkId, float x, float y, float px, float py, float pvx, float pvy, float speed, int origin) {
        super(engine);

        this.networkId = networkId;
        this.x = x;
        this.y = y;
        this.px = px;
        this.py = py;
        this.pvx = pvx;
        this.pvy = pvy;
        this.speed = speed;
        this.origin = origin;
    }

    @Override
    public void spawnClientInternal() {
        TextureAtlas atlas = TextureAtlas.get();
        String textureName = isEnemy() ? "bullets/enemy_bullets.png" : "bullets/friend_bullets.png";
        Texture bulletTexture = atlas.getRegion(textureName);
        entity.addComponent(new RenderComponent(bulletTexture));
    }

    @Override
    public void spawnCommon() {
        /*
        bullet should go to (px, py) from (x, y) with an initial speed of `speed`.
         */
        Vector2f dir = new Vector2f(px - x, py - y).normalize();
        // speed = new Vector2f(dir.x, dir.y).mul(speed).add(pvx, pvy).length();

        float vx = dir.x * speed + pvx;
        float vy = dir.y * speed + pvy;

        TransformComponent transform = new TransformComponent(new Vector2f(x, y), new Vector2f(0.5f, 0.5f));
        transform.rotation = (float) Math.toDegrees(Math.atan2(dir.y, dir.x)) + 90.0f;
        entity.addComponent(transform);
        entity.addComponent(new MovementComponent(0, 0, 0, new Vector2f(vx, vy)));

        BulletComponent bulletComp = new BulletComponent();
        bulletComp.origin = origin;
        bulletComp.lifetime = isEnemy() ? 3.0f : 0.8f;
        entity.addComponent(bulletComp);

        entity.addComponent(new CollisionComponent(new AABBf(
                new Vector3f(-4.0f, -4.0f, 0.0f),
                new Vector3f(4.0f, 4.0f, 0.1f))));
        entity.addComponent(new NetworkIdComponent(networkId));
    }

    public static Bullet deserialize(Engine<Context> engine, int networkId, ByteBuffer bytes) throws IOException {
        float x = bytes.getFloat();
        float y = bytes.getFloat();
        float px = bytes.getFloat();
        float py = bytes.getFloat();
        float pvx = bytes.getFloat();
        float pvy = bytes.getFloat();
        float speed = bytes.getFloat();
        int origin = bytes.getInt();

        return new Bullet(engine, networkId, x, y, px, py, pvx, pvy, speed, origin);
    }

    public static byte[] serialize(float x, float y, float px, float py, float pvx, float pvy, float speed, int origin) {
        ByteBuffer bytes = ByteBuffer.allocate(32);

        bytes.putFloat(x);
        bytes.putFloat(y);
        bytes.putFloat(px);
        bytes.putFloat(py);
        bytes.putFloat(pvx);
        bytes.putFloat(pvy);
        bytes.putFloat(speed);
        bytes.putInt(origin);

        return bytes.array();
    }

    public boolean isEnemy() {
        return origin == -1;
    }
}
