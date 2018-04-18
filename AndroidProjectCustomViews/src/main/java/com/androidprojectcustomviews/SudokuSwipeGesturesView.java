package com.androidprojectcustomviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by LorenWang on 2018/4/13.
 * 创建时间：2018/4/13 16:20
 * 创建人：王亮（Loren wang）
 * 功能作用：九宫格手势滑动操作
 * 思路：首先制定好九个圆圈的位置，以及相应的中心坐标，还有滑动的有效半径，显示中心圆半径
 *      1、还需要设置当圆圈被选中、未选中、以及选中错误的情况下的颜色显示
 *      2、设置显示模式
 *           ①、中心圆实心，外层圆空心，滑动时中心圆变色，外围出现指定宽度的边框，连接线从中心点出来，此模式下需要传入边框宽度
 *           ②、中心圆空心，外层圆实心，滑动时外层圆直接变色，中心出现和外层圆相同颜色的圆点，连接线从中心点出来，此模式需要传入选中时中心圆半径
 *           ③、后续模式待设计
 *      3、是否要显示绘制轨迹
 *      4、连接线宽度、颜色
 *      5、完成手势之后需要接收回调值，如果无回调则直接在指定时间之后重置，如果有回调并且有返回值则操作后回调
 *      6、圆圈绘制使用横向排列绘制
 * 方法：1、释放所有变量
 *      2、重置显示到最初显示状态
 *      3、设置有效半径以及中心圆半径
 *      4、设置圆圈被选中、未选中、以及选中错误下的颜色显示
 *      5、设置显示模式
 *      6、设置是否要显示轨迹
 *      7、设置连接线颜色、宽度
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */

public class SudokuSwipeGesturesView extends View {
    private final int CIRCLE_SHOW_TYPE_1 = 1;//显示模式1
    private final int CIRCLE_SHOW_TYPE_2 = 2;//显示模式2

    private int circleEffectiveRadius = 0;//圆圈的有效半径
    private int circleCenterRadius = 0;//圆圈的中心半径
    private int circleBorderWidth = 0;//圆圈边框宽度
    private int circleSelectedColor = Color.WHITE;//圆圈的选中颜色
    private int circleUnSelectedColor = Color.WHITE;//圆圈的未选中颜色
    private int circleSelectedErrorColor = Color.WHITE;//圆圈的选中移除颜色
    private int circleShowType = CIRCLE_SHOW_TYPE_1;//圆圈显示模式
    private boolean isShowTrack = true;//是否显示绘制轨迹
    private int connectingLineWidth = 0;//连接线宽度


    private List<CircleInfo> circleInfoList = new ArrayList<>();//圆圈集合,存储横向排列
    private Paint circleSelectCenterPaint = new Paint();//圆圈被选中的时候的中心圆的画笔
    private Paint circleSelectBorderPaint = new Paint();//圆圈被选中的时候中心圆的画笔
    private Paint circleUnSelectCenterPaint = new Paint();//圆圈未选中的时候的中心圆的画笔
    private Paint circleUnSelectBorderPaint = new Paint();//圆圈未选中的时候中心圆的画笔
    private Paint circleSelectErrorCenterPaint = new Paint();//圆圈选中错误的时候的中心圆的画笔
    private Paint circleSelectErrorBorderPaint = new Paint();//圆圈选中错误的时候中心圆的画笔
    private Paint connectingSelectLinePaint = new Paint();//连接线画笔
    private Paint connectingSelectErrorLinePaint = new Paint();//连接线画笔移除
    private List<CircleInfo> selectCirclePosiList = new ArrayList<>();//选中列表
    private int maxCircleEffectiveRadius = 0;//最大的圆圈有效半径
    private InputStateChangeCallback inputStateChangeCallback;//输入状态改变回传

    private final int DATA_STATE_INIT = 0;//当前的数据状态是初始化
    private final int DATA_STATE_INPUT = 1;//当前的数据状态是输入中
    private final int DATA_STATE_FINISH = 2;//当前的数据状态是结束输入
    private final int DATA_STATE_TRUE = 3;//当前的数据状态是正确输入
    private final int DATA_STATE_FALSE = 4;//当前的数据状态是错误输入
    private int nowDataState = DATA_STATE_INIT;//当前的数据状态

