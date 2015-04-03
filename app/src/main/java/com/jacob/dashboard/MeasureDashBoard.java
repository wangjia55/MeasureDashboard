package com.jacob.dashboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;

/**
 * Package : com.jacob.paint
 * Author : jacob
 * Date : 15-3-20
 * Description : 这个类是测量一个控件
 */
public class MeasureDashBoard extends View {

    /**
     * 圆盘相关的paint
     */
    private Paint mPaintDashboard = new Paint();
    /**
     * 圆盘中心的文字的paint
     */
    private Paint mPaintCenterText = new Paint();
    /**
     * 滑块相关的paint
     */
    private Paint mPaintSlider = new Paint();
    /**
     * 滑块上文字的paint
     */
    private Paint mPaintSliderText = new Paint();
    /**
     * title 相关的paint
     */
    private Paint mPaintTitle = new Paint();
    /**
     * 绘制圆盘的路径
     */
    private Path mDashPath;

    //圆盘的半径
    private int mRadiusMain = dpToPx(100);

    //滑块的半径
    private int mRadiusChild = dpToPx(20);

    //屏幕的尺寸，正方型尺寸
    private int mLayoutSize;

    //中心点坐标
    private int mCenterX;
    private int mCenterY;

    //滑块中心点和屏幕中心点的距离
    private int mDistanceSlider;

    //滑块中心点坐标
    private float mSliderX;
    private float mSliderY;

    private float lineX = -1;
    private float lineY = -1;


    //3个和绘制文字相关的rect
    private Rect mRectCenter = new Rect();
    private Rect mRectTitle = new Rect();
    private Rect mRectSlider = new Rect();

    //仪表盘渐变色
    private int START_COLOR = Color.parseColor("#FF27D373");
    private int CENTER_COLOR = Color.parseColor("#BBE7EF83");
    private int END_COLOR = Color.parseColor("#FFFF4F4F");

    //屏幕中心文字的大小和颜色
    private int mTextColor = Color.parseColor("#FF27D373");
    private float mTextSize = spToPx(45);

    //圆盘的起始角度，和覆盖的角度
    public static final int START_ANGLE = 35;
    public static final int SWEEP_ANGLE = 290;

    //仪表盘支持的最大最小值
    public int max_value = 50;
    public int min_value = 10;

    private int mStroke = 50;

    private String mTextCenter = "10.0";
    private String mTitle = "测体温";

    /**
     * 判断是否接触到滑块
     */
    private boolean hasTouchSlider;

    /**
     * 判断是否数据越界
     */
    private boolean isOutBound = false;

    public MeasureDashBoard(Context context) {
        this(context, null);
    }

