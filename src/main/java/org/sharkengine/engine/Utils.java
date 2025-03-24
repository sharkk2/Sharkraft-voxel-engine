package org.sharkengine.engine;

import org.lwjgl.system.MemoryUtil;
import org.sharkengine.world.entities.Block;
import org.sharkengine.world.World;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

public class Utils {
    private static final Utils INSTANCE = new Utils();
    private static final World world = World.getInstance();
    private long window;
    private int width;
    private int height;
    private float FOV;

    private Utils() {}

    public static Utils getInstance() {
        return INSTANCE;
    }


    public void setFov(float fov) {FOV = fov;}
    public float getFov() {return FOV;}
    public void setWindow(long win) {window = win;}
    public long getWindow() {return window;}
    public void setDim(int w, int h) {width = w; height = h;}
    public int getWidth() {return width;}
    public int getHeight() {return height;}

    public boolean isBlock(float x, float y, float z, boolean fixedDimensions) {
        for (Block block : world.blocks) {
            if (block.x == x && block.y == y && block.z == z) {
                if (block.depth != 1 || block.height != 1 || block.width != 1) {
                    if (fixedDimensions) { return true; } else { return false; }
                }
                return true;
            }
        }
        return false;
    }

    public Block fetchBlock(float x, float y, float z) {
        for (Block block : world.blocks) {
            if (block.x == x && block.y == y && block.z == z) {
                return block;
            }
        }
        return null;
    }

    public boolean isSurrounded(float x, float y, float z) {
        if (
                isBlock(x+1, y, z, false) &&
                        isBlock(x, y+1, z, false) &&
                        isBlock(x, y, z+1, false) &&
                        isBlock(x-1, y, z, false) &&
                        isBlock(x, y-1, z, false) &&
                        isBlock(x, y, z-1, false)
        ) {
            return true;
        }
        return false;
    }

    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) {
        try {
            ByteBuffer buffer;
            java.nio.file.Path path = Paths.get(resource);

            if (Files.isReadable(path)) {
                try (FileChannel fc = FileChannel.open(path, StandardOpenOption.READ)) {
                    buffer = MemoryUtil.memAlloc((int) fc.size() + 1);
                    while (fc.read(buffer) != -1);
                }
            } else {
                try (InputStream source = Utils.class.getClassLoader().getResourceAsStream(resource)) {
                    if (source == null) {
                        throw new IOException("Resource not found: " + resource);
                    }
                    byte[] bytes = source.readAllBytes();
                    buffer = MemoryUtil.memAlloc(bytes.length + 1);
                    buffer.put(bytes);
                }
            }
            buffer.flip();
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + resource, e);
        }
    }
}
