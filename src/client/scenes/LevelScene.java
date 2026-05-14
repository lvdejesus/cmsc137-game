    package client.scenes;

    import client.components.AnimationComponent;
    import client.components.RenderComponent;
    import client.components.TextComponent;
    import client.components.TransformComponent;
    import client.components.player.MovementInputComponent;
    import client.components.MovementComponent;
    import client.components.player.PlayerStateComponent;
    import client.entities.*;
    import client.network.NetworkManager;
    import client.network.messages.Message;
    import client.network.messages.server.*;
    import client.rendering.*;
    import client.systems.client.*;
    import client.scenes.overlays.Menu;
    import framework.engine.*;
    import org.joml.Vector2f;
    import org.joml.Vector4f;
    import org.lwjgl.BufferUtils;

    import java.io.IOException;
    import java.nio.ByteBuffer;
    import java.nio.file.Files;
    import java.nio.file.Paths;
    import java.util.ArrayList;
    import java.util.List;

    import static org.lwjgl.glfw.GLFW.*;

    import java.util.HashMap;
    import java.util.Map;
    import java.util.concurrent.ConcurrentHashMap;

    public class LevelScene extends Scene {
        private Engine<Context> engine;
        private Player player;

        private TransformComponent playerTransform;
        private MovementInputComponent playerMovementInput;
        private PlayerStateComponent playerState;
        private MovementComponent playerMovement;

        // For netrorks
        private ClientPrefabRegistry prefabRegistry = new ClientPrefabRegistry();
        private final Map<Integer, Integer> networkEntityMap = new ConcurrentHashMap<>();

        //Debug text
        private DebugText debugText;

        // pause menu
        private Menu menu;

        @Override
        public void init(Engine<Context> engine) {
            this.engine = engine;

            // Ensure systems are enabled
            setGameSystemsEnabled(true);

            int playerIndex = NetworkManager.getInstance().getPlayerIndex();
            this.player = new Player(engine, playerIndex, NetworkManager.getInstance().getNetworkId());
            this.player.spawn();

            this.playerTransform = player.getEntity().getComponent(TransformComponent.class);
            this.playerMovement = player.getEntity().getComponent(MovementComponent.class);
            this.playerMovementInput = player.getEntity().getComponent(MovementInputComponent.class);
            this.playerState = player.getEntity().getComponent(PlayerStateComponent.class);

            // Load background map
            Entity<Context> mapBg = engine.createEntity();
            mapBg.addComponent(new TransformComponent(new Vector2f(400, 300), new Vector2f(800.0f / 1339.0f, 600.0f / 1175.0f), Anchor.CENTER));
            mapBg.addComponent(new RenderComponent(TextureAtlas.get().getRegion("map_1.png"), -0.5f));

            // Initialize pause menu
            try {
                ByteBuffer fontBuffer = loadResource("res/fonts/KiwiSoda.ttf");
                Font pauseFont = new Font(fontBuffer, 32); // Smaller font
                this.menu = new Menu(engine, pauseFont);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Initialize debug text
            debugText = DebugText.create(engine, "State: idle");

        }

        private void setGameSystemsEnabled(boolean enabled) {
            // enableSystem(EnemySystem.class, enabled);
            enableSystem(BulletSystem.class, enabled);
            enableSystem(MovementSystem.class, enabled);
            enableSystem(client.systems.client.player.PlayerRotationSystem.class, enabled);
            enableSystem(DamageSystem.class, enabled);
            enableSystem(PhysicsSystem.class, enabled);
        }

        private <T extends EntitySystem<Context>> void enableSystem(Class<T> type, boolean enabled) {
            T system = engine.getSystem(type);
            if (system != null) {
                system.setEnabled(enabled);
            }
        }

        @Override
        public void update() {
            InputHandler input = InputHandler.getInstance();
            this.debugText.setText("State: " + player.getState());
            // Toggle menu with Esc
            if (input.keyDown(GLFW_KEY_ESCAPE)) {
                menu.toggleMenu();
            }

            if (menu.isVisible()) {
                menu.handlePauseMenuInput(input);
            }

            NetworkManager nm = NetworkManager.getInstance();

            for (InputHandler.MouseEvent event : input.getEvents()) {
                if (event.type == InputHandler.MouseEventType.LEFT_CLICK && !event.consumed) {
                    event.consume();

                    if (playerTransform != null ) {
                        Vector2f d = camera.toWorldPosition(event.position).sub(playerTransform.position);
                        float angle = (float) Math.toDegrees(Math.atan2(d.y, d.x));
                        
                        // Get player's current velocity for velocity inheritance
                        float pvx = playerMovementInput.x * playerMovement.speed;
                        float pvy = playerMovementInput.y * playerMovement.speed;
                        nm.shoot(playerTransform.position.x, playerTransform.position.y, angle, pvx, pvy);
                    }
                }
            }

            // 1. Broadcast our position
            if (playerTransform != null && playerState != null && playerMovementInput != null) {
                nm.broadcastPosition(playerTransform.position.x, playerTransform.position.y, playerTransform.rotation, playerState.previous, playerState.current, playerMovementInput.x, playerMovementInput.y);
            }

            Message msg;
            while ((msg = nm.inQueue.poll()) != null) {
                if (msg instanceof S_Spawn m) {
                    // if self, skip
                    if (nm.networkId == m.getNetworkId()) continue;
                    Prefab prefab = prefabRegistry.spawn(engine, m.getPrefabId(), m.getNetworkId(), m.getBytes());
                    networkEntityMap.put(m.getNetworkId(), prefab.getEntity().getId());
                } else if (msg instanceof S_Despawn m) {
                    int entityId = networkEntityMap.remove(m.getNetworkId());
                    engine.destroyEntity(entityId);
                } else if (msg instanceof S_Snapshot m) {
                    for (var entitySnapshot : m.getEntitySnapshots()) {
                        Integer entityId = networkEntityMap.get(entitySnapshot.getNetworkId());
                        if (entitySnapshot.getNetworkId() == nm.networkId) continue;

                        if (entityId == null) {
                            continue;
                        }

                        for (var component : entitySnapshot.getComponents()) {
                            var cc = engine.getComponentClass(component.getComponentId());
                            var syncComponent = (SyncComponent) engine.getMapper(cc).get(entityId);
                            if (syncComponent == null) {
                                continue;
                            }
                            syncComponent.syncFromBytes(component.getData());
                        }
                    }
                }
            }
        }

        private ByteBuffer loadResource(String path) throws IOException {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            return buffer;
        }

        @Override
        public void clean() {
            menu.hidePauseMenu();
        }
    }
