package org.sharkengine.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.sharkengine.Player;
import org.sharkengine.engine.Frustum;
import org.sharkengine.engine.PerlinNoise;
import org.sharkengine.engine.Utils;
import org.sharkengine.world.entities.Block;

public class World {
    private static World instance;
    private Frustum frustum = new Frustum();
    private Player player;
    private final Utils utils = Utils.getInstance();
    private final Random random = new Random();
    public final List<Block> blocks = new ArrayList<>();
    private PerlinNoise noise;

    private final int chunkSize = 8;
    private int chunksWidth = 2;
    private int chunksDepth = 2;

    public static World getInstance() {
        if (instance == null) {
            instance = new World();
        }
        return instance;
    }

    public Block getBlock(float x, float y, float z) {
        for (Block block : blocks) {
            if (block.x == x && block.y == y && block.z == z) {
                return block;
            }
        }
        return null;
    }

    public void generateWorld(boolean flat) {
        int width = chunksWidth * chunkSize;
        int depth = chunksDepth * chunkSize;
        int baseHeight = 6;
        int boxHeight = 5;
        long seed = random.nextLong();
        random.setSeed(seed);
        blocks.clear();

        noise = new PerlinNoise(seed);


        for (int chunkX = 0; chunkX < chunksWidth; chunkX++) {
            for (int chunkZ = 0; chunkZ < chunksDepth; chunkZ++) {
                for (int x = 0; x < chunkSize; x++) {
                    for (int z = 0; z < chunkSize; z++) {
                        int worldX = chunkX * chunkSize + x;
                        int worldZ = chunkZ * chunkSize + z;

                        double heightVariation = flat ? 0 : noise.noise(worldX * 0.08, worldZ * 0.08) * 8;
                        int surfaceHeight = baseHeight + (int) heightVariation;

                        for (int y = baseHeight - boxHeight; y <= surfaceHeight; y++) {
                            Block block;
                            if (y == surfaceHeight) {
                                block = BlockRegistry.getInstance().makeBlock("grass", worldX, y, worldZ);
                            } else if (y >= surfaceHeight - 3) {
                                block = BlockRegistry.getInstance().makeBlock("grass", worldX, y, worldZ);
                            } else {
                                block = BlockRegistry.getInstance().makeBlock("grass", worldX, y, worldZ);
                            }

                            blocks.add(block);
                        }
                    }
                }
            }
        }

        System.out.println("Generated " + blocks.size() + " Blocks with seed: " + seed);
    }

    public void setChunkSize(int chunksWidth, int chunksDepth) {
        this.chunksWidth = chunksWidth;
        this.chunksDepth = chunksDepth;
    }

    public void regenerate(boolean flat) {
        generateWorld(flat);
    }


}