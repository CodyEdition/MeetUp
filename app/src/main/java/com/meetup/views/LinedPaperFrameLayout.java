package com.meetup.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.meetup.R;

/**
 * {@link FrameLayout} that draws ruled horizontal lines and a vertical margin line behind its
 * children. Height always matches content (no separate overlay measurement issues).
 */
public class LinedPaperFrameLayout extends FrameLayout {

    private static final int RULE_COLOR = 0xFF5A5A5A;
    private static final int MARGIN_RULE_COLOR = 0xFF6E6E6E;

    private final Paint rulePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint marginPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float lineSpacingPx;
    private float firstRuleFromTopPx;
    private float marginFromStartPx;

    public LinedPaperFrameLayout(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public LinedPaperFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LinedPaperFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        float d = context.getResources().getDisplayMetrics().density;
        float defStep = 28f * d;
        float defFirst = 40f * d;
        float defMargin = 28f * d;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinedPaperFrameLayout);
            lineSpacingPx = a.getDimension(R.styleable.LinedPaperFrameLayout_linedPaperLineSpacing, defStep);
            firstRuleFromTopPx = a.getDimension(
                    R.styleable.LinedPaperFrameLayout_linedPaperFirstRuleFromTop, defFirst);
            marginFromStartPx = a.getDimension(
                    R.styleable.LinedPaperFrameLayout_linedPaperMarginFromStart, defMargin);
            a.recycle();
        } else {
            lineSpacingPx = defStep;
            firstRuleFromTopPx = defFirst;
            marginFromStartPx = defMargin;
        }

        rulePaint.setStyle(Paint.Style.STROKE);
        rulePaint.setStrokeWidth(Math.max(1f, d));
        rulePaint.setColor(RULE_COLOR);
        marginPaint.setStyle(Paint.Style.STROKE);
        marginPaint.setStrokeWidth(Math.max(1f, d * 1.25f));
        marginPaint.setColor(MARGIN_RULE_COLOR);

        setWillNotDraw(false);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        drawRuledLines(canvas);
        super.dispatchDraw(canvas);
    }

    private void drawRuledLines(@NonNull Canvas canvas) {
        if (lineSpacingPx <= 0f) {
            return;
        }
        int pl = getPaddingLeft();
        int pt = getPaddingTop();
        int pr = getPaddingRight();
        int pb = getPaddingBottom();
        int w = getWidth();
        int h = getHeight();
        float left = pl;
        float right = w - pr;
        float top = pt;
        float bottom = h - pb;
        if (right <= left || bottom <= top) {
            return;
        }
        float y = top + firstRuleFromTopPx;
        while (y <= bottom) {
            canvas.drawLine(left, y, right, y, rulePaint);
            y += lineSpacingPx;
        }
        float mx = pl + marginFromStartPx;
        if (mx < right) {
            canvas.drawLine(mx, top, mx, bottom, marginPaint);
        }
    }
}
