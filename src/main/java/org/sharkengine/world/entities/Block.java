package org.sharkengine.world.entities;

import org.sharkengine.engine.*;
import org.sharkengine.world.World;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Block {
    public float x, y, z;
    public float width, height, depth;
    public String texturePath;
    public int textureID;
    public boolean highlighted = false;
    private Utils utils = Utils.getInstance();
    private World world = World.getInstance();
    public Integer rayHitFace;

    public Block(float x, float y, float z, float width, float height, float depth, String texturePath) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.texturePath = texturePath;

        Texture texture = new Texture();
        this.textureID = texture.loadTexture(texturePath);

    }

    public void destroy() {
        world.blocks.remove(this);
    }

    public void bounce(float offset, float speed) {
        float time = (float) (System.nanoTime() / 1_000_000_000.0);
        this.y += Math.sin(time * speed) * offset;
    }

    public void highlight(boolean highlight) {
        highlighted = highlight;
    }
    public void setHitFace(Integer face) { rayHitFace = face; }
    public void setX(float nx) { x = nx; }
    public void setY(float ny) { y = ny; }
    public void setZ(float nz) { z = nz; }
    public void render() {


        if (textureID == -1) {
            return;
        }

        glEnable(GL_MULTISAMPLE);

        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureID);

        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);

        float[] lightPosition = {0.0f, 50.0f, 0.0f, 1.0f};


        glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);
        glLightfv(GL_LIGHT0, GL_AMBIENT, new float[]{0.2f, 0.2f, 0.2f, 1.0f});
        glLightfv(GL_LIGHT0, GL_DIFFUSE, new float[]{1.0f, 1.0f, 1.0f, 1.0f});
        glLightfv(GL_LIGHT0, GL_SPECULAR, new float[]{1.0f, 1.0f, 1.0f, 1.0f});

        float[] materialAmbient = {0.1f, 0.1f, 0.1f, 1.0f};
        float[] materialDiffuse = {0.8f, 0.8f, 0.8f, 1.0f};
        float[] materialSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
        float shininess = 32.0f;

        glMaterialfv(GL_FRONT, GL_AMBIENT, materialAmbient);
        glMaterialfv(GL_FRONT, GL_DIFFUSE, materialDiffuse);
        glMaterialfv(GL_FRONT, GL_SPECULAR, materialSpecular);
        glMaterialf(GL_FRONT, GL_SHININESS, shininess);

        if (highlighted) {
            glColor3f(10.5f, 10.5f, 10.5f);
        } else {
            glColor3f(1.0f, 1.0f, 1.0f);
        }

        glBegin(GL_QUADS);

        // Front face
        if (!utils.isBlock(x, y, z + 1, false)) {
            glNormal3f(0.0f, 0.0f, 1.0f);
            glTexCoord2f(1.0f, 1.0f);
            glVertex3f(x - width / 2, y - height / 2, z + depth / 2);
            glTexCoord2f(0.0f, 1.0f);
            glVertex3f(x + width / 2, y - height / 2, z + depth / 2);
            glTexCoord2f(0.0f, 0.0f);
            glVertex3f(x + width / 2, y + height / 2, z + depth / 2);
            glTexCoord2f(1.0f, 0.0f);
            glVertex3f(x - width / 2, y + height / 2, z + depth / 2);
        }
        // Back face
        if (!utils.isBlock(x, y, z - 1, false)) {
           glNormal3f(0.0f, 0.0f, -1.0f);
           glTexCoord2f(0.0f, 1.0f);
           glVertex3f(x - width / 2, y - height / 2, z - depth / 2);
           glTexCoord2f(0.0f, 0.0f);
           glVertex3f(x - width / 2, y + height / 2, z - depth / 2);
           glTexCoord2f(1.0f, 0.0f);
           glVertex3f(x + width / 2, y + height / 2, z - depth / 2);
           glTexCoord2f(1.0f, 1.0f);
           glVertex3f(x + width / 2, y - height / 2, z - depth / 2);
        }

        // Left face
        if (!utils.isBlock(x - 1, y, z, false)) {
            glNormal3f(-1.0f, 0.0f, 0.0f);
            glTexCoord2f(0.0f, 1.0f);
            glVertex3f(x - width / 2, y - height / 2, z - depth / 2);
            glTexCoord2f(1.0f, 1.0f);
            glVertex3f(x - width / 2, y - height / 2, z + depth / 2);
            glTexCoord2f(1.0f, 0.0f);
            glVertex3f(x - width / 2, y + height / 2, z + depth / 2);
            glTexCoord2f(0.0f, 0.0f);
            glVertex3f(x - width / 2, y + height / 2, z - depth / 2);
        }

        // Right face
        if (!utils.isBlock(x + 1, y, z, false)) {
            glNormal3f(1.0f, 0.0f, 0.0f);
            glTexCoord2f(0.0f, 1.0f);
            glVertex3f(x + width / 2, y - height / 2, z - depth / 2);
            glTexCoord2f(1.0f, 1.0f);
            glVertex3f(x + width / 2, y - height / 2, z + depth / 2);
            glTexCoord2f(1.0f, 0.0f);
            glVertex3f(x + width / 2, y + height / 2, z + depth / 2);
            glTexCoord2f(0.0f, 0.0f);
            glVertex3f(x + width / 2, y + height / 2, z - depth / 2);
        }

        // Top face
        if (!utils.isBlock(x, y + 1, z, false)) {
            glNormal3f(0.0f, 1.0f, 0.0f);
            glTexCoord2f(0.0f, 0.0f);
            glVertex3f(x - width / 2, y + height / 2, z - depth / 2);
            glTexCoord2f(1.0f, 0.0f);
            glVertex3f(x + width / 2, y + height / 2, z - depth / 2);
            glTexCoord2f(1.0f, 1.0f);
            glVertex3f(x + width / 2, y + height / 2, z + depth / 2);
            glTexCoord2f(0.0f, 1.0f);
            glVertex3f(x - width / 2, y + height / 2, z + depth / 2);
        }
        // Bottom face
        if (!utils.isBlock(x, y - 1, z, false)) {
            glNormal3f(0.0f, -1.0f, 0.0f);
            glTexCoord2f(0.0f, 0.0f);
            glVertex3f(x - width / 2, y - height / 2, z - depth / 2);
            glTexCoord2f(1.0f, 0.0f);
            glVertex3f(x + width / 2, y - height / 2, z - depth / 2);
            glTexCoord2f(1.0f, 1.0f);
            glVertex3f(x + width / 2, y - height / 2, z + depth / 2);
            glTexCoord2f(0.0f, 1.0f);
            glVertex3f(x - width / 2, y - height / 2, z + depth / 2);
        }
        glEnd();
        glPopMatrix();

        glDisable(GL_LIGHTING);
        glDisable(GL_TEXTURE_2D);

        glDisable(GL_MULTISAMPLE);

    }

}
