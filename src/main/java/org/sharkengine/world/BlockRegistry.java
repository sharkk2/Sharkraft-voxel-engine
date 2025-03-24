package org.sharkengine.world;

import org.sharkengine.world.entities.Block;

import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {
    private static final BlockRegistry INSTANCE = new BlockRegistry();
    private final Map<String, Block> registeredBlocks = new HashMap<>();

    private BlockRegistry() {}

    public static BlockRegistry getInstance() {
        return INSTANCE;
    }

    public void registerBlock(String id, String texturePath, float width, float height, float depth) {
        if (!registeredBlocks.containsKey(id)) {
            Block block = new Block(0, 0, 0, width, height, depth, texturePath);
            registeredBlocks.put(id, block);
        }
    }

    public Block getBlock(String id) {
        return registeredBlocks.get(id);
    }

    public Block makeBlock(String id, float x, float y, float z) {
        Block template = registeredBlocks.get(id);
        if (template != null) {
            return new Block(x, y, z, template.width, template.height, template.depth, template.texturePath);
        }
        return new Block(x, y, z, 1, 1, 1, "src/main/resources/assets/placeholder.png");
    }
}

