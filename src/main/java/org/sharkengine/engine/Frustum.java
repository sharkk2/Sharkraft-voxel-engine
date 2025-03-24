package org.sharkengine.engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Frustum {
    private final Vector4f[] planes = new Vector4f[6];
    private static final float edgeBuffer = 2.6f; // lower = sharper culling

    public Frustum() {
        for (int i = 0; i < 6; i++) {
            planes[i] = new Vector4f();
        }
    }

    public void updateFrustum(float fov, float aspectRatio, float near, float far, float camX, float camY, float camZ, float pitch, float yaw) {
        Vector3f cameraFront = new Vector3f(
                (float) -(Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw))),
                (float) Math.sin(Math.toRadians(pitch)),
                (float) -(Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(yaw)))
        );

        Vector3f cameraTarget = new Vector3f(camX, camY, camZ).add(cameraFront);
        Matrix4f viewMatrix = new Matrix4f().lookAt(
                new Vector3f(camX, camY, camZ),
                cameraTarget,
                new Vector3f(0, 1, 0)
        );


        Matrix4f projectionMatrix = new Matrix4f()
                .perspective((float) Math.toRadians(fov), aspectRatio, near, far);

        Matrix4f combinedMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);

        for (int i = 0; i < 6; i++) {
            combinedMatrix.frustumPlane(i, planes[i]);
        }
    }

    public boolean BlockInFrustum(float x, float y, float z, float size) {
        boolean intersects = false;


        for (Vector4f plane : planes) {
            boolean fullyOutside = true;

            for (int i = 0; i < 8; i++) {
                float px = x + ((i & 1) == 0 ? -size / 2 : size / 2);
                float py = y + ((i & 2) == 0 ? -size / 2 : size / 2);
                float pz = z + ((i & 4) == 0 ? -size / 2 : size / 2);

                if (plane.x * px + plane.y * py + plane.z * pz + plane.w >= -edgeBuffer) {
                    fullyOutside = false;
                }
            }

            if (fullyOutside) {
                return false;
            }
            intersects = true;
        }
        return intersects;
    }
}
