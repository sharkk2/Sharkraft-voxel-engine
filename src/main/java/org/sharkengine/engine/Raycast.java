package org.sharkengine.engine;

public class Raycast {
    private static Utils utils = Utils.getInstance();

    public static class Vec3 {
        public float x, y, z;

        public Vec3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vec3 add(Vec3 other) {
            return new Vec3(this.x + other.x, this.y + other.y, this.z + other.z);
        }

        public Vec3 scale(float factor) {
            return new Vec3(this.x * factor, this.y * factor, this.z * factor);
        }
    }

    public static int[] castRay(float startX, float startY, float startZ, float yaw, float pitch) {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        float maxDistance = 5.0f;

        float dx = (float) -(Math.cos(pitchRad) * Math.sin(yawRad));
        float dy = (float) (Math.sin(pitchRad));
        float dz = (float) -(Math.cos(pitchRad) * Math.cos(yawRad));

        Vec3 direction = new Vec3(dx, dy, dz);

        Vec3 position = new Vec3(startX, startY, startZ);
        Vec3 prevPosition = new Vec3(startX, startY, startZ);

        float stepSize = 0.1f;
        float distanceTraveled = 0;

        while (distanceTraveled < maxDistance) {
            prevPosition = new Vec3(position.x, position.y, position.z);
            position = position.add(direction.scale(stepSize));
            distanceTraveled += stepSize;

            int blockX = Math.round(position.x);
            int blockY = Math.round(position.y);
            int blockZ = Math.round(position.z);

            if (utils.isBlock(blockX, blockY, blockZ, true)) {
                int face = getHitFace(prevPosition, blockX, blockY, blockZ);
                return new int[]{blockX, blockY, blockZ, face};
            }
        }

        return null;
    }


    private static int getHitFace(Vec3 prevPos, int blockX, int blockY, int blockZ) {
        int prevBlockX = Math.round(prevPos.x);
        int prevBlockY = Math.round(prevPos.y);
        int prevBlockZ = Math.round(prevPos.z);

        if (prevBlockX < blockX) return 0; // left: 0
        if (prevBlockX > blockX) return 1; // right: 1
        if (prevBlockY < blockY) return 2; // bottom: 2
        if (prevBlockY > blockY) return 3; // top: 3
        if (prevBlockZ < blockZ) return 4; // front: 4
        if (prevBlockZ > blockZ) return 5; // back: 5

        return 69; // unknown: 69
    }
}
