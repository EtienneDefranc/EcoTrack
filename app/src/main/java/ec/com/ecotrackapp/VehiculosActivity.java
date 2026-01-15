package ec.com.ecotrackapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ec.com.ecotrackapp.controller.SistemaEcoTrack;
import ec.com.ecotrackapp.models.VehiculoRecolector;
import java.util.Locale;

public class VehiculosActivity extends AppCompatActivity {

    private SistemaEcoTrack sistema;
    private ListView lvVehiculos;
    private Button btnDespachar, btnActualizar, btnVolver;
    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehiculos);

        sistema = SistemaEcoTrack.getInstance(this);

        inicializarVistas();
        configurarListeners();
        actualizarListaVehiculos();
    }

    private void inicializarVistas() {
        lvVehiculos = findViewById(R.id.lvVehiculos);
        btnDespachar = findViewById(R.id.btnDespachar);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnVolver = findViewById(R.id.btnVolver);
        tvInfo = findViewById(R.id.tvInfo);
    }

    private void configurarListeners() {
        btnDespachar.setOnClickListener(v -> despacharVehiculo());
        btnActualizar.setOnClickListener(v -> actualizarListaVehiculos());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void despacharVehiculo() {
        VehiculoRecolector vehiculo = sistema.despacharVehiculo();
        if (vehiculo != null) {
            Toast.makeText(this, "Vehículo despachado: " + vehiculo.getPlaca(),
                Toast.LENGTH_SHORT).show();
            sistema.guardarDatos(this);
            actualizarListaVehiculos();
        } else {
            Toast.makeText(this, "No hay vehículos disponibles", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarListaVehiculos() {
        java.util.List<String> items = new java.util.ArrayList<>();

        items.add("=== VEHÍCULOS EN RUTA ===");
        if (sistema.getVehiculosEnRuta().isEmpty()) {
            items.add("No hay vehículos en ruta");
        } else {
            for (VehiculoRecolector v : sistema.getVehiculosEnRuta()) {
                items.add(v.toString());
            }
        }

        items.add("");
        items.add("=== VEHÍCULOS DISPONIBLES ===");
        if (sistema.getVehiculosDisponibles().estaVacia()) {
            items.add("No hay vehículos disponibles");
        } else {
            items.add("Total en cola: " + sistema.getVehiculosDisponibles().getTamanio());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            items
        );
        lvVehiculos.setAdapter(adapter);

        tvInfo.setText(String.format(Locale.getDefault(), "Disponibles: %d | En Ruta: %d",
            sistema.getVehiculosDisponibles().getTamanio(),
            sistema.getVehiculosEnRuta().size()));
    }
}
