package com.canyinghao.candialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ArrayRes;
import android.support.annotation.IntRange;
import android.support.annotation.IntegerRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.canyinghao.cananimation.CanAnimation;
import com.canyinghao.caneffect.ViewAnimationUtils;
import com.wnafee.vector.compat.ResourcesCompat;
import com.wnafee.vector.compat.Tintable;


/**
 * Copyright 2016 canyinghao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public final class CanDialog extends FrameLayout {

    //  dialog类型
    public static final byte DIALOG_MSG = 0;
    public static final byte DIALOG_LIST_ITEM = 1;
    public static final byte DIALOG_SINGLE_CHOICE = 2;
    public static final byte DIALOG_MULTI_CHOICE = 3;
    public static final byte DIALOG_EDIT = 4;
    public static final byte DIALOG_PROGRESS = 5;
    public static final byte DIALOG_CUSTOM = 6;

    //  svg图标类型
    public static final byte ICON_SUCCESS = 11;
    public static final byte ICON_DANGER = 12;
    public static final byte ICON_INFO = 13;
    public static final byte ICON_WARNING = 14;


    //  svg图标动画类型
    public static final byte ANIM_INFO_SUCCESS = 21;
    public static final byte ANIM_INFO_DANGER = 22;
    public static final byte ANIM_INFO_WARNING = 23;


    //  dialog当前类型
    private byte mType;
    //  svg图标当前类型
    private byte mIconType;
    //  多选时存储选择的状态
    private int checkedItem;
    //  单选时存储选中的状态
    private boolean[] checkedItems;

    private Button mButtonPositive;

    private Button mButtonNegative;

    private Button mButtonNeutral;


    private LinearLayout mTopPanel;
    private FrameLayout mContentPanel;
    private FrameLayout mCustomPanel;
    private LinearLayout mButtonPanel;
    private TextView mAlertTitle;
    private ImageView mIcon;
    private ScrollView mScrollView;
    private TextView mMessage;
    private View textSpacerNoButtons;

    private FrameLayout mCustom;

    //  添加到getWindow().getDecorView()的布局，用以显示黑色半透明背景，添加此dialog等
    private FrameLayout animLayout;

    //  dialog所在的activity
    private Activity mContext;
    //  dialog自己
    private CanDialog mDialog;
    //  是否可取消，为false时，点击back键也不能取消
    private boolean mCancelable = true;
    //  animLayout的颜色
    private int mFullBackgroundColor;
    //    是否设置过animLayout的背景
    private boolean mHaveFullBackgroundColor;
    //  animLayout的背景
    private int mFullBackgroundResid;
    //  是否处于显示状态
    private boolean isShowing;

    //   显示动画
    private Animator mAnimatorStart;
    //    消失动画
    private Animator mAnimatorEnd;

    //  用来监听view是否测量完毕
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

    //   消失时监听
    private CanDialogInterface.OnDismissListener mOnDismissListener;
    //    显示时监听
    private CanDialogInterface.OnShowListener mOnShowListener;
    //    按键监听
    private CanDialogInterface.OnKeyListener mOnKeyListener;
    //    显示或消失动画是否正在播放中
    private boolean isAnimatorPlaying;
    //   消失的点击事件
    private OnClickListener dismissListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {

            dismiss();

        }
    };

    //   显示或消失动画的监听事件
    private AnimatorListenerAdapter animatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);
            isAnimatorPlaying = false;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            isAnimatorPlaying = false;
        }
    };


    private CanDialog(Activity context) {
        this(context, null);
    }

    private CanDialog(Activity context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private CanDialog(Activity context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        onCrate();
    }


    private void onCrate() {

        mDialog = this;
        LayoutInflater.from(mContext).inflate(R.layout.dialog_layout, this);

        setBackgroundResource(R.drawable.white_shape);

        setOnClickListener(null);


        mTopPanel = (LinearLayout) findViewById(R.id.topPanel);

        mContentPanel = (FrameLayout) findViewById(R.id.contentPanel);
        mCustomPanel = (FrameLayout) findViewById(R.id.customPanel);
        mButtonPanel = (LinearLayout) findViewById(R.id.buttonPanel);


        mAlertTitle = (TextView) findViewById(R.id.alertTitle);
        mIcon = (ImageView) findViewById(android.R.id.icon);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mMessage = (TextView) findViewById(android.R.id.message);
        textSpacerNoButtons = findViewById(R.id.textSpacerNoButtons);


        mCustom = (FrameLayout) findViewById(R.id.custom);


        mButtonPositive = (Button) findViewById(android.R.id.button1);
        mButtonNegative = (Button) findViewById(android.R.id.button2);
        mButtonNeutral = (Button) findViewById(android.R.id.button3);


        mIcon.setVisibility(View.GONE);
        mContentPanel.setVisibility(View.GONE);
        mCustomPanel.setVisibility(View.GONE);
        mButtonPanel.setVisibility(View.GONE);

        mButtonPositive.setVisibility(View.GONE);
        mButtonNegative.setVisibility(View.GONE);
        mButtonNeutral.setVisibility(View.GONE);


    }


    private void setType(byte type) {

        this.mType = type;

    }


    public void setAnimatorStart(Animator mAnimatorStart) {
        this.mAnimatorStart = mAnimatorStart;
    }

    public void setAnimatorStart(Runnable runnable) {
        this.mAnimatorStart = CanAnimation.run(runnable);
    }

    public void setAnimatorEnd(Animator mAnimatorEnd) {
        this.mAnimatorEnd = mAnimatorEnd;
    }

    public void setAnimatorEnd(Runnable runnable) {
        this.mAnimatorEnd = CanAnimation.run(runnable);
    }


    public void setIcon(int resId) {

        mIcon.setVisibility(View.VISIBLE);
        mIcon.setImageResource(resId);
    }

    public void setIcon(Drawable icon) {
        mIcon.setVisibility(View.VISIBLE);
        mIcon.setImageDrawable(icon);
    }

    public void setTitle(int rid) {

        mAlertTitle.setText(rid);
    }

    public void setTitle(CharSequence title) {

        mAlertTitle.setText(title);
    }


    public void setMessage(int rid) {

        setMessage(mContext.getText(rid));
    }

    public void setMessage(CharSequence title) {

        setType(DIALOG_MSG);

        mContentPanel.setVisibility(View.VISIBLE);


        for (int i = 0; i < mContentPanel.getChildCount(); i++) {

            View v = mContentPanel.getChildAt(i);
            if (v != mScrollView) {
                mContentPanel.removeView(v);
            }

        }
        mScrollView.setVisibility(View.VISIBLE);
        mMessage.setText(title);
    }


    /**
     * button的点击事件可返回点击item的position，text，单选中的position，多选的选择状态等。
     *
     * @param button
     * @param text
     * @param dismiss
     * @param listener
     */
    private void setButton(Button button, CharSequence text, final boolean dismiss, final CanDialogInterface.OnClickListener listener) {
        mButtonPanel.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
        button.setText(text);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                hideSoftInput();

                if (listener != null) {

                    switch (mType) {
                        case DIALOG_EDIT:
                            final EditText etPwd = (EditText) findViewById(R.id.et_pwd);
                            listener.onClick(mDialog, 0, etPwd.getText().toString(), null);
                            break;

                        case DIALOG_SINGLE_CHOICE:
                            listener.onClick(mDialog, checkedItem, null, null);
                            break;

                        case DIALOG_MULTI_CHOICE:
                            listener.onClick(mDialog, 0, null, checkedItems);
                            break;
                    }

                }


                if (dismiss) {
                    dismiss();
                }


            }
        });


    }


    public void setPositiveButton(int textId, boolean dismiss, CanDialogInterface.OnClickListener listener) {


        setPositiveButton(mContext.getText(textId), dismiss, listener);


    }


    public void setPositiveButton(CharSequence text, boolean dismiss, CanDialogInterface.OnClickListener listener) {
        setButton(mButtonPositive, text, dismiss, listener);


    }

    public void setNegativeButton(int textId, boolean dismiss, CanDialogInterface.OnClickListener listener) {

        setNegativeButton(mContext.getText(textId), dismiss, listener);


    }

    public void setNegativeButton(CharSequence text, boolean dismiss, CanDialogInterface.OnClickListener listener) {


        setButton(mButtonNegative, text, dismiss, listener);


    }

    public void setNeutralButton(int textId, boolean dismiss, CanDialogInterface.OnClickListener listener) {

        setNeutralButton(mContext.getText(textId), dismiss, listener);


    }

    public void setNeutralButton(CharSequence text, boolean dismiss, CanDialogInterface.OnClickListener listener) {

        setButton(mButtonNeutral, text, dismiss, listener);

    }


    /**
     * 设置是否可取消
     *
     * @param cancelable
     */
    public void setCancelable(boolean cancelable) {

        this.mCancelable = cancelable;
    }


    /**
     * 自定义Title
     *
     * @param customTitleView
     */
    public void setCustomTitle(View customTitleView) {

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mTopPanel.addView(customTitleView, 0, lp);

        findViewById(R.id.title_template).setVisibility(View.GONE);

    }


    public void setOnDismissListener(CanDialogInterface.OnDismissListener onDismissListener) {

        this.mOnDismissListener = onDismissListener;
    }

    public void setOnShowListener(CanDialogInterface.OnShowListener onShowListener) {

        this.mOnShowListener = onShowListener;
    }

    public void setOnKeyListener(CanDialogInterface.OnKeyListener onKeyListener) {

        this.mOnKeyListener = onKeyListener;
    }


    public void setView(int layoutResId) {

        View customView = LayoutInflater.from(mContext).inflate(layoutResId, null);

        setView(customView);

    }

    /**
     * 设置自定义view
     *
     * @param customView
     */
    public void setView(View customView) {

        setType(DIALOG_CUSTOM);
        mCustomPanel.setVisibility(View.VISIBLE);
        mContentPanel.setVisibility(View.GONE);
        mCustom.addView(customView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setView(View customView, int viewSpacingLeft, int viewSpacingTop,
                        int viewSpacingRight, int viewSpacingBottom) {

        setView(customView);

        mCustom.setPadding(
                viewSpacingLeft, viewSpacingTop, viewSpacingRight, viewSpacingBottom);
    }


    public void setItems(int itemsId, CanDialogInterface.OnClickListener listener) {

        setItems(mContext.getResources().getTextArray(itemsId), listener);
    }


    public void setItems(final CharSequence[] mItems, final CanDialogInterface.OnClickListener listener) {

        setType(DIALOG_LIST_ITEM);
        final ListView listView = (ListView) LayoutInflater.from(mContext).inflate(R.layout.dialog_list, null);


        CheckedItemAdapter adapter = new CheckedItemAdapter(mContext, R.layout.dialog_item, android.R.id.text1, mItems);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                if (listener != null) {

                    listener.onClick(mDialog, position, mItems[position], null);

                }
            }
        });
        showListOrEditView(listView);
    }

    public void setSingleChoiceItems(int itemsId, int checkedItem,
                                     CanDialogInterface.OnClickListener listener) {

        setSingleChoiceItems(mContext.getResources().getTextArray(itemsId), checkedItem, listener);
    }


    public void setSingleChoiceItems(final CharSequence[] mItems, int checkedItem,
                                     final CanDialogInterface.OnClickListener listener) {

        setType(DIALOG_SINGLE_CHOICE);

        mDialog.checkedItem = checkedItem;

        final ListView listView = (ListView) LayoutInflater.from(mContext).inflate(R.layout.dialog_list, null);


        CheckedItemAdapter adapter = new CheckedItemAdapter(mContext, R.layout.dialog_singchoice, android.R.id.text1, mItems);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                mDialog.checkedItem = position;
                if (listener != null) {
                    listener.onClick(mDialog, position, mItems[position], null);
                }
            }
        });

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setItemChecked(checkedItem, true);
        showListOrEditView(listView);
    }


    public void setMultiChoiceItems(int itemsId, final boolean[] checkedItems,
                                    final CanDialogInterface.OnMultiChoiceClickListener listener) {

        setMultiChoiceItems(mContext.getResources().getTextArray(itemsId), checkedItems, listener);

    }

    public void setMultiChoiceItems(CharSequence[] items, final boolean[] checkedItems,
                                    final CanDialogInterface.OnMultiChoiceClickListener listener) {

        setType(DIALOG_MULTI_CHOICE);

        mDialog.checkedItems = checkedItems;

        final ListView listView = (ListView) LayoutInflater.from(mContext).inflate(R.layout.dialog_list, null);

        ArrayAdapter adapter = new ArrayAdapter<CharSequence>(
                mContext, R.layout.dialog_multichoice, android.R.id.text1, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (checkedItems != null) {
                    boolean isItemChecked = checkedItems[position];
                    if (isItemChecked) {
                        listView.setItemChecked(position, true);
                    }
                }
                return view;
            }
        };

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {


                if (checkedItems != null) {
                    checkedItems[position] = listView.isItemChecked(position);
                    mDialog.checkedItems = checkedItems;
                }
                if (listener != null) {
                    listener.onClick(
                            mDialog, position, listView.isItemChecked(position));
                }

            }
        });


        showListOrEditView(listView);


    }

    public void setFullBackgroundColor(int color) {
        mHaveFullBackgroundColor = true;
        this.mFullBackgroundColor = color;

    }

    public void setFullBackgroundResource(int rid) {
        this.mFullBackgroundResid = rid;

    }


    /**
     * 设置成输入框dialog
     *
     * @param hintText
     * @param isPwd
     * @param minLength
     * @param eyeColor
     */
    public void setEditDialog(String hintText, boolean isPwd, final int minLength, final int eyeColor) {

        setType(DIALOG_EDIT);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_edit, null);

        final EditText etPwd = (EditText) view.findViewById(R.id.et_pwd);
        final ImageView eye = (ImageView) view.findViewById(R.id.eye);

        if (isPwd) {
            eye.setVisibility(View.VISIBLE);

            final Drawable eyeClose = ResourcesCompat.getDrawable(mContext, R.drawable.svg_eyeclose);
            if (eyeColor != 0) {
                applyTint(eyeClose, eyeColor);
            }
            eye.setImageDrawable(ResourcesCompat.getDrawable(mContext, R.drawable.svg_eyeclose));


            eye.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getTag() == null) {


                        final Drawable drawable = ResourcesCompat.getDrawable(mContext, R.drawable.svg_animated_eyeclose2eye);
                        if (eyeColor != 0) {
                            applyTint(drawable, eyeColor);
                        }
                        eye.setImageDrawable(drawable);
                        beginAnimation(drawable);


                        // 文本正常显示

                        etPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);


                        Editable etable = etPwd.getText();

                        Selection.setSelection(etable, etable.length());

                        v.setTag("");
                    } else {

                        final Drawable drawable = ResourcesCompat.getDrawable(mContext, R.drawable.svg_animated_eye2eyeclose);
                        if (eyeColor != 0) {
                            applyTint(drawable, eyeColor);
                        }
                        eye.setImageDrawable(drawable);
                        beginAnimation(drawable);


                        etPwd.setInputType(InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                        // 下面两行代码实现: 输入框光标一直在输入文本后面

                        Editable etable = etPwd.getText();

                        Selection.setSelection(etable, etable.length());

                        v.setTag(null);
                    }
                }
            });


        } else {
            eye.setVisibility(View.GONE);
            etPwd.setInputType(InputType.TYPE_CLASS_TEXT);

        }

        if (minLength > 0) {
            mButtonPositive.setEnabled(false);
            etPwd.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    if (charSequence.length() >= minLength) {
                        mButtonPositive.setEnabled(true);
                    } else {
                        mButtonPositive.setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        } else {
            mButtonPositive.setEnabled(true);
        }


        etPwd.setHint(hintText);

        showListOrEditView(view);


    }


    /**
     * 设置加载的dialog
     *
     * @param centerText
     * @param loadText
     */
    public void setProgress(String centerText, String loadText) {

        setType(DIALOG_PROGRESS);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_progress, null);

        TextView tvCenter = (TextView) view.findViewById(R.id.tv_center);
        TextView tv_load = (TextView) view.findViewById(R.id.tv_load);
        tvCenter.setText(centerText);
        tv_load.setText(loadText);
        showListOrEditView(view);


        hideButtons();
        hideTitle();
        setBackgroundColor(Color.TRANSPARENT);
        setFullBackgroundColor(Color.TRANSPARENT);

    }

    /**
     * 设置自定义加载的dialog
     *
     * @param customView
     */
    public void setProgressCustomView(View customView) {

        setType(DIALOG_PROGRESS);
        showListOrEditView(customView);

        hideButtons();
        hideTitle();
        setBackgroundColor(Color.TRANSPARENT);
        setFullBackgroundColor(Color.TRANSPARENT);

    }

    /**
     * 隐藏标题栏
     */
    public void hideTitle() {

        mTopPanel.setVisibility(View.GONE);

    }

    /**
     * 隐藏button栏
     */
    public void hideButtons() {

        mButtonPanel.setVisibility(View.GONE);

    }

    /**
     * 如果是list、单选、多选、输入框、加载时隐藏默认的msg栏
     *
     * @param view
     */
    private void showListOrEditView(View view) {

        if (view != null) {
            mContentPanel.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
            mContentPanel.addView(view);


        }
    }


    /**
     * 显示
     */
    public void show() {


        isShowing = true;
        if (mButtonPanel.getVisibility() == View.GONE) {

            textSpacerNoButtons.setVisibility(View.VISIBLE);
        }


        FrameLayout layout = createAnimLayout();
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.leftMargin = InputUtils.dp2px(mContext, 20);
        params.rightMargin = InputUtils.dp2px(mContext, 20);
        layout.addView(this, params);


        if (mAnimatorStart != null) {

            try {
                mAnimatorStart.start();
            } catch (Exception e) {
                e.printStackTrace();
                isAnimatorPlaying = false;
                setVisibility(View.VISIBLE);
            }

        }
        if (mOnShowListener != null) {

            mOnShowListener.onShow(mDialog);
        }


    }

    /**
     * 取消
     */
    public void dismiss() {


        if (mAnimatorEnd != null) {
            try {

                mAnimatorEnd.start();
            } catch (Exception e) {
                e.printStackTrace();
                isAnimatorPlaying = false;
                dismissAll();

            }

        } else {
            dismissAll();

        }


    }

    /**
     * 无动画的取消
     */
    private void dismissAll() {
        isShowing = false;
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(mDialog);
        }


        removeAnimLayout();

    }

    /**
     * 是否显示中
     *
     * @return
     */
    public boolean isShow() {

        return isShowing;
    }


    /**
     * 隐藏软键盘
     */
    private void hideSoftInput() {

        if (mType != DIALOG_EDIT) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(findViewById(R.id.et_pwd).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


    }

    /**
     * 创建animLayout层
     *
     * @return
     */
    private FrameLayout createAnimLayout() {
        ViewGroup rootView = (ViewGroup) mContext.getWindow().getDecorView();


        if (animLayout != null) {


            if (mType != DIALOG_PROGRESS) {

                if (mCancelable) {
                    animLayout.setOnClickListener(dismissListener);
                } else {
                    animLayout.setOnClickListener(null);
                }
            }


            return animLayout;

        }
        animLayout = new FrameLayout(mContext);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);


        animLayout.setLayoutParams(params);
        if (mHaveFullBackgroundColor) {
            animLayout.setBackgroundColor(mFullBackgroundColor);
        } else if (mFullBackgroundResid != 0) {
            animLayout.setBackgroundResource(mFullBackgroundResid);
        } else {
            animLayout.setBackgroundColor(Color.parseColor("#77000000"));
        }


        if (mType != DIALOG_PROGRESS) {

            if (mCancelable) {
                animLayout.setOnClickListener(dismissListener);
            } else {
                animLayout.setOnClickListener(null);
            }
        }


        rootView.addView(animLayout);


        animLayout.setFocusable(true);
        animLayout.setFocusableInTouchMode(true);
        animLayout.requestFocus();
        animLayout.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    if (mCancelable) {
                        dismiss();
                    }
                }

                if (mOnKeyListener != null) {
                    return mOnKeyListener.onKey(mDialog, i, keyEvent);
                }
                return true;
            }
        });

        InputUtils.assist(animLayout);
        return animLayout;
    }


    /**
     * 移出animLayout层
     */
    private void removeAnimLayout() {

        ViewGroup rootView = (ViewGroup) mContext.getWindow().getDecorView();

        if (animLayout != null) {
            rootView.removeView(animLayout);
            animLayout = null;
        }

    }

    private static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        public CheckedItemAdapter(Context context, int resource, int textViewResourceId,
                                  CharSequence[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }


    /**
     * CircularReveal动画的类型
     */
    public enum CircularRevealStatus {

        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT

    }


    /**
     * 获取CircularReveal动画
     *
     * @param isShow
     * @param status
     * @return
     */
    private Animator getCircularRevealAnimator(final boolean isShow, final CircularRevealStatus status) {


        return CanAnimation.run(new Runnable() {
            @Override
            public void run() {

                if (isAnimatorPlaying) {
                    return;
                }
                isAnimatorPlaying = true;

                if (onGlobalLayoutListener != null) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
                    onGlobalLayoutListener = null;
                }
                mContentPanel.setTag(null);

                if (isShow) {
                    setVisibility(View.INVISIBLE);
                    onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (mContentPanel.getTag() == null) {
                                setVisibility(View.VISIBLE);


                                int radius = getCircularRadius();

                                int[] point = getCircularPoint(status);

                                final Animator animator = ViewAnimationUtils.createCircularReveal(mDialog, point[0], point[1], 0, radius);
                                animator.setDuration(1000);
                                animator.addListener(animatorListener);
                                animator.start();
                                mContentPanel.setTag("");
                            }

                        }
                    };

                    getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);


                } else {

                    int radius = getCircularRadius();

                    int[] point = getCircularPoint(status);

                    final Animator animator = ViewAnimationUtils.createCircularReveal(mDialog, point[0], point[1], radius, 0);
                    animator.setDuration(1000);
                    animator.addListener(animatorListener);
                    CanAnimation.animationSequence(animator, CanAnimation.run(new Runnable() {
                        @Override
                        public void run() {
                            dismissAll();
                        }
                    })).start();


                }
            }
        });


    }

    /**
     * 获取CircularReveal动画半径
     *
     * @return
     */
    private int getCircularRadius() {
        int h = getMeasuredHeight();

        int w = getMeasuredWidth();

        double pow = Math.pow(h, 2) + Math.pow(w, 2);

        return (int) Math.sqrt(pow);
    }


    /**
     * 获取CircularReveal动画开始点
     *
     * @param status
     * @return
     */
    private int[] getCircularPoint(CircularRevealStatus status) {
        int h = getMeasuredHeight();


        int w = getMeasuredWidth();

        int[] point = new int[]{0, 0};


        switch (status) {
            case TOP_LEFT:
                break;

            case TOP_RIGHT:
                point[0] = w;
                break;
            case BOTTOM_LEFT:
                point[1] = h;
                break;

            case BOTTOM_RIGHT:
                point[0] = w;
                point[1] = h;
                break;

        }

        return point;
    }


    /**
     * 磁块动画
     *
     * @param isShow
     * @return
     */
    private Animator getTileAnimator(final boolean isShow) {


        return CanAnimation.run(new Runnable() {
            @Override
            public void run() {


                if (isAnimatorPlaying) {
                    return;
                }
                isAnimatorPlaying = true;

                if (onGlobalLayoutListener != null) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
                    onGlobalLayoutListener = null;
                }
                mContentPanel.setTag(null);

                if (isShow) {
                    setVisibility(View.INVISIBLE);

                    onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (mContentPanel.getTag() == null) {
                                Animator animator = CanAnimation.animationTogether(TileAnimation.show(mContext, mDialog, isShow));
                                animator.addListener(animatorListener);
                                animator.start();
                                mContentPanel.setTag("");
                            }

                        }
                    };

                    getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);


                } else {

                    final Animator animatorTile = CanAnimation.animationTogether(TileAnimation.show(mContext, mDialog, isShow));


                    onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (mContentPanel.getTag() == null) {
                                animatorTile.addListener(animatorListener);

                                CanAnimation.animationSequence(
                                        animatorTile,
                                        CanAnimation.run(new Runnable() {
                                            @Override
                                            public void run() {
                                                dismissAll();
                                            }
                                        })

                                ).start();
                                mContentPanel.setTag("");
                            }

                        }
                    };

                    getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);


                }


            }
        });


    }


    /**
     * dialog改变成msg类型，并播放icon的动画
     * 自有ICON_INFO类型可以改变成其它类型
     *
     * @param animType
     * @param message
     */
    public void setAnimationMessage(@IntRange(from = ANIM_INFO_SUCCESS, to = ANIM_INFO_WARNING) byte animType, String message) {

        setMessage(message);

        if (mIconType != ICON_INFO) {
            return;
        }

        int drawableId = 0;
        switch (animType) {
            case ANIM_INFO_SUCCESS:

                drawableId = R.drawable.svg_animated_info2success;

                mIconType = ICON_SUCCESS;

                break;
            case ANIM_INFO_DANGER:
                drawableId = R.drawable.svg_animated_info2danger;

                mIconType = ICON_DANGER;
                break;
            case ANIM_INFO_WARNING:
                drawableId = R.drawable.svg_animated_info2warning;

                mIconType = ICON_WARNING;
                break;

            default:

                break;
        }

        if (drawableId == 0) {
            return;
        }


        final Drawable drawable = ResourcesCompat.getDrawable(mContext, drawableId);

        setIconDrawable(drawable);

        beginAnimation(drawable);


    }

    /**
     * 播放svg动画
     *
     * @param drawable
     * @return
     */
    private boolean beginAnimation(Drawable drawable) {
        if (drawable != null && drawable instanceof Animatable) {
            ((Animatable) drawable).start();


            return true;
        }
        return false;
    }

    public void setIconType(byte type) {
        setIconType(type, 0);
    }

    /**
     * 设置icon的类型
     *
     */
    public void setIconType(byte type, int color) {

        this.mIconType = type;

        int drawableId = 0;
        switch (mIconType) {
            case ICON_SUCCESS:

                drawableId = R.drawable.svg_success;

                break;
            case ICON_DANGER:
                drawableId = R.drawable.svg_danger;
                break;
            case ICON_INFO:
                drawableId = R.drawable.svg_info;
                break;
            case ICON_WARNING:
                drawableId = R.drawable.svg_warning;
                break;
            default:

                break;
        }

        if (drawableId != 0) {
            Drawable drawable = ResourcesCompat.getDrawable(mContext, drawableId);

            setIconDrawable(drawable, color);


        }


    }

    private void setIconDrawable(Drawable drawable) {
        setIconDrawable(drawable, 0);
    }

    /**
     * 设置icon的drawable和颜色
     *
     * @param drawable
     * @param color
     */
    private void setIconDrawable(Drawable drawable, int color) {
        if (color != 0) {
            applyTint(drawable, color);
        }
        int dp_20 = InputUtils.dp2px(mContext, 20);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mIcon.getLayoutParams();
        params.height = dp_20;
        params.width = dp_20;
        params.rightMargin = InputUtils.dp2px(mContext, 8);
        mIcon.setLayoutParams(params);
        setIcon(drawable);
    }


    /**
     * 设置svg图标颜色
     *
     * @param d
     * @param color
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void applyTint(Drawable d, int color) {

        ColorStateList colorList = ColorStateList.valueOf(color);
        if (ResourcesCompat.LOLLIPOP) {

            d = d.mutate();

            d.setTintList(colorList);


            d.setTintMode(PorterDuff.Mode.SRC_IN);


        } else if (d instanceof Tintable) {

            d = d.mutate();
            Tintable tintable = (Tintable) d;

            tintable.setTintList(colorList);


            tintable.setTintMode(PorterDuff.Mode.SRC_IN);

        } else {

            int colorF = colorList.getColorForState(getDrawableState(), Color.TRANSPARENT);
            d.setColorFilter(colorF, PorterDuff.Mode.SRC_IN);

        }

    }


    public static class Builder {


        private CanDialog mDialog;

        public Builder(Activity context) {

            mDialog = new CanDialog(context);
        }


        public Builder setIcon(@IntegerRes int resId) {
            mDialog.setIcon(resId);

            return this;
        }

        public Builder setIcon(Drawable icon) {
            mDialog.setIcon(icon);

            return this;
        }

        public Builder setTitle(@StringRes int rid) {

            mDialog.setTitle(rid);

            return this;
        }

        public Builder setTitle(CharSequence title) {

            mDialog.setTitle(title);

            return this;
        }

        public Builder setMessage(@StringRes int rid) {
            mDialog.setMessage(rid);

            return this;
        }

        public Builder setMessage(CharSequence message) {
            mDialog.setMessage(message);


            return this;
        }


        public Builder setPositiveButton(@StringRes int textId, boolean dismiss, CanDialogInterface.OnClickListener listener) {
            mDialog.setPositiveButton(textId, dismiss, listener);

            return this;
        }

        public Builder setPositiveButton(CharSequence text, boolean dismiss, CanDialogInterface.OnClickListener listener) {
            mDialog.setPositiveButton(text, dismiss, listener);

            return this;
        }


        public Builder setNegativeButton(@StringRes int textId, boolean dismiss, CanDialogInterface.OnClickListener listener) {
            mDialog.setNegativeButton(textId, dismiss, listener);

            return this;
        }

        public Builder setNegativeButton(CharSequence text, boolean dismiss, CanDialogInterface.OnClickListener listener) {
            mDialog.setNegativeButton(text, dismiss, listener);


            return this;
        }


        public Builder setNeutralButton(@StringRes int textId, boolean dismiss, CanDialogInterface.OnClickListener listener) {
            mDialog.setNeutralButton(textId, dismiss, listener);
            return this;
        }

        public Builder setNeutralButton(CharSequence text, boolean dismiss, CanDialogInterface.OnClickListener listener) {
            mDialog.setNeutralButton(text, dismiss, listener);

            return this;
        }

        public Builder setCustomTitle(View customTitleView) {
            mDialog.setCustomTitle(customTitleView);

            return this;
        }


        public Builder setView(@LayoutRes int layoutResId) {

            mDialog.setView(layoutResId);

            return this;

        }


        public Builder setView(View customView) {
            mDialog.setView(customView);

            return this;
        }


        public Builder setItems(@ArrayRes int itemsId, CanDialogInterface.OnClickListener listener) {

            mDialog.setItems(itemsId, listener);

            return this;
        }

        public Builder setItems(CharSequence[] items, CanDialogInterface.OnClickListener listener) {

            mDialog.setItems(items, listener);

            return this;
        }


        public Builder setSingleChoiceItems(@ArrayRes int mItemsId, int checkedItem,
                                            CanDialogInterface.OnClickListener listener) {
            mDialog.setSingleChoiceItems(mItemsId, checkedItem, listener);

            return this;
        }

        public Builder setSingleChoiceItems(CharSequence[] mItems, int checkedItem,
                                            CanDialogInterface.OnClickListener listener) {
            mDialog.setSingleChoiceItems(mItems, checkedItem, listener);

            return this;
        }


        public Builder setMultiChoiceItems(@ArrayRes int mItemsId, final boolean[] checkedItems,
                                           final CanDialogInterface.OnMultiChoiceClickListener listener) {

            mDialog.setMultiChoiceItems(mItemsId, checkedItems, listener);
            return this;
        }


        public Builder setMultiChoiceItems(CharSequence[] items, final boolean[] checkedItems,
                                           final CanDialogInterface.OnMultiChoiceClickListener listener) {
            mDialog.setMultiChoiceItems(items, checkedItems, listener);
            return this;
        }


        public Builder setEditDialog(String hintText, boolean isPwd, final int minLength, int eyeColor) {


            mDialog.setEditDialog(hintText, isPwd, minLength, eyeColor);

            return this;
        }


        public Builder setProgress(String centerText, String loadText) {

            mDialog.setProgress(centerText, loadText);


            return this;
        }

        public Builder setProgressCustomView(View customView) {

            mDialog.setProgressCustomView(customView);

            return this;
        }


        public Builder setFullBackgroundColor(int color) {
            mDialog.setFullBackgroundColor(color);
            return this;
        }

        public Builder setFullBackgroundResource(@IntegerRes int rid) {
            mDialog.setFullBackgroundResource(rid);

            return this;
        }


        public Builder setIconType(@IntRange(from = ICON_SUCCESS, to = ICON_WARNING) byte type) {

            mDialog.setIconType(type);
            return this;
        }

        public Builder setIconType(@IntRange(from = ICON_SUCCESS, to = ICON_WARNING) byte type, int color) {

            mDialog.setIconType(type, color);
            return this;
        }

        public Builder setCancelable(boolean cancelable) {

            mDialog.setCancelable(cancelable);

            return this;
        }


        public Builder setAnimatorStart(Animator mAnimatorStart) {
            mDialog.setAnimatorStart(mAnimatorStart);
            return this;
        }

        public Builder setAnimatorStart(Runnable runnable) {
            mDialog.setAnimatorStart(runnable);
            return this;
        }

        public Builder setAnimatorEnd(Animator mAnimatorEnd) {
            mDialog.setAnimatorEnd(mAnimatorEnd);
            return this;
        }

        public Builder setAnimatorEnd(Runnable runnable) {
            mDialog.setAnimatorEnd(runnable);
            return this;
        }


        public Builder setCircularRevealAnimator(CircularRevealStatus statue) {

            mDialog.setAnimatorStart(mDialog.getCircularRevealAnimator(true, statue));
            mDialog.setAnimatorEnd(mDialog.getCircularRevealAnimator(false, statue));
            return this;
        }

        public Builder setTileAnimator() {

            mDialog.setAnimatorStart(mDialog.getTileAnimator(true));
            mDialog.setAnimatorEnd(mDialog.getTileAnimator(false));
            return this;
        }


        public CanDialog create() {
            return mDialog;
        }

        public CanDialog show() {
            mDialog.show();
            return mDialog;
        }


    }

}
