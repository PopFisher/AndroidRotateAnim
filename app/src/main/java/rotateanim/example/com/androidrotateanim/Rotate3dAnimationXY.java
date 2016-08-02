/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rotateanim.example.com.androidrotateanim;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.graphics.Camera;
import android.graphics.Matrix;

/**
 * An animation that rotates the view on the Y axis between two specified angles.
 * This animation also adds a translation on the Z axis (depth) to improve the effect.
 */
public class Rotate3dAnimationXY extends Animation {
    private final float mFromDegrees;
    private final float mToDegrees;
    private final float mCenterX;
    private final float mCenterY;
    private final float mDepthZ;
    private final boolean mReverse;
    private Camera mCamera;
    private byte mRotateXOrY = 0;

    /**
     * Creates a new 3D rotation on the Y axis. The rotation is defined by its
     * start angle and its end angle. Both angles are in degrees. The rotation
     * is performed around a center point on the 2D space, definied by a pair
     * of X and Y coordinates, called centerX and centerY. When the animation
     * starts, a translation on the Z axis (depth) is performed. The length
     * of the translation can be specified, as well as whether the translation
     * should be reversed in time.
     *
     * @param fromDegrees the start angle of the 3D rotation
     * @param toDegrees the end angle of the 3D rotation
     * @param centerX the X center of the 3D rotation
     * @param centerY the Y center of the 3D rotation
     * @param reverse true if the translation should be reversed, false otherwise
     */
    public Rotate3dAnimationXY(float fromDegrees, float toDegrees,
            float centerX, float centerY, float depthZ, boolean reverse , byte rotate) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterX = centerX;
        mCenterY = centerY;
        mDepthZ = depthZ;
        mReverse = reverse;
        mRotateXOrY = rotate;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final float fromDegrees = mFromDegrees;
        float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

        final float centerX = mCenterX;
        final float centerY = mCenterY;
        final Camera camera = mCamera;

        final Matrix matrix = t.getMatrix();
        // 将当前的摄像头位置保存下来，以便变换进行完成后恢复成原位
        camera.save();
        
        // camera.translate，这个方法接受3个参数，分别是x,y,z三个轴的偏移量，我们这里只将z轴进行了偏移
        if (mReverse) {
        	// z的偏移会越来越大。这就会形成这样一个效果，view从近到远  
            camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
        } else {
        	// z的偏移会越来越小。这就会形成这样一个效果，我们的View从一个很远的地方向我们移过来，越来越近，最终移到了我们的窗口上面
            camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
        }
        
        switch (mRotateXOrY) {
		case 0:
			// 是给我们的View加上旋转效果，在移动的过程中，视图还会移X轴为中心进行旋转。
			camera.rotateX(degrees); 			
			break;

		case 1:
			// 是给我们的View加上旋转效果，在移动的过程中，视图还会移X轴为中心进行旋转。
			camera.rotateY(degrees);
			break;
		}
        
        // 这个是将我们刚才定义的一系列变换应用到变换矩阵上面，调用完这句之后，我们就可以将camera的位置恢复了，以便下一次再使用。
        camera.getMatrix(matrix);
        // camera位置恢复 
        camera.restore();
        
        float fAlphaRate = 0.2f;
        // 改变翻转时的透明度
        if (!mReverse)
        	t.setAlpha(fAlphaRate + (1.0f-fAlphaRate)* interpolatedTime );
        else
        	t.setAlpha(1.0f - fAlphaRate* interpolatedTime );

        // 以View的中心点为旋转中心,如果不加这两句，就是以（0,0）点为旋转中心  
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }
}
