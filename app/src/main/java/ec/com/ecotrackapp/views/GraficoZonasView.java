package ec.com.ecotrackapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ec.com.ecotrackapp.models.Zona;

/**
 * CustomView que dibuja un gráfico de barras verticales
 * con la cantidad de residuos por zona.
 */
public class GraficoZonasView extends View {

    private List<Zona> zonas = new ArrayList<>();

    private Paint paintEjes;
    private Paint paintBarra;
    private Paint paintTexto;
    private Paint paintLineaGuia;

    private float paddingIzq = 80f;
    private float paddingDer = 40f;
    private float paddingTop = 80f;
    private float paddingBottom = 100f;

    private int[] coloresNormales = {
            Color.parseColor("#2196F3"), // azul
            Color.parseColor("#4CAF50"), // verde
            Color.parseColor("#FFC107"), // ámbar
            Color.parseColor("#9C27B0")  // morado
    };

    private int[] coloresCriticos = {
            Color.parseColor("#F44336"), // rojo
            Color.parseColor("#FF5722")  // naranja
    };

    public GraficoZonasView(Context context) {
        super(context);
        init();
    }

    public GraficoZonasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraficoZonasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintEjes = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintEjes.setColor(Color.DKGRAY);
        paintEjes.setStrokeWidth(4f);

        paintBarra = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBarra.setColor(Color.parseColor("#2196F3"));

        paintTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTexto.setColor(Color.DKGRAY);
        paintTexto.setTextSize(26f);

        paintLineaGuia = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLineaGuia.setColor(Color.LTGRAY);
        paintLineaGuia.setStrokeWidth(2f);
    }

    /** Recibe la lista de zonas a graficar */
    public void setZonas(List<Zona> zonas) {
        this.zonas = zonas != null ? zonas : new ArrayList<>();
        invalidate(); // redibujar
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        if (zonas == null || zonas.isEmpty()) {
            // Mensaje si no hay datos
            String msg = "Sin datos para graficar";
            float textWidth = paintTexto.measureText(msg);
            canvas.drawText(msg, (width - textWidth) / 2, height / 2, paintTexto);
            return;
        }

        // Área útil para el gráfico
        float left = paddingIzq;
        float right = width - paddingDer;
        float top = paddingTop;
        float bottom = height - paddingBottom;

        // Dibujar ejes
        canvas.drawLine(left, top, left, bottom, paintEjes);      // eje Y
        canvas.drawLine(left, bottom, right, bottom, paintEjes);  // eje X

        // Encontrar el máximo de residuos para escalar las barras
        int maxResiduos = 0;
        for (Zona z : zonas) {
            if (z.getCantidadResiduos() > maxResiduos) {
                maxResiduos = z.getCantidadResiduos();
            }
        }
        if (maxResiduos == 0) maxResiduos = 1; // evitar división por cero

        float anchoTotal = right - left;
        int n = zonas.size();
        float espacioPorBarra = anchoTotal / n;
        float anchoBarra = espacioPorBarra * 0.6f; // 60% del espacio

        // Líneas de guía horizontales alineadas a los valores reales
        int numDivisiones = maxResiduos;   // una línea por cada valor 1..maxResiduos

        for (int value = 1; value <= numDivisiones; value++) {
            // posición vertical correspondiente a este valor
            float relacion = (float) value / maxResiduos;              // 0..1
            float y = bottom - (bottom - top) * relacion;              // interpola entre bottom y top

            // línea de guía
            canvas.drawLine(left, y, right, y, paintLineaGuia);

            // etiqueta del valor en el eje Y
            canvas.drawText(String.valueOf(value), left - 60, y + 8, paintTexto);
        }




        // Dibujar cada barra
        for (int i = 0; i < n; i++) {
            Zona z = zonas.get(i);
            float xCentro = left + espacioPorBarra * i + espacioPorBarra / 2f;

            float alturaRelativa = (float) z.getCantidadResiduos() / maxResiduos;
            float barraTop = bottom - (bottom - top) * alturaRelativa;

            float leftBarra = xCentro - anchoBarra / 2f;
            float rightBarra = xCentro + anchoBarra / 2f;

            // Color diferente si supera el umbral (colores criticos) y color diferente para cada zona (colores normales)
            int color;
            if (z.superaUmbral()) {
                // colores para zonas críticas
                color = coloresCriticos[i % coloresCriticos.length];
            } else {
                // colores para zonas normales
                color = coloresNormales[i % coloresNormales.length];
            }
            paintBarra.setColor(color);


            canvas.drawRect(leftBarra, barraTop, rightBarra, bottom, paintBarra);

            // Nombre de la zona (abajo)
            String nombre = z.getNombre();
            float textWidth = paintTexto.measureText(nombre);
            canvas.drawText(nombre, xCentro - textWidth / 2f, bottom + 30, paintTexto);

            // Valor numérico encima de la barra
            String valor = String.valueOf(z.getCantidadResiduos());
            float vw = paintTexto.measureText(valor);

            // posición ideal (10px encima de la barra)
            float yTexto = barraTop - 10;

            // no dejar que el texto suba más arriba de un margen seguro
            float minY = top + paintTexto.getTextSize();
            if (yTexto < minY) {
                yTexto = minY;
            }

            canvas.drawText(valor, xCentro - vw / 2f, yTexto, paintTexto);

        }
    }
}
