package com.dolphin.gridlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

/**
 * 2014/04/14
 * 
 * @author dolphinWang
 * 
 */
public class DolphinGridLayout extends ViewGroup {
    private static final String DEBUG_TAG = "DolphinGridLayout";

    private static final int DEFAULT_ROW_AND_COLUMN_COUNT = 1;

    /**
     * Count of rows
     */
    private int mRowCount = DEFAULT_ROW_AND_COLUMN_COUNT;

    /**
     * Count if columns
     */
    private int mColumnCount = DEFAULT_ROW_AND_COLUMN_COUNT;

    private Rect mPadding;
    private int mItemSpaceHorizontal;
    private int mItemSpaceVertical;

    private int mColumnWith;
    private int mRowHeight;

    public DolphinGridLayout(Context context) {
        this(context, null);
    }

    public DolphinGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.DolphinGridLayout);
            mRowCount = a.getInt(R.styleable.DolphinGridLayout_rowCount,
                    DEFAULT_ROW_AND_COLUMN_COUNT);

            mColumnCount = a.getInt(R.styleable.DolphinGridLayout_columnCount,
                    DEFAULT_ROW_AND_COLUMN_COUNT);

            mItemSpaceHorizontal = (int) a.getDimension(
                    R.styleable.DolphinGridLayout_itemSpaceHorizontal, 0);
            mItemSpaceVertical = (int) a.getDimension(
                    R.styleable.DolphinGridLayout_itemSpaceVertical, 0);

            a.recycle();
        }

    }

    private void init() {
        mPadding = new Rect(getPaddingLeft(), getPaddingTop(),
                getPaddingRight(), getPaddingBottom());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            checkParamsLegaled((LayoutParams) getChildAt(i).getLayoutParams());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        mRowHeight = (heightSize - mPadding.top - mPadding.bottom - (mRowCount - 1)
                * mItemSpaceVertical)
                / mRowCount;
        mColumnWith = (widthSize - mPadding.left - mPadding.right - (mColumnCount - 1)
                * mItemSpaceVertical)
                / mColumnCount;

        setMeasuredDimension(widthSize, heightSize);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        final int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            final LayoutParams glp = (LayoutParams) child.getLayoutParams();
            checkParamsLegaled(glp);

            final int childRowStart = glp.mRowIndex;
            final int childRowEnd = childRowStart + glp.mRowSpec;
            final int childColumnStart = glp.mColumnIndex;
            final int childColumnEnd = childColumnStart + glp.mColumnSpec;

            // Measure first
            final int heightProvide = (childRowEnd - childRowStart)
                    * mRowHeight + (glp.mRowSpec - 1) * mItemSpaceVertical;
            final int widthProvide = (childColumnEnd - childColumnStart)
                    * mColumnWith + (glp.mColumnSpec - 1)
                    * mItemSpaceHorizontal;

            if (glp.width > 0 || glp.width == LayoutParams.MATCH_PARENT) {
                measureChild(child, MeasureSpec.makeMeasureSpec(widthProvide,
                        MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                        heightProvide, MeasureSpec.EXACTLY));
            } else {
                measureChild(child, MeasureSpec.makeMeasureSpec(widthProvide,
                        MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(
                        heightProvide, MeasureSpec.AT_MOST));
            }

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            if (childWidth > widthProvide || childHeight > heightProvide) {
                if (widthProvide - childWidth >= heightProvide - childHeight) {
                    // Width based
                    final float ratio = (float) widthProvide / childWidth;
                    childHeight = (int) (childHeight * ratio);
                    childWidth = (int) (childWidth * ratio);
                } else {
                    // Height based
                    final float ratio = (float) heightProvide / childHeight;
                    childWidth = (int) (childWidth * ratio);
                    childHeight = (int) (childHeight * ratio);
                }

                // Re-measure to meet parent provide
                child.measure(MeasureSpec.makeMeasureSpec(childWidth,
                        MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                        childHeight, MeasureSpec.EXACTLY));
            }

            // After measure child, we should layout it now.
            final int layoutTop = childRowStart == 0 ? mPadding.top
                    : mPadding.top + childRowStart
                            * (mItemSpaceVertical + mRowHeight);

            final int layoutLeft = childColumnStart == 0 ? mPadding.left
                    : mPadding.left + childColumnStart
                            * (mItemSpaceHorizontal + mColumnWith);

            child.layout(layoutLeft, layoutTop, layoutLeft + childWidth,
                    layoutTop + childHeight);
        }
    }

    private void checkParamsLegaled(LayoutParams lp) {
        final int rowCount = mRowCount;
        final int columnCount = mColumnCount;

        final int rowEnd = lp.mRowIndex + lp.mRowSpec;
        if (rowEnd > rowCount) {
            lp.mRowSpec = rowCount - lp.mRowIndex;

            if (lp.mRowSpec == 0) {
                lp.mRowSpec = 1;
                lp.mRowIndex--;

                Log.w(DEBUG_TAG, "Child need row from " + lp.mRowIndex + " to "
                        + rowEnd + " . But total row count is " + rowCount
                        + " . Set rowSpec to " + lp.mRowSpec
                        + " , rowIndex to " + lp.mRowIndex + " .");
            } else {
                Log.w(DEBUG_TAG, "Child need row from " + lp.mRowIndex + " to "
                        + rowEnd + " . But total row count is " + rowCount
                        + " . Set rowSpec to " + lp.mRowSpec);
            }
        }

        final int columnEnd = lp.mColumnIndex + lp.mColumnSpec;
        if (columnEnd > columnCount) {
            lp.mColumnSpec = columnCount - lp.mColumnIndex;

            if (lp.mColumnSpec == 0) {
                lp.mColumnSpec = 1;
                lp.mColumnIndex--;

                Log.w(DEBUG_TAG, "Child need column from " + lp.mColumnIndex
                        + " to " + columnEnd + " . But total column count is "
                        + columnCount + " . Set columnSpec to "
                        + lp.mColumnSpec + " , columnIndex to "
                        + lp.mColumnIndex + " .");
            } else {
                Log.w(DEBUG_TAG, "Child need column from " + lp.mColumnIndex
                        + " to " + columnEnd + " . But total column count is "
                        + columnCount + " . Set columnSpec to "
                        + lp.mColumnSpec);
            }
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new LayoutParams(lp);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams lp) {
        return lp instanceof LayoutParams;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public void setRowCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Row count less than 0. Do you really want to do this?");
        }

        mRowCount = count;
    }

    public void setColumnCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Column count less than 0. Do you really want to do this?");
        }

        mColumnCount = count;
    }

    public void setItemHorizontalSpace(int space) {
        if (space < 0) {
            Log.w(DEBUG_TAG,
                    "Set horizontal space less than 0! Change it to 0.");
            space = 0;

            return;
        }

        mItemSpaceHorizontal = space;
    }

    public void setItemVerticalSpace(int space) {
        if (space < 0) {
            Log.w(DEBUG_TAG, "Set vertical space less than 0! Change it to 0.");
            space = 0;

            return;
        }

        mItemSpaceVertical = space;
    }

    /**
     * LayoutParams for this grid layout
     * 
     * @author dolphinWang
     * 
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {

        /**
         * Occupy in row
         */
        int mRowSpec;

        /**
         * Occupy in column
         */
        int mColumnSpec;

        /**
         * Index in row
         */
        int mRowIndex;

        /**
         * Index in column
         */
        int mColumnIndex;

        int mGravity = Gravity.TOP | Gravity.LEFT;

        public LayoutParams(ViewGroup.LayoutParams lp) {
            super(lp);
            defaultParams();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            defaultParams();
        }

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.DolphinGridLayout);

            mRowIndex = a.getInt(R.styleable.DolphinGridLayout_rowIndex, 0);
            mRowSpec = a.getInt(R.styleable.DolphinGridLayout_rowSpec, 1);
            mColumnIndex = a.getInt(R.styleable.DolphinGridLayout_columnIndex,
                    0);
            mColumnSpec = a.getInt(R.styleable.DolphinGridLayout_columnSpec, 1);

            a.recycle();

        }

        private void defaultParams() {
            mRowSpec = 1;
            mColumnSpec = 1;
            mRowIndex = 0;
            mColumnIndex = 0;
        }
    }
}