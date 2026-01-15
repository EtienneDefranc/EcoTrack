package ec.com.ecotrackapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import ec.com.ecotrackapp.controller.SistemaEcoTrack;
import ec.com.ecotrackapp.models.Residuo;
import ec.com.ecotrackapp.tda.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Intent;

public class EstadisticasActivity extends AppCompatActivity {

    private SistemaEcoTrack sistema;
    private ListView lvEstadisticas;
    private MaterialButton btnActualizar, btnVerGraficos, btnVolver;
    private TextView tvInfo, tvTotalResiduos, tvPesoTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        sistema = SistemaEcoTrack.getInstance(this);

        inicializarVistas();
        configurarListeners();
        actualizarEstadisticas();
    }

    private void inicializarVistas() {
        lvEstadisticas = findViewById(R.id.lvEstadisticas);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnVerGraficos = findViewById(R.id.btnVerGraficos);
        btnVolver = findViewById(R.id.btnVolver);
        tvInfo = findViewById(R.id.tvInfo);
        tvTotalResiduos = findViewById(R.id.tvTotalResiduos);
        tvPesoTotal = findViewById(R.id.tvPesoTotal);
    }

    private void configurarListeners() {
        btnActualizar.setOnClickListener(v -> actualizarEstadisticas());

        btnVerGraficos.setOnClickListener(v -> {
            Intent intent = new Intent(EstadisticasActivity.this, GraficosActivity.class);
            startActivity(intent);
        });

        btnVolver.setOnClickListener(v -> finish());
    }

    private void actualizarEstadisticas() {
        java.util.List<String> items = new java.util.ArrayList<>();
        Map<String, Object> stats = sistema.obtenerEstadisticas();

        // Actualizar cards de resumen
        tvTotalResiduos.setText(String.valueOf(stats.get("totalResiduos")));
        tvPesoTotal.setText(String.format(Locale.getDefault(), "%.1f", (Double) stats.get("pesoTotal")));

        // Lista de detalles
        items.add("📊 RESUMEN GENERAL");
        items.add("━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        items.add("");
        items.add(String.format(Locale.getDefault(), "📦 Total de residuos: %d", (Integer) stats.get("totalResiduos")));
        items.add(String.format(Locale.getDefault(), "♻️ Residuos en centro: %d", (Integer) stats.get("residuosEnCentro")));
        items.add(String.format(Locale.getDefault(), "⚖️ Peso total: %.2f kg", (Double) stats.get("pesoTotal")));
        items.add("");

        items.add("🚛 VEHÍCULOS");
        items.add("━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        items.add("");
        items.add(String.format(Locale.getDefault(), "✅ Disponibles: %d", (Integer) stats.get("vehiculosDisponibles")));
        items.add(String.format(Locale.getDefault(), "🚗 En ruta: %d", (Integer) stats.get("vehiculosEnRuta")));
        items.add("");

        items.add("🗺️ ZONAS URBANAS");
        items.add("━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        items.add("");
        items.add(String.format(Locale.getDefault(), "📍 Total de zonas: %d", (Integer) stats.get("zonasTotales")));
        items.add(String.format(Locale.getDefault(), "🚨 Zonas críticas: %d", (Integer) stats.get("zonasCriticas")));
        items.add("");

        items.add("♻️ ESTADÍSTICAS POR TIPO");
        items.add("━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        items.add("");

        Map<Residuo.TipoResiduo, Double> estadisticasPorTipo = sistema.getEstadisticasPorTipo();
        if (estadisticasPorTipo.isEmpty()) {
            items.add("   No hay datos disponibles");
        } else {
            for (Map.Entry<Residuo.TipoResiduo, Double> entry : estadisticasPorTipo.entrySet()) {
                String emoji = obtenerEmojiPorTipo(entry.getKey());
                items.add(String.format(Locale.getDefault(), "%s %s: %.2f kg",
                    emoji,
                    entry.getKey().getNombre(),
                    entry.getValue()));
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            items
        );
        lvEstadisticas.setAdapter(adapter);

        // Actualizar hora
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            tvInfo.setText(getString(R.string.ultima_actualizacion, formatter.format(new Date())));
        } catch (Exception e) {
            tvInfo.setText(getString(R.string.ultima_actualizacion_ahora));
        }
    }

    private String obtenerEmojiPorTipo(Residuo.TipoResiduo tipo) {
        switch (tipo) {
            case PLASTICO: return "🥤";
            case VIDRIO: return "🍾";
            case PAPEL: return "📄";
            case METAL: return "🔧";
            case ORGANICO: return "🍎";
            default: return "📦";
        }
    }
}
