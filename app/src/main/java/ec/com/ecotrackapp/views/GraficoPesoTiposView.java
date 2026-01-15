package ec.com.ecotrackapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import ec.com.ecotrackapp.tda.ArrayList;
import ec.com.ecotrackapp.tda.List;
import ec.com.ecotrackapp.tda.Map;

/**
 * Pie chart del peso por tipo de residuo.
 * Recibe un Map<String, Float> (tipo -> peso).
 */
public class GraficoPesoTiposView extends View {

    private List<String> etiquetas = new ArrayList<>();
    private List<Float> valores = new ArrayList<>();

    private Paint paintSector;
    private Paint paintTexto;
    private RectF rectF = new RectF();

    private int[] colores = {
            Color.parseColor("#2196F3"), // azul
            Color.parseColor("#4CAF50"), // verde
            Color.parseColor("#FFC107"), // ámbar
            Color.parseColor("#9C27B0"), // morado
            Color.parseColor("#00BCD4"), // cyan
            Color.parseColor("#FF5722")  // naranja
    };

    public GraficoPesoTiposView(Context context) {
        super(context);
        init();
    }

    public GraficoPesoTiposView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraficoPesoTiposView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintSector = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSector.setStyle(Paint.Style.FILL);

        paintTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTexto.setColor(Color.DKGRAY);
        paintTexto.setTextSize(22f);
    }

    public void setDatos(Map<String, Float> datos) {
        etiquetas.clear();
        valores.clear();

        if (datos != null) {
            for (Map.Entry<String, Float> e : datos.entrySet()) {
                if (e.getValue() != null && e.getValue() > 0f) {
                    etiquetas.add(e.getKey());
                    valores.add(e.getValue());
                }
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        if (etiquetas.isEmpty() || valores.isEmpty()) {
            String msg = "Sin datos de tipos de residuo";
            float tw = paintTexto.measureText(msg);
            canvas.drawText(msg, (width - tw) / 2, height / 2, paintTexto);
            return;
        }

        float total = 0f;
        for (Float v : valores) total += v;
        if (total <= 0f) total = 1f;

        float size = Math.min(width, height) * 0.6f;
        float cx = width / 3f;
        float cy = height / 2f;

        rectF.set(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2);

        float angInicio = -90f;
        for (int i = 0; i < valores.size(); i++) {
            float v = valores.get(i);
            float angulo = 360f * (v / total);

            paintSector.setColor(colores[i % colores.length]);
            canvas.drawArc(rectF, angInicio, angulo, true, paintSector);

            angInicio += angulo;
        }

        // Leyenda a la derecha
        float startX = width * 0.6f;
        float y = cy - (etiquetas.size() * 35f) / 2f;
        float boxSize = 24f;

        for (int i = 0; i < etiquetas.size(); i++) {
            paintSector.setColor(colores[i % colores.length]);
            canvas.drawRect(startX, y, startX + boxSize, y + boxSize, paintSector);

            String label = etiquetas.get(i) + " (" + String.format("%.1f", valores.get(i)) + " kg)";
            canvas.drawText(label, startX + boxSize + 16, y + boxSize * 0.8f, paintTexto);

            y += 35f;
        }
    }
}
