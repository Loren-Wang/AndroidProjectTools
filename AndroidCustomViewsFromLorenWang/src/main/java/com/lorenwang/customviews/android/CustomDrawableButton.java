package com.lorenwang.customviews.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.Gravity;

public class CustomDrawableButton extends android.support.v7.widget.AppCompatButton {

    private final int DRAWABLE_POSI_NONE = 0;//取值为none的时候代表着不显示图片
    private final int DRAWABLE_POSI_LEFT = 1;
    private final int DRAWABLE_POSI_TOP = 2;
    private final int DRAWABLE_POSI_RIGHT = 3;
    private final int DRAWABLE_POSI_BOTTOM = 4;

    private int drawablePosi = DRAWABLE_POSI_NONE;//图片位置
    private int drawableWidth = 0;//图片宽度
    private int drawableHeight = 0;//图片高度
    private int drawableTextDistance = 0;//图片文字间距
    private int drawableResId;//图片资源id

    //要绘制的图片
    private Bitmap drawBitmap;
    private Rect drawBitmapSrcRect;
    private Rect drawBitmapDstRect;
    private Paint drawBitmapPaint = new Paint();

    private boolean  allCaps = false;

    public CustomDrawableButton(Context context) {
        super(context);
        init(context,null,-1);
    }

    public CustomDrawableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs,-1);
    }

    public CustomDrawableButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr){

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CustomDrawableButton);
        drawablePosi = attributes.getInt(R.styleable.CustomDrawableButton_drawablePosi,drawablePosi);
        drawableWidth = attributes.getDimensionPixelOffset(R.styleable.CustomDrawableButton_drawableWidth,drawableWidth);
        drawableHeight = attributes.getDimensionPixelOffset(R.styleable.CustomDrawableButton_drawableHeight,drawableHeight);
        drawableTextDistance = attributes.getDimensionPixelOffset(R.styleable.CustomDrawableButton_drawableTextDistance,drawableTextDistance);
        drawableResId = attributes.getResourceId(R.styleable.CustomDrawableButton_drawableResId,-1);

        setDrawablePosi(drawablePosi,drawableWidth,drawableHeight,drawableTextDistance,drawableResId);

        super.setGravity(Gravity.CENTER);
        setIncludeFontPadding(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setDrawablePosi(drawablePosi,drawableWidth,drawableHeight,drawableTextDistance,drawableResId);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(drawBitmap != null && drawBitmapDstRect != null) {
            canvas.drawBitmap(drawBitmap, drawBitmapSrcRect, drawBitmapDstRect, drawBitmapPaint);
        }
        super.onDraw(canvas);
    }

    /**
     * 设置资源id
     * @param drawableResId
     */
    @SuppressLint("ResourceType")
    public void serShowDrawableResId(@DrawableRes int drawableResId) {
        if(drawableResId > 0) {
            Drawable drawable = getResources().getDrawable(drawableResId);
            drawable.setBounds(0, 0, drawableWidth, drawableHeight);//第一0是距左右边距离，第二0是距上下边距离，第三69长度,第四宽度
            drawBitmap = ((BitmapDrawable)drawable).getBitmap();
            drawBitmapSrcRect = new Rect(0, 0, drawBitmap.getWidth(), drawBitmap.getHeight());
            setDrawablePosi(drawablePosi,drawableWidth,drawableHeight,drawableTextDistance,drawableResId);
            invalidate();
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        setDrawablePosi(drawablePosi,drawableWidth,drawableHeight,drawableTextDistance,drawableResId);
        invalidate();
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        setDrawablePosi(drawablePosi,drawableWidth,drawableHeight,drawableTextDistance,drawableResId);
        invalidate();
    }

    @Override
    public void setGravity(int gravity) {
        super.setGravity(Gravity.CENTER);
    }

    @Override
    public void setAllCaps(boolean allCaps) {
        super.setAllCaps(allCaps);
        this.allCaps = allCaps;
    }

    /**
     * 设置图片显示位置
     * @param drawablePosi
     * @return
     */
    @SuppressLint("ResourceType")
    public synchronized CustomDrawableButton setDrawablePosi(Integer drawablePosi
            , Integer drawableWidth, Integer drawableHeight, Integer drawableTextDistance, @DrawableRes Integer drawableResId) {

        if(drawablePosi != null) {
            this.drawablePosi = drawablePosi;
        }
        if(drawableWidth != null) {
            this.drawableWidth = drawableWidth;
        }
        if(drawableHeight != null) {
            this.drawableHeight = drawableHeight;
        }
        if(drawableTextDistance != null) {
            this.drawableTextDistance = drawableTextDistance;
        }
        if(drawableResId != null) {
            this.drawableResId = drawableResId;
        }else {
            this.drawableResId = drawableResId = -1;
        }

        if(drawableResId > 0) {
            Drawable drawable = getResources().getDrawable(drawableResId);
            drawable.setBounds(0, 0, this.drawableWidth,  this.drawableHeight);//第一0是距左右边距离，第二0是距上下边距离，第三69长度,第四宽度
            drawBitmap = ((BitmapDrawable)drawable).getBitmap();
            drawBitmapSrcRect = new Rect(0, 0, drawBitmap.getWidth(), drawBitmap.getHeight());
        }

        int centerX = getMeasuredWidth() / 2;
        int centerY = getMeasuredHeight() / 2;
        if(centerX <= 0 || centerY <= 0){
            return this;
        }

        int drawableWidthHalf =  this.drawableWidth / 2;
        int drawableHeightHalf =  this.drawableHeight / 2;

        String str = getText().toString();
        if(allCaps){
            str = str.toUpperCase();
        }

        Paint paint = new Paint();
        paint.setTextSize(getTextSize());
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        int textWidthHalf = rect.width() / 2;//文本的宽度
        int textHeightHalf = rect.height() / 2;//文本的高度
        rect = null;
        paint = null;



        switch (this.drawablePosi) {
            case DRAWABLE_POSI_LEFT:
                drawBitmapDstRect = new Rect(centerX - textWidthHalf -  this.drawableTextDistance -  this.drawableWidth
                        ,centerY - drawableHeightHalf
                        ,centerX - textWidthHalf -  this.drawableTextDistance
                        ,centerY + drawableHeightHalf);
                break;
            case DRAWABLE_POSI_TOP:
                drawBitmapDstRect = new Rect(centerX - drawableWidthHalf
                        ,centerY - textHeightHalf -  this.drawableTextDistance -  this.drawableHeight
                        ,centerX + drawableWidthHalf
                        ,centerY - textHeightHalf -  this.drawableTextDistance);
                break;
            case DRAWABLE_POSI_RIGHT:
                drawBitmapDstRect = new Rect(centerX + textWidthHalf +  this.drawableTextDistance
                        ,centerY - drawableHeightHalf
                        ,centerX + textWidthHalf +  this.drawableTextDistance+  this.drawableWidth
                        ,centerY + drawableHeightHalf);
                break;
            case DRAWABLE_POSI_BOTTOM:
                drawBitmapDstRect = new Rect(centerX - drawableWidthHalf
                        ,centerY + textHeightHalf +  this.drawableTextDistance
                        ,centerX + drawableWidthHalf
                        ,centerY + textHeightHalf +  this.drawableTextDistance +  this.drawableHeight);
                break;
            case DRAWABLE_POSI_NONE:
            default:
                drawBitmapDstRect = null;
                break;
        }
        return this;
    }
}
