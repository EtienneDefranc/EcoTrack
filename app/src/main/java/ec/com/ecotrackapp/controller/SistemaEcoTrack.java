package ec.com.ecotrackapp.controller;

import android.content.Context;
import ec.com.ecotrackapp.models.Residuo;
import ec.com.ecotrackapp.models.VehiculoRecolector;
import ec.com.ecotrackapp.models.Zona;
import ec.com.ecotrackapp.tda.ColaPrioridad;
import ec.com.ecotrackapp.tda.ListaCircularDoble;
import ec.com.ecotrackapp.tda.PilaReciclaje;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import ec.com.ecotrackapp.models.EstadoSistema;
import ec.com.ecotrackapp.models.Zona;
import ec.com.ecotrackapp.models.Residuo;
import ec.com.ecotrackapp.models.VehiculoRecolector;
/**
 * Singleton del Sistema EcoTrack para Android
 */
public class SistemaEcoTrack implements Serializable {
    private static final long serialVersionUID = 1L;
    private static SistemaEcoTrack instance;
    private static final String ARCHIVO_DATOS = "ecotrack_data.dat";

    private ListaCircularDoble<Residuo> residuos;
    private ColaPrioridad<VehiculoRecolector> vehiculosDisponibles;
    private PilaReciclaje<Residuo> centroReciclaje;
    private HashMap<String, Zona> zonas;
    private TreeMap<Residuo.TipoResiduo, Double> estadisticasPorTipo;
    private ArrayList<VehiculoRecolector> vehiculosEnRuta;

    private static final String NOMBRE_BACKUP = "ecotrack_backup.json";

    private SistemaEcoTrack() {
        this.residuos = new ListaCircularDoble<>();
        this.vehiculosDisponibles = new ColaPrioridad<>(
            (v1, v2) -> Integer.compare(v2.getPrioridad(), v1.getPrioridad())
        );
        this.centroReciclaje = new PilaReciclaje<>();
        this.zonas = new HashMap<>();
        this.estadisticasPorTipo = new TreeMap<>();
        this.vehiculosEnRuta = new ArrayList<>();

        inicializarEstadisticas();
    }

    public static synchronized SistemaEcoTrack getInstance(Context context) {
        if (instance == null) {
            instance = cargarDatos(context);
            if (instance == null) {
                instance = new SistemaEcoTrack();
            }
        }
        return instance;
    }

    private void inicializarEstadisticas() {
        for (Residuo.TipoResiduo tipo : Residuo.TipoResiduo.values()) {
            estadisticasPorTipo.put(tipo, 0.0);
        }
    }

    public void registrarResiduo(String nombre, Residuo.TipoResiduo tipo,
                                 double peso, LocalDate fecha, String zona,
                                 int prioridad) {
        Residuo residuo = new Residuo(nombre, tipo, peso, fecha, zona, prioridad);
        residuos.agregar(residuo);

        Zona zonaObj = zonas.getOrDefault(zona, new Zona(zona));
        zonaObj.agregarResiduoPendiente(peso);
        zonas.put(zona, zonaObj);

        double pesoActual = estadisticasPorTipo.get(tipo);
        estadisticasPorTipo.put(tipo, pesoActual + peso);
    }

    public void registrarVehiculo(String placa, String zona,
                                  double capacidad, int prioridad) {
        VehiculoRecolector vehiculo = new VehiculoRecolector(
            placa, zona, capacidad, prioridad
        );
        vehiculosDisponibles.encolar(vehiculo);
    }

    public VehiculoRecolector despacharVehiculo() {
        if (vehiculosDisponibles.estaVacia()) {
            return null;
        }

        VehiculoRecolector vehiculo = vehiculosDisponibles.desencolar();
        vehiculo.setEnRuta(true);
        vehiculosEnRuta.add(vehiculo);
        return vehiculo;
    }

    public void procesarEnCentroReciclaje(Residuo residuo) {
        centroReciclaje.apilar(residuo);

        Zona zona = zonas.get(residuo.getZona());
        if (zona != null) {
            zona.procesarResiduo(residuo.getPeso());
        }
    }

    public Residuo retirarDelCentroReciclaje() {
        if (centroReciclaje.estaVacia()) {
            return null;
        }
        return centroReciclaje.desapilar();
    }

    public List<Residuo> ordenarResiduos(Comparator<Residuo> comparador) {
        List<Residuo> lista = new ArrayList<>();
        for (Residuo residuo : residuos) {
            lista.add(residuo);
        }
        lista.sort(comparador);
        return lista;
    }

    public List<Zona> obtenerZonasCriticas() {
        List<Zona> criticas = new ArrayList<>();
        for (Zona zona : zonas.values()) {
            if (zona.esCritica()) {
                criticas.add(zona);
            }
        }
        criticas.sort((z1, z2) -> Double.compare(z1.calcularUtilidad(), z2.calcularUtilidad()));
        return criticas;
    }

    public List<Zona> obtenerTodasLasZonas() {
        List<Zona> lista = new ArrayList<>(zonas.values());
        lista.sort(Comparator.comparing(Zona::calcularUtilidad));
        return lista;
    }

    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalResiduos", residuos.getTamanio());
        stats.put("residuosEnCentro", centroReciclaje.getTamanio());
        stats.put("vehiculosDisponibles", vehiculosDisponibles.getTamanio());
        stats.put("vehiculosEnRuta", vehiculosEnRuta.size());
        stats.put("zonasTotales", zonas.size());
        stats.put("zonasCriticas", obtenerZonasCriticas().size());

        double pesoTotal = 0;
        for (double peso : estadisticasPorTipo.values()) {
            pesoTotal += peso;
        }
        stats.put("pesoTotal", pesoTotal);

