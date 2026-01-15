package ec.com.ecotrackapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import ec.com.ecotrackapp.controller.SistemaEcoTrack;
import ec.com.ecotrackapp.models.Zona;

import ec.com.ecotrackapp.tda.List;
import java.util.Locale;

public class ZonasActivity extends AppCompatActivity {

    private SistemaEcoTrack sistema;
    private ListView lvZonas, lvZonasCriticas;
    private MaterialButton btnActualizar, btnVolver;
    private TextView tvInfo, tvCriticasCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zonas);

        sistema = SistemaEcoTrack.getInstance(this);

        inicializarVistas();
        configurarListeners();
        actualizarZonas();
    }

    private void inicializarVistas() {
        lvZonas = findViewById(R.id.lvZonas);
        lvZonasCriticas = findViewById(R.id.lvZonasCriticas);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnVolver = findViewById(R.id.btnVolver);
        tvInfo = findViewById(R.id.tvInfo);
        tvCriticasCount = findViewById(R.id.tvCriticasCount);
    }

    private void configurarListeners() {
        btnActualizar.setOnClickListener(v -> actualizarZonas());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void actualizarZonas() {
        List<Zona> zonasCriticas = sistema.obtenerZonasCriticas();
        List<Zona> todasLasZonas = sistema.obtenerTodasLasZonas();

        // Actualizar lista de zonas críticas
        actualizarListaZonasCriticas(zonasCriticas);

        // Actualizar lista de todas las zonas
        actualizarListaTodasZonas(todasLasZonas);

        // Actualizar información del header
        tvInfo.setText(String.format(Locale.getDefault(), "Total: %d zonas | Críticas: %d",
            todasLasZonas.size(), zonasCriticas.size()));

        // Actualizar contador de críticas
        if (zonasCriticas.isEmpty()) {
            tvCriticasCount.setText(R.string.no_hay_zonas_criticas);
        } else {
            tvCriticasCount.setText(String.format(Locale.getDefault(), "%d zona%s requiere%s atención inmediata",
                zonasCriticas.size(),
                zonasCriticas.size() > 1 ? "s" : "",
                zonasCriticas.size() > 1 ? "n" : ""));
        }
    }

    private void actualizarListaZonasCriticas(List<Zona> zonasCriticas) {
        java.util.List<String> items = new java.util.ArrayList<>();

        if (zonasCriticas.isEmpty()) {
            items.add("");
            items.add("✅ No hay zonas críticas");
            items.add("");
            items.add("Todas las zonas están");
            items.add("en condiciones normales");
        } else {
            for (Zona zona : zonasCriticas) {
                items.add("━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                items.add("🚨 " + zona.getNombre());
                items.add("");
                items.add(String.format(Locale.getDefault(), "⚖️ Peso pendiente: %.2f kg", zona.getPesoPendiente()));
                items.add(String.format(Locale.getDefault(), "♻️ Peso recolectado: %.2f kg", zona.getPesoRecolectado()));
                items.add(String.format(Locale.getDefault(), "⚠️ Prioridad: %d/10", zona.getNivelPrioridad()));
                items.add("📊 Estado: CRÍTICO");
                items.add("");
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            items
        );
        lvZonasCriticas.setAdapter(adapter);
    }

    private void actualizarListaTodasZonas(List<Zona> todasLasZonas) {
        java.util.List<String> items = new java.util.ArrayList<>();

        if (todasLasZonas.isEmpty()) {
            items.add("");
            items.add("📭 No hay zonas registradas");
            items.add("");
            items.add("Las zonas se crearán");
            items.add("automáticamente al");
            items.add("registrar residuos");
        } else {
            for (Zona zona : todasLasZonas) {
                String emoji = zona.esCritica() ? "🔴" : "🟢";
                String estado = zona.esCritica() ? "CRÍTICA" : "NORMAL";

                items.add("━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                items.add(String.format(Locale.getDefault(), "%s %s", emoji, zona.getNombre()));
                items.add("");
                items.add(String.format(Locale.getDefault(), "📦 Residuos: %d", zona.getCantidadResiduos()));
                items.add(String.format(Locale.getDefault(), "⚖️ Peso pendiente: %.2f kg", zona.getPesoPendiente()));
                items.add(String.format(Locale.getDefault(), "📊 Estado: %s", estado));

                if (zona.esCritica()) {
                    items.add(String.format(Locale.getDefault(), "⚠️ Prioridad: %d/10", zona.getNivelPrioridad()));
                }

                items.add("");
            }

            items.add("━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            items.add(String.format(Locale.getDefault(), "📊 Total: %d zona%s registrada%s",
                todasLasZonas.size(),
                todasLasZonas.size() > 1 ? "s" : "",
                todasLasZonas.size() > 1 ? "s" : ""));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            items
        );
        lvZonas.setAdapter(adapter);
    }
}
