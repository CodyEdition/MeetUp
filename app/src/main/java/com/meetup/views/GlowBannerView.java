package com.meetup.views;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.meetup.R;

public class GlowBannerView extends View {

    private Paint basePaint;
    private Paint mainGlowPaint;
    private Paint secondaryGlowPaint;
    private Paint edgeGlowPaint;
    private Path clipPath;
    private RectF bannerRect;
    
    private float cornerRadius;
    private float density;

    public GlowBannerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public GlowBannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GlowBannerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        
        density = context.getResources().getDisplayMetrics().density;
        cornerRadius = 24f * density;

        int mainGlowColor = context.getColor(R.color.accent_orange);
        int secondaryGlowColor = context.getColor(R.color.accent_light_orange);

        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        mainGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainGlowPaint.setColor(mainGlowColor);
        mainGlowPaint.setMaskFilter(new BlurMaskFilter(60 * density, BlurMaskFilter.Blur.NORMAL));

        secondaryGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secondaryGlowPaint.setColor(secondaryGlowColor);
        secondaryGlowPaint.setAlpha(180);
        secondaryGlowPaint.setMaskFilter(new BlurMaskFilter(40 * density, BlurMaskFilter.Blur.NORMAL));

        edgeGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgeGlowPaint.setColor(Color.WHITE);
        edgeGlowPaint.setAlpha(100);
        edgeGlowPaint.setMaskFilter(new BlurMaskFilter(15 * density, BlurMaskFilter.Blur.NORMAL));

        clipPath = new Path();
        bannerRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        bannerRect.set(0, 0, w, h);
        
        clipPath.reset();
        float[] radii = {0, 0, 0, 0, cornerRadius, cornerRadius, cornerRadius, cornerRadius};
        clipPath.addRoundRect(bannerRect, radii, Path.Direction.CW);

        basePaint.setShader(new LinearGradient(
                0, h, 0, 0,
                Color.parseColor("#1a1208"),
                Color.parseColor("#0A0909"),
                Shader.TileMode.CLAMP
        ));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        canvas.save();
        canvas.clipPath(clipPath);

        canvas.drawRect(bannerRect, basePaint);

        float glowCenterX = width / 2f;
        float glowCenterY = height + (20 * density);

        canvas.drawOval(
                glowCenterX - (width * 0.6f),
                height - (60 * density),
                glowCenterX + (width * 0.6f),
                glowCenterY + (40 * density),
                mainGlowPaint
        );

        canvas.drawOval(
                glowCenterX - (width * 0.45f),
                height - (35 * density),
                glowCenterX + (width * 0.45f),
                glowCenterY + (20 * density),
                secondaryGlowPaint
        );

        canvas.drawOval(
                glowCenterX - (width * 0.35f),
                height - (12 * density),
                glowCenterX + (width * 0.35f),
                height + (8 * density),
                edgeGlowPaint
        );

        canvas.restore();
    }
}
