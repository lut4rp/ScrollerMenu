package com.appmogli.widget.scrollermenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Arrays;


public class ScrollerMenu extends RelativeLayout implements GestureDetector.OnGestureListener {

    public static interface ScrollerMenuListener {
        /**
         * @param menuItems
         * @param index
         */
        public void onMenuItemSelected(String[] menuItems, int index);

        /**
         * @param menuItems
         * @param index
         * @param progressedBy
         */
        public void onMenuItemProgressed(String[] menuItems, int index, int progressedBy);
    }


    private enum MENU_MODE {
        NONE, SCROLLING, VALUE_PROGRESSING
    }


    private ScrollerMenuListener menuListener;

    private static final boolean DEBUG = false;
    private static final String TAG = ScrollerMenu.class.getSimpleName();

    private static final int MENU_ITEM_WIDTH_IN_DP = 120;
    private static final int MENU_ITEM_HEIGHT_IN_DP = 36;
    private static final int MENU_ITEM_DIVIDER_MARGIN_IN_DP = 2;
    private static final int MENU_ITEM_TEXT_COLOR = Color.WHITE;

    private Drawable menuItemDefaultBackground;
    private Drawable menuItemSelectedBackground;
    private Drawable menuPanelBackground;
    private int menuItemSelectedIndex = 0;
    private int menuItemTextColor;
    private float menuItemWidth;
    private float menuItemHeight;
    private String[] menuItems;
    private ScrollerMenuScrollView mScrollerMenuScrollView;
    private LinearLayout menuPanel;
    private GestureDetector mGestureDetector = null;
    private TextView selectedMenu;
    private MENU_MODE mode;

    private Handler handler = new Handler();

    public ScrollerMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public ScrollerMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScrollerMenu(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ScrollerMenu,
                0, 0);

        try {
            menuItemDefaultBackground = a.getDrawable(R.styleable.ScrollerMenu_scrollerMenuItemDefaultBackground);
            if (menuItemDefaultBackground == null) {
                menuItemDefaultBackground = context.getResources().getDrawable(R.drawable.scroller_menu_item_default_background);
            }
            menuItemSelectedBackground = a.getDrawable(R.styleable.ScrollerMenu_scrollerMenuItemSelectedBackground);
            if (menuItemSelectedBackground == null) {
                menuItemSelectedBackground = context.getResources().getDrawable(R.drawable.scroller_menu_item_selected_background);
            }

            menuPanelBackground = a.getDrawable(R.styleable.ScrollerMenu_scrollerMenuPanelBackground);
            if (menuPanelBackground == null) {
                menuPanelBackground = context.getResources().getDrawable(R.drawable.scroller_menu_panel_background);
            }

            menuItemWidth = a.getDimension(R.styleable.ScrollerMenu_scrollerMenuItemWidth, MENU_ITEM_WIDTH_IN_DP);
            menuItemHeight = a.getDimension(R.styleable.ScrollerMenu_scrollerMenuItemHeight, MENU_ITEM_HEIGHT_IN_DP);
            menuItemTextColor = a.getColor(R.styleable.ScrollerMenu_scrollerMenuItemTextColor, context.getResources().getColor(R.color.scroller_menu_item_text_color));

            CharSequence[] menuArray = a.getTextArray(R.styleable.ScrollerMenu_scrollerMenuItems);
            if (menuArray != null && menuArray.length >= 2) {
                menuItems = new String[menuArray.length];
                int i = 0;
                for (CharSequence ch : menuArray) {
                    menuItems[i++] = ch.toString();
                }
            }

        } finally {
            a.recycle();
        }

