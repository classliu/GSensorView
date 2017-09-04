package com.classliu.gsensorview.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.classliu.gsensorview.listener.OnTagAngleChangedListener;
import com.classliu.gsensorview.listener.OnTagClickedListener;
import com.classliu.gsensorview.tag.ImageTag;
import com.classliu.gsensorview.util.PhoneAccelHelper;


/**
 * 重力感应的view
 * Created by Cheng on 2017/9/2.
 */

public class GSensorView extends View implements OnTagAngleChangedListener {

    private Bitmap[] bitmaps = null;

    //手指点击的位置
    private float fingerX = -1l;
    private float fingerY = -1l;


    /**
     * 临时变量，用于记录标签相对于图片的位置
     */
    private float[] mTempPoint = new float[2];

    /**
     * 标签数组
     */
    protected ImageTag[] mTags = null;

    /**
     * 标签touch区域Rect
     */
    private RectF[] mTouchRects = null;
    private RectF r = new RectF(0, 0, 1080, 900); // 设置当前的显示的位置
    /**
     * 临时变量，用于计算touch区域Rect的值
     */
    private Matrix mTempMatrix = new Matrix();


    private long firstClickTime = 0;
    /**
     * tag点击事件监听
     */
    private OnTagClickedListener mClickListener = null;


    public GSensorView(@NonNull Context context) {
        super(context, null);
    }

    public GSensorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public GSensorView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawImageTags(canvas);
    }

    /**
     * 绘制Tag
     *
     * @param canvas
     */
    protected void drawImageTags(Canvas canvas) {
        if (mTags == null || mTags.length < 1) {
            return;
        }
        if (bitmaps == null || bitmaps.length < 1) {
            return;
        }
        if (bitmaps.length < mTags.length) return;

        for (int i = 0; i < mTags.length; i++) {

            // 计算tag标签在图片上的位置
            float pointX = mTags[i].getX() * r.width();
            float pointY = mTags[i].getY() * r.height();

            // 计算tags的位置(转换成相对于ImageView的位置)
            mTempPoint[0] = pointX + r.left;
            mTempPoint[1] = pointY + r.top;

            canvas.save();
            // 将画布原点移动到要绘制的点
            canvas.translate(mTempPoint[0], mTempPoint[1]);
            // 获取旋转角度
            if (getAccelHelper() != null) {
                canvas.rotate(getAccelHelper().getCurrentAngleDegrees());
            }

            int mTagWidth = bitmaps[i].getWidth();
            int mTagHeight = bitmaps[i].getHeight();

            // 绘制图片
            canvas.drawBitmap(bitmaps[i], 0 - (mTagWidth >> 1), 0, new Paint());
            canvas.restore();

            // 计算Tag的可touch区域
            calculateTouchRectF(i, pointX, pointY, mTagWidth, mTagHeight);
        }
    }

    /**
     * 计算相应Tag的可touch区域
     *
     * @param index
     * @param pointX
     * @param pointY
     */
    private void calculateTouchRectF(int index, float pointX, float pointY, int mTagWidth, int mTagHeight) {
        mTouchRects[index] = new RectF(pointX - (mTagWidth >> 1), pointY, pointX - (mTagWidth >> 1)
                + mTagWidth, pointY + mTagHeight);
        mTempMatrix.reset();
        // 获取旋转角度
        if (getAccelHelper() != null) {
            mTempMatrix.setRotate(getAccelHelper().getCurrentAngleDegrees(), pointX, pointY);
        }
        // 此处其实是一个简单的矩形旋转，借助Matrix类
        mTempMatrix.mapRect(mTouchRects[index], mTouchRects[index]);
    }


    /**
     * @return
     */
    private PhoneAccelHelper getAccelHelper() {
        return PhoneAccelHelper.getInstance(getContext().getApplicationContext());
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getAccelHelper() != null) {
            getAccelHelper().addTagAngleListener(this);
        }
    }

    /**
     * 刷新图片的位置
     */
    @Override
    public void onTagAngleChanged() {
        invalidate();
    }

    /**
     * 设置图标单击事件回调
     *
     * @return
     */
    public void setOnTagClickedListener(OnTagClickedListener onTagClickedListener) {
        this.mClickListener = onTagClickedListener;
    }

    /**
     * 设置当前要显示的图片的个数 >= tags
     * @param res
     */
    public void setTagBitmap(int... res) {
        if (res != null) {
            bitmaps = new Bitmap[res.length];
            for (int i = 0; i < res.length; i++) {
                bitmaps[i] = BitmapFactory.decodeResource(getResources(), res[i]/*,BitmapFactory.Options opts*/);//自行设置
            }
        }
    }

    /**
     * 设置tags
     *
     * @param tags
     */
    public void setTags(ImageTag... tags) {
        mTags = tags;
        if (tags != null) {
            mTouchRects = new RectF[tags.length];
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                fingerX = event.getX();
                fingerY = event.getY();
                if (isCanClick(500)) touchTagListener();
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 点击取down的位置回调
     */
    private void touchTagListener() {
        if (mClickListener != null) {
            for (int i = 0; i < mTouchRects.length; i++) {
                if (mTouchRects[i].contains(fingerX, fingerY)) {
                    mClickListener.onTagClicked(i, mTags[i]);
                }
            }
        }
    }


    /**
     * 点击间隔(防止按钮被连续点击）
     *
     * @param duration
     * @return
     */
    private boolean isCanClick(long duration) {
        if (System.currentTimeMillis() - firstClickTime > duration) {
            firstClickTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
