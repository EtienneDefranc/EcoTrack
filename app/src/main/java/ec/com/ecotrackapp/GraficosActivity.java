package ec.com.ecotrackapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ec.com.ecotrackapp.tda.ArrayList;
import ec.com.ecotrackapp.tda.HashMap;
import ec.com.ecotrackapp.tda.List;
import ec.com.ecotrackapp.tda.Map;

import ec.com.ecotrackapp.controller.SistemaEcoTrack;
import ec.com.ecotrackapp.models.Residuo;
import ec.com.ecotrackapp.models.Zona;
import ec.com.ecotrackapp.views.GraficoZonasView;
import ec.com.ecotrackapp.views.GraficoPesosZonasView;
import ec.com.ecotrackapp.views.GraficoCriticidadView;
import ec.com.ecotrackapp.views.GraficoPesoTiposView;

public class GraficosActivity extends AppCompatActivity {

    private SistemaEcoTrack sistema;
    private GraficoZonasView graficoZonasView;
    private GraficoPesosZonasView graficoPesosView;
    private GraficoCriticidadView graficoCriticidadView;
    private GraficoPesoTiposView graficoPesoTiposView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graficos);

        sistema = SistemaEcoTrack.getInstance(this);

        graficoZonasView = findViewById(R.id.graficoZonas);
        graficoPesosView = findViewById(R.id.graficoPesos);
        graficoCriticidadView = findViewById(R.id.graficoCriticidad);
        graficoPesoTiposView = findViewById(R.id.graficoTipos);

        cargarDatosEnGraficos();
    }

    private void cargarDatosEnGraficos() {
        // Zonas como lista
        List<Zona> zonasLista = new ArrayList<>(sistema.getZonas().values());
        graficoZonasView.setZonas(zonasLista);
        graficoPesosView.setZonas(zonasLista);

        // Criticidad
        int criticas = sistema.obtenerZonasCriticas().size();
        int totalZonas = sistema.getZonas().size();
        int noCriticas = Math.max(0, totalZonas - criticas);
        graficoCriticidadView.setDatos(criticas, noCriticas);

        // Peso por tipo de residuo
        Map<Residuo.TipoResiduo, Double> statsTipo = sistema.getEstadisticasPorTipo();
        Map<String, Float> datos = new HashMap<>();

        for (Map.Entry<Residuo.TipoResiduo, Double> e : statsTipo.entrySet()) {
            String nombre = e.getKey().getNombre();
            float valor = e.getValue() != null ? e.getValue().floatValue() : 0f;
            if (valor > 0f) {
                datos.put(nombre, valor);
            }
        }

        graficoPesoTiposView.setDatos(datos);
    }
}
