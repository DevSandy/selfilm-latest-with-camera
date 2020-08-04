package com.example.selfilm.helper;

//import android.support.v7.app.ActionBar;
//import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.appcompat.app.ActionBar;

import com.example.selfilm.R;

/**
 * @author LLhon
 * @Project Android-Video-Editor
 * @Package com.marvhong.videoeditor.helper
 * @Date 2018/8/27 11:02
 * @description
 */
public class ToolbarHelper {

    private Toolbar mToolbar;
    private ActionBar mActionBar;

    public ToolbarHelper(Toolbar toolbar, ActionBar actionBar) {
        mToolbar = toolbar;
        mActionBar = actionBar;
    }

    public ToolbarHelper(Toolbar toolbar) {
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public void setTitle(String title) {
        TextView tv = mToolbar.findViewById(R.id.toolbar_title);
        tv.setText(title);
    }

    public void setMenuTitle(String menuTitle, View.OnClickListener listener) {
        TextView tv = mToolbar.findViewById(R.id.toolbar_menu_title);
        tv.setText(menuTitle);
        tv.setOnClickListener(listener);
    }

    /**
     * 隐藏左边返回箭头
     */
    public void hideBackArrow() {
        mActionBar.setDisplayHomeAsUpEnabled(false);
    }
}
