package client.entities;

public class ClientPrefabRegistry extends PrefabRegistry {
    public ClientPrefabRegistry() {
        register(Player.class, Player::deserialize);
        register(RemotePlayer.class, RemotePlayer::deserialize);
        register(Bullet.class, Bullet::deserialize);
        register(Enemy.class, Enemy::deserialize);
        register(TilePrefab.class, TilePrefab::deserialize);
        register(Key.class, Key::deserialize);
        register(HealthPickup.class, HealthPickup::deserialize);
    }
}
