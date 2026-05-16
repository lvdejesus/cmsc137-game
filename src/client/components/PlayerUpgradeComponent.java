package client.components;

import client.components.player.PlayerStateComponent;
import client.entities.Player;
import framework.engine.Component;
import framework.engine.SyncComponent;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class PlayerUpgradeComponent implements SyncComponent {
    public float bulletSpeed = 700.0f;
    public float fireRate = 10.0f;
    public int splatter = 1;

    public Queue<Integer> applyQueue = new ArrayDeque<>();

    public float getEffectiveFireRate() {
        if (splatter > 1) {
            return fireRate * 0.3f;
        } else {
            return fireRate;
        }
    }

    public void queueApply(int index) {
        applyQueue.offer(index);
    }

    public void applyActual(int index) {
        switch (index) {
            case 1:
                fireRate += 4.0f;
                break;
            case 2:
                bulletSpeed += 200.0f;
                break;
            case 3:
                splatter = splatter == 1 ? 6 : (splatter + 2);
                break;
            default:
                throw new RuntimeException("Invalid upgrade type.");
        }
    }

    @Override
    public void fromBytes(byte[] bytes) {
        var buf = ByteBuffer.wrap(bytes);

        bulletSpeed = buf.getFloat();
        fireRate = buf.getFloat();
        splatter = buf.getInt();
    }

    @Override
    public byte[] toBytes() {
        var buf = ByteBuffer.allocate(12);

        buf.putFloat(bulletSpeed);
        buf.putFloat(fireRate);
        buf.putInt(splatter);

        return buf.array();
    }

    @Override
    public boolean isEqual(SyncComponent other) {
        if (!(other instanceof PlayerUpgradeComponent c)) return false;

        return c.bulletSpeed == bulletSpeed && c.fireRate == fireRate && c.splatter == splatter;
    }

    @Override
    public void copyFrom(SyncComponent other) {
        if (!(other instanceof PlayerUpgradeComponent c)) return;

        this.bulletSpeed = c.bulletSpeed;
        this.fireRate = c.fireRate;
        this.splatter = c.splatter;
    }

    @Override
    public SyncComponent clone() {
        var c = new PlayerUpgradeComponent();
        c.copyFrom(this);
        return c;
    }
}
