

> 脱坑指南：告诉女票这是七夕节的礼物,我敢保证这是你的最后一个七夕节。😝


## 上效果图
![](http://ww1.sinaimg.cn/large/77ca6427ly1fj7m1604ebg208w0fs7hf.gif)


## 如何使用
* xml加入布局文件

        <com.classliu.gsensorview.widget.GSensorView
        android:id="@+id/gsensorview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    
* 设置当前显示的图片和绑定当前的图片tag
   ```  
    /**
     * 设置当前要显示的图片的个数 >= tags
     * @param res
     */
    public void setTagBitmap(int... res) {
        if (res != null) {
            bitmaps = new Bitmap[res.length];
            for (int i = 0; i < res.length; i++) {
                bitmaps[i] = BitmapFactory.decodeResource(getResources(), res[i]/*,BitmapFactory.Options opts*/);
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
    ```
### 设置点击的回调
    setOnTagClickedListener(OnTagClickedListener onTagClickedListener)
### 获取当前的旋转角度
PhoneAccelHelper.java

    ```这三个方法的调用
    PhoneAccelHelper.getInstance(this).resume();
    PhoneAccelHelper.getInstance(this).destroy();
    PhoneAccelHelper.getInstance(this).pause();
    ```



桥等玛得，等我画个图，稍后补上
```
/**
     * 计算角度 π/180×角度 <=> 180/π×弧度
     */
    public void recalculateAngle() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Service.WINDOW_SERVICE);
        int i = wm.getDefaultDisplay().getRotation();
        mAccelerationX = kFilteringFactor * x + (1 - kFilteringFactor) * mAccelerationX;
        mAccelerationY = kFilteringFactor * y + (1 - kFilteringFactor) * mAccelerationY;
        mAccelerationZ = kFilteringFactor * z + (1 - kFilteringFactor) * mAccelerationZ;
        double d1 = 0.0D;
        switch (i) {
            case Surface.ROTATION_0:// 手机处于正常状态
                d1 = Math.PI;
                rotation = 180.0F;
                break;
            case Surface.ROTATION_90:// 手机旋转90度
                d1 = Math.PI / 2;
                rotation = 90.0F;
                break;
            case Surface.ROTATION_180:// 手机旋转180°
                d1 = 0D;
                rotation = 0.0F;
                break;
            case Surface.ROTATION_270:// 手机旋转270°
                d1 = 0 - Math.PI / 2;
                rotation = 270.0F;
                break;
            default:
                break;
        }

        //计算当前的弧度 和 角度的转换
        double d2 = 0.0D;
        if ((Math.abs(mAccelerationZ) > 3.5D * Math.abs(mAccelerationX))
                && (Math.abs(mAccelerationZ) > 3.5D * Math.abs(mAccelerationY))) {
            d2 = d1 + tagAngleRadians;
        } else {
            d2 = tagAngleRadians - Math.atan2(-mAccelerationX, -mAccelerationY);
        }
        if (d2 > Math.PI) {
            d2 -= PI_TIMES_2;
        } else if (d2 < 0 - Math.PI) {
            d2 += PI_TIMES_2;
        }
        //计算当前的角速度 （类似单摆的去想）
        angularVelocity = -0.1D * d2 - 0.1D * angularVelocity + angularVelocity;
        tagAngleRadians += angularVelocity;
        tagAngleDegreen = (float) Math.toDegrees(tagAngleRadians) + rotation;
        if (Math.abs(angularVelocity) > angularTolerance) { //左右倾斜的位置大于 0.5弧度 -> 角度
            notifyAngleChanged();
        }
    }
```

### 获取旋转角度
GSensorView.java  20Hz频率的刷新
图片的旋转跟随变动代码
```
            / 计算tag标签在图片上的位置
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
```
计算当前点击的位置在图片上
```
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
.........
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
```
### 总结：
大致就这些，写的比较糙，仅供参考和娱乐，喜欢的点个赞给个star呗。评论给一下修改意见也是极好的， 

[Demo链接](https://github.com/classliu/GSensorView)

如果要深入理解的话可以看一下这篇文章，站在巨人的肩膀上行走

[链接地址](http://www.jianshu.com/p/3dd3d1524851)

![](http://upload-images.jianshu.io/upload_images/2198310-d67971fa521f3f1c.gif?imageMogr2/auto-orient/strip)