        mGestureDetector = new GestureDetector(context, this);
    }

    public void setMenuItems(String[] items) {
        this.menuItems = Arrays.copyOf(items, items.length);
        requestLayout();
        invalidate();
    }

    public void setMenuItems(int arrayResource) {
        this.menuItems = getResources().getStringArray(arrayResource);
        requestLayout();
        invalidate();
    }

    public void setMenuItemBackgroundDrawableResource(int drawableResource) {
        this.menuItemDefaultBackground = getContext().getResources().getDrawable(drawableResource);
        invalidate();
    }

    public void setMenuItemHeight(int dimesionResource) {
        this.menuItemHeight = getResources().getDimension(dimesionResource);
        requestLayout();
        invalidate();
    }

    public void setMenuItemWidth(int dimesionResource) {
        this.menuItemWidth = getResources().getDimension(dimesionResource);
        requestLayout();
        invalidate();
    }


    public void setMenuItemTextColor(int colorResource) {
        this.menuItemTextColor = getResources().getColor(colorResource);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isMenuScrolling()) {
                notifyMenuItemSelected();
            }
            mode = MENU_MODE.NONE;
            hidePanel();
            return false;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    private boolean isModeNone() {
        return mode == MENU_MODE.NONE;
    }

    private boolean isMenuScrolling() {
        return mode == MENU_MODE.SCROLLING;
    }

    private boolean isMenuValueProgressing() {
        return mode == MENU_MODE.VALUE_PROGRESSING;
    }

    private void hidePanel() {
        mScrollerMenuScrollView.setVisibility(View.INVISIBLE);
        selectedMenu.setVisibility(View.INVISIBLE);
    }

    private void showPanel() {
        mScrollerMenuScrollView.setVisibility(View.VISIBLE);
        selectedMenu.setVisibility(View.VISIBLE);
    }

    private void notifyMenuItemProgressed(final int progress) {
        if (DEBUG) {
            Log.d(TAG, "Progress changed by:" + progress + " for menu item:" + menuItems[menuItemSelectedIndex]);
        }

        if (this.menuListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ScrollerMenu.this.menuListener.onMenuItemProgressed(menuItems, menuItemSelectedIndex, progress);
                }
            });
        }
    }

    private void notifyMenuItemSelected() {
        menuItemSelectedIndex = mScrollerMenuScrollView.getSelectedChild();
        if (DEBUG) {
            Log.d(TAG, "Menu item selected is:" + this.menuItems[menuItemSelectedIndex]);
        }
        if (this.menuListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ScrollerMenu.this.menuListener.onMenuItemSelected(menuItems, menuItemSelectedIndex);
                }
            });
        }
    }

    public void setMenuListener(ScrollerMenuListener listener) {
        this.menuListener = listener;
    }

    public void setSelectedMenuItem(int selectedMenuItem) {
        this.menuItemSelectedIndex = selectedMenuItem;
    }

    /**
     * @return index of the menu item which is selected
     */
    public int getSelectedMenuItem() {
        return this.menuItemSelectedIndex;
    }

    @Override
    public boolean isInEditMode() {
        return false;
    }

    @Override
    protected void onSizeChanged(int neww, int newh, int oldw, int oldh) {
        super.onSizeChanged(neww, newh, oldw, oldh);

        if (menuItems == null || menuItems.length < 2) {
            Log.e(TAG, "Menu items are not provided");
            return;
        }

        Context context = getContext();
        mScrollerMenuScrollView = new ScrollerMenuScrollView(context);
        LinearLayout completeScrollPanel = new LinearLayout(context);
        menuPanel = new LinearLayout(context);

        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, menuItemWidth, getResources().getDisplayMetrics());
        float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, menuItemHeight, getResources().getDisplayMetrics());

        float menuItemDivider = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MENU_ITEM_DIVIDER_MARGIN_IN_DP, getResources().getDisplayMetrics());
        float totalMenuItemDividerHeight = (menuItems.length - 1) * menuItemDivider;

        float menuPanelHeight = totalMenuItemDividerHeight + menuItems.length * height;
        float menuPanelWidth = width;

        LinearLayout.LayoutParams menuPanelLayoutParams = new LinearLayout.LayoutParams(Math.round(menuPanelWidth), Math.round(menuPanelHeight));
        menuPanel.setBackgroundDrawable(menuPanelBackground);
        menuPanel.setOrientation(LinearLayout.VERTICAL);
        TextView[] menuTvs = new TextView[menuItems.length];
        for (int i = 0; i < menuItems.length; i++) {
            TextView tv = new TextView(context);
            menuTvs[i] = tv;
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(Math.round(menuPanelWidth), Math.round(height));
            tv.setBackgroundDrawable(menuItemDefaultBackground);
            tv.setText(menuItems[i]);
            tv.setGravity(Gravity.CENTER);
            tv.setSingleLine(true);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            tv.setLayoutParams(tvParams);
            tv.setTextColor(menuItemTextColor);
            menuPanel.addView(tv);
            //now add margin
            if (i != (menuItems.length - 1)) {
                View view = new View(context);
                LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(Math.round(menuPanelWidth), Math.round(menuItemDivider));
                view.setLayoutParams(viewParams);
                menuPanel.addView(view);

            }
        }
        menuPanel.setLayoutParams(menuPanelLayoutParams);

        float menuPanelAboveDividerHeight = (menuItems.length - 1) * menuItemDivider;
        float totalMenuPanelAboveHeight = (menuItems.length - 1) * height + menuPanelAboveDividerHeight;

        View menuPanelAboveView = new View(context);
        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(Math.round(menuPanelWidth), Math.round(totalMenuPanelAboveHeight));
        menuPanelAboveView.setLayoutParams(viewParams);

        View menuPanelBelowView = new View(context);
        viewParams = new LinearLayout.LayoutParams(Math.round(menuPanelWidth), Math.round(totalMenuPanelAboveHeight));
        menuPanelBelowView.setLayoutParams(viewParams);

        ScrollView.LayoutParams completeScrollPanelParams = new ScrollView.LayoutParams(Math.round(menuPanelWidth), 2 * Math.round(totalMenuPanelAboveHeight) + Math.round(menuPanelHeight));
        completeScrollPanel.setLayoutParams(completeScrollPanelParams);
        completeScrollPanel.setOrientation(LinearLayout.VERTICAL);
        completeScrollPanel.addView(menuPanelAboveView);
        completeScrollPanel.addView(menuPanel);
        completeScrollPanel.addView(menuPanelBelowView);

        RelativeLayout.LayoutParams scrollParams = new LayoutParams(Math.round(menuPanelWidth), Math.round(totalMenuPanelAboveHeight) + Math.round(menuPanelHeight));
        scrollParams.topMargin = Math.round(newh / 2f) - Math.round(totalMenuPanelAboveHeight);
        scrollParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mScrollerMenuScrollView.setLayoutParams(scrollParams);

        mScrollerMenuScrollView.addView(completeScrollPanel);
        mScrollerMenuScrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        mScrollerMenuScrollView.setHorizontalScrollBarEnabled(false);
        mScrollerMenuScrollView.setVerticalScrollBarEnabled(false);

        addView(mScrollerMenuScrollView);

        //lets create selected item
        selectedMenu = new TextView(context);
        LayoutParams tvParams = new LayoutParams(Math.round(menuPanelWidth), Math.round(height));
        tvParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tvParams.topMargin = Math.round(newh / 2f);
        selectedMenu.setBackgroundDrawable(menuItemSelectedBackground);
        selectedMenu.setText(menuItems[0]);
        selectedMenu.setGravity(Gravity.CENTER);
        selectedMenu.setSingleLine(true);
        selectedMenu.setEllipsize(TextUtils.TruncateAt.END);
        selectedMenu.setLayoutParams(tvParams);
        selectedMenu.setTextColor(Color.WHITE);
        menuTvs[0].setVisibility(View.INVISIBLE);
        addView(selectedMenu);
        mScrollerMenuScrollView.setMenuPanel(menuTvs, height, selectedMenu, Math.round(totalMenuPanelAboveHeight));

        mScrollerMenuScrollView.setVisibility(View.INVISIBLE);
        selectedMenu.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float scrollX, float scrollY) {
        if (isModeNone()) {
            if (Math.abs(scrollX) > Math.abs(scrollY)) {
                mode = MENU_MODE.VALUE_PROGRESSING;
            } else {
                mode = MENU_MODE.SCROLLING;
            }
        }
        if (isMenuValueProgressing()) {
            hidePanel();
            int halfWidthOfView = getWidth() / 2;
            float effectiveDistance = -scrollX * 100f / halfWidthOfView;
            final int progress = Math.round(effectiveDistance);
            notifyMenuItemProgressed(progress);
        } else {
            //scroll menu
            showPanel();
            mScrollerMenuScrollView.smoothScrollBy((int) scrollX, (int) scrollY);
        }

        return true;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float scrollX, float scrollY) {
        return false;
    }
}
