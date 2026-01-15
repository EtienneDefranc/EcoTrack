package ec.com.ecotrackapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ec.com.ecotrackapp.controller.SistemaEcoTrack;
import ec.com.ecotrackapp.models.Residuo;
import ec.com.ecotrackapp.tda.ArrayList;
import ec.com.ecotrackapp.tda.Comparadores;

import ec.com.ecotrackapp.tda.List;
import java.util.Locale;

public class ResiduosActivity extends AppCompatActivity {

    private SistemaEcoTrack sistema;
    private ListView lvResiduos;
    private Spinner spOrdenamiento;
    private Button btnActualizar, btnIterarAdelante, btnIterarAtras, btnVolver;
    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_residuos);

        sistema = SistemaEcoTrack.getInstance(this);

        inicializarVistas();
        configurarSpinner();
        configurarListeners();
        actualizarListaResiduos();
    }

    private void inicializarVistas() {
        lvResiduos = findViewById(R.id.lvResiduos);
        spOrdenamiento = findViewById(R.id.spOrdenamiento);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnIterarAdelante = findViewById(R.id.btnIterarAdelante);
        btnIterarAtras = findViewById(R.id.btnIterarAtras);
        btnVolver = findViewById(R.id.btnVolver);
        tvInfo = findViewById(R.id.tvInfo);
    }

    private void configurarSpinner() {
        String[] opciones = {"Peso", "Tipo", "Prioridad Ambiental", "Zona", "Fecha"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            opciones
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spOrdenamiento.setAdapter(adapter);
    }

    private void configurarListeners() {
        btnActualizar.setOnClickListener(v -> actualizarListaResiduos());
        btnIterarAdelante.setOnClickListener(v -> iterarAdelante());
        btnIterarAtras.setOnClickListener(v -> iterarAtras());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void actualizarListaResiduos() {
        if (sistema.getResiduos().estaVacia()) {
            tvInfo.setText(R.string.no_hay_residuos_registrados);
            lvResiduos.setAdapter(null);
            return;
        }

        // Reiniciar el cursor al mostrar la lista completa
        sistema.getResiduos().reiniciarCursor();

        String criterio = (String) spOrdenamiento.getSelectedItem();
        List<Residuo> listaOrdenada;

        switch (criterio) {
            case "Peso":
                listaOrdenada = sistema.ordenarResiduos(new Comparadores.ComparadorPorPeso());
                break;
            case "Tipo":
                listaOrdenada = sistema.ordenarResiduos(new Comparadores.ComparadorPorTipo());
                break;
            case "Prioridad Ambiental":
                listaOrdenada = sistema.ordenarResiduos(new Comparadores.ComparadorPorPrioridad());
                break;
            case "Zona":
                listaOrdenada = sistema.ordenarResiduos(new Comparadores.ComparadorPorZona());
                break;
            case "Fecha":
                listaOrdenada = sistema.ordenarResiduos(new Comparadores.ComparadorPorFecha());
                break;
            default:
                listaOrdenada = sistema.ordenarResiduos(new Comparadores.ComparadorPorPrioridad());
        }

        List<String> items = new ArrayList<>();
        for (Residuo residuo : listaOrdenada) {
            items.add(residuo.toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            new java.util.ArrayList<>(convertirLista(items))
        );
        lvResiduos.setAdapter(adapter);

        tvInfo.setText(String.format(Locale.getDefault(), "Total de residuos: %d | Ordenado por: %s",
            listaOrdenada.size(), criterio));
    }

    private void iterarAdelante() {
        if (sistema.getResiduos().estaVacia()) {
            tvInfo.setText(R.string.no_hay_residuos_iterar);
            return;
        }

        // Usar el método siguiente() del cursor
        Residuo residuo = sistema.getResiduos().siguiente();
        mostrarResiduo(residuo, "Siguiente");
    }

    private void iterarAtras() {
        if (sistema.getResiduos().estaVacia()) {
            tvInfo.setText(R.string.no_hay_residuos_iterar);
            return;
        }

        // Usar el método anterior() del cursor
        Residuo residuo = sistema.getResiduos().anterior();
        mostrarResiduo(residuo, "Anterior");
    }

    private void mostrarResiduo(Residuo residuo, String direccion) {
        java.util.List<String> items = new java.util.ArrayList<>();
        items.add("🆔 ID: " + residuo.getId());
        items.add("📦 " + residuo.getNombre());
        items.add("🏷️ Tipo: " + residuo.getTipo().getNombre());
        items.add("⚖️ Peso: " + String.format(Locale.getDefault(), "%.2f kg", residuo.getPeso()));
        items.add("📍 Zona: " + residuo.getZona());
        items.add("⚠️ Prioridad: " + residuo.getPrioridadAmbiental() + "/10");
        if (residuo.getFechaRecoleccion() != null) {
            items.add("📅 Fecha: " + residuo.getFechaRecoleccion().toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            items
        );
        lvResiduos.setAdapter(adapter);

        int posicion = sistema.getResiduos().getPosicionCursor();
        int total = sistema.getResiduos().getTamanio();
        tvInfo.setText(String.format(Locale.getDefault(), "🔄 %s - Posición: %d/%d (Lista Circular)",
            direccion, posicion, total));
    }

    private java.util.List<String> convertirLista(List<String> customList) {
        java.util.List<String> javaList = new java.util.ArrayList<>();
        for (String item : customList) {
            javaList.add(item);
        }
        return javaList;
    }
}
