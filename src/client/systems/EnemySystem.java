package client.systems;

import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import client.components.enemy.EnemyComponent;
import client.components.TransformComponent;
import client.components.MovementComponent;
import client.entities.Enemy;

import java.util.Random;

public class EnemySystem extends EntitySystem<Context> {
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<MovementComponent> mm;
    private Engine<Context> engine;
    private Random random = new Random();
    private float spawnTimer = 0.0f;
    private float spawnInterval = 3.0f;

    public EnemySystem() {
        super(EnemyComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.engine = engine;

        this.tm = engine.getMapper(TransformComponent.class);
        this.mm = engine.getMapper(MovementComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        TransformComponent transform = tm.get(id);
        MovementComponent movement = mm.get(id);

        // random movement
        if (random.nextFloat() < 0.02f) {
            float angle = random.nextFloat() * (float) Math.PI * 2;
            float speed = 30.0f + random.nextFloat() * 40.0f;
            movement.velocity.x = (float) Math.cos(angle) * speed;
            movement.velocity.y = (float) Math.sin(angle) * speed;
        }
    }

    @Override
    public void update(Context ctx) {
        super.update(ctx);


        spawnTimer -= ctx.deltaTime;
        if (spawnTimer <= 0.0f) {
            spawnEnemy();
            spawnTimer = spawnInterval;
        }
    }

    private void spawnEnemy() {
        new Enemy(engine);
    }
}