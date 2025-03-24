package org.sharkengine.engine;

import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryUtil.*;

public class FontRenderer {
    private int textureId;
    private STBTTFontinfo fontInfo;
    private ByteBuffer fontBuffer;
    private float scale;
    private int ascent;
    private STBTTBakedChar.Buffer charData;

    public FontRenderer(String fontPath, float fontSize) {
        fontInfo = STBTTFontinfo.create();
        fontBuffer = Utils.getInstance().ioResourceToByteBuffer(fontPath, 1024 * 1024);

        if (!stbtt_InitFont(fontInfo, fontBuffer)) {
            throw new RuntimeException("Failed to initialize font");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer ascentBuffer = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, ascentBuffer, null, null);
            ascent = ascentBuffer.get(0);
        }

        scale = stbtt_ScaleForPixelHeight(fontInfo, fontSize);
        generateFontTexture(fontSize);
    }

    private void generateFontTexture(float fontSize) {
        int bitmapW = 512, bitmapH = 512;
        ByteBuffer bitmap = memAlloc(bitmapW * bitmapH);
        charData = STBTTBakedChar.malloc(96);

        int result = stbtt_BakeFontBitmap(fontBuffer, fontSize, bitmap, bitmapW, bitmapH, 32, charData);
        if (result <= 0) throw new RuntimeException("Failed to bake font");

        // Convert grayscale bitmap to RGBA
        ByteBuffer rgbaBuffer = memAlloc(bitmapW * bitmapH * 4);
        for (int i = 0; i < bitmapW * bitmapH; i++) {
            byte alpha = bitmap.get(i);
            rgbaBuffer.put((byte) 255); // red
            rgbaBuffer.put((byte) 255); // green
            rgbaBuffer.put((byte) 255); // blue
            rgbaBuffer.put(alpha);      // alhpa
        }
        rgbaBuffer.flip();

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bitmapW, bitmapH, 0, GL_RGBA, GL_UNSIGNED_BYTE, rgbaBuffer);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        memFree(bitmap);
        memFree(rgbaBuffer);
    }

    public void addText(String text, float x, float y, float size) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);

        glColor4f(1, 1, 1, 1);

        glPushMatrix();
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, 800, 600, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        float xpos = x;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xBuffer = stack.mallocFloat(1);
            FloatBuffer yBuffer = stack.mallocFloat(1);
            xBuffer.put(0, xpos);
            yBuffer.put(0, y);

            for (char c : text.toCharArray()) {
                if (c < 32 || c >= 128) continue;

                STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
                stbtt_GetBakedQuad(charData, 512, 512, c - 32, xBuffer, yBuffer, quad, true);
                renderCharQuad(quad);
            }
        }

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glPopMatrix();
    }

    private void renderCharQuad(STBTTAlignedQuad quad) {
        glBegin(GL_QUADS);
        glTexCoord2f(quad.s0(), quad.t0()); glVertex2f(quad.x0(), quad.y0());
        glTexCoord2f(quad.s1(), quad.t0()); glVertex2f(quad.x1(), quad.y0());
        glTexCoord2f(quad.s1(), quad.t1()); glVertex2f(quad.x1(), quad.y1());
        glTexCoord2f(quad.s0(), quad.t1()); glVertex2f(quad.x0(), quad.y1());
        glEnd();
    }
}
