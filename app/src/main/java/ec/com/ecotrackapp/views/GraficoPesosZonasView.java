package ec.com.ecotrackapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import ec.com.ecotrackapp.tda.ArrayList;
import ec.com.ecotrackapp.tda.List;

import ec.com.ecotrackapp.models.Zona;

/**
 * Gráfico de barras dobles:
 * pesoRecolectado (verde) vs pesoPendiente (naranja) por zona.
 */
public class GraficoPesosZonasView extends View {

    private List<Zona> zonas = new ArrayList<>();

    private Paint paintEjes;
    private Paint paintBarraRecolectado;
    private Paint paintBarraPendiente;
    private Paint paintTexto;
    private Paint paintLineaGuia;

    private float paddingIzq = 80f;
    private float paddingDer = 40f;
    private float paddingTop = 60f;
    private float paddingBottom = 100f;

    public GraficoPesosZonasView(Context context) {
        super(context);
        init();
    }

    public GraficoPesosZonasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraficoPesosZonasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintEjes = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintEjes.setColor(Color.DKGRAY);
        paintEjes.setStrokeWidth(4f);

        paintBarraRecolectado = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBarraRecolectado.setColor(Color.parseColor("#4CAF50")); // verde

        paintBarraPendiente = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBarraPendiente.setColor(Color.parseColor("#FF9800")); // naranja

        paintTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTexto.setColor(Color.DKGRAY);
        paintTexto.setTextSize(24f);

        paintLineaGuia = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLineaGuia.setColor(Color.LTGRAY);
        paintLineaGuia.setStrokeWidth(2f);
    }

    public void setZonas(List<Zona> zonas) {
        this.zonas = zonas != null ? zonas : new ArrayList<>();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        if (zonas == null || zonas.isEmpty()) {
            String msg = "Sin datos para graficar";
            float tw = paintTexto.measureText(msg);
            canvas.drawText(msg, (width - tw) / 2, height / 2, paintTexto);
            return;
        }

        float left = paddingIzq;
        float right = width - paddingDer;
        float top = paddingTop;
        float bottom = height - paddingBottom;

        // Ejes
        canvas.drawLine(left, top, left, bottom, paintEjes);
        canvas.drawLine(left, bottom, right, bottom, paintEjes);

        // Máximo peso entre recolectado y pendiente
        double maxPeso = 0;
        for (Zona z : zonas) {
            if (z.getPesoRecolectado() > maxPeso) maxPeso = z.getPesoRecolectado();
            if (z.getPesoPendiente() > maxPeso) maxPeso = z.getPesoPendiente();
        }
        if (maxPeso <= 0) maxPeso = 1;

        float anchoTotal = right - left;
        int n = zonas.size();
        float espacioPorZona = anchoTotal / n;
        float anchoBarra = espacioPorZona * 0.35f; // 35% para cada barra

        // Líneas de guía (4 niveles)
        int divisiones = 4;
        for (int i = 1; i <= divisiones; i++) {
            float relacion = (float) i / divisiones;
            float y = bottom - (bottom - top) * relacion;

            canvas.drawLine(left, y, right, y, paintLineaGuia);

            double valor = maxPeso * relacion;
            String etiqueta = String.format("%.1f", valor);
            canvas.drawText(etiqueta, left - 70, y + 8, paintTexto);
        }

        // Barras por zona
        for (int i = 0; i < n; i++) {
            Zona z = zonas.get(i);
            float xCentroZona = left + espacioPorZona * i + espacioPorZona / 2f;

            float alturaRecolectado = (float) (z.getPesoRecolectado() / maxPeso);
            float alturaPendiente = (float) (z.getPesoPendiente() / maxPeso);

            float topRecolectado = bottom - (bottom - top) * alturaRecolectado;
            float topPendiente = bottom - (bottom - top) * alturaPendiente;

            // Recolectado (barra izquierda)
            float leftR = xCentroZona - anchoBarra - 5;
            float rightR = xCentroZona - 5;
            canvas.drawRect(leftR, topRecolectado, rightR, bottom, paintBarraRecolectado);

            // Pendiente (barra derecha)
            float leftP = xCentroZona + 5;
            float rightP = xCentroZona + anchoBarra + 5;
            canvas.drawRect(leftP, topPendiente, rightP, bottom, paintBarraPendiente);

            // Nombre de zona
            String nombre = z.getNombre();
            float tw = paintTexto.measureText(nombre);
            canvas.drawText(nombre, xCentroZona - tw / 2f, bottom + 30, paintTexto);

            // Valores encima (opcional, solo si hay espacio)
            String vR = String.format("%.1f", z.getPesoRecolectado());
            String vP = String.format("%.1f", z.getPesoPendiente());

            float yTextoR = topRecolectado - 8;
            float yTextoP = topPendiente - 8;
            float minY = top + paintTexto.getTextSize();
            if (yTextoR < minY) yTextoR = minY;
            if (yTextoP < minY) yTextoP = minY;

            canvas.drawText(vR, leftR, yTextoR, paintTexto);
            canvas.drawText(vP, rightP - paintTexto.measureText(vP), yTextoP, paintTexto);
        }
    }
}
