package rotateanim.example.com.androidrotateanim;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;


import java.util.Iterator;
import java.util.Vector;

/**
 * Created by yuzhangsheng on 2015/11/17.
 * 带进度的圆弧进度条，线程安全的View，可直接在线程中更新进度
 * 可配置顺时针和逆时针旋转
 */
public class RoundNumProgressView extends View {

    private static final int ROUND_COLOR_DEFAULT = 0x33ffffff;
    private static final int ROUND_WIDTH_DEFAULT = 2; //dp
    private static final int ROUND_PROGRESS_COLOR_DEFAULT = 0xffffffff;
    private static final int PERCENT_TEXTCOLOR_DEFAULT = 0xffffffff;
    private static final int PERCENT_TEXTSIZE_DEFAULT = 9; //sp
    private static final int MAXPROGRESS_DEFAULT = 100;
    /** 允许显示的最大最小值 - 这个与mMaxProgress和 mMinProgress不一样 */
    private static final int ALLOW_SHOW_MAX = 95;
    private static final int ALLOW_SHOW_MIN = 5;
    private static final int PERCENT_BASE = 100;

    /** 正常累加进度每次移动的角度 */
    private static final int NORMAL_PRORESS_STEP = 2;
    /** 闪烁进度条每次移动的角度 */
    private static final int FLICKER_PRORESS_STEP = 40;
    private static final int TOTAL_DEGREE = 360;
    /** 闪烁进度的时候，画的弧长 */
    private static final int FLICKER_ARC_LENGTH = 270;
    /** 顶部作为计数起点 */
    private static final int START_POINT_TOP = -90;

    /** 定义一支画笔 */
    private Paint mPaint;
    private Paint mGradientArcPaint;
    private SweepGradient mSweepGradient;
    private int[] mColors = {
            0x00ffffff, 0x40ffffff, 0x80ffffff, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, 0x80ffffff, 0x40ffffff, 0x00ffffff
    };
    private Matrix mMatrix = new Matrix();
    /** 圆环的颜色 */
    private int mRoundColor;
    /** 圆环的宽度 */
    private float mRoundWidth;
    /** 圆环进度的颜色 */
    private int mRoundProgressColor;
    /** 中间进度百分比的字符串的颜色 */
    private int mPercentTextColor;
    /** 中间进度百分比的字符串的字体 */
    private float mPercentTextSize;
    /** 最大进度数（可以允许超过100的,但是没什么意义，数字可以超过100%） */
    private int mMaxProgress;
    /** 允许显示的最大进度 */
    private int mAllowMaxProgress = ALLOW_SHOW_MAX;
    /** 允许显示的最小进度 */
    private int mAllowMinProgress = ALLOW_SHOW_MIN;
    /** 当前进度 */
    private int mCurProgress = ALLOW_SHOW_MIN;

    /** 是否显示中间的进度 */
    private boolean isPercentTextDisplayable = true;
    /** 用于定义的圆弧的形状和大小的界限 */
    private RectF mArcLimitRect = new RectF();

    /** 进度的风格，实心(FILL)或者空心(STROKE) */
    private int mStyle;
    public static final int STROKE = 0;
    public static final int FILL = 1;

    /** 进度旋转方向，顺时针(CLOCKWISE)或者逆时针(COUNTERCLOCKWISE) */
    private int mRotateOrientation;
    public static final int COUNTERCLOCKWISE = 0;
    public static final int CLOCKWISE = 1;

    /** 闪烁（循环旋转）进度条的时间 */
    private int mFlickerProgressTime = 1000;
    private int mFlickerProgressTotal = 0;

    /** 闪烁进度条动画正在进行 */
    private boolean isFlickerProgressWorking = false;
    private boolean isRefreshingProgress = false;

    private Vector<IRoundNumProgressListener> mRoundNumProgressListeners;

    public interface IRoundNumProgressListener {
        void onFlickerProgressEnd();
    }

    public RoundNumProgressView(Context context) {
        this(context, null);
    }

