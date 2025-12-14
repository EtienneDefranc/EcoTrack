package ec.com.ecotrackapp.models;

import java.util.List;
import java.util.Map;

/**
 * Representa el estado completo del sistema
 * exportar/importar en JSON.
 *
 */
public class EstadoSistema {

    public List<Zona> zonas;
    public List<Residuo> residuos;
    public List<VehiculoRecolector> vehiculos;
    public EstadoSistema() {
    }
}
