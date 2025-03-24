package org.sharkengine;

import java.awt.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.sharkengine.engine.*;
import org.sharkengine.world.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.system.MemoryUtil.*;


public class Game {
    public long window;

    private FontRenderer fontRenderer;
    private Player player;
    private final Renderer render = new Renderer();
    private final World world = World.getInstance();
    private final BlockRegistry blockRegistry = BlockRegistry.getInstance();

    private int chunkDisplayList;

    // ### SETTINGS ###
    private final boolean FLYING = true;
    private final boolean flat_world = false;
    private final boolean wireFrame = false;
    private final int width = 800;
    private final int height = 600;
    private float cameraX = 5.0f, cameraY = 20.0f, cameraZ = 5.0f;
    private float cameraYaw = 0.0f, cameraPitch = -60.0f;
    private final String fontPath = "src/main/resources/assets/font.ttf";
    public float FOV = 70f;


    public void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        this.window = glfwCreateWindow(width, height, "SharkVoxel engine", NULL, NULL);
        if (this.window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        Utils utils = Utils.getInstance();
        player = Player.getInstance(window);
        glfwMakeContextCurrent(this.window);
        glfwShowWindow(this.window);
        glfwSetInputMode(this.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        utils.setWindow(this.window);
        utils.setDim(this.width, this.height);
        utils.setFov(this.FOV);

        GL.createCapabilities();
        String version = GL11.glGetString(GL11.GL_VERSION);
        String renderer = GL11.glGetString(GL11.GL_RENDERER);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("OpenGL Version: " + version + "\n" + renderer + "\nResolution: " + width + "x" + height + " (" + screenSize.width + "x" + screenSize.height + ")\n--------------------");
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glEnable(GL_MULTISAMPLE);
        if (wireFrame) {glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);}

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        fontRenderer = new FontRenderer(fontPath, 24);

        glfwWindowHint(GLFW_SAMPLES, 10);  // 10 lmao

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(FOV, 800.0f / 600.0f, 0.1f, 100.0f);
        glMatrixMode(GL_MODELVIEW);


        glfwSetFramebufferSizeCallback(window, (w, width, height) -> {
            glViewport(0, 0, width, height);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            gluPerspective(70, (float) width / (float) height, 0.1f, 100.0f);
            glMatrixMode(GL_MODELVIEW);
        });

        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glEnable(GL_COLOR_MATERIAL);

        float[] lightPosition = {0.0f, 10.0f, 0.0f, 1.0f};
        float[] lightColor = {1.0f, 1.0f, 1.0f, 1.0f};

        glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);
        glLightfv(GL_LIGHT0, GL_DIFFUSE, lightColor);
        glLightfv(GL_LIGHT0, GL_SPECULAR, lightColor);

        chunkDisplayList = glGenLists(1);
        glNewList(chunkDisplayList, GL_COMPILE);
        glEndList();

        blockRegistry.registerBlock("stone", "src/main/resources/assets/stone.png", 1, 1, 1);
        blockRegistry.registerBlock("grass", "src/main/resources/assets/grass.png", 1, 1, 1);
        blockRegistry.registerBlock("dirt", "src/main/resources/assets/dirt.png", 1, 1, 1);
        blockRegistry.registerBlock("nigga", "src/main/resources/assets/nig.png", 0.5f, 1, 0.5f);

        System.out.println("Generating world...");
        world.generateWorld(flat_world);
        System.out.println("Loading player...");
        player.setCameraPos(cameraX, cameraY, cameraZ);
        player.setFlying(FLYING);
        player.setResetAsFlat(flat_world);
        player.setCameraAngle(cameraYaw, cameraPitch);
        System.out.println("Loaded :D");
    }

    private void gluPerspective(float fov, float aspect, float zNear, float zFar) {
        float ymax = zNear * (float) Math.tan(Math.toRadians(fov / 2));
        float xmax = ymax * aspect;
        glFrustum(-xmax, xmax, -ymax, ymax, zNear, zFar);
    }

    private void processPlayer() {
        player.process();
        cameraX = player.getCameraX();
        cameraY = player.getCameraY();
        cameraZ = player.getCameraZ();
        cameraYaw = player.getCameraYaw();
        cameraPitch = player.getCameraPitch();
    }


    public void loop() {
        while (!glfwWindowShouldClose(this.window)) {
            render.render(fontRenderer, chunkDisplayList, window, cameraX, cameraY, cameraZ, cameraPitch, cameraYaw);
            processPlayer();
            glfwSwapBuffers(this.window);
            glfwPollEvents();
        }
    }

    public void cleanup() {
        glfwDestroyWindow(this.window);
        glfwTerminate();
    }

    public static void main(String[] args) {
        System.out.println("Initializing...");
        Game game = new Game();
        game.init();
        game.loop();
        game.cleanup();
    }
}