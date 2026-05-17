package client.components;

import framework.engine.SyncComponent;

import java.nio.ByteBuffer;

public class ExperienceComponent implements SyncComponent {
    // exponential xp required to incentivize giving many people xp instead of funneling to one person
    // 100, 300, 700, 1500, ...
    public int usedLevels;
    public int exp;

    public int lastUpgradeCount = -1;
    public int lastLevelCount = -1;
    public int lastLevelNotifShown = -1;

    public int getLevels() {
        return (int)Math.floor(Math.log1p(exp / 100.0) / Math.log(2));
    }

    public int getRemainingUpgrades(){
        return getLevels() - usedLevels;
    }

    @Override
    public void fromBytes(byte[] bytes) {
        var buf = ByteBuffer.wrap(bytes);

        usedLevels = buf.getInt();
        exp = buf.getInt();
    }

    @Override
    public byte[] toBytes() {
        var buf = ByteBuffer.allocate(8);

        buf.putInt(usedLevels);
        buf.putInt(exp);

        return buf.array();
    }

    @Override
    public boolean isEqual(SyncComponent other) {
        if (!(other instanceof ExperienceComponent c)) return false;

        return c.exp == exp && c.usedLevels == usedLevels;
    }

    @Override
    public void copyFrom(SyncComponent other) {
        if (!(other instanceof ExperienceComponent c)) return;

        usedLevels = c.usedLevels;
        exp = c.exp;
    }

    @Override
    public SyncComponent clone() {
        var c = new ExperienceComponent();
        c.copyFrom(this);
        return c;
    }
}