    public RoundNumProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundNumProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = new Paint();
        mGradientArcPaint = new Paint();
        //获取自定义属性和默认值
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundNumProgressView);
        mRoundColor = mTypedArray.getColor(R.styleable.RoundNumProgressView_roundColor, ROUND_COLOR_DEFAULT);
        mRoundProgressColor = mTypedArray.getColor(R.styleable.RoundNumProgressView_roundProgressColor, ROUND_PROGRESS_COLOR_DEFAULT);
        mPercentTextColor = mTypedArray.getColor(R.styleable.RoundNumProgressView_percentTextColor, PERCENT_TEXTCOLOR_DEFAULT);

        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, PERCENT_TEXTSIZE_DEFAULT, metrics);
        final float roundWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ROUND_WIDTH_DEFAULT, metrics);
        mPercentTextSize = mTypedArray.getDimension(R.styleable.RoundNumProgressView_percentTextSize, textSize);
        mRoundWidth = mTypedArray.getDimension(R.styleable.RoundNumProgressView_roundWidth, roundWidth);
        mMaxProgress = mTypedArray.getInteger(R.styleable.RoundNumProgressView_maxProgress, MAXPROGRESS_DEFAULT);
        isPercentTextDisplayable = mTypedArray.getBoolean(R.styleable.RoundNumProgressView_percentTextDisplayable, true);
        mStyle = mTypedArray.getInt(R.styleable.RoundNumProgressView_style, STROKE);
        mRotateOrientation = mTypedArray.getInt(R.styleable.RoundNumProgressView_rotateOrientation, COUNTERCLOCKWISE);
        mTypedArray.recycle();
    }

    public void addRoundNumProgressListener(IRoundNumProgressListener roundNumProgressListener) {
        if (roundNumProgressListener == null)
            return;
        if (mRoundNumProgressListeners == null) {
            mRoundNumProgressListeners = new Vector<IRoundNumProgressListener>();
        }
        mRoundNumProgressListeners.add(roundNumProgressListener);
    }

    private void notifyAllRoundNumProressListeners() {
        if (mRoundNumProgressListeners == null)
            return;
        Iterator<IRoundNumProgressListener> iterator = mRoundNumProgressListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().onFlickerProgressEnd();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int centerX = getWidth() / 2;                         // 获取圆心的x坐标
        final int radius = (int) (centerX - mRoundWidth / 2);       // 圆环的半径
        // 画最外层的大圆环
        drawOuterCircle(canvas, centerX, radius);
        // 画进度百分比
        drawPercentText(canvas, centerX);
        // 画闪烁的进度条动画
        if (isFlickerProgressWorking) {
            drawFlickerArcProgress(canvas, centerX, radius);
        } else {
            // 画圆弧 ，画圆环的进度
            drawArcProgress(canvas, centerX, radius);
        }
    }

    private void drawOuterCircle(final Canvas canvas, final int centerX, final int radius) {
        mPaint.setColor(mRoundColor);                        // 设置圆环的颜色
        mPaint.setStyle(Paint.Style.STROKE);                 // 设置空心
        mPaint.setStrokeWidth(mRoundWidth);                  // 设置圆环的宽度
        mPaint.setAntiAlias(true);                           // 消除锯齿
        canvas.drawCircle(centerX, centerX, radius, mPaint); // 画出圆环
    }

    private void drawPercentText(final Canvas canvas, final int center) {
        mPaint.setStrokeWidth(0);
        mPaint.setColor(mPercentTextColor);
        mPaint.setTextSize(mPercentTextSize);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);                        // 设置字体
        final int percent = (int) (((float) mCurProgress / (float) mMaxProgress) * PERCENT_BASE);    // 中间的进度百分比，先转换成float在进行除法运算，不然都为0
        final float textWidth = mPaint.measureText(percent + "%");                          // 测量字体宽度，我们需要根据字体的宽度设置在圆环中间
        if (isPercentTextDisplayable && percent != 0 && mStyle == STROKE) {
            canvas.drawText(percent + "%", center - textWidth / 2, center + mPercentTextSize / 2 - 3, mPaint); // 画出进度百分比
        }
    }

    private void drawArcProgress(final Canvas canvas, final int centerX, final int radius) {
        mPaint.setStrokeWidth(mRoundWidth);         // 设置圆环的宽度
        mPaint.setColor(mRoundProgressColor);       // 设置进度的颜色
        // 用于定义的圆弧的形状和大小的界限
        mArcLimitRect.set(centerX - radius, centerX - radius, centerX + radius, centerX + radius);
        if (mStyle == STROKE) {
            mPaint.setStyle(Paint.Style.STROKE);
        } else if (mStyle == FILL) {
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        }
        final float percent = ((float) mCurProgress / (float) mMaxProgress);
        if (mRotateOrientation == COUNTERCLOCKWISE) {   // 计数起点在顶部，所以为-90
            canvas.drawArc(mArcLimitRect, START_POINT_TOP, -TOTAL_DEGREE * percent, false, mPaint);     // 根据进度画圆弧
        } else if (mRotateOrientation == CLOCKWISE) {
            canvas.drawArc(mArcLimitRect, START_POINT_TOP, TOTAL_DEGREE * percent, false, mPaint);      // 根据进度画圆弧
        }
    }

    private void drawFlickerArcProgress(final Canvas canvas, final int centerX, final int radius) {
        mGradientArcPaint.setStrokeWidth(mRoundWidth);         // 设置圆环的宽度
        mGradientArcPaint.setColor(mRoundProgressColor);       // 设置进度的颜色
        mGradientArcPaint.setAntiAlias(true);
        mArcLimitRect.set(centerX - radius, centerX - radius, centerX + radius, centerX + radius);
        mGradientArcPaint.setStyle(Paint.Style.STROKE);
        // 定义一个梯度渲染，由于梯度渲染是从三点钟方向开始，所以再让他逆时针旋转90°，从0点开始
        if (mSweepGradient == null)
            mSweepGradient = new SweepGradient(centerX, centerX, mColors, null);
        mMatrix.setRotate(-90 - mFlickerProgressTotal, centerX, centerX);
        mSweepGradient.setLocalMatrix(mMatrix);
        mGradientArcPaint.setShader(mSweepGradient);
        canvas.drawCircle(centerX, centerX, radius, mGradientArcPaint); // 画出圆环
        post(new Runnable() {
            @Override
            public void run() {
                mFlickerProgressTotal += FLICKER_PRORESS_STEP;
                postInvalidate();
            }
        });
    }

    /***
     * 启动闪烁动画，点击的时候调用会起到很好的提示作用
     */
    public void startFlickerArcProgress(final Runnable endFlagRunnable) {
        if (isFlickerProgressWorking)
            return;
        isFlickerProgressWorking = true;
        postInvalidate();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                isFlickerProgressWorking = false;
                if (endFlagRunnable != null) {
                    endFlagRunnable.run();
                }
                notifyAllRoundNumProressListeners();
            }
        }, mFlickerProgressTime);
    }

    private int mTempCurProgress;
    /**
     * 重新刷新显示进度，从0开始累加到要显示的值
     */
    public void reRefreshToOriginProgress() {
        reRefreshToOriginProgress(getCurProgress());
    }

    public void reRefreshToOriginProgress(final int toProgress) {
        if (isRefreshingProgress)
            return;
        if (toProgress <= mAllowMinProgress)
            return;
        mTempCurProgress = mAllowMinProgress;
        post(new Runnable() {
            @Override
            public void run() {
                if (mTempCurProgress >= toProgress) {
                    isRefreshingProgress = false;
                    setCurProgress(toProgress);
                    return;
                }
                setCurProgress(mTempCurProgress += NORMAL_PRORESS_STEP);
                post(this);
            }
        });
    }

    public boolean isFlickerProgressWorking() {
        return isFlickerProgressWorking;
    }

    public boolean isRefreshingProgress() {
        return isRefreshingProgress;
    }

    public synchronized int getMaxProgress() {
        return mMaxProgress;
    }

    public synchronized int getMinProgress() {
        return mMaxProgress;
    }

    /**
     * 设置总的进度数
     *
     * @param maxProgress
     */
    public synchronized void setMaxProgress(int maxProgress) {
        if (maxProgress < 0) {
            throw new IllegalArgumentException("max not less than 0");
        }
        mMaxProgress = maxProgress;
    }

    /**
     * 设置允许显示进度的最小值
     *
     * @param allowMinProgress
     */
    public synchronized void setAlloShowMinProgress(int allowMinProgress) {
        if (allowMinProgress < 0) {
            throw new IllegalArgumentException("min not less than 0");
        }
        mAllowMinProgress = allowMinProgress;
    }

    /**
     * 获取进度.需要同步
     *
     * @return
     */
    public synchronized int getCurProgress() {
        return mCurProgress;
    }

    /**
     * 设置进度，此为线程安全控件，由于考虑多线程的问题，需要同步
     * 刷新界面调用postInvalidate()能在非UI线程刷新
     *
     * @param curProgress
     */
    public synchronized void setCurProgress(int curProgress) {
        if (curProgress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (curProgress > mAllowMaxProgress) {
            curProgress = mAllowMaxProgress;
        } else if (curProgress < mAllowMinProgress) {
            curProgress = mAllowMinProgress;
        }
        mCurProgress = curProgress;
        postInvalidate();
    }

    public int getOuterRoundColor() {
        return mRoundColor;
    }

    public void setOuterRoundColor(int roundColor) {
        mRoundColor = roundColor;
    }

    public int getRoundProgressColor() {
        return mRoundProgressColor;
    }

    public void setRoundProgressColor(int roundProgressColor) {
        mRoundProgressColor = roundProgressColor;
    }

    public int getPercentTextColor() {
        return mPercentTextColor;
    }

    public void setPercentTextColor(int textColor) {
        mPercentTextColor = textColor;
    }

    public float getPercentTextSize() {
        return mPercentTextSize;
    }

    public void setPercentTextSize(float percentTextSize) {
        mPercentTextSize = percentTextSize;
    }

    public float getRoundWidth() {
        return mRoundWidth;
    }

    public void setRoundWidth(float roundWidth) {
        mRoundWidth = roundWidth;
    }

    public void setRotateOrientation(int rotateOrientation) {
        mRotateOrientation = rotateOrientation;
    }

    public void setStyle(int style) {
        mStyle = style;
    }
}
