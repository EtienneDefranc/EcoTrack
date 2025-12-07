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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import ec.com.ecotrackapp.models.Zona;

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

        inicializarVistas();
        configurarListeners();
        cargarDatosDePrueba();
        actualizarEstadisticasRapidas();
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
        cardRegistro.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegistroResiduoActivity.class));
        });

        cardResiduos.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ResiduosActivity.class));
        });

        cardVehiculos.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, VehiculosActivity.class));
        });

        cardCentro.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CentroReciclajeActivity.class));
        });

        cardEstadisticas.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EstadisticasActivity.class));
        });

        cardZonas.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ZonasActivity.class));
        });
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
        if (sistema.getResiduos().getTamanio() == 0) {
            // Agregar datos de prueba solo la primera vez
            sistema.registrarVehiculo("ABC-123", "Norte", 1000, 9);
            sistema.registrarVehiculo("XYZ-789", "Sur", 800, 7);
            sistema.registrarVehiculo("DEF-456", "Centro", 1200, 8);
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
