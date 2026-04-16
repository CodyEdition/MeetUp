package com.meetup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.tracing.Trace;
import androidx.recyclerview.widget.RecyclerView;

final class CityWheelUiHelper {

    private static final float TEXT_SIZE_CENTER_SP = 20f;
    private static final float TEXT_SIZE_EDGE_SP = 14f;
    private static final float ALPHA_CENTER = 1f;
    private static final float ALPHA_EDGE = 0.45f;

    private CityWheelUiHelper() {
    }

    static void applyWheelTransforms(RecyclerView recyclerView) {
        Trace.beginSection("CityWheel:applyWheelTransforms");
        try {
            int height = recyclerView.getHeight();
            if (height <= 0) {
                return;
            }
            int centerY = height / 2;
            float maxDist = height / 2f;

            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                View child = recyclerView.getChildAt(i);
                TextView text = child.findViewById(R.id.cityNameText);
                if (text == null) {
                    continue;
                }
                int childCenterY = (child.getTop() + child.getBottom()) / 2;
                float dist = Math.abs(childCenterY - centerY);
                float t = Math.min(1f, dist / maxDist);
                float textSizeSp = TEXT_SIZE_CENTER_SP - t * (TEXT_SIZE_CENTER_SP - TEXT_SIZE_EDGE_SP);
                float alpha = ALPHA_CENTER - t * (ALPHA_CENTER - ALPHA_EDGE);
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
                text.setAlpha(alpha);
            }
        } finally {
            Trace.endSection();
        }
    }

    static final class SelectionBandDecoration extends RecyclerView.ItemDecoration {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int itemHeightPx;

        SelectionBandDecoration(Context context) {
            paint.setColor(ContextCompat.getColor(context, R.color.accent_orange));
            paint.setStyle(Paint.Style.STROKE);
            float stroke = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1f,
                    context.getResources().getDisplayMetrics());
            paint.setStrokeWidth(stroke);
            itemHeightPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    56f,
                    context.getResources().getDisplayMetrics());
        }

        @Override
        public void onDrawOver(
                @NonNull Canvas c,
                @NonNull RecyclerView parent,
                @NonNull RecyclerView.State state) {
            int cy = parent.getHeight() / 2;
            int half = itemHeightPx / 2;
            float left = parent.getPaddingLeft();
            float right = parent.getWidth() - parent.getPaddingRight();
            c.drawLine(left, cy - half, right, cy - half, paint);
            c.drawLine(left, cy + half, right, cy + half, paint);
        }
    }
}
