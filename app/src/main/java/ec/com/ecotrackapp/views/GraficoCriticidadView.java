package ec.com.ecotrackapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class GraficoCriticidadView extends View {

    private int zonasCriticas = 0;
    private int zonasNoCriticas = 0;

    private Paint paintCriticas;
    private Paint paintNoCriticas;
    private Paint paintTexto;
    private Paint paintBorde;

    private RectF rectF = new RectF();

    public GraficoCriticidadView(Context context) {
        super(context);
        init();
    }

    public GraficoCriticidadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraficoCriticidadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintCriticas = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCriticas.setColor(Color.parseColor("#F44336")); // rojo

        paintNoCriticas = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNoCriticas.setColor(Color.parseColor("#4CAF50")); // verde

        paintTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTexto.setColor(Color.DKGRAY);
        paintTexto.setTextSize(28f);

        paintBorde = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorde.setStyle(Paint.Style.STROKE);
        paintBorde.setStrokeWidth(4f);
        paintBorde.setColor(Color.DKGRAY);
    }

    /** criticas y noCriticas deben ser >= 0 */
    public void setDatos(int criticas, int noCriticas) {
        this.zonasCriticas = Math.max(0, criticas);
        this.zonasNoCriticas = Math.max(0, noCriticas);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int total = zonasCriticas + zonasNoCriticas;
        float width = getWidth();
        float height = getHeight();

        if (total == 0) {
            String msg = "Sin datos de zonas";
            float tw = paintTexto.measureText(msg);
            canvas.drawText(msg, (width - tw) / 2, height / 2, paintTexto);
            return;
        }

        float size = Math.min(width, height) * 0.6f;
        float cx = width / 3f;          // círculo a la izquierda
        float cy = height / 2f;

        rectF.set(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2);

        float angTotal = 360f;
        float angCrit = angTotal * zonasCriticas / total;
        float angNoCrit = angTotal - angCrit;

        // porción críticas (rojo)
        canvas.drawArc(rectF, -90, angCrit, true, paintCriticas);
        // porción no críticas (verde)
        canvas.drawArc(rectF, -90 + angCrit, angNoCrit, true, paintNoCriticas);
        // borde
        canvas.drawOval(rectF, paintBorde);

        // texto central
        String centro = zonasCriticas + "/" + total;
        float twCentro = paintTexto.measureText(centro);
        canvas.drawText(centro, cx - twCentro / 2f, cy + paintTexto.getTextSize() / 3f, paintTexto);

        // Leyenda a la derecha
        float startX = width * 0.6f;
        float yBase = cy - 30;

        float boxSize = 30f;

        // crítico
        canvas.drawRect(startX, yBase, startX + boxSize, yBase + boxSize, paintCriticas);
        canvas.drawText("Críticas (" + zonasCriticas + ")", startX + boxSize + 16, yBase + boxSize * 0.8f, paintTexto);

        // no críticas
        float y2 = yBase + 50;
        canvas.drawRect(startX, y2, startX + boxSize, y2 + boxSize, paintNoCriticas);
        canvas.drawText("No críticas (" + zonasNoCriticas + ")", startX + boxSize + 16, y2 + boxSize * 0.8f, paintTexto);
    }
}