    public MeasureDashBoard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeasureDashBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MeasureDashBoard);
        mRadiusMain = typedArray.getDimensionPixelSize(R.styleable.MeasureDashBoard_radius_dashboard, mRadiusMain);
        mRadiusChild = typedArray.getDimensionPixelSize(R.styleable.MeasureDashBoard_radius_slider, mRadiusChild);
        mTextSize = typedArray.getDimension(R.styleable.MeasureDashBoard_main_textSize, mTextSize);
        max_value = typedArray.getInteger(R.styleable.MeasureDashBoard_maxValue, max_value);
        min_value = typedArray.getInteger(R.styleable.MeasureDashBoard_minValue, min_value);
        mTextCenter = String.valueOf(formatDecimal(min_value, 1));
        typedArray.recycle();

        //屏幕尺寸,要加上滑块的直径长度
        mLayoutSize = mRadiusMain * 2 + mRadiusChild * 3 + mStroke * 2;

        //屏幕中心的位置
        mCenterX = mLayoutSize / 2;
        mCenterY = mLayoutSize / 2;

        mDistanceSlider = mRadiusMain + mStroke / 2 + mRadiusChild + 5;

        //设置起始滑块的位置，放在最小位置
        mSliderX = mCenterX - (float) (mDistanceSlider * Math.sin(START_ANGLE * Math.PI * 2 / 360.0));
        mSliderY = mCenterY + (float) (mDistanceSlider * Math.cos(START_ANGLE * Math.PI * 2 / 360.0));
        lineX = mCenterX - (float) ((mRadiusMain - mStroke / 2) * Math.sin(START_ANGLE * Math.PI * 2 / 360.0));
        lineY = mCenterY + (float) ((mRadiusMain - mStroke / 2) * Math.cos(START_ANGLE * Math.PI * 2 / 360.0));


        initCirclePaint();

        initCenterPaint();

        initSliderPaint();

        initTitlePaint();

        mPaintTitle.getTextBounds("H", 0, 1, mRectSlider);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mLayoutSize, mLayoutSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.GRAY);

        //绘制圆盘
        canvas.save();
        canvas.rotate(90, mCenterX, mCenterX);
        canvas.drawPath(mDashPath, mPaintDashboard);
        canvas.restore();

        //绘制和手指一起滑动的区域
        if (mSliderX > 0 && mSliderY > 0) {
            //绘制滑块
            canvas.drawCircle(mSliderX, mSliderY, mRadiusChild, mPaintSlider);

            //绘制滑块旁边的直线

            canvas.drawLine(mSliderX, mSliderY, lineX, lineY, mPaintSlider);

            //绘制滑块上的文字
            canvas.drawText("T", mSliderX - mRectSlider.width() / 2,
                    mSliderY + mRectSlider.height() / 2, mPaintSliderText);
        }

        //绘制控件中心的数据
        canvas.drawText(mTextCenter, mCenterX - mRectCenter.width() / 2,
                mCenterX + mRectCenter.height() / 2, mPaintCenterText);

        //绘制控件底部的提示文字
        canvas.drawText(mTitle, mCenterX - mRectTitle.width() / 2,
                mCenterX + mRadiusMain - mRectTitle.height() / 2, mPaintTitle);

        //绘制最小值的文字
        double angle = Math.PI * 2 * 30 / 360.0f;
        canvas.drawText(String.valueOf(min_value), (float) (mCenterX - mRadiusMain * Math.sin(angle)),
                (float) (mCenterY + mRadiusMain * Math.cos(angle)), mPaintTitle);

        //绘制最大值的文字
        canvas.drawText(String.valueOf(max_value), (float) (mCenterX + mRadiusMain * Math.sin(angle)) - 20,
                (float) (mCenterY + mRadiusMain * Math.cos(angle)), mPaintTitle);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float angle = 0;
        float touchDisX = 0;
        float touchDisY = 0;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                isOutBound = false;
                touchDisX = Math.abs(x - mCenterX);
                touchDisY = Math.abs(y - mCenterY);

                hasTouchSlider = isInTheIndicatorScale(x, y);
                double touchDistance = Math.hypot(touchDisX, touchDisY);

                float lineDisX = (float) ((mRadiusMain - mStroke / 2) * touchDisX / touchDistance);
                float lineDisY = (float) ((mRadiusMain - mStroke / 2) * touchDisY / touchDistance);

                //得到触摸的点在第几象限
                int guadrant = getQuadrant(x, y);
                switch (guadrant) {
                    case 1:
                        mSliderX = mCenterX + (float) (touchDisX * mDistanceSlider / touchDistance);
                        mSliderY = mCenterY - (float) (touchDisY * mDistanceSlider / touchDistance);
                        lineX = mCenterX + lineDisX;
                        lineY = mCenterY - lineDisY;
                        break;
                    case 2:
                        mSliderX = mCenterX - (float) (touchDisX * mDistanceSlider / touchDistance);
                        mSliderY = mCenterY - (float) (touchDisY * mDistanceSlider / touchDistance);
                        lineX = mCenterX - lineDisX;
                        lineY = mCenterY - lineDisY;
                        break;
                    case 3:
                        mSliderX = mCenterX - (float) (touchDisX * mDistanceSlider / touchDistance);
                        mSliderY = mCenterY + (float) (touchDisY * mDistanceSlider / touchDistance);
                        lineX = mCenterX - lineDisX;
                        lineY = mCenterY + lineDisY;
                        break;
                    case 4:
                        mSliderX = mCenterX + (float) (touchDisX * mDistanceSlider / touchDistance);
                        mSliderY = mCenterY + (float) (touchDisY * mDistanceSlider / touchDistance);
                        lineX = mCenterX + lineDisX;
                        lineY = mCenterY + lineDisY;
                        break;
                }

                angle = (float) (Math.asin(Math.abs(mSliderY - mCenterY) / Math.hypot(Math.abs(mSliderX - mCenterY),
                        Math.abs(mSliderY - mCenterY))) * 180 / Math.PI);

                //根据象限判断出实际滑动的角度
                switch (guadrant) {
                    case 1:
                        angle = 180 + 55 - angle;
                        break;
                    case 2:
                        angle = 55 + angle;
                        break;
                    case 3:
                        angle = 55 - angle;
                        break;
                    case 4:
                        angle = 180 + 55 + angle;
                        break;
                }

                //如果滑动的角度过界了，就取出临界的值
                if (angle <= 0) {
                    angle = 0;
                    isOutBound = true;
                }

                if (angle >= SWEEP_ANGLE) {
                    angle = SWEEP_ANGLE;
                    isOutBound = true;
                }

                // 得到中心文字的颜色
                if (angle <= SWEEP_ANGLE / 2) {
                    mTextColor = getColorFromAngle(START_COLOR, CENTER_COLOR, angle);
                } else {
                    mTextColor = getColorFromAngle(CENTER_COLOR, END_COLOR, angle - SWEEP_ANGLE / 2);
                }
                mPaintCenterText.setColor(mTextColor);

                //通过角度计算滑动的数据
                float value = (float) ((max_value - min_value) * angle * 1.0 / SWEEP_ANGLE);
                mTextCenter = formatDecimal(min_value + value, 1);

                //只有当数据没有越界，并且点击的区域在滑快区域才可以重绘
                if (hasTouchSlider && !isOutBound) {
                    invalidate();
                }
                break;
        }
        return true;
    }


    /**
     * 格式化数据
     */
    public String formatDecimal(double number, int digits) {

        StringBuffer a = new StringBuffer();
        for (int i = 0; i < digits; i++) {
            if (i == 0)
                a.append(".");
            a.append("0");
        }
        DecimalFormat nf = new DecimalFormat("###,###,###,##0" + a.toString());
        String formatted = nf.format(number);
        return formatted;
    }

    /**
     * 判断当前点所在的象限
     */
    public int getQuadrant(float x, float y) {
        float tempX = x - mCenterX;
        float tempY = y - mCenterY;
        if (tempX >= 0) {
            return tempY >= 0 ? 4 : 1;
        } else {
            return tempY >= 0 ? 3 : 2;
        }
    }

    private void initTitlePaint() {
        mPaintTitle.setTextSize(35);
        mPaintTitle.setColor(START_COLOR);
        mPaintTitle.setAntiAlias(true);
        mPaintTitle.setDither(true);
        mPaintTitle.setStrokeCap(Paint.Cap.ROUND);
        mPaintTitle.setStrokeWidth(20);
        mPaintTitle.getTextBounds(mTitle, 0, mTitle.length(), mRectTitle);
    }

    private void initSliderPaint() {
        mPaintSlider.setColor(END_COLOR);
        mPaintSlider.setAntiAlias(true);
        mPaintSlider.setDither(true);
        mPaintSlider.setStrokeCap(Paint.Cap.ROUND);
        mPaintSlider.setStrokeWidth(5);

        mPaintSliderText.setColor(Color.WHITE);
        mPaintSliderText.setAntiAlias(true);
        mPaintSliderText.setDither(true);
        mPaintSliderText.setTextSize(35);
        mPaintSliderText.setStrokeCap(Paint.Cap.ROUND);
        mPaintSliderText.setStrokeWidth(5);
    }

    private void initCenterPaint() {
        mPaintCenterText.setAntiAlias(true);
        mPaintCenterText.setDither(true);
        mPaintCenterText.setColor(mTextColor);
        mPaintCenterText.setStyle(Paint.Style.FILL);
        mPaintCenterText.setTextSize(mTextSize);
        mPaintCenterText.getTextBounds("ABC", 0, 3, mRectCenter);
    }


    /**
     * 重点：绘制这种间隔拍类的圆盘的绘制方法
     */
    private void initCirclePaint() {
        mPaintDashboard = new Paint();
        mPaintDashboard.setColor(START_COLOR);
        mPaintDashboard.setAntiAlias(true);
        mPaintDashboard.setDither(true);
        mPaintDashboard.setStrokeWidth(mStroke);
        mPaintDashboard.setStrokeJoin(Paint.Join.BEVEL);
        mPaintDashboard.setStyle(Paint.Style.STROKE);
        mDashPath = new Path();
        RectF rectF = new RectF(mCenterX - mRadiusMain,
                mCenterY - mRadiusMain,
                mCenterX + mRadiusMain,
                mCenterY + mRadiusMain);
        mDashPath.addArc(rectF, START_ANGLE, SWEEP_ANGLE);
        PathEffect effects = new DashPathEffect(new float[]{6, 9}, 2);
        PathEffect effect1 = new CornerPathEffect(5);
        ComposePathEffect composePathEffect = new ComposePathEffect(effects, effect1);
        mPaintDashboard.setPathEffect(composePathEffect);
        Shader shader = new SweepGradient(mCenterX, mCenterY,
                new int[]{START_COLOR, CENTER_COLOR, Color.RED},
                new float[]{0.2f, 0.45f, 1f});
        mPaintDashboard.setShader(shader);
    }

    /**
     * 判断手机点击区域是否是在滑块的区域以内，
     * 如果点中了才可以滑动
     */
    public boolean isInTheIndicatorScale(float x, float y) {
        float deltaX= x-mCenterX;
        float deltaY = y -mCenterY;
        float distance = (float) Math.sqrt(deltaX*deltaX+deltaY*deltaY);
        if (distance>=mRadiusMain-mStroke/2){
            return true;
        }else{
            return false;
        }
    }


    /**
     * 根据旋转的角度，得到渐变的颜色的值，达到文字动态改变颜色的目的
     */
    public int getColorFromAngle(int startColor, int endColor, float angle) {
        int startR = Color.red(startColor);
        int startG = Color.green(startColor);
        int startB = Color.blue(startColor);

        int endR = Color.red(endColor);
        int endG = Color.green(endColor);
        int endB = Color.blue(endColor);

        int red = (int) (startR + (endR - startR) * angle * 2.0 / SWEEP_ANGLE);
        int green = (int) (startG + (endG - startG) * angle * 2.0 / SWEEP_ANGLE);
        int blue = (int) (startB + (endB - startB) * angle * 2.0 / SWEEP_ANGLE);
        return Color.rgb(red, green, blue);

    }


    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int spToPx(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }
}
