package org.sharkengine.engine;

import org.sharkengine.Player;
import org.sharkengine.world.entities.Block;
import org.sharkengine.world.World;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;


public class Renderer {
    private final Utils utils = Utils.getInstance();
    private long lastTime = System.nanoTime();
    private World world = World.getInstance();
    private Frustum frustum = new Frustum();
    private Player player;
    private long lastFPSUpdate = System.nanoTime();
    private int fps = 0;
    private int displayedFPS = 0;
    private int[] lasthit;
    private int renderedBlocks = 0;


    private void renderCrosshair(long window) {
        glDisable(GL_DEPTH_TEST);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();

        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetFramebufferSize(window, width, height);
        glOrtho(0, width[0], height[0], 0, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glLineWidth(2.0f);
        glColor3f(1.0f, 1.0f, 1.0f);

        glBegin(GL_LINES);
        glVertex2f(width[0] / 2 - 10, height[0] / 2);
        glVertex2f(width[0] / 2 + 10, height[0] / 2);
        glVertex2f(width[0] / 2, height[0] / 2 - 10);
        glVertex2f(width[0] / 2, height[0] / 2 + 10);
        glEnd();

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);

        glEnable(GL_DEPTH_TEST);
    }


    private void updateWindowTitle(long window, float cameraX, float cameraY, float cameraZ, float cameraPitch, float cameraYaw) {
        String title = "SharkVoxel engine | X: " + Math.round(cameraX) + " Y: " + Math.round(cameraY) + " Z: " + Math.round(cameraZ) + " | " + Math.round(cameraPitch) + "," + Math.round(cameraYaw) + " | Rendered cubes: " + renderedBlocks;
        glfwSetWindowTitle(window, title);
        renderedBlocks = 0;
    }

    private String getFPS() {
        long currentTime = System.nanoTime();
        fps++;

        if ((currentTime - lastFPSUpdate) >= 1_000_000_000L) { // 1 second
            displayedFPS = fps;
            fps = 0;
            lastFPSUpdate = currentTime;
        }
        return String.valueOf(displayedFPS);
    }

    public void renderWorld(long window) {
        player = Player.getInstance(window);
        // culling updaters!!!!
        frustum.updateFrustum(
                utils.getFov(),
                (float) utils.getWidth() / utils.getHeight(),
                0.1f, 70.0f,
                player.getCameraX(),
                player.getCameraY(),
                player.getCameraZ(),
                player.getCameraPitch(),
                player.getCameraYaw()
        );

        for (Block block : world.blocks) {
            boolean inFrustum = frustum.BlockInFrustum(block.x, block.y, block.z, Math.max(block.width, Math.max(block.height, block.depth)) / 2);
            if (inFrustum) {
                if (utils.isSurrounded(block.x, block.y, block.z)) { continue; }
                block.render();
                renderedBlocks++;
            }
        }
    }


    public void render(FontRenderer fontRenderer, int chunkDisplayList, long window, float cameraX, float cameraY, float cameraZ, float cameraPitch, float cameraYaw) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        player = Player.getInstance(window);

        // rendering
        renderWorld(window);
        glLoadIdentity();
        glRotatef(-cameraPitch, 1.0f, 0.0f, 0.0f);
        glRotatef(-cameraYaw, 0.0f, 1.0f, 0.0f);
        glTranslatef(-cameraX, -cameraY, -cameraZ);

        glClearColor(0.5f, 0.7f, 1.0f, 1.0f);
        glCallList(chunkDisplayList);
        renderCrosshair(window);
        updateWindowTitle(window, cameraX, cameraY, cameraZ, cameraPitch, cameraYaw);

        fontRenderer.addText("Sharkraft! 1.0", 10, 24, 50);
        fontRenderer.addText("FPS: " + getFPS(), 10, 47, 50);

        // raycasting check
        int[] hit =  Raycast.castRay(cameraX, cameraY, cameraZ, cameraYaw, cameraPitch);
        if (hit != null) {
            if (lasthit != null && hit != lasthit) {
                Block lblock = world.getBlock(lasthit[0], lasthit[1], lasthit[2]);
                if (lblock != null) {
                    lblock.highlight(false);
                    lblock.setHitFace(null);
                }
            }
            Block block = world.getBlock(hit[0], hit[1], hit[2]);
            if (block != null) {
                block.highlight(true);
                lasthit = hit;
                if (hit[3] != 69) {
                    block.setHitFace(hit[3]);
                }
                player.setLookedBlock(block);

            }
        } else {
            if (lasthit != null) {
                Block lblock = world.getBlock(lasthit[0], lasthit[1], lasthit[2]);
                if (lblock != null) {
                    lblock.highlight(false);
                    lblock.setHitFace(null);
                    player.setLookedBlock(null);

                }
            }
        }
    }
}