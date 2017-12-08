package rotateanim.example.com.androidrotateanim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by yzs on 2017/7/13.
 * 圆形进度控件，在SurfaceView中绘制
 */

public class CircleProgressView extends SurfaceView implements SurfaceHolder.Callback  {

    // ================ 公共数据 ============= //
    /** 顶部作为计数起点, 右边是0，左边是-180或者180，底部是90 */
    private static final int START_POINT_TOP = -90;
    private static final float TOTAL_ANGLE = 360f;
    private static final int CLEAR_COLOR = 0xff0583f7;

    /** 圆环进度的颜色 */
    private int mRoundProgressColor = 0xff04d3ff;
    /** 背景颜色 */
    private int mRoundProgressBgColor = CLEAR_COLOR;
    /** 圆心的x坐标 */
    float mCenterX = 0;
    /** 圆心的y坐标 */
    float mCenterY = 0;
    /** 定义画笔 */
    private Paint mProgressPaint;
    /** SurfaceView的绘制线程 */
    private DrawThread mDrawThread;
    private SurfaceHolder mSurfaceHolder;
    private DisplayMetrics mMetrics;
    /** 最大帧数 (1000 / 20) */
    private static final int DRAW_INTERVAL = 20;


    public CircleProgressView(Context context) {
        super(context);
        init();
    }

    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mMetrics = getResources().getDisplayMetrics();
        mOuterRoundWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mMetrics);
        mOuterHeadCircleWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, mMetrics);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
    }

    private void start() {
        if (mDrawThread == null) {
            mDrawThread = new DrawThread(mSurfaceHolder, getContext());
        }
        try {
            if (!mDrawThread.isRunning) {
                mDrawThread.isRunning = true;
                mDrawThread.start();
            }
        } catch (Exception ignore) {}
    }

    private void stop() {
        mDrawThread.isRunning = false;
        try {
            mDrawThread.join();
            mDrawThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    class DrawThread extends Thread {
        SurfaceHolder surfaceHolder;
        Context context;
        boolean isRunning;

        public DrawThread(SurfaceHolder surfaceHolder, Context context) {
            this.surfaceHolder = surfaceHolder;
            this.context = context;
        }

        @Override
        public void run() {
            long timeStartPerDraw;
            long deltaTime;
            while (isRunning) {
                Canvas canvas = null;
                timeStartPerDraw = System.currentTimeMillis();
                try {
                    synchronized (surfaceHolder) {
                        Surface surface = surfaceHolder.getSurface();
                        if (surface != null && surface.isValid()) {
                            canvas = surfaceHolder.lockCanvas(null);
                        }
                        if (canvas != null) {
                            doDraw(canvas);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (surfaceHolder != null && canvas != null) {
                        try {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        } catch (Exception ignore) {}
                    }
                }
                deltaTime = System.currentTimeMillis() - timeStartPerDraw;
                if (deltaTime < DRAW_INTERVAL) {
                    try {
                        // 控制帧数
                        Thread.sleep(DRAW_INTERVAL - deltaTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private void doDraw(Canvas canvas) {
        calculatePreValue();
        canvas.drawColor(mRoundProgressBgColor);
        drawProgressPart(canvas);
    }

    private int mProgressAlpha = 255;
    private void drawProgressPart(Canvas canvas) {
        drawOuterGradientProgress(canvas);
    }

    private void calculatePreValue() {
        if (mCenterX <= 0) {
            mCenterX = getWidth() / 2;                         // 获取圆心的x坐标
        }
        if (mCenterY <= 0) {
            mCenterY = getHeight() / 2;
        }
        if (mOuterRadius <= 0) {
            mOuterRadius = getWidth() / 2 - mOuterRoundWidth;
        }
    }

    // ================ 外环进度数据 ============= //
    /** 顶部作为计数起点 270度, 计算圆上的任意点坐标时顺时针为正，右边是0 */
    private static final float HEAD_CIRCLE_START_ANGLE = 270f;
    /** 进度条每次移动的角度 */
    private static int mOuterProgressStep = 8;
    /** 修改这个颜色数组就会出现不一样的渐变圆弧 */
    private int[] mColors = {
            0x0004d3ff, 0x0004d3ff, 0x4004d3ff, 0x8004d3ff, 0xff04d3ff
    };
    /** 外环渐变处理器 */
    private SweepGradient mOuterSweepGradient;
    /** 外环用于旋转的矩阵 Matrix */
    private Matrix mOuterMatrix = new Matrix();
    /** 外圆环的宽度 */
    private float mOuterRoundWidth;
    /** 外圆环头部的圆圈半径 */
    private float mOuterHeadCircleWidth;
    /** 外环的半径 */
    private float mOuterRadius = 0;
    /** 外环角度旋转总进度*/
    private float mOuterAngleProgressTotal = 0;
    /** 外环头部圆选择角度 */
    private float mOuterHeadCircleAngleTotal = 0;
    private double mOuterHeadCircleAngleTotalMath = 0;
    private void drawOuterGradientProgress(final Canvas canvas) {
        mProgressPaint.setStrokeWidth(mOuterRoundWidth);         // 设置圆环的宽度
        mProgressPaint.setColor(mRoundProgressColor);       // 设置进度的颜色
        mProgressPaint.setAlpha(mProgressAlpha);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        // 定义一个梯度渲染，由于梯度渲染是从三点钟方向开始，所以再让他逆时针旋转90°，从0点开始
        if (mOuterSweepGradient == null) {
            mOuterSweepGradient = new SweepGradient(mCenterX, mCenterY, mColors, null);
        }
        mOuterMatrix.setRotate((START_POINT_TOP + mOuterAngleProgressTotal), mCenterX, mCenterY);
        mOuterSweepGradient.setLocalMatrix(mOuterMatrix);
        mProgressPaint.setShader(mOuterSweepGradient);
        canvas.drawCircle(mCenterX, mCenterY, mOuterRadius, mProgressPaint); // 画出圆环
        drawOuterArcHeadCircle(canvas);
        mOuterAngleProgressTotal += mOuterProgressStep;
        if (mOuterAngleProgressTotal > TOTAL_ANGLE) {
            mOuterAngleProgressTotal -= TOTAL_ANGLE;
        }
    }

    private void drawOuterArcHeadCircle(final Canvas canvas) {
        mProgressPaint.setShader(null);
        mProgressPaint.setStrokeWidth(0);
        mProgressPaint.setStyle(Paint.Style.FILL);
        // 一开始从顶部开始旋转
        mOuterHeadCircleAngleTotal = (HEAD_CIRCLE_START_ANGLE + mOuterAngleProgressTotal);
        if (mOuterHeadCircleAngleTotal - TOTAL_ANGLE > 0) {
            mOuterHeadCircleAngleTotal -= TOTAL_ANGLE;
        }
        // 根据旋转角度计算圆上当前位置点坐标，再以当前位置左边点位圆心画一个圆
        mOuterHeadCircleAngleTotalMath = mOuterHeadCircleAngleTotal * Math.PI / 180f;
        canvas.drawCircle((float) (mCenterX + mOuterRadius * Math.cos(mOuterHeadCircleAngleTotalMath)),
                (float) (mCenterY + mOuterRadius * Math.sin(mOuterHeadCircleAngleTotalMath)),
                mOuterHeadCircleWidth, mProgressPaint);
    }

    public void setProgressColors(int colors[]) {
        mColors = colors;
        mRoundProgressColor = mColors[mColors.length - 1];
    }

    public void setRoundProgressBgColor(int roundProgressBgColor) {
        mRoundProgressBgColor = roundProgressBgColor;
    }
}
