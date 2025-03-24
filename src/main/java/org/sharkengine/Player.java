package org.sharkengine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.sharkengine.engine.Utils;
import org.sharkengine.world.BlockRegistry;
import org.sharkengine.world.World;
import org.sharkengine.world.entities.Block;

import java.util.HashMap;
import java.util.Map;

public class Player {
    private static Player instance;

    private float cameraX, cameraY, cameraZ;
    private float cameraYaw, cameraPitch;

    private Utils utils = Utils.getInstance();
    private World world = World.getInstance();

    private float velocityX, velocityY, velocityZ;
    private boolean FLYING;
    private static final float ACCELERATION = 0.05f;
    private static final float FRICTION = 0.85f;
    private static final float MAX_SPEED = 0.1f;
    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float PLAYER_HEIGHT = 1.75f;

    private Block lookedCube;

    private boolean resetAsFlat;
    private boolean[] keys = new boolean[GLFW.GLFW_KEY_LAST];

    private double lastMouseX, lastMouseY;

    private boolean firstMouse = true;
    private boolean leftMousePressed = false;
    private boolean rightMousePressed = false;
    private boolean clickBreak = false;

    private int currentBlock = 1;

    private Player(long window) {
        GLFW.glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (firstMouse) {
                    lastMouseX = xpos;
                    lastMouseY = ypos;
                    firstMouse = false;
                }

                float xOffset = (float) (xpos - lastMouseX);
                float yOffset = (float) (lastMouseY - ypos);

                lastMouseX = xpos;
                lastMouseY = ypos;

                xOffset *= MOUSE_SENSITIVITY;
                yOffset *= MOUSE_SENSITIVITY;

                cameraYaw -= xOffset;
                cameraPitch += yOffset;
                cameraPitch = Math.max(-89.0f, Math.min(89.0f, cameraPitch));
            }
        });

        GLFW.glfwSetKeyCallback(window, (windowHandle, key, scancode, action, mods) -> {
            if (key >= 0 && key < keys.length) {
                if (action == GLFW.GLFW_PRESS) {
                    keys[key] = true;
                } else if (action == GLFW.GLFW_RELEASE) {
                    keys[key] = false;

                }
            }
        });



        GLFW.glfwSetMouseButtonCallback(window, (windowHandle, button, action, mods) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW.GLFW_PRESS) {
                    if (!leftMousePressed) {
                        leftMousePressed = true;
                    }
                } else if (action == GLFW.GLFW_RELEASE) {
                    leftMousePressed = false;
                    clickBreak = false;
                }
            }

            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (action == GLFW.GLFW_PRESS) {
                    if (!rightMousePressed) {
                        rightMousePressed = true;

                    }
                } else if (action == GLFW.GLFW_RELEASE) {
                    rightMousePressed = false;
                    clickBreak = false;
                }
            }
        });


    }

    public static Player getInstance(long window) {
        if (instance == null) {
            instance = new Player(window);
        }
        return instance;
    }

    public void process() {
        boolean isOnGround = utils.isBlock(Math.round(cameraX), Math.round(cameraY - 1), Math.round(cameraZ), false) ||
                utils.isBlock(Math.round(cameraX), Math.round(cameraY - PLAYER_HEIGHT), Math.round(cameraZ), false);

        cameraYaw = (cameraYaw + 360.0f) % 360.0f;
        float radYaw = (float) Math.toRadians(cameraYaw);
        float radPitch = (float) Math.toRadians(cameraPitch);
        float forwardX = (float) (Math.cos(radPitch) * Math.sin(radYaw));
        float forwardZ = (float) (Math.cos(radPitch) * Math.cos(radYaw));
        float rightX = (float) Math.cos(radYaw);
        float rightZ = (float) -Math.sin(radYaw);
        float moveX = 0, moveZ = 0;

        if (keys[GLFW.GLFW_KEY_W]) { moveX -= forwardX; moveZ -= forwardZ; }
        if (keys[GLFW.GLFW_KEY_S]) { moveX += forwardX; moveZ += forwardZ; }
        if (keys[GLFW.GLFW_KEY_A]) { moveX -= rightX; moveZ -= rightZ; }
        if (keys[GLFW.GLFW_KEY_D]) { moveX += rightX; moveZ += rightZ; }
        if (leftMousePressed) {
            if (!clickBreak) {
                if (lookedCube != null) {
                    lookedCube.destroy();
                }
                clickBreak = true;
            }

        }

        if (keys[GLFW.GLFW_KEY_1]) { currentBlock = 1; }
        if (keys[GLFW.GLFW_KEY_2]) { currentBlock = 2; }
        if (keys[GLFW.GLFW_KEY_3]) { currentBlock = 3; }
        if (keys[GLFW.GLFW_KEY_4]) { currentBlock = 4; }

        if (rightMousePressed) {
            if (!clickBreak) {
                placeBlock();
            }
            clickBreak = true;
        }

        if (keys[GLFW.GLFW_KEY_R]) {
            world.regenerate(resetAsFlat);
            resetCamera();
        }

        if (cameraY < -10) { resetCamera(); }

        if (FLYING) {
            if (keys[GLFW.GLFW_KEY_SPACE]) { cameraY += 0.5f; }
            if (keys[GLFW.GLFW_KEY_LEFT_CONTROL]) { cameraY -= 0.5f; }
        } else if (isOnGround && keys[GLFW.GLFW_KEY_SPACE]) {
            velocityY = 0.45f;
        }

        float length = (float) Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (length > 0) {
            moveX /= length;
            moveZ /= length;
        }

        velocityX = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, (velocityX + moveX * ACCELERATION) * FRICTION));
        velocityZ = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, (velocityZ + moveZ * ACCELERATION) * FRICTION));

        if (!FLYING) {
            if (!isOnGround) {
                velocityY = Math.max(velocityY - 0.05f, -MAX_SPEED);
            } else if (velocityY < 0) {
                velocityY = 0;
            }
        }

        float cx = Math.round((cameraX + velocityX) + 0.1);
        float cy = Math.round((cameraY + velocityY) + 0.1);
        float cz = Math.round((cameraZ + velocityZ) + 0.1);

        boolean collision = utils.isBlock(Math.round(cx), Math.round(cy), Math.round(cz), false) ||
                utils.isBlock(Math.round(cx), Math.round(cy + PLAYER_HEIGHT), Math.round(cz), false) ||
                utils.isBlock(Math.round(cx), Math.round(cy - 1.0f), Math.round(cz), false);

        if (!collision) {
            cameraX = cameraX + velocityX;
            cameraY = cameraY + velocityY;
            cameraZ = cameraZ + velocityZ;
        } else {
            velocityX = velocityY = velocityZ = 0;
        }
    }

    private void placeBlock() {
        if (lookedCube != null) {
            Integer face = lookedCube.rayHitFace;
            Map<Integer, String> blocks = new HashMap<>() {{
                put(1, "nigga");
                put(2, "dirt");
                put(3, "stone");
                put(4, "grass");
            }};

            int[][] offsets = {
                    {-1,  0,  0},
                    { 1,  0,  0},
                    { 0, -1,  0},
                    { 0,  1,  0},
                    { 0,  0, -1},
                    { 0,  0,  1}
            };

            if (face != null) {
                if (face < 0 || face >= offsets.length) {
                    System.out.println("Unknown face hit: " + face);
                    return;
                }

                String blockid = blocks.getOrDefault(currentBlock, "stone");

                int[] offset = offsets[face];
                Block block = BlockRegistry.getInstance().makeBlock(blockid, lookedCube.x + offset[0], lookedCube.y + offset[1], lookedCube.z + offset[2]);
                world.blocks.add(block);
            }
        }
    }

    private void resetCamera() {
        cameraX = 5.0f;
        cameraY = 20.0f;
        cameraZ = 5.0f;
        cameraPitch = 0.0f;
        cameraYaw = 0.0f;
        velocityX = velocityY = velocityZ = 0;
    }

    public void setCameraPos(float x, float y, float z) { cameraX = x; cameraY = y; cameraZ = z; }
    public void setCameraAngle(float yaw, float pitch) { cameraYaw = yaw; cameraPitch = pitch; }
    public void setFlying(boolean flying) { FLYING = flying; }
    public void setResetAsFlat(boolean isFlat) { resetAsFlat = isFlat; }
    public void setLookedBlock(Block block) { lookedCube = block; }
    public float getCameraX() { return cameraX; }
    public float getCameraY() { return cameraY; }
    public float getCameraZ() { return cameraZ; }
    public float getCameraYaw() { return cameraYaw; }
    public float getCameraPitch() { return cameraPitch; }

}