package com.practice.luckypan;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;

public class LuckyPan extends View {

    private Paint paint;
    private ArrayList<RectF> reactFs; //存储矩形集合
    private float strokWidth = 7; //矩形边框宽度
    private int rectSize; //正方形矩形边长
    private int[] itemColor = {Color.GREEN, Color.RED};

    private int repeatCount = 3; //转圈数
    private int luckNum ;  //中奖位置
    private int position = -1;  //抽奖块的位置
    private int startPosition = 0;  //开始抽奖的位置
    private boolean clickGoFlag = false;  //是否点击中间Go


    public LuckyPan(Context context){
        this(context, null);
    }

    public LuckyPan(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LuckyPan(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //设置画笔
        paint = new Paint(Paint.ANTI_ALIAS_FLAG); //抗锯齿
        paint.setStrokeWidth(strokWidth); //设置边框宽
        paint.setStyle(Paint.Style.FILL); //填满方式
        reactFs = new ArrayList<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        rectSize = Math.min(w, h) / 3; //过去矩形宽和高
        reactFs.clear();  //控件大小改变时清空记录数据
        initRect();
    }

    /**
     * 加载矩形数据
     */
    private void initRect(){
        //加载前三个矩形
        for (int x = 0; x < 3; x++){
            float left = x * rectSize;    //根据左轴乘以第几个倍数来控制距离布局
            float top = 0;
            float right = (x + 1) * rectSize;
            float bottom = rectSize;
            RectF rectF = new RectF(left,top,right,bottom);  //根据坐标绘制生成矩形
            reactFs.add(rectF);
        }

        //加载第四个
        reactFs.add(new RectF(getWidth() - rectSize, rectSize, getWidth(), rectSize * 2));

        //加载第五~七个
        for (int y = 3; y > 0; y--){
            float left = getWidth() - (4 - y) * rectSize;
            float top = rectSize * 2;
            float right = (y - 3) * rectSize + getWidth();
            float bottom = rectSize * 3;
            RectF rectF = new RectF(left, top, right, bottom);
            reactFs.add(rectF);
        }

        //加载第八个
        reactFs.add(new RectF(0, rectSize, rectSize, rectSize * 2));

        //加载第九个
        reactFs.add(new RectF(rectSize, rectSize, rectSize * 2, rectSize * 2));
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        drawRects(canvas);
        drawText(canvas);
    }


    /**
     * 画九宫格
     */
    private void drawRects(Canvas canvas){
        for (int i = 0; i < reactFs.size(); i++){
            RectF rectF = reactFs.get(i);
            if (i == 8){
                //中心矩阵背景设为白色
                paint.setColor(Color.WHITE);
                canvas.drawRect(rectF, paint);
            }
            else {
                paint.setColor(itemColor[i % 2]);//保证相邻两矩形颜色不同
                if (position == i){
                    paint.setColor(Color.BLUE);
                }
                canvas.drawRect(rectF, paint);
            }
        }
    }

    /**
     * 给矩形添加文字信息
     */
    private void drawText(Canvas canvas){
        for (int i = 0; i < reactFs.size(); i++){
            RectF rectF = reactFs.get(i);
            if (i == 8){
                paint.setTextSize(150);
                paint.setColor(Color.RED);
                canvas.drawText("Go", rectF.centerX()-80, rectF.centerY()+50, paint);
            }
            else {
                paint.setTextSize(100);
                canvas.drawText("" + (i+1), rectF.centerX(), rectF.centerY(), paint);
            }

        }
    }

    private void setPosition(int position){
        this.position = position;
        invalidate();  //调用刷新
    }

    /**
     * 抽奖动画
     */

    public void startAnimation(){
        Random random = new Random();
        luckNum = random.nextInt(8);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startPosition, repeatCount * 8 + luckNum).setDuration(6000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int position = (int) valueAnimator.getAnimatedValue();
                setPosition(position % 8);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator){
                startPosition = luckNum;
            }
        });
        valueAnimator.start();
    }

    /**
     *  重写Go的事件分发
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            if (reactFs.get(8).contains(event.getX(), event.getY())){
                clickGoFlag = true;
            }
            else {
                clickGoFlag = false;
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP){
            if (clickGoFlag){
                if (reactFs.get(8).contains(event.getX(), event.getY())){
                    startAnimation(); //只有手指落下和抬起都在Go的矩形内才触发
                }
                clickGoFlag = false;
            }
        }
        return super.onTouchEvent(event);   //请求抛给父类（事件分发机制）
    }

}
