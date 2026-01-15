package ec.com.ecotrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import ec.com.ecotrackapp.controller.SistemaEcoTrack;

import java.util.Locale;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

// imports para manejar permiso para las notificaciones
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import ec.com.ecotrackapp.models.Zona;
import ec.com.ecotrackapp.models.Residuo;
import java.time.LocalDate;

public class MainActivity extends AppCompatActivity {

    private SistemaEcoTrack sistema;
    private TextView tvEstadisticasRapidas;
    private CardView cardRegistro, cardResiduos, cardVehiculos, cardCentro, cardEstadisticas, cardZonas;
    private static final int REQ_POST_NOTIFICATIONS = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar sistema
        sistema = SistemaEcoTrack.getInstance(this);
        crearCanalAlertas();
        verificarPermisoNotificaciones();

        MaterialButton btnExportarJson = findViewById(R.id.btnExportarJson);
        MaterialButton btnImportarJson = findViewById(R.id.btnImportarJson);

        btnExportarJson.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1002);
                    return;
                }
            }
            performExport();
        });

        btnImportarJson.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                 if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            1003);
                    return;
                }
            }
            performImport();
        });

        inicializarVistas();
        configurarListeners();
        cargarDatosDePrueba();
        actualizarEstadisticasRapidas();
    }

    private void performExport() {
        boolean ok = sistema.exportarJson(MainActivity.this);
        Toast.makeText(MainActivity.this,
                ok ? "Exportación JSON completada" : "Error al exportar JSON",
                Toast.LENGTH_SHORT).show();
    }

    private void performImport() {
        boolean ok = sistema.importarJson(MainActivity.this);
        Toast.makeText(MainActivity.this,
                ok ? "Importación JSON completada" : "Error al importar JSON",
                Toast.LENGTH_SHORT).show();

        if (ok) {
            actualizarEstadisticasRapidas();
        }
    }

    private void inicializarVistas() {
        tvEstadisticasRapidas = findViewById(R.id.tvEstadisticasRapidas);
        cardRegistro = findViewById(R.id.cardRegistro);
        cardResiduos = findViewById(R.id.cardResiduos);
        cardVehiculos = findViewById(R.id.cardVehiculos);
        cardCentro = findViewById(R.id.cardCentro);
        cardEstadisticas = findViewById(R.id.cardEstadisticas);
        cardZonas = findViewById(R.id.cardZonas);
    }

    private void configurarListeners() {
        cardRegistro.setOnClickListener(v ->
            startActivity(new Intent(MainActivity.this, RegistroResiduoActivity.class))
        );

        cardResiduos.setOnClickListener(v ->
            startActivity(new Intent(MainActivity.this, ResiduosActivity.class))
        );

        cardVehiculos.setOnClickListener(v ->
            startActivity(new Intent(MainActivity.this, VehiculosActivity.class))
        );

        cardCentro.setOnClickListener(v ->
            startActivity(new Intent(MainActivity.this, CentroReciclajeActivity.class))
        );

        cardEstadisticas.setOnClickListener(v ->
            startActivity(new Intent(MainActivity.this, EstadisticasActivity.class))
        );

        cardZonas.setOnClickListener(v ->
            startActivity(new Intent(MainActivity.this, ZonasActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarEstadisticasRapidas();
        revisarZonasYNotificar();
    }

    private void actualizarEstadisticasRapidas() {
        int totalResiduos = sistema.getResiduos().getTamanio();
        int residuosEnCentro = sistema.getCentroReciclaje().getTamanio();
        int vehiculosDisponibles = sistema.getVehiculosDisponibles().getTamanio();
        int zonasCriticas = sistema.obtenerZonasCriticas().size();

        String estadisticas = String.format(Locale.getDefault(),
            "📊 Resumen:\n" +
            "Residuos: %d | Centro: %d | Vehículos: %d | Zonas Críticas: %d",
            totalResiduos, residuosEnCentro, vehiculosDisponibles, zonasCriticas
        );

        tvEstadisticasRapidas.setText(estadisticas);
    }

    private void cargarDatosDePrueba() {
        if (sistema.getResiduos().estaVacia() && sistema.getVehiculosDisponibles().estaVacia()) {
            // --- Vehículos ---
            sistema.registrarVehiculo("ECO-001", "Norte", 1000, 9);
            sistema.registrarVehiculo("ECO-002", "Sur", 1500, 8);
            sistema.registrarVehiculo("ECO-003", "Centro", 1200, 10);
            sistema.registrarVehiculo("ECO-004", "Este", 800, 7);
            sistema.registrarVehiculo("ECO-005", "Oeste", 2000, 5);

            // --- Residuos ---

            // Zona Norte (Residuos variados)
            sistema.registrarResiduo("Botellas PET", Residuo.TipoResiduo.PLASTICO, 120.5, LocalDate.now().minusDays(1), "Norte", 5);
            sistema.registrarResiduo("Cajas de cartón", Residuo.TipoResiduo.PAPEL, 50.0, LocalDate.now(), "Norte", 3);

            // Zona Sur (Orgánicos y Vidrio)
            sistema.registrarResiduo("Restos de mercado", Residuo.TipoResiduo.ORGANICO, 300.0, LocalDate.now().minusDays(2), "Sur", 8);
            sistema.registrarResiduo("Botellas de vidrio", Residuo.TipoResiduo.VIDRIO, 150.0, LocalDate.now(), "Sur", 6);

            // Zona Centro (Alta densidad - Posible zona crítica)
            sistema.registrarResiduo("Estructuras metálicas", Residuo.TipoResiduo.METAL, 800.0, LocalDate.now().minusDays(5), "Centro", 9);
            sistema.registrarResiduo("Baterías usadas", Residuo.TipoResiduo.PELIGROSO, 25.0, LocalDate.now(), "Centro", 10);
            sistema.registrarResiduo("Documentos oficina", Residuo.TipoResiduo.PAPEL, 100.0, LocalDate.now(), "Centro", 4);
            sistema.registrarResiduo("Latas aluminio", Residuo.TipoResiduo.METAL, 45.0, LocalDate.now().minusDays(1), "Centro", 5);

            // Zona Oeste (Electrónicos)
            sistema.registrarResiduo("Monitores viejos", Residuo.TipoResiduo.ELECTRONICO, 60.0, LocalDate.now().minusDays(3), "Oeste", 7);
            sistema.registrarResiduo("Cables varios", Residuo.TipoResiduo.ELECTRONICO, 15.0, LocalDate.now(), "Oeste", 6);

            Toast.makeText(this, "Datos de prueba cargados automáticamente", Toast.LENGTH_SHORT).show();
        }
    }
    private void verificarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTIFICATIONS
                );
            }
        }
    }


    private void crearCanalAlertas() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "alertas_zonas",
                    "Alertas de zonas críticas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones cuando una zona supera el umbral de residuos");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void enviarAlertaZona(Zona zona) {

        Log.w("EcoAlertas", "Zona " + zona.getNombre() + " superó umbral");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "alertas_zonas")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Zona crítica: " + zona.getNombre())
                .setContentText("Ha superado el umbral de residuos.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {

            int notificationId = (int) (System.currentTimeMillis() & 0xfffffff);
            manager.notify(notificationId, builder.build());
        }
    }

    private void revisarZonasYNotificar() {
        for (Zona z : sistema.obtenerZonasConUmbralSuperado()) {
            enviarAlertaZona(z);
        }
    }
}
