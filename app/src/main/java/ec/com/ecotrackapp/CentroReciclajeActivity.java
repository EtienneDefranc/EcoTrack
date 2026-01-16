package ec.com.ecotrackapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import ec.com.ecotrackapp.controller.SistemaEcoTrack;
import ec.com.ecotrackapp.models.Residuo;
import ec.com.ecotrackapp.tda.ArrayList;
import ec.com.ecotrackapp.tda.ListaCircularDoble;

import ec.com.ecotrackapp.tda.List;
import ec.com.ecotrackapp.tda.TdaListAdapter;

public class CentroReciclajeActivity extends AppCompatActivity {

    private SistemaEcoTrack sistema;
    private ListView lvCentro;
    private MaterialButton btnProcesar, btnRetirar, btnActualizar, btnVolver;
    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_centro_reciclaje);

        sistema = SistemaEcoTrack.getInstance(this);

        inicializarVistas();
        configurarListeners();
        actualizarCentro();
    }

    private void inicializarVistas() {
        lvCentro = findViewById(R.id.lvCentro);
        btnProcesar = findViewById(R.id.btnProcesar);
        btnRetirar = findViewById(R.id.btnRetirar);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnVolver = findViewById(R.id.btnVolver);
        tvInfo = findViewById(R.id.tvInfo);
    }

    private void configurarListeners() {
        btnProcesar.setOnClickListener(v -> procesarResiduo());
        btnRetirar.setOnClickListener(v -> retirarResiduo());
        btnActualizar.setOnClickListener(v -> actualizarCentro());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void procesarResiduo() {
        ListaCircularDoble<Residuo> listaResiduos = sistema.getResiduos();

        if (listaResiduos.estaVacia()) {
            Toast.makeText(this, "No hay residuos para procesar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Procesar el primer residuo disponible
        Residuo residuo = listaResiduos.obtener(0);
        sistema.procesarEnCentroReciclaje(residuo);
        listaResiduos.eliminar(residuo);
        sistema.guardarDatos(this);

        Toast.makeText(this, "Residuo procesado en el centro", Toast.LENGTH_SHORT).show();
        actualizarCentro();
    }

    private void retirarResiduo() {
        Residuo residuo = sistema.retirarDelCentroReciclaje();
        if (residuo != null) {
            Toast.makeText(this, "Residuo retirado: " + residuo.getNombre(),
                Toast.LENGTH_SHORT).show();
            sistema.guardarDatos(this);
            actualizarCentro();
        } else {
            Toast.makeText(this, "El centro está vacío", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarCentro() {
        List<String> items = new ArrayList<>();

        int totalResiduos = sistema.getCentroReciclaje().getTamanio();

        if (totalResiduos == 0) {
            items.add("");
            items.add("📭 El centro está vacío");
            items.add("");
            items.add("💡 Usa 'Procesar' para agregar");
            items.add("   residuos al centro desde");
            items.add("   la cola de recolección");
        } else {
            items.add("📊 Información del Centro:");
            items.add("━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            items.add("");
            items.add(String.format(java.util.Locale.getDefault(), "📦 Total de residuos: %d", totalResiduos));
            items.add("");
            items.add("━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            items.add("🔝 Último residuo (TOPE):");
            items.add("━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            try {
                Residuo tope = sistema.getCentroReciclaje().verTope();
                items.add("");
                items.add("📌 " + tope.getNombre());
                items.add("🏷️  Tipo: " + tope.getTipo().getNombre());
                items.add("⚖️  Peso: " + String.format(java.util.Locale.getDefault(), "%.2f kg", tope.getPeso()));
                if (tope.getZona() != null && !tope.getZona().isEmpty()) {
                    items.add("📍 Zona: " + tope.getZona());
                }
            } catch (Exception e) {
                items.add("⚠️ Error al obtener información");
            }

            items.add("");
            items.add("━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            items.add("ℹ️ Estructura LIFO (Pila)");
            items.add("   El último en entrar es");
            items.add("   el primero en salir");
        }

        lvCentro.setAdapter(new TdaListAdapter(this, items));

        tvInfo.setText(String.format(java.util.Locale.getDefault(), "Residuos en centro: %d", totalResiduos));
    }
}
