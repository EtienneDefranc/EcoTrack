package ec.com.ecotrackapp.models;

import ec.com.ecotrackapp.tda.List;
import ec.com.ecotrackapp.tda.Map;

/**
 * Representa el estado completo del sistema
 * exportar/importar en JSON.
 *
 */
public class EstadoSistema {

    public List<Zona> zonas;
    public List<Residuo> residuos;
    public List<VehiculoRecolector> vehiculos;
    public Map<String, Object> estadisticas;
    public EstadoSistema() {
    }
}