        return stats;
    }

    public void guardarDatos(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(ARCHIVO_DATOS, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static SistemaEcoTrack cargarDatos(Context context) {
        try {
            FileInputStream fis = context.openFileInput(ARCHIVO_DATOS);
            ObjectInputStream ois = new ObjectInputStream(fis);
            SistemaEcoTrack sistema = (SistemaEcoTrack) ois.readObject();
            ois.close();
            fis.close();
            return sistema;
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    public ListaCircularDoble<Residuo> getResiduos() {
        return residuos;
    }

    public PilaReciclaje<Residuo> getCentroReciclaje() {
        return centroReciclaje;
    }

    public HashMap<String, Zona> getZonas() {
        return zonas;
    }

    public TreeMap<Residuo.TipoResiduo, Double> getEstadisticasPorTipo() {
        return estadisticasPorTipo;
    }

    public ColaPrioridad<VehiculoRecolector> getVehiculosDisponibles() {
        return vehiculosDisponibles;
    }

    public ArrayList<VehiculoRecolector> getVehiculosEnRuta() {
        return vehiculosEnRuta;
    }

    public List<Zona> obtenerZonasConUmbralSuperado() {
        List<Zona> resultado = new ArrayList<>();

        for (Zona z : zonas.values()) {
            if (z.superaUmbral()) {
                resultado.add(z);
            }
        }
        return resultado;
    }



    // tengo que modificarlo para que, confirmar la forma en la que accedo a
    // residuos y vehiculos
    private EstadoSistema construirEstado() {
        EstadoSistema estado = new EstadoSistema();

        // Si tienes HashMap<String, Zona> zonas;
        estado.zonas = new ArrayList<>(zonas.values());

        estado.residuos = new ArrayList<>();
        for (int i = 0; i < residuos.getTamanio(); i++) {
            estado.residuos.add(residuos.obtener(i));
        }

        // Vehículos
        estado.vehiculos = new ArrayList<>();
        for (int i = 0; i < vehiculosEnRuta.size(); i++) {
            estado.vehiculos.add(vehiculosEnRuta.get(i)); // ajusta
        }

        return estado;
    }
    public JSONArray zonasToJsonArray(HashMap<String, Zona> zonas) throws Exception {
        JSONArray arr = new JSONArray();
        for (Zona z : zonas.values()) {
            JSONObject o = new JSONObject();
            o.put("nombre", z.getNombre());
            o.put("pesoRecolectado", z.getPesoRecolectado());
            o.put("pesoPendiente", z.getPesoPendiente());
            o.put("cantidadResiduos", z.getCantidadResiduos());
            arr.put(o);
        }
        return arr;
    }
    public JSONArray residuosToJsonArray(ListaCircularDoble<Residuo> residuos) throws Exception {
        JSONArray arr = new JSONArray();

        for (Residuo r : residuos) {
            JSONObject o = new JSONObject();
            o.put("nombre", r.getNombre());
            o.put("tipo", r.getTipo().name());
            o.put("peso", r.getPeso());
            o.put("fecha", r.getFechaRecoleccion() != null ? r.getFechaRecoleccion().toString() : "");
            o.put("zona", r.getZona());
            o.put("prioridad", r.getPrioridadAmbiental());
            arr.put(o);
        }
        return arr;
    }

    public boolean exportarJson(Context context) {
        try {
            JSONObject root = new JSONObject();
            root.put("zonas", zonasToJsonArray(zonas));
            root.put("residuos", residuosToJsonArray(residuos));
            // root.put("vehiculos", vehiculosToJsonArray(vehiculos));

            File file = new File(context.getFilesDir(), "ecotrack_backup.json");
            FileWriter fw = new FileWriter(file);
            fw.write(root.toString(2)); // bonito
            fw.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean importarJson(Context context) {
        try {
            File file = new File(context.getFilesDir(), "ecotrack_backup.json");
            if (!file.exists()) return false;

            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            JSONObject root = new JSONObject(sb.toString());

            // Limpias estructuras
            zonas.clear();
            residuos.limpiar();
            // vehiculos.limpiar();

            // Zonas
            JSONArray jZonas = root.getJSONArray("zonas");
            for (int i = 0; i < jZonas.length(); i++) {
                JSONObject o = jZonas.getJSONObject(i);
                Zona z = new Zona(o.getString("nombre"));
                z.setPesoRecolectado(o.getDouble("pesoRecolectado"));
                z.setPesoPendiente(o.getDouble("pesoPendiente"));
                z.setCantidadResiduos(o.getInt("cantidadResiduos"));
                zonas.put(z.getNombre(), z);
            }

            // Reiniciar estadísticas
            inicializarEstadisticas();

// Residuos
            JSONArray jRes = root.getJSONArray("residuos");
            for (int i = 0; i < jRes.length(); i++) {
                JSONObject o = jRes.getJSONObject(i);

                String nombre = o.getString("nombre");
                Residuo.TipoResiduo tipo = Residuo.TipoResiduo.valueOf(o.getString("tipo"));
                double peso = o.getDouble("peso");
                String fechaStr = o.optString("fecha", "");
                String zona = o.getString("zona");
                int prioridad = o.getInt("prioridad");

                LocalDate fecha = null;
                if (!fechaStr.isEmpty()) {
                    fecha = LocalDate.parse(fechaStr);
                }

                Residuo r = new Residuo(
                        nombre,
                        tipo,
                        peso,
                        fecha,
                        zona,
                        prioridad
                );

                residuos.agregar(r);

                // Recalcular estadísticas
                estadisticasPorTipo.put(
                        tipo,
                        estadisticasPorTipo.get(tipo) + peso
                );
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}

