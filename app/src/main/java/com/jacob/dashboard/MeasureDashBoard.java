package com.jacob.dashboard;

import android.content.Context;
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
    private Paint mPaintCircle = new Paint();
    private Paint mPaintCenterText = new Paint();
    private Paint mPaintIndicator = new Paint();
    private Paint mPaintTextTips = new Paint();
    private Paint mPaintIndicatorText = new Paint();
    private Path path;

    private int mPaddingCircle = 30;
    private int mRadiusMain = 280;
    private int mRadiusChild = 30;
    private int mDistanceCircle;
    private int mDistanceLine;
    private int mLayoutW;

    private int mCenterX;
    private int mCenterY;

    private float circleX = -1;
    private float circleY = -1;

    private float lineX = -1;
    private float lineY = -1;


    private String mTextCenter = "10.0";
    private String textTips = "测体温";

    private Rect mRectCenter = new Rect();
    private Rect mRectTips = new Rect();
    private Rect mRectIndicator = new Rect();

    private int START_COLOR = Color.parseColor("#FF27D373");
    private int mTextColor = Color.parseColor("#FF27D373");

    public static final int START_ANGLE = 35;
    public static final int SWEEP_ANGLE = 290;

    public static final int MAX_VALUE = 50;
    public static final int MIN_VALUE = 10;

    private boolean isInCircle;

    private boolean isOutBound = false;

    public MeasureDashBoard(Context context) {
        this(context, null);
    }

    public MeasureDashBoard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeasureDashBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mLayoutW = 420 * 2;
        mDistanceCircle = mRadiusMain + mPaddingCircle + mRadiusChild;
        mCenterX = mLayoutW / 2;
        mCenterY = mLayoutW / 2;

        initCirclePaint();

        initCenterPaint();

        initIndicatorPaint();

        initTipsPaint();

        mPaintTextTips.getTextBounds("H", 0, 1, mRectIndicator);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mLayoutW, mLayoutW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.GRAY);

        //绘制圆盘
        canvas.save();
        canvas.rotate(90, mCenterX, mCenterX);
        canvas.drawPath(path, mPaintCircle);
        canvas.restore();

        //绘制和手指一起滑动的区域
        if (circleX > 0 && circleY > 0) {
            //绘制滑块
            canvas.drawCircle(circleX, circleY, mRadiusChild, mPaintIndicator);

            //绘制滑块旁边的直线

            canvas.drawLine(circleX, circleY, lineX, lineY, mPaintIndicator);

            //绘制滑块上的文字
            canvas.drawText("T", circleX - mRectIndicator.width() / 2,
                    circleY + mRectIndicator.height() / 2, mPaintIndicatorText);
        }

        //绘制控件中心的数据
        canvas.drawText(mTextCenter, mCenterX - mRectCenter.width() / 2,
                mCenterX + mRectCenter.height() / 2, mPaintCenterText);

        //绘制控件底部的提示文字
        canvas.drawText(textTips, mCenterX - mRectTips.width() / 2,
                mCenterX + mRadiusMain - mRectTips.height() / 2, mPaintTextTips);

        //绘制最小值的文字
        double angle = Math.PI * 2 * 30 / 360.0f;
        canvas.drawText(String.valueOf(MIN_VALUE), (float) (mCenterX - mRadiusMain * Math.sin(angle)),
                (float) (mCenterY + mRadiusMain * Math.cos(angle)), mPaintTextTips);

        //绘制最大值的文字
        canvas.drawText(String.valueOf(MAX_VALUE), (float) (mCenterX + mRadiusMain * Math.sin(angle)) - 20,
                (float) (mCenterY + mRadiusMain * Math.cos(angle)), mPaintTextTips);
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

                isInCircle = isInTheIndicatorScale(x, y);
                double touchDistance = Math.hypot(touchDisX, touchDisY);

                float lineDisX = (float) ((mRadiusMain - 25) * touchDisX / touchDistance);
                float lineDisY = (float) ((mRadiusMain - 25) * touchDisY / touchDistance);

                //得到触摸的点在第几象限
                int guadrant = getQuadrant(x, y);
                switch (guadrant) {
                    case 1:
                        circleX = mCenterX + (float) (touchDisX * mDistanceCircle / touchDistance);
                        circleY = mCenterY - (float) (touchDisY * mDistanceCircle / touchDistance);
                        lineX = mCenterX + lineDisX;
                        lineY = mCenterY - lineDisY;
                        break;
                    case 2:
                        circleX = mCenterX - (float) (touchDisX * mDistanceCircle / touchDistance);
                        circleY = mCenterY - (float) (touchDisY * mDistanceCircle / touchDistance);
                        lineX = mCenterX - lineDisX;
                        lineY = mCenterY - lineDisY;
                        break;
                    case 3:
                        circleX = mCenterX - (float) (touchDisX * mDistanceCircle / touchDistance);
                        circleY = mCenterY + (float) (touchDisY * mDistanceCircle / touchDistance);
                        lineX = mCenterX - lineDisX;
                        lineY = mCenterY + lineDisY;
                        break;
                    case 4:
                        circleX = mCenterX + (float) (touchDisX * mDistanceCircle / touchDistance);
                        circleY = mCenterY + (float) (touchDisY * mDistanceCircle / touchDistance);
                        lineX = mCenterX + lineDisX;
                        lineY = mCenterY + lineDisY;
                        break;
                }

                angle = (float) (Math.asin(Math.abs(circleY - mCenterY) / Math.hypot(Math.abs(circleX - mCenterY),
                        Math.abs(circleY - mCenterY))) * 180 / Math.PI);

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
                    mTextColor = getColorFromAngle(START_COLOR, Color.parseColor("#BBE7EF83"), angle);
                } else {
                    mTextColor = getColorFromAngle(Color.parseColor("#BBE7EF83"), Color.RED, angle - SWEEP_ANGLE / 2);
                }
                mPaintCenterText.setColor(mTextColor);

                //通过角度计算滑动的数据
                float value = (float) ((MAX_VALUE - MIN_VALUE) * angle * 1.0 / SWEEP_ANGLE);
                mTextCenter = formatDecimal(MIN_VALUE + value, 1);

                //只有当数据没有越界，并且点击的区域在滑快区域才可以重绘
                if (isInCircle && !isOutBound) {
                    invalidate();
                }
                break;
        }
        return true;
    }


    public static String formatDecimal(double number, int digits) {

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

    public int getQuadrant(float x, float y) {
        float tempX = x - mCenterX;
        float tempY = y - mCenterY;
        if (tempX >= 0) {
            return tempY >= 0 ? 4 : 1;
        } else {
            return tempY >= 0 ? 3 : 2;
        }
    }

    private void initTipsPaint() {
        mPaintTextTips.setTextSize(35);
        mPaintTextTips.setColor(START_COLOR);
        mPaintTextTips.setAntiAlias(true);
        mPaintTextTips.setDither(true);
        mPaintTextTips.setStrokeCap(Paint.Cap.ROUND);
        mPaintTextTips.setStrokeWidth(20);
        mPaintTextTips.getTextBounds(textTips, 0, textTips.length(), mRectTips);
    }

    private void initIndicatorPaint() {
        mPaintIndicator.setColor(Color.RED);
        mPaintIndicator.setAntiAlias(true);
        mPaintIndicator.setDither(true);
        mPaintIndicator.setStrokeCap(Paint.Cap.ROUND);
        mPaintIndicator.setStrokeWidth(5);

        mPaintIndicatorText.setColor(Color.WHITE);
        mPaintIndicatorText.setAntiAlias(true);
        mPaintIndicatorText.setDither(true);
        mPaintIndicatorText.setTextSize(35);
        mPaintIndicatorText.setStrokeCap(Paint.Cap.ROUND);
        mPaintIndicatorText.setStrokeWidth(5);
    }

    private void initCenterPaint() {
        mPaintCenterText.setAntiAlias(true);
        mPaintCenterText.setDither(true);
        mPaintCenterText.setColor(mTextColor);
        mPaintCenterText.setStyle(Paint.Style.FILL);
        mPaintCenterText.setTextSize(125);
        mPaintCenterText.getTextBounds("ABC", 0, 3, mRectCenter);
    }


    private void initCirclePaint() {
        mPaintCircle = new Paint();
        mPaintCircle.setColor(START_COLOR);
        mPaintCircle.setAntiAlias(true);
        mPaintCircle.setDither(true);
        mPaintCircle.setStrokeWidth(50);
        mPaintCircle.setStrokeJoin(Paint.Join.BEVEL);
        mPaintCircle.setStyle(Paint.Style.STROKE);
        path = new Path();
        RectF rectF = new RectF(mCenterX - mRadiusMain,
                mCenterY - mRadiusMain,
                mCenterX + mRadiusMain,
                mCenterY + mRadiusMain);
        path.addArc(rectF, START_ANGLE, SWEEP_ANGLE);
        PathEffect effects = new DashPathEffect(new float[]{6, 9}, 2);
        PathEffect effect1 = new CornerPathEffect(5);
        ComposePathEffect composePathEffect = new ComposePathEffect(effects, effect1);
        mPaintCircle.setPathEffect(composePathEffect);
        Shader shader = new SweepGradient(mCenterX, mCenterY,
                new int[]{START_COLOR, Color.YELLOW, Color.RED},
                new float[]{0.2f, 0.45f, 1f});
        mPaintCircle.setShader(shader);
    }

    /**
     * 判断手机点击区域是否是在滑块的区域以内，
     * 如果点中了才可以滑动
     */
    public boolean isInTheIndicatorScale(float x, float y) {
        return (x >= circleX - mRadiusChild * 2
                && x <= circleX + mRadiusChild * 2)
                && (y >= circleY - mRadiusChild * 2
                && y <= circleY + mRadiusChild * 2);
    }


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

}
