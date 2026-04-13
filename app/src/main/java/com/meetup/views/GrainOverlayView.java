package com.meetup.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Random;

/**
 * Subtle monochrome grain overlay. One noise bitmap per layout size (no tiling).
 * Bitmap is generated at a capped resolution and scaled to the view to limit memory.
 */
public class GrainOverlayView extends View {

    private static final int MAX_BITMAP_PIXELS = 1_000_000;
    private static final int MAX_WIDTH = 720;
    private static final int MAX_HEIGHT = 1280;

    private final Paint drawPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random(42);

    private Bitmap noiseBitmap;
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();

    private int lastGenW;
    private int lastGenH;

    public GrainOverlayView(@NonNull Context context) {
        super(context);
    }

    public GrainOverlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GrainOverlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * (Re)builds noise when size is known and the bitmap is missing or dimensions changed.
     * After {@link #onDetachedFromWindow()}, {@link #onSizeChanged} may not run on re-attach if
     * width/height are unchanged, so we also call this from {@link #onAttachedToWindow} and
     * {@link #onLayout}.
     */
    private void ensureNoiseBitmap(int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        if (w == lastGenW && h == lastGenH && noiseBitmap != null && !noiseBitmap.isRecycled()) {
            return;
        }
        lastGenW = w;
        lastGenH = h;
        rebuildNoiseBitmap(w, h);
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Post: at attach time width/height are often still 0; after layout this fills the gap
        // when onSizeChanged is skipped (same w/h as before detach).
        post(() -> ensureNoiseBitmap(getWidth(), getHeight()));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        ensureNoiseBitmap(right - left, bottom - top);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ensureNoiseBitmap(w, h);
    }

    private void rebuildNoiseBitmap(int viewW, int viewH) {
        recycleNoiseBitmap();

        int genW = Math.min(viewW, MAX_WIDTH);
        int genH = Math.min(viewH, MAX_HEIGHT);

        while (genW > 1 && genH > 1 && (long) genW * genH > MAX_BITMAP_PIXELS) {
            genW = Math.max(1, genW * 9 / 10);
            genH = Math.max(1, genH * 9 / 10);
        }

        noiseBitmap = Bitmap.createBitmap(genW, genH, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[genW * genH];
        random.setSeed(42);

        for (int i = 0; i < pixels.length; i++) {
            int gray = 110 + random.nextInt(50);
            int alpha = 5 + random.nextInt(8);
            pixels[i] = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
        }

        noiseBitmap.setPixels(pixels, 0, genW, 0, 0, genW, genH);
        srcRect.set(0, 0, genW, genH);
        dstRect.set(0, 0, viewW, viewH);
    }

    private void recycleNoiseBitmap() {
        if (noiseBitmap != null && !noiseBitmap.isRecycled()) {
            noiseBitmap.recycle();
        }
        noiseBitmap = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        recycleNoiseBitmap();
        lastGenW = 0;
        lastGenH = 0;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (noiseBitmap != null && !noiseBitmap.isRecycled()) {
            dstRect.set(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(noiseBitmap, srcRect, dstRect, drawPaint);
        }
    }
}