    private final int RESET_VIEW = 0;//重置视图
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case RESET_VIEW:
                    resetAll();
                    break;
                default:
                    break;
            }
        }
    };


    public SudokuSwipeGesturesView(Context context) {
        super(context);
        init(context,null,-1);
    }

    public SudokuSwipeGesturesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs,-1);
    }

    public SudokuSwipeGesturesView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr);
    }


    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.SudokuSwipeGesturesView);
        circleEffectiveRadius = attributes.getDimensionPixelOffset(R.styleable.SudokuSwipeGesturesView_circleEffectiveRadius,dip2px(25));
        circleCenterRadius = attributes.getDimensionPixelOffset(R.styleable.SudokuSwipeGesturesView_circleCenterRadius,dip2px(5));
        isShowTrack = attributes.getBoolean(R.styleable.SudokuSwipeGesturesView_isShowTrack,true);
        connectingLineWidth = attributes.getDimensionPixelOffset(R.styleable.SudokuSwipeGesturesView_connectingLineWidth,dip2px(2));
        circleBorderWidth = attributes.getDimensionPixelOffset(R.styleable.SudokuSwipeGesturesView_circleBorderWidth,dip2px(1));
        circleSelectedColor = attributes.getColor(R.styleable.SudokuSwipeGesturesView_circleSelectedColor, Color.WHITE);
        circleUnSelectedColor = attributes.getColor(R.styleable.SudokuSwipeGesturesView_circleUnSelectedColor,Color.WHITE);
        circleSelectedErrorColor = attributes.getColor(R.styleable.SudokuSwipeGesturesView_circleSelectedErrorColor,Color.WHITE);
        circleShowType = attributes.getInt(R.styleable.SudokuSwipeGesturesView_circleShowType,CIRCLE_SHOW_TYPE_1);

        setPaint(circleSelectCenterPaint,circleSelectedColor,false,0);
        setPaint(circleUnSelectCenterPaint,circleUnSelectedColor,false,0);
        setPaint(circleSelectErrorCenterPaint,circleSelectedErrorColor,false,0);
        setPaint(circleSelectBorderPaint,circleSelectedColor,true,circleBorderWidth);
        setPaint(circleUnSelectBorderPaint,circleUnSelectedColor,true,circleBorderWidth);
        setPaint(circleSelectErrorBorderPaint,circleSelectedErrorColor,true,circleBorderWidth);
        setPaint(connectingSelectLinePaint,circleSelectedColor,true,connectingLineWidth);
        setPaint(connectingSelectErrorLinePaint,circleUnSelectedColor,true,connectingLineWidth);
    }

    /**
     * 设置画笔
     * @param paint
     * @param color
     * @param isHaveBorder
     * @param borderWidth
     */
    private void setPaint(Paint paint,int color,boolean isHaveBorder,int borderWidth){
        paint.setAntiAlias(true);
        paint.setColor(color);
        if(isHaveBorder){
            paint.setStrokeWidth(borderWidth);
            paint.setStyle(Paint.Style.STROKE);
        }else {
            paint.setStyle(Paint.Style.FILL);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(getMeasuredWidth() > 0 && getMeasuredHeight() > 0){
            //init all circles
            int hor = getMeasuredWidth() / 6;
            int ver = getMeasuredHeight() / 6;
            circleInfoList.clear();
            for (int i = 0; i < 9; i++) {
                int tempX = (i % 3 + 1) * 2 * hor - hor;
                int tempY = (i / 3 + 1) * 2 * ver - ver;
                circleInfoList.add(new CircleInfo(i,tempX,tempY,false));
            }

            int min = Math.min(hor, ver);
            maxCircleEffectiveRadius = min;
            if(circleEffectiveRadius > maxCircleEffectiveRadius){
                circleEffectiveRadius = maxCircleEffectiveRadius;
            }
            if(circleCenterRadius + circleBorderWidth > circleEffectiveRadius){
                circleCenterRadius = circleEffectiveRadius - circleBorderWidth;
            }

        }
    }




    /***************************************手势滑动监听以及图像绘制***********************************/
    private int nowX;
    private int nowY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        nowX = (int) event.getX();
        nowY = (int) event.getY();
        CircleInfo circle = getOuterCircle(nowX, nowY);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                resetAll();
            case MotionEvent.ACTION_MOVE:
                if (circle != null) {
                    circle.isSelect = true;
                    selectCirclePosiList.add(circle);
                }
                nowDataState = DATA_STATE_INPUT;
                break;
            case MotionEvent.ACTION_UP:
                nowDataState = DATA_STATE_FINISH;
                finishInput();
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if(circleInfoList.size() > 0){
            switch (circleShowType){
                case CIRCLE_SHOW_TYPE_1:
                    drawCanvasCircleShowType1(canvas);
                    break;
                case CIRCLE_SHOW_TYPE_2:
                    drawCanvasCircleShowType2(canvas);
                    break;
                default:
                    break;

            }
            drawLineMove(canvas);
        }
        super.onDraw(canvas);
    }
    /**
     * 判断传入坐标是否在圆内
     * @param x
     * @param y
     * @return
     */
    @Nullable
    private CircleInfo getOuterCircle(int x, int y) {
        Iterator<CircleInfo> iterator = circleInfoList.iterator();
        CircleInfo circleInfo;
        int distanceX;
        int distanceY;
        int distanceZ;
        while (iterator.hasNext()){
            circleInfo = iterator.next();
            //点击位置x坐标与圆心的x坐标的距离
            distanceX = Math.abs(circleInfo.x - x);
            //点击位置y坐标与圆心的y坐标的距离
            distanceY = Math.abs(circleInfo.y - y);
            //点击位置与圆心的直线距离
            distanceZ = (int) Math.sqrt(Math.pow(distanceX,2)+Math.pow(distanceY,2));

            //如果点击位置与圆心的距离大于圆的半径，证明点击位置没有在圆内,同时也没有被选中的
            if(distanceZ <= circleEffectiveRadius && !circleInfo.isSelect){
                iterator = null;
                return circleInfo;
            }
        }
        return null;
    }
    /**
     * 绘制移动线条
     * @param canvas
     */
    private void drawLineMove(Canvas canvas){
        int size = selectCirclePosiList.size();
        if(nowDataState == DATA_STATE_INPUT && isShowTrack && size > 0 && size < 9) {
            CircleInfo circleInfo = selectCirclePosiList.get(selectCirclePosiList.size() - 1);
            canvas.drawLine(circleInfo.x, circleInfo.y, nowX, nowY, connectingSelectLinePaint);
        }
    }
    /**
     * 绘制显示模式1,中心圆实心，外层圆空心，滑动时中心圆变色，外围出现指定宽度的边框，连接线从中心点出来，此模式下需要传入边框宽度
     * @param canvas
     */
    private void drawCanvasCircleShowType1(Canvas canvas){
        CircleInfo circleInfo;
        if(isShowTrack) {
            //先绘制选中的
            int size = selectCirclePosiList.size();
            CircleInfo circleInfoLast = null;
            //数据状态是输入中的时候绘制选中状态下的状况
            if(nowDataState == DATA_STATE_INPUT || nowDataState == DATA_STATE_FINISH){
                for (int i = 0; i < size; i++) {
                    circleInfo = selectCirclePosiList.get(i);
                    drawItem(canvas,circleInfo,true,circleCenterRadius,circleSelectCenterPaint, circleEffectiveRadius
                            , circleSelectBorderPaint,true,circleInfoLast,connectingSelectLinePaint);
                    circleInfoLast = circleInfo;
                }
            }else if(nowDataState == DATA_STATE_FALSE){
                for (int i = 0; i < size; i++) {
                    circleInfo = selectCirclePosiList.get(i);
                    drawItem(canvas,circleInfo,true,circleCenterRadius,circleSelectErrorCenterPaint, circleEffectiveRadius
                            , circleSelectErrorBorderPaint,true,circleInfoLast,connectingSelectErrorLinePaint);
                    circleInfoLast = circleInfo;
                }
            }
            circleInfo = null;
            circleInfoLast = null;
        }

        //绘制未选中的
        for(int i = 0; i < 9 ; i++){
            circleInfo = circleInfoList.get(i);
            if(selectCirclePosiList.contains(circleInfo) && isShowTrack){
                continue;
            }
            //绘制中心圆
            canvas.drawCircle(circleInfo.x, circleInfo.y, circleCenterRadius, circleUnSelectCenterPaint);
            canvas.drawCircle(circleInfo.x, circleInfo.y, circleEffectiveRadius, circleUnSelectBorderPaint);
        }

    }
    /**
     * 绘制显示模式2,中心圆空心，外层圆实心，滑动时外层圆直接变色，中心出现和外层圆相同颜色的圆点，连接线从中心点出来，此模式需要传入选中时中心圆半径
     * @param canvas
     */
    private void drawCanvasCircleShowType2(Canvas canvas){

    }
    /**
     * 绘制每一个位置的圆圈
     * @param canvas 画布
     * @param circleInfo 要绘制的圆对象
     * @param centerRadiu 中心圆半径
     * @param centerPaint 中心圆画笔
     * @param centerCircleIsShow 中心圆是否显示
     * @param effectiveRadius 有效区域半径
     * @param borderPaint 外圈边框宽度
     * @param circleInfoLast 要连接线的上一个实体圆对象，为空的时候不连接
     * @param linePaint 连接线画笔
     */
    private void drawItem(Canvas canvas,CircleInfo circleInfo,boolean centerCircleIsShow,int centerRadiu,Paint centerPaint
            ,int effectiveRadius,Paint borderPaint,boolean borderCircleIsShow,CircleInfo circleInfoLast,Paint linePaint){
        //绘制中心圆
        if(centerCircleIsShow){
            canvas.drawCircle(circleInfo.x, circleInfo.y, centerRadiu, centerPaint);
        }
        //绘制外圈圆边框
        if(borderCircleIsShow) {
            canvas.drawCircle(circleInfo.x, circleInfo.y, effectiveRadius, borderPaint);
        }
        //绘制两个圆中间的连接线
        if(circleInfoLast != null && linePaint != null){
            canvas.drawLine(circleInfoLast.x, circleInfoLast.y, circleInfo.x, circleInfo.y, linePaint);
        }
    }





    /******************************************内部私有方法******************************************/

    /**
     * 重置所有状态
     */
    private void resetAll() {
        nowDataState = DATA_STATE_INIT;
        Iterator<CircleInfo> iterator = selectCirclePosiList.iterator();
        CircleInfo circleInfo;
        while (iterator.hasNext()){
            circleInfo = iterator.next();
            circleInfo.isSelect = false;
            iterator.remove();
            selectCirclePosiList.remove(circleInfo);
        }
        invalidate();
    }

    /**
     * 结束输入并回传数据
     */
    private void finishInput(){
        if(inputStateChangeCallback == null){
            handler.sendEmptyMessageDelayed(RESET_VIEW,1500);
        }else {
            int size = selectCirclePosiList.size();
            int[] posis = new int[size];
            for(int i = 0 ; i < size ; i++){
                posis[i] = selectCirclePosiList.get(i).posi;
            }
            inputStateChangeCallback.finishInput(posis);
        }
    }







    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @return
     */
    private int dip2px(float dipValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 圆圈的部分属性
     */
    private class CircleInfo{
        private int x;
        private int y;
        private int posi;
        private boolean isSelect;//是否选中了

        public CircleInfo(int posi,int x, int y, boolean isSelect) {
            this.posi = posi;
            this.x = x;
            this.y = y;
            this.isSelect = isSelect;
        }

        @Override
        public String toString() {
            return "CircleInfo{" +
                    "x=" + x +
                    ", y=" + y +
                    ", isSelect=" + isSelect +
                    '}';
        }
    }

    public interface InputStateChangeCallback{
        void finishInput(int[] posis);//回传位置数组
        void beInputting();//正在输入
    }
}
