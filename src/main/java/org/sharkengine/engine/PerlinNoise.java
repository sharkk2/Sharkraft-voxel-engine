package org.sharkengine.engine;

import java.util.Random;

public class PerlinNoise {
    private final int[] permutationTable = new int[512];

    public PerlinNoise(long seed) {
        Random random = new Random(seed);
        int[] perm = new int[256];

        for (int i = 0; i < 256; i++) {
            perm[i] = i;
        }

        for (int i = 255; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = perm[i];
            perm[i] = perm[index];
            perm[index] = temp;
        }

        for (int i = 0; i < 512; i++) {
            permutationTable[i] = perm[i % 256];
        }
    }

    public double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    public double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    public double grad(int hash, double x, double y) {
        int h = hash & 3;
        double u = h < 2 ? x : y;
        double v = h < 1 || h == 2 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    public double noise(double x, double y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);

        double u = fade(x);
        double v = fade(y);

        int A = permutationTable[X] + Y;
        int B = permutationTable[X + 1] + Y;

        return lerp(v, lerp(u, grad(permutationTable[A], x, y), grad(permutationTable[B], x - 1, y)),
                lerp(u, grad(permutationTable[A + 1], x, y - 1), grad(permutationTable[B + 1], x - 1, y - 1)));
    }
}
