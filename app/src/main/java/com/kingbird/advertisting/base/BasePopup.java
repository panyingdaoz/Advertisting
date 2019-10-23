package com.kingbird.advertisting.base;

import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kingbird.advertisting.R;
import com.kingbird.advertisting.activity.CustomerKeyboard;
import com.kingbird.advertisting.activity.PasswordEditText;
import com.kingbird.advertisting.utils.Const;
import com.kingbird.advertisting.utils.Plog;
import com.kingbird.advertisting.utils.SpUtil;

import razerdp.basepopup.BasePopupWindow;

import static com.kingbird.advertisting.base.Base.bindDealer;
import static com.kingbird.advertisting.utils.Config.BACK_PASSWORD;

/**
 * 类具体作用
 *
 * @author Administrator
 * @date 2018/12/11/011.
 */
public class BasePopup extends BasePopupWindow implements View.OnClickListener, PasswordEditText.PasswordFullListener
        , CustomerKeyboard.CustomerKeyboardClickListener {

    private PasswordEditText mPassword;
    private CustomerKeyboard mKeyBoard;
    private TextView mEditText;
    private boolean isFirst;
    private String code;
    private Context context;

    public BasePopup(Context context, int width, boolean isFirstStart) {
        super(context);
        Plog.e("对象", context);
        this.context = context;
        isFirst = isFirstStart;
        code = "";
        LinearLayout.LayoutParams pp;
//        RelativeLayout.LayoutParams pDrawLine2;

//        View mDrawLine = findViewById(R.id.draw_line);
        View mDrawLine = findViewById(R.id.Line);
//        mPassword = findViewById(R.id.password_edit_text1);
//        TextView mEditText = findViewById(R.id.exit_text);

        mKeyBoard = findViewById(R.id.custom_key_board);
        TextView textView = findViewById(R.id.exit_text2);
//        RelativeLayout.LayoutParams pp = (RelativeLayout.LayoutParams) mPassword.getLayoutParams();
//        RelativeLayout.LayoutParams pDrawLine = (RelativeLayout.LayoutParams) mDrawLine.getLayoutParams();
//        RelativeLayout.LayoutParams pEditText = (RelativeLayout.LayoutParams) mEditText.getLayoutParams();
        if (isFirst) {
            mEditText = findViewById(R.id.proxyId);
            pp = (LinearLayout.LayoutParams) mEditText.getLayoutParams();
            View mDrawLine2 = findViewById(R.id.Line2);
            LinearLayout.LayoutParams pDrawLine2 = (LinearLayout.LayoutParams) mDrawLine2.getLayoutParams();
            pDrawLine2.width = width;
            mDrawLine2.setLayoutParams(pDrawLine2);
        } else {
            mPassword = findViewById(R.id.password_edit_text);
            pp = (LinearLayout.LayoutParams) mPassword.getLayoutParams();
        }
        RelativeLayout.LayoutParams pDrawLine = (RelativeLayout.LayoutParams) mDrawLine.getLayoutParams();
        RelativeLayout.LayoutParams pEditText = (RelativeLayout.LayoutParams) textView.getLayoutParams();
        pp.width = width;
        pDrawLine.width = width;
        pEditText.width = width;
        if (isFirst) {
            mEditText.setLayoutParams(pp);
            mEditText.setVisibility(View.VISIBLE);
        } else {
            mPassword.setLayoutParams(pp);
        }
        mDrawLine.setLayoutParams(pDrawLine);
        textView.setLayoutParams(pEditText);
        //自动打开输入法
//        setAutoShowInputMethod(mPassword, true);
        setPopupGravity(Gravity.CENTER);
        bindEvent();

    }

    private void bindEvent() {
        mKeyBoard.setOnCustomerKeyboardClickListener(this);
        if (isFirst) {
            mEditText.setOnClickListener(this);
        } else {
            mPassword.setOnClickListener(this);
            mPassword.setOnPasswordFullListener(this);
        }
    }

    /**
     * 必须实现，这里返回您的contentView
     * 为了让库更加准确的做出适配，强烈建议使用createPopupById()进行inflate
     */
    @Override
    public View onCreateContentView() {
        View view;
        boolean isFirst = SpUtil.readBoolean(Const.BASE_POPUP_TYPE);
        if (isFirst) {
            view = createPopupById(R.layout.dialog_proxy_binding);
        } else {
            view = createPopupById(R.layout.dialog_customer_keyboard);
        }
        Plog.e("返回的View", view);
        return view;
    }

    /**
     * 以下为可选代码（非必须实现）
     * 返回作用于PopupWindow的show和dismiss动画，本库提供了默认的几款动画，这里可以自由实现
     */
    @Override
    protected Animation onCreateShowAnimation() {
        return getDefaultScaleAnimation(true);
    }

    @Override
    protected Animation onCreateDismissAnimation() {
        return getDefaultScaleAnimation(false);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.password_edit_text) {
            Plog.e("点击（退出密码）输入框");
        } else if (view.getId() == R.id.proxyId) {
            Plog.e("点击(合伙人)输入框");
        }
    }

    @Override
    public void passwordFull(String password) {
        Plog.e("输入的密码", password);
        if (password.endsWith(BACK_PASSWORD)) {
            mPassword.emptyPassword();
            SpUtil.writeInt(Const.RESTART_APP_TIME, 300);
            Plog.e("密码正确");
            Base.intentActivity("6");
        } else {
            mPassword.emptyPassword();
            Plog.e("密码不正确，请重新输入！");
        }
    }

    @Override
    public boolean onDispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        Plog.e("接收到的按键信息：" + keyCode);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int number = 0;
            switch (keyCode) {
                case 7:
                    number = 0;
                    break;
                case 8:
                    number = 1;
                    break;
                case 9:
                    number = 2;
                    break;
                case 10:
                    number = 3;
                    break;
                case 11:
                    number = 4;
                    break;
                case 12:
                    number = 5;
                    break;
                case 13:
                    number = 6;
                    break;
                case 14:
                    number = 7;
                    break;
                case 15:
                    number = 8;
                    break;
                case 16:
                    number = 9;
                    break;
                case 23:
                    popupBindDealer();
                    break;
                case 67:
                    deleteNumber();
                    break;
                default:
            }
            if (number != 0) {
                addPassword(Integer.toString(number));
            }
        }

        return super.onDispatchKeyEvent(event);
    }

    @Override
    public void click(String number) {
        addPassword(number);
    }

    /**
     * 添加密码
     */
    private void addPassword(String number) {
        if (isFirst) {
            if (number != null) {
                code = code + number;
                if (mEditText != null) {
                    mEditText.setText(code);
                }
            }
        } else {
            if (mPassword != null) {
                mPassword.addPassword(number);
            }
        }
    }

    @Override
    public void delete() {
        deleteNumber();
    }

    /**
     * 删除密码
     */
    private void deleteNumber() {
        if (isFirst) {
            if (code.length() > 0) {
                code = code.substring(0, code.length() - 1);
                Plog.e("删除后的数字", code);
                if (mEditText != null) {
                    mEditText.setText(code);
                }
            }
        } else {
            if (mPassword != null) {
                mPassword.deleteLastPassword();
            }
        }
    }

    @Override
    public void sure(String password) {
        popupBindDealer();
    }

    /**
     * 绑定合伙人
     */
    private void popupBindDealer() {
        if (isFirst) {
            Plog.e("获取到的合伙人ID", code);
            SpUtil.writeString(Const.DEALER_CODE, code);
            bindDealer(context, code);
            dismiss();
        }
    }

    @Override
    public BasePopupWindow setPopupFadeEnable(boolean isPopupFadeAnimate) {
        return super.setPopupFadeEnable(isPopupFadeAnimate);
    }
}