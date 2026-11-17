package Juego;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ResultadoPartida {

    private final int numeroPartida;
    private final List<RegistroRonda> rondas;
    private final List<String> campeones;
    private final Map<String, Integer> puntosFinales;
    private final Map<String, Integer> victoriasFinales;

    ResultadoPartida(int numeroPartida, List<RegistroRonda> rondas, List<String> campeones,
                    Map<String, Integer> puntosFinales, Map<String, Integer> victoriasFinales) {
        this.numeroPartida = numeroPartida;
        this.rondas = Collections.unmodifiableList(new ArrayList<>(rondas));
        this.campeones = Collections.unmodifiableList(new ArrayList<>(campeones));
        this.puntosFinales = Collections.unmodifiableMap(new LinkedHashMap<>(puntosFinales));
        this.victoriasFinales = Collections.unmodifiableMap(new LinkedHashMap<>(victoriasFinales));
    }

    public int getNumeroPartida() {
        return numeroPartida;
    }

    public List<RegistroRonda> getRondas() {
        return rondas;
    }

    public List<String> getCampeones() {
        return campeones;
    }

    public Map<String, Integer> getPuntosFinales() {
        return puntosFinales;
    }

    public Map<String, Integer> getVictoriasFinales() {
        return victoriasFinales;
    }

    public boolean isEmpate() {
        return campeones.size() > 1;
    }
}
