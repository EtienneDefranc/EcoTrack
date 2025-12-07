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

}

