package com.kingbird.advertisting.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.inputmethod.EditorInfo;

import com.kingbird.advertisting.R;

import static com.kingbird.advertisting.utils.Plog.e;

//import com.socks.library.KLog;

/**
 * 密码输入框
 *
 * @author Administrator
 * @date 2018/12/7/007.
 */
public class PasswordEditText extends AppCompatEditText {
    /**
     *  画笔
     */
    private Paint mPaint;
    /**
     *  一个密码所占的宽度
     */
    private int mPasswordItemWidth;
    /**
     *  密码的个数默认为6位数
     */
    private int mPasswordNumber = 6;
    /**
     *  背景边框颜色
     */
    private int mBgColor = Color.parseColor("#d1d2d6");
    /**
     *  背景边框大小
     */
    private int mBgSize = 1;
    /**
     *  密码边框圆角大小
     */
    private int mBgCorner = 0;
    /**
     *  分割线的颜色
     */
    private int mDivisionLineColor = mBgColor;
    /**
     *  分割线的大小
     */
    private int mDivisionLineSize = 1;
    /**
     *  密码圆点的颜色
     */
    private int mPasswordColor = mDivisionLineColor;
    /**
     *  密码圆点的半径大小
     */
    private int mPasswordRadius = 4;

    public PasswordEditText(Context context) {
        this(context, null);
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        initAttributeSet(context, attrs);
        // 设置输入模式是密码
//        setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        // 不显示光标
        setCursorVisible(false);
    }

    /**
     * 初始化属性
     */
    private void initAttributeSet(Context context, AttributeSet attrs) {
        @SuppressLint("CustomViewStyleable")
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PasswordEditText);
        // 获取大小
        mDivisionLineSize = (int) array.getDimension(R.styleable.PasswordEditText_divisionLineSize, dip2px(mDivisionLineSize));
        mPasswordRadius = (int) array.getDimension(R.styleable.PasswordEditText_passwordRadius, dip2px(mPasswordRadius));
        mBgSize = (int) array.getDimension(R.styleable.PasswordEditText_bgSize, dip2px(mBgSize));
        mBgCorner = (int) array.getDimension(R.styleable.PasswordEditText_bgCorner, 0);
        // 获取颜色
        mBgColor = array.getColor(R.styleable.PasswordEditText_bgColor, mBgColor);
        mDivisionLineColor = array.getColor(R.styleable.PasswordEditText_divisionLineColor, mDivisionLineColor);
        mPasswordColor = array.getColor(R.styleable.PasswordEditText_passwordColor, mDivisionLineColor);
        array.recycle();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    /**
     * dip 转 px
     */
    private int dip2px(int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dip, getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int passwordWidth = getWidth() - (mPasswordNumber - 1) * mDivisionLineSize;
        mPasswordItemWidth = passwordWidth / mPasswordNumber;
        // 绘制背景
        drawBg(canvas);
        // 绘制分割线
        drawDivisionLine(canvas);
        // 绘制密码
        drawHidePassword(canvas);

        // 当前密码是不是满了
        if (mListener != null) {
            String password = getText().toString().trim();
            if (password.length() >= mPasswordNumber) {
                mListener.passwordFull(password);
            }
        }
    }

    /**
     * 绘制背景
     */
    private void drawBg(Canvas canvas) {
        mPaint.setColor(mBgColor);
        // 设置画笔为空心
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBgSize);
        RectF rect = new RectF(mBgSize, mBgSize, getWidth() - mBgSize, getHeight() - mBgSize);
        // 如果没有设置圆角，就画矩形
        if (mBgCorner == 0) {
            canvas.drawRect(rect, mPaint);
        } else {
            // 如果有设置圆角就画圆矩形
            e("圆角");
            canvas.drawRoundRect(rect, mBgCorner, mBgCorner, mPaint);
        }
    }

    /**
     * 绘制隐藏的密码
     */
    private void drawHidePassword(Canvas canvas) {
        int passwordLength = getText().length();
        mPaint.setColor(mPasswordColor);
        // 设置画笔为实心
        mPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < passwordLength; i++) {
            int cx = i * mDivisionLineSize + i * mPasswordItemWidth + mPasswordItemWidth / 2 + mBgSize;
            canvas.drawCircle(cx, getHeight() / 2, mPasswordRadius, mPaint);
        }
    }

    /**
     * 绘制分割线
     */
    private void drawDivisionLine(Canvas canvas) {
        mPaint.setStrokeWidth(mDivisionLineSize);
        mPaint.setColor(mDivisionLineColor);
        for (int i = 0; i < mPasswordNumber - 1; i++) {
            int start = (i + 1) * mDivisionLineSize + (i + 1) * mPasswordItemWidth + mBgSize;
            canvas.drawLine(start, mBgSize, start, getHeight() - mBgSize, mPaint);
        }
    }

    /**
     * 添加密码
     */
    public void addPassword(String number) {
        number = getText().toString().trim() + number;
        if (number.length() > mPasswordNumber) {
            return;
        }
        setText(number);
    }

    /**
     * 删除最后一位密码
     */
    public void deleteLastPassword() {
        String currentText = getText().toString().trim();
        if (TextUtils.isEmpty(currentText)) {
            return;
        }
        currentText = currentText.substring(0, currentText.length() - 1);
        setText(currentText);
    }

    /**
     * 清空密码
     */
    public void emptyPassword() {
        String password = getText().toString().trim();
        // 判断当前密码是不是空
        if (TextUtils.isEmpty(password)) {
            return;
        }
        setText(null);
    }

    /**
     * 设置当前密码是否已满的接口回掉
     */
    private PasswordFullListener mListener;

    public void setOnPasswordFullListener(PasswordFullListener listener) {
        this.mListener = listener;
    }

    public interface PasswordFullListener {
        /**
         * 监听
         *
         * @param password 密码
         */
        void passwordFull(String password);
    }
}