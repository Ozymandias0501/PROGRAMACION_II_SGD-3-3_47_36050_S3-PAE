package Juego;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class RegistroRonda {

    public static final class Turno {
        private final String jugador;
        private final boolean eliminado;

        Turno(String jugador, boolean eliminado) {
            this.jugador = jugador;
            this.eliminado = eliminado;
        }

        public String getJugador() {
            return jugador;
        }

        public boolean isEliminado() {
            return eliminado;
        }
    }

    private final int numeroPartida;
    private final int numeroRonda;
    private final String ganador;
    private final List<String> participantes;
    private final List<Turno> turnos;
    private final Map<String, Integer> puntosGanados;

    RegistroRonda(int numeroPartida, int numeroRonda, String ganador, List<String> participantes,
                List<Turno> turnos, Map<String, Integer> puntosGanados) {
        this.numeroPartida = numeroPartida;
        this.numeroRonda = numeroRonda;
        this.ganador = ganador;
        this.participantes = Collections.unmodifiableList(new ArrayList<>(participantes));
        this.turnos = Collections.unmodifiableList(new ArrayList<>(turnos));
        this.puntosGanados = Collections.unmodifiableMap(new LinkedHashMap<>(puntosGanados));
    }

    public int getNumeroPartida() {
        return numeroPartida;
    }

    public int getNumeroRonda() {
        return numeroRonda;
    }

    public String getGanador() {
        return ganador;
    }

    public List<String> getParticipantes() {
        return participantes;
    }

    public List<Turno> getTurnos() {
        return turnos;
    }

    public Map<String, Integer> getPuntosGanados() {
        return puntosGanados;
    }

    public List<String> getEliminadosEnOrden() {
        List<String> eliminados = new ArrayList<>();
        for (Turno turno : turnos) {
            if (turno.isEliminado()) {
                eliminados.add(turno.getJugador());
            }
        }
        return eliminados;
    }
}
