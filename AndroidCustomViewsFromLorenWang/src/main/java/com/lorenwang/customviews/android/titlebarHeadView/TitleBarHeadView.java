package com.lorenwang.customviews.android.titlebarHeadView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lorenwang.customviews.android.R;

/**
 * 创建时间： 2018/10/25 0025 下午 14:51:35
 * 创建人：LorenWang
 * 功能作用：标题操作栏
 * 方法介绍：
 * 思路：1、通过type加载不同的布局
 * 2、传递过来titlebar高度后设置给params
 * 3、自定义的几个xml布局文件
 * 4、标题大小以及颜色需要单独设置
 * 5、type样式布局列表
 * （0）自定义view
 * （1）后退、标题
 * （2）后退、标题、右侧按钮
 * （3）标题、右侧按钮
 * （4）左侧按钮、标题
 * （5）左侧按钮、右侧按钮
 * （6）后退、右侧按钮
 * （7）后退、标题、右侧图标
 * （8）标题、右侧图标
 * （9）后退、右侧图标
 * 修改人：
 * 修改时间：
 * 备注：
 */
public class TitleBarHeadView extends FrameLayout implements View.OnClickListener {
    private final int LAYOUT_TYPE_0 = 0;//（0）自定义view
    private final int LAYOUT_TYPE_1 = 1;//（1）后退、标题
    private final int LAYOUT_TYPE_2 = 2;//（2）后退、标题、右侧按钮
    private final int LAYOUT_TYPE_3 = 3;//（3）标题、右侧按钮
    private final int LAYOUT_TYPE_4 = 4;//（4）左侧按钮、标题
    private final int LAYOUT_TYPE_5 = 5;//（5）左侧按钮、右侧按钮
    private final int LAYOUT_TYPE_6 = 6;//（6）后退、右侧按钮
    private final int LAYOUT_TYPE_7 = 7;//（7）后退、标题、右侧图标
    private final int LAYOUT_TYPE_8 = 8;//（8）标题、右侧图标
    private final int LAYOUT_TYPE_9 = 9;//（9）后退、右侧图标
    private int layoutType = LAYOUT_TYPE_1;//布局类型

    public TitleBarHeadView(Context context) {
        super(context);
        init(context, null, -1);
    }

    public TitleBarHeadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1);
    }

    public TitleBarHeadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /**
     * 初始化
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.TitleBarHeadView);
        layoutType = attributes.getInt(R.styleable.TitleBarHeadView_layoutType, LAYOUT_TYPE_1);
        int customLayout = attributes.getResourceId(R.styleable.TitleBarHeadView_customLayout, -1);


        //根据不同类型，读取不同数据
        View layoutView = getLayoutView(context, customLayout);
        //移除内部所有的view
        removeAllViews();
        //进行view变更
        if (layoutView != null) {
            addView(layoutView);
            //设置view数据
            setLayoutChildView(context,attributes);
        }
    }

    /**
     * 获取布局viwe
     *
     * @param context
     * @param customLayout
     * @return
     */
    private View getLayoutView(Context context, int customLayout) {
        View layoutView;
        switch (layoutType) {
            case LAYOUT_TYPE_0:
                layoutView = LayoutInflater.from(context).inflate(customLayout, null);
                break;
            case LAYOUT_TYPE_2:
                layoutView = LayoutInflater.from(context).inflate(R.layout.title_bar_head_view_type_2, null);
                break;
            case LAYOUT_TYPE_3:
                layoutView = LayoutInflater.from(context).inflate(R.layout.title_bar_head_view_type_3, null);
                break;
            case LAYOUT_TYPE_4:
                layoutView = LayoutInflater.from(context).inflate(R.layout.title_bar_head_view_type_4, null);
                break;
            case LAYOUT_TYPE_5:
                layoutView = LayoutInflater.from(context).inflate(R.layout.title_bar_head_view_type_5, null);
                break;
            case LAYOUT_TYPE_6:
                layoutView = LayoutInflater.from(context).inflate(R.layout.title_bar_head_view_type_6, null);
                break;
            case LAYOUT_TYPE_7:
                layoutView = LayoutInflater.from(context).inflate(R.layout.title_bar_head_view_type_7, null);
                break;
            case LAYOUT_TYPE_8:
                layoutView = LayoutInflater.from(context).inflate(R.layout.title_bar_head_view_type_8, null);
                break;
            case LAYOUT_TYPE_9:
                layoutView = LayoutInflater.from(context).inflate(R.layout.title_bar_head_view_type_9, null);
                break;
            case LAYOUT_TYPE_1:
            default:
                layoutView = LayoutInflater.from(context).inflate(R.layout.title_bar_head_view_type_1, null);
                break;
        }
        return layoutView;
    }

    /**
     * 设置组件属性
     *
     * @param context
     * @param attributes
     */
    private void setLayoutChildView(Context context, TypedArray attributes) {
        switch (layoutType) {
            case LAYOUT_TYPE_0:
                break;
            case LAYOUT_TYPE_2:
                break;
            case LAYOUT_TYPE_3:
                break;
            case LAYOUT_TYPE_4:
                break;
            case LAYOUT_TYPE_5:
                break;
            case LAYOUT_TYPE_6:
                break;
            case LAYOUT_TYPE_7:
                break;
            case LAYOUT_TYPE_8:
                break;
            case LAYOUT_TYPE_9:
                break;
            case LAYOUT_TYPE_1:
            default:
                //设置后退按钮大小
                int backWidthHeight = (int) attributes.getDimension(R.styleable.TitleBarHeadView_backImgWidthHeight, -1);
                if (backWidthHeight > 0) {
                    ViewGroup.LayoutParams layoutParams = findViewById(R.id.imgBtnBack).getLayoutParams();
                    if (layoutParams == null) {
                        layoutParams = new LayoutParams(backWidthHeight, backWidthHeight);
                    } else {
                        layoutParams.width = layoutParams.height = backWidthHeight;
                    }
                    findViewById(R.id.imgBtnBack).setLayoutParams(layoutParams);
                }
                //设置后退按钮颜色
                int backColor = (int) attributes.getColor(R.styleable.TitleBarHeadView_backImgColor, Color.BLACK);
                findViewById(R.id.imgBtnBack).getBackground().setTint(backColor);
                //设置标题
                ((TextView)findViewById(R.id.tvTitle)).setTextColor(attributes.getColor(R.styleable.TitleBarHeadView_titleTextColor, Color.BLACK));
                ((TextView)findViewById(R.id.tvTitle)).setTextSize(TypedValue.COMPLEX_UNIT_DIP,attributes.getDimensionPixelSize(R.styleable.TitleBarHeadView_titleTextSize, 20));
                ((TextView)findViewById(R.id.tvTitle)).setText(attributes.getString(R.styleable.TitleBarHeadView_titleText));
                break;
        }
        //设置背景颜色
        int viewBgColor = attributes.getColor(R.styleable.TitleBarHeadView_viewBgColor, Color.WHITE);
        setBackgroundColor(viewBgColor);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.fmBack || i == R.id.imgBtnBack) {
            //后退按钮
        } else if (i == R.id.tvTitle) {
            //标题点击
        }
    }
}
