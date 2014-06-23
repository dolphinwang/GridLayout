
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
 */
public class DolphinGridLayout extends ViewGroup {
    private static final String DEBUG_TAG = "DolphinGridLayout";

    private static final int DEFAULT_ROW_AND_COLUMN_COUNT = 1;

    /**
     * Count of rows
     */
    private int mRowCount = DEFAULT_ROW_AND_COLUMN_COUNT;

    /**
     * Count of columns
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

            mColumnCount = a.getInt(
                    R.styleable.DolphinGridLayout_columnCount,
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
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        mRowHeight = (heightSize - mPadding.top - mPadding.bottom - (mRowCount - 1)
                * mItemSpaceVertical)
                / mRowCount;
        mColumnWith = (widthSize - mPadding.left - mPadding.right - (mColumnCount - 1)
                * mItemSpaceVertical)
                / mColumnCount;

        final int childCount = getChildCount();
        if (mColumnCount > 0 && widthMode == MeasureSpec.UNSPECIFIED) {
            measureChildInWidthModeUNSPECIFIED(childCount);

            widthSize = mPadding.left + mPadding.right + (mColumnCount - 1)
                    * mItemSpaceHorizontal + mColumnCount * mColumnWith;
        }

        if (mRowCount > 0 && heightMode == MeasureSpec.UNSPECIFIED) {
            measureChildInHeightModeUNSPECIFIED(childCount);

            heightSize = mPadding.top + mPadding.bottom + (mRowCount - 1)
                    * mItemSpaceVertical + mRowCount * mRowHeight;
        }

        // Measure child, and check whether child need re-measure to meet parent
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            checkParamsLegaled(lp);

            // Calculate whether need re-measure
            final int heightProvide = (int) (lp.rowSpec * mRowHeight + (lp.rowSpec - 1)
                    * mItemSpaceVertical);
            final int widthProvide = (int) (lp.columnSpec * mColumnWith + (lp.columnSpec - 1)
                    * mItemSpaceHorizontal);

            int heightSpec = makeMeasuerSpec(lp.height, heightProvide);
            int widthSpec = makeMeasuerSpec(lp.width, widthProvide);

            child.measure(widthSpec, heightSpec);

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
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    private void measureChildInWidthModeUNSPECIFIED(int childCount) {
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            checkParamsLegaled(lp);

            final int height = (int) (mRowHeight * lp.rowSpec + (lp.rowSpec - 1)
                    * mItemSpaceVertical);

            int childWidth = lp.width;
            int childHeight = lp.height;
            if (childWidth < 0) {
                child.measure(
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

                childWidth = child.getMeasuredWidth();
                childHeight = child.getMeasuredHeight();

            }

            if (lp.width == LayoutParams.MATCH_PARENT) {
                float ratio = (float) height / childHeight;
                childWidth = (int) (childWidth * ratio);
            }

            int tempColumnWidth = (int) ((childWidth
                    - (lp.columnSpec - 1)
                    * mItemSpaceHorizontal) / lp.columnSpec);
            if (mColumnWith < tempColumnWidth) {
                mColumnWith = tempColumnWidth;
            }
        }
    }

    private void measureChildInHeightModeUNSPECIFIED(int childCount) {
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            checkParamsLegaled(lp);

            final int width = (int) (mColumnWith * lp.columnSpec + (lp.columnSpec - 1)
                    * mItemSpaceHorizontal);

            int childWidth = lp.width;
            int childHeight = lp.height;

            if (childHeight < 0) {
                child.measure(
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

                childHeight = child.getMeasuredHeight();
                childWidth = child.getMeasuredWidth();
            }

            if (lp.height == LayoutParams.MATCH_PARENT) {
                float ratio = (float) width / childWidth;
                childHeight = (int) (childHeight * ratio);
            }

            int tempRowHeight = (int) ((childHeight
                    - (lp.rowSpec - 1)
                    * mItemSpaceVertical) / lp.rowSpec);
            if (mRowHeight < tempRowHeight) {
                mRowHeight = tempRowHeight;
            }
        }
    }

    private int makeMeasuerSpec(int childSize, int provideSize) {
        int measureSpec = 0;

        if (childSize > 0) {
            measureSpec = MeasureSpec.makeMeasureSpec(childSize,
                    MeasureSpec.EXACTLY);
        } else {
            if (childSize == LayoutParams.MATCH_PARENT) {
                measureSpec = MeasureSpec.makeMeasureSpec(provideSize,
                        MeasureSpec.EXACTLY);
            } else {
                measureSpec = MeasureSpec.makeMeasureSpec(provideSize,
                        MeasureSpec.AT_MOST);
            }
        }

        return measureSpec;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        final int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            final float childRowStart = lp.rowIndex;
            final float childColumnStart = lp.columnIndex;

            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();

            // We should layout it now.
            int layoutTop = 0;
            int layoutLeft = 0;
            final int layoutGravity = lp.gravity;
            switch (layoutGravity) {
                case Gravity.LEFT:
                case Gravity.START:
                case Gravity.LEFT | Gravity.TOP:
                    layoutTop = calculateTop4Top(childRowStart);
                    layoutLeft = calculateLeft4Left(childColumnStart);
                    break;
                case Gravity.LEFT | Gravity.BOTTOM:
                    layoutTop = calculateTop4Bottom(childRowStart + lp.rowSpec - 1,
                            childHeight);
                    layoutLeft = calculateLeft4Left(childColumnStart);
                    break;
                case Gravity.RIGHT:
                case Gravity.RIGHT | Gravity.TOP:
                    layoutTop = calculateTop4Top(childRowStart);
                    layoutLeft = calculateLeft4Right(childColumnStart
                            + lp.columnSpec - 1, childWidth);
                    break;
                case Gravity.END:
                case Gravity.RIGHT | Gravity.BOTTOM:
                    layoutTop = calculateTop4Bottom(childRowStart + lp.rowSpec - 1,
                            childHeight);
                    layoutLeft = calculateLeft4Right(childColumnStart
                            + lp.columnSpec - 1, childWidth);
                    break;
                case Gravity.CENTER:
                    layoutTop = calculateTop4CenterVertical(childRowStart,
                            childRowStart + lp.rowSpec - 1, childHeight);
                    layoutLeft = calculateLeft4CenterHorizontal(childColumnStart,
                            childColumnStart + lp.columnSpec - 1, childWidth);
                    break;
                case Gravity.CENTER_VERTICAL:
                case Gravity.CENTER_VERTICAL | Gravity.LEFT:
                    layoutTop = calculateTop4CenterVertical(childRowStart,
                            childRowStart + lp.rowSpec - 1, childHeight);
                    layoutLeft = calculateLeft4Left(childColumnStart);
                    break;
                case Gravity.CENTER_VERTICAL | Gravity.RIGHT:
                    layoutTop = calculateTop4CenterVertical(childRowStart,
                            childRowStart + lp.rowSpec - 1, childHeight);
                    layoutLeft = calculateLeft4Right(childColumnStart
                            + lp.columnSpec - 1, childWidth);
                    break;
                case Gravity.CENTER_HORIZONTAL:
                case Gravity.CENTER_HORIZONTAL | Gravity.TOP:
                    layoutTop = calculateTop4Top(childRowStart);
                    layoutLeft = calculateLeft4CenterHorizontal(childColumnStart,
                            childColumnStart + lp.columnSpec - 1, childWidth);
                    break;
                case Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM:
                    layoutTop = calculateTop4Bottom(childRowStart + lp.rowSpec - 1,
                            childHeight);
                    layoutLeft = calculateLeft4CenterHorizontal(childColumnStart,
                            childColumnStart + lp.columnSpec - 1, childWidth);
                    break;
                default:
                    layoutTop = calculateTop4Top(childRowStart);
                    layoutLeft = calculateLeft4Left(childColumnStart);
                    break;
            }

            child.layout(layoutLeft, layoutTop, layoutLeft + childWidth,
                    layoutTop + childHeight);
        }
    }

    private int calculateLeft4Left(float columnStart) {
        int left = (int) (mPadding.left + columnStart
                * (mItemSpaceHorizontal + mColumnWith));

        return left;
    }

    private int calculateTop4Top(float rowStart) {
        int top = (int) (mPadding.top + rowStart
                * (mItemSpaceVertical + mRowHeight));

        return top;
    }

    private int calculateTop4Bottom(float rowEnd, float childHeight) {
        int top = (int) (mPadding.top + rowEnd
                * (mItemSpaceVertical + mRowHeight) - childHeight);

        return top;
    }

    private int calculateLeft4Right(float columnEnd, float childWidth) {
        int left = (int) (mPadding.left + columnEnd
                * (mItemSpaceHorizontal + mColumnCount) - childWidth);

        return left;
    }

    private int calculateLeft4CenterHorizontal(float columnStart,
            float columnEnd, int childWidth) {
        float leftBoundary = mPadding.left + columnStart
                * (mItemSpaceHorizontal + mColumnWith);
        float rightBoundary = leftBoundary + (columnEnd - columnStart)
                * mItemSpaceHorizontal + (columnEnd - columnStart + 1)
                * mColumnWith;
        int left = (int) (leftBoundary + (rightBoundary - leftBoundary) / 2 - childWidth / 2);

        return left;
    }

    private int calculateTop4CenterVertical(float rowStart, float rowEnd,
            int childHeight) {
        float topBoundary = mPadding.top + rowStart
                * (mItemSpaceVertical + mRowHeight);
        float bottomBoundary = topBoundary + (rowEnd - rowStart)
                * mItemSpaceVertical + (rowEnd - rowStart + 1) * mRowHeight;
        int top = (int) (topBoundary + (bottomBoundary - topBoundary) / 2 - childHeight / 2);

        return top;
    }

    private void checkParamsLegaled(LayoutParams lp) {
        final int rowCount = mRowCount;
        final int columnCount = mColumnCount;

        final float rowEnd = lp.rowIndex + lp.rowSpec - 1;
        if (rowEnd > rowCount - 1) {
            lp.rowSpec = rowCount - lp.rowIndex;

            if (lp.rowSpec == 0) {
                lp.rowSpec = 1;
                lp.rowIndex--;

                Log.w(DEBUG_TAG, "Child need row from " + lp.rowIndex + " to "
                        + rowEnd + " . But total row count is " + rowCount
                        + " . Set rowSpec to " + lp.rowSpec + " , rowIndex to "
                        + lp.rowIndex + " .");
            } else {
                Log.w(DEBUG_TAG, "Child need row from " + lp.rowIndex + " to "
                        + rowEnd + " . But total row count is " + rowCount
                        + " . Set rowSpec to " + lp.rowSpec);
            }
        }

        final float columnEnd = lp.columnIndex + lp.columnSpec - 1;
        if (columnEnd > columnCount - 1) {
            lp.columnSpec = columnCount - lp.columnIndex;

            if (lp.columnSpec == 0) {
                lp.columnSpec = 1;
                lp.columnIndex--;

                Log.w(DEBUG_TAG, "Child need column from " + lp.columnIndex
                        + " to " + columnEnd + " . But total column count is "
                        + columnCount + " . Set columnSpec to " + lp.columnSpec
                        + " , columnIndex to " + lp.columnIndex + " .");
            } else {
                Log.w(DEBUG_TAG, "Child need column from " + lp.columnIndex
                        + " to " + columnEnd + " . But total column count is "
                        + columnCount + " . Set columnSpec to " + lp.columnSpec);
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

        requestLayout();
    }

    public void setColumnCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Column count less than 0. Do you really want to do this?");
        }

        mColumnCount = count;

        requestLayout();
    }

    public void setItemHorizontalSpace(int space) {
        if (space < 0) {
            Log.w(DEBUG_TAG,
                    "Set horizontal space less than 0! Change it to 0.");
            space = 0;

            return;
        }

        mItemSpaceHorizontal = space;
        requestLayout();
    }

    public void setItemVerticalSpace(int space) {
        if (space < 0) {
            Log.w(DEBUG_TAG, "Set vertical space less than 0! Change it to 0.");
            space = 0;

            return;
        }

        mItemSpaceVertical = space;
        requestLayout();
    }

    /**
     * LayoutParams for this grid layout
     * 
     * @author dolphinWang
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {

        private static final int[] INTERNAL_ATTR = new int[] {
                android.R.attr.layout_gravity
        };

        /**
         * Occupy in row
         */
        public float rowSpec;

        /**
         * Occupy in column
         */
        public float columnSpec;

        /**
         * Index in row
         */
        public float rowIndex;

        /**
         * Index in column
         */
        public float columnIndex;

        public int gravity = Gravity.TOP | Gravity.LEFT;

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

            rowIndex = a.getFloat(R.styleable.DolphinGridLayout_rowIndex, 0);
            rowSpec = a.getFloat(R.styleable.DolphinGridLayout_rowSpec, 1);
            columnIndex = a.getFloat(R.styleable.DolphinGridLayout_columnIndex,
                    0);
            columnSpec = a
                    .getFloat(R.styleable.DolphinGridLayout_columnSpec, 1);

            a.recycle();

            TypedArray b = context.obtainStyledAttributes(attrs, INTERNAL_ATTR);
            gravity = b.getInt(0, Gravity.TOP | Gravity.LEFT);
            b.recycle();
        }

        private void defaultParams() {
            rowSpec = 1.0f;
            columnSpec = 1.0f;
            rowIndex = 0;
            columnIndex = 0;
        }
    }
}
