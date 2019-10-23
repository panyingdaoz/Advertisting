package com.kingbird.advertisting.activity;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kingbird.advertisting.R;

import static com.kingbird.advertisting.utils.Config.SUER;

/**
 * 自定义键盘类
 *
 * @author Pan
 * @date 2018/12/20/020.
 */
public class CustomerKeyboard extends LinearLayout implements View.OnClickListener {
    private CustomerKeyboardClickListener mListener;

    public CustomerKeyboard(Context context) {
        this(context, null);
    }

    public CustomerKeyboard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomerKeyboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.ui_customer_keyboard, this);
        setChildViewOnclick(this);
    }

    /**
     * 设置键盘子View的点击事件
     */
    private void setChildViewOnclick(ViewGroup parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            // 不断的递归设置点击事件
            View view = parent.getChildAt(i);
            if (view instanceof ViewGroup) {
                setChildViewOnclick((ViewGroup) view);
                continue;
            }
            view.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            // 如果点击的是TextView
            String number = ((TextView) v).getText().toString();
//            KLog.e("获取的内容", number);
            if (!TextUtils.isEmpty(number)) {
                if (mListener != null) {
                    if (number.equals(SUER)) {
                        //成功回调
                        mListener.sure(number);
                    } else {
                        //点击数字回调
                        mListener.click(number);
                    }
                }
            }
        } else if (v instanceof ImageView) {
            // 如果是图片那肯定点击的是删除
            if (mListener != null) {
                mListener.delete();
            }
        }
    }

    /**
     * 设置键盘的点击回调监听
     */
    public void setOnCustomerKeyboardClickListener(CustomerKeyboardClickListener listener) {
        this.mListener = listener;
    }

    /**
     * 点击键盘的回调监听
     */
    public interface CustomerKeyboardClickListener {
        /**
         * 点击监听
         *
         * @param number 点击数字
         */
        void click(String number);

        /**
         * 删除监听
         */
        void delete();

        /**
         * 确定监听
         *
         * @param password 密码
         */
        void sure(String password);
    }
}
