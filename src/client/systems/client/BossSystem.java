package client.systems.client;

import client.components.BossComponent;
import client.components.HealthComponent;
import client.components.MovementComponent;
import client.components.TransformComponent;
import client.components.enemy.EnemyComponent;
import client.components.player.PlayerNetworkComponent;
import client.entities.Bullet;
import client.entities.Enemy;
import client.network.NetworkSpawnManager;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BossSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<MovementComponent> mm;
    private ComponentMapper<HealthComponent> healthM;
    private ComponentMapper<MovementComponent> movementM;
    private final Random random = new Random();

    private NetworkSpawnManager nsm;

    public BossSystem(NetworkSpawnManager nsm) {
        super(EnemyComponent.class, BossComponent.class);

        this.nsm = nsm;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.engine = engine;

        this.tm = engine.getMapper(TransformComponent.class);
        this.mm = engine.getMapper(MovementComponent.class);
        this.healthM = engine.getMapper(HealthComponent.class);
        this.movementM = engine.getMapper(MovementComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        EnemyComponent ec = engine.getMapper(EnemyComponent.class).get(id);
        BossComponent bc = engine.getMapper(BossComponent.class).get(id);

        TransformComponent transform = tm.get(id);
        MovementComponent movement = mm.get(id);

        bc.secondaryTimer -= ctx.deltaTime;
        bc.stateChangeTimer -= ctx.deltaTime;

        if (bc.stateChangeTimer <= 0.0f) {
            bc.stateChangeTimer = bc.stateChangeInterval;
            ArrayList<BossComponent.State> roots = new ArrayList<>(List.of(BossComponent.roots.clone()));
            roots.remove(bc.state);
            bc.state = roots.get(random.nextInt(roots.size()));
            System.out.println("Transitioned to state " + bc.state + " due to time");
        }

        if (bc.state == BossComponent.State.Lingering) {
            transform.rotation += 45.0f * ctx.deltaTime;
            transform.rotation %= 360.0f;

            // random movement
            if (random.nextFloat() < 0.0001f) {
                movement.velocity.x = 0.0f;
                movement.velocity.y = 0.0f;
            } else if (random.nextFloat() < 0.002f) {
                if (movement.velocity.x == 0 && movement.velocity.y == 0) {
                    float angle = random.nextFloat() * (float) Math.PI * 2;
                    float speed = 30.0f + random.nextFloat() * 40.0f;
                    movement.velocity.x = (float) Math.cos(angle) * speed;
                    movement.velocity.y = (float) Math.sin(angle) * speed;
                }
            }

            if (ec.shootTimer <= 0.0f) {
                shootAtPlayer(id);
                ec.shootTimer = ec.shootInterval + ec.shootVariance * (random.nextFloat() * 0.5f);
            }
        } else if (bc.state == BossComponent.State.PulseRotate) {
            transform.rotation += 30.0f * ctx.deltaTime * bc.direction;
            if (bc.secondaryTimer <= 0.0f) {
                bc.state = BossComponent.State.PulseHit;
                System.out.println("Transitioned to state " + bc.state + " due to finishing PulseRotate");
            }
            if (ec.shootTimer <= 0.0f) {
                shootAtPlayer(id);
                ec.shootTimer = 1.5f * ec.shootInterval + ec.shootVariance * (random.nextFloat() * 0.5f);
            }
        } else if (bc.state == BossComponent.State.PulseMove) {
            var dir = new Vector2f(bc.target).sub(transform.position);
            if (dir.length() < 10.0f) {
                startPulse(bc);
                System.out.println("Transitioned to state " + bc.state + " due to finishing PulseMove");
            } else {
                movement.velocity = dir.normalize().mul(movement.speed);
            }
            if (ec.shootTimer <= 0.0f) {
                shootAtPlayer(id);
                ec.shootTimer = 1.5f * ec.shootInterval + ec.shootVariance * (random.nextFloat() * 0.5f);
            }
        } else if (bc.state == BossComponent.State.Pulse) {
            bc.target = new Vector2f(bc.cx * 64.0f, bc.cy * 64.0f);
            bc.state = BossComponent.State.PulseMove;
            System.out.println("Transitioned to state " + bc.state + " due to finishing Pulse");
        } else if (bc.state == BossComponent.State.PulseHit) {
            for (int i = 0; i < 30; i++) {
                float angleOffset = 30.0f + 10.0f * i;
                float angleRad = (float) Math.toRadians(transform.rotation + angleOffset);
                Vector2f dest = new Vector2f(transform.position).add(new Vector2f((float) Math.cos(angleRad), (float) Math.sin(angleRad)).mul(100.0f));
                nsm.spawn(Bullet.class, Bullet.serialize(transform.position.x, transform.position.y, dest.x, dest.y, 0.0f, 0.0f, ec.bulletSpeed, -1));
            }
            startPulse(bc);
            System.out.println("Transitioned to state " + bc.state + " due to finishing PulseHit");
        }
    }

    private void startPulse(BossComponent bc) {
        float targetAngle = random.nextFloat(30.0f, 120.0f);
        bc.direction = random.nextInt(0, 2) * 2 - 1;
        bc.secondaryTimer = Math.abs(targetAngle) / 30.0f;
        bc.state = BossComponent.State.PulseRotate;
    }

    static class DistanceStats {
        float distance;
        Vector2f position;
        Vector2f prediction;

        public DistanceStats(float distance, Vector2f position, Vector2f prediction) {
            this.distance = distance;
            this.position = position;
            this.prediction = prediction;
        }

    }

    public void shootAtPlayer(int enemyId) {
        HealthComponent enemyHealth = healthM.get(enemyId);
        if (!enemyHealth.isAlive()) return;

        EnemyComponent ec = engine.getMapper(EnemyComponent.class).get(enemyId);
        TransformComponent enemyTransform = tm.get(enemyId);

        Iterable<Integer> playerIterator = engine.getFamily(PlayerNetworkComponent.class, HealthComponent.class)::iterator;

        ArrayList<DistanceStats> distanceStats = new ArrayList<>();
        for (int i : playerIterator) {
            HealthComponent playerHealth = healthM.get(i);
            if (!playerHealth.isAlive()) continue;

            TransformComponent playerTransform = tm.get(i);
            MovementComponent mc = movementM.get(i);

            float distance = new Vector2f(playerTransform.position).distance(enemyTransform.position);

            var time = distance / 300.0f;
            var targetPosition = new Vector2f(playerTransform.position).add(new Vector2f(mc.velocity).mul(time));

            distanceStats.add(new DistanceStats(distance, playerTransform.position, targetPosition));
        }

        MovementComponent enemyMovement = movementM.get(enemyId);
        distanceStats.sort((x, y) -> Float.compare(x.distance, y.distance));
        var targets = distanceStats.subList(0, Math.min(distanceStats.size(), 3));
        for (var target : targets) {
            float variance = target.distance * 0.3f;

            float dx = random.nextFloat() * variance * 2 - variance;
            float dy = random.nextFloat() * variance * 2 - variance;

            var position = target.position;
            nsm.spawn(Bullet.class, Bullet.serialize(enemyTransform.position.x, enemyTransform.position.y, position.x + dx, position.y + dy, enemyMovement.velocity.x, enemyMovement.velocity.y, ec.bulletSpeed, -1));

            position = target.prediction;
            nsm.spawn(Bullet.class, Bullet.serialize(enemyTransform.position.x, enemyTransform.position.y, position.x + dx, position.y + dy, enemyMovement.velocity.x, enemyMovement.velocity.y, ec.bulletSpeed, -1));
        }
    }
}