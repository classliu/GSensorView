package com.classliu.gsensorview.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.classliu.gsensorview.R;
import com.classliu.gsensorview.listener.OnTagClickedListener;
import com.classliu.gsensorview.tag.ImageTag;
import com.classliu.gsensorview.util.PhoneAccelHelper;
import com.classliu.gsensorview.widget.GSensorView;


/**
 * 手机界面
 * Created by Cheng on 2017/9/3.
 */

public class MainActivity extends AppCompatActivity implements OnTagClickedListener {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsensorview);
        GSensorView gsensorview = (GSensorView) findViewById(R.id.gsensorview);
        gsensorview.setOnTagClickedListener(this);
        gsensorview.setTagBitmap(R.drawable.icon_key, R.drawable.icon_key/*,R.drawable.icon_key,R.drawable.icon_key,R.drawable.icon_key*/);
        gsensorview.setTags(buildTags());
    }


    @Override
    protected void onResume() {
        super.onResume();
        PhoneAccelHelper.getInstance(this).resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PhoneAccelHelper.getInstance(this).destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PhoneAccelHelper.getInstance(this).pause();
    }

    @Override
    public void onTagClicked(int i, ImageTag tag) {
        Toast.makeText(this, tag.getId() + "", Toast.LENGTH_SHORT).show();
    }

    private int photoTag = 0;

    private ImageTag[]buildTags() {
        ImageTag[] tags = {
                new ImageTag(++photoTag, 0.3F, 0.3F), new ImageTag(++photoTag, 0.8F, 0.3F),
               /* new ImageTag(++photoTag, 0.3F, 0.5F), new ImageTag(++photoTag, 0.8F, 0.5F),
                new ImageTag(++photoTag, 0.3F, 0.8F), new ImageTag(++photoTag, 0.8F, 0.8F)*/
        };
        return tags;
    }

}
