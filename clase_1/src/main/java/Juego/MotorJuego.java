package Juego;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MotorJuego {

    public static final int MIN_JUGADORES = 2;
    public static final int MAX_JUGADORES = 6;
    public static final int MIN_RONDAS = 1;
    public static final int MAX_RONDAS = 10;
    public static final int LONGITUD_MAX_NOMBRE = 15;

    public static final int PUNTOS_TURNO_SUPERADO = 1;
    public static final int PUNTOS_VICTORIA_RONDA = 3;

    private final Revolver revolver;
    private final List<Jugador> jugadores = new ArrayList<>();
    private final List<RegistroRonda> rondasDePartida = new ArrayList<>();
    private final List<ResultadoPartida> historial = new ArrayList<>();

    private int siguienteId = 1;
    private int numeroPartida;
    private int totalRondas;
    private boolean partidaEnCurso;

    public MotorJuego() {
        this(new Revolver());
    }

    public MotorJuego(Revolver revolver) {
        this.revolver = Objects.requireNonNull(revolver, "El revólver no puede ser nulo.");
    }

    public Jugador registrarJugador(String nombre) {
        exigirSinPartida("registrar jugadores");
        if (jugadores.size() >= MAX_JUGADORES) {
            throw new IllegalStateException("Se alcanzó el máximo de " + MAX_JUGADORES + " jugadores.");
        }
        String limpio = validarNombre(nombre);
        Jugador jugador = new Jugador(siguienteId++, limpio);
        jugadores.add(jugador);
        return jugador;
    }

    public void limpiarJugadores() {
        exigirSinPartida("cambiar los jugadores");
        jugadores.clear();
        rondasDePartida.clear();
        siguienteId = 1;
    }
    
    public boolean puedeIniciarPartida() {
        return !partidaEnCurso && jugadores.size() >= MIN_JUGADORES;
    }

    public void iniciarPartida(int rondas) {
        exigirSinPartida("iniciar otra partida");
        if (jugadores.size() < MIN_JUGADORES) {
            throw new IllegalStateException("Se necesitan al menos " + MIN_JUGADORES + " jugadores para iniciar.");
        }
        if (rondas < MIN_RONDAS || rondas > MAX_RONDAS) {
            throw new IllegalArgumentException(
                    "El número de rondas debe estar entre " + MIN_RONDAS + " y " + MAX_RONDAS + ".");
        }
        for (Jugador jugador : jugadores) {
            jugador.reiniciarEstadisticas();
        }
        rondasDePartida.clear();
        totalRondas = rondas;
        numeroPartida++;
        partidaEnCurso = true;
    }

    public RegistroRonda jugarRonda() {
        if (!partidaEnCurso) {
            throw new IllegalStateException("No hay una partida en curso: inicie una con iniciarPartida().");
        }
        for (Jugador jugador : jugadores) {
            jugador.reiniciarEstado();
        }
        revolver.girar();

        List<RegistroRonda.Turno> turnos = new ArrayList<>();
        Map<String, Integer> puntosGanados = new LinkedHashMap<>();
        List<String> participantes = new ArrayList<>();
        for (Jugador jugador : jugadores) {
            participantes.add(jugador.getNombre());
            puntosGanados.put(jugador.getNombre(), 0);
        }

        int indice = rondasDePartida.size() % jugadores.size();
        while (contarVivos() > 1) {
            Jugador jugador = jugadores.get(indice);
            if (jugador.isVivo()) {
                boolean eliminado = revolver.disparar();
                turnos.add(new RegistroRonda.Turno(jugador.getNombre(), eliminado));
                if (eliminado) {
                    jugador.eliminar();
                    if (contarVivos() > 1) {
                        revolver.girar();
                    }
                } else {
                    jugador.sumarPuntos(PUNTOS_TURNO_SUPERADO);
                    puntosGanados.merge(jugador.getNombre(), PUNTOS_TURNO_SUPERADO, Integer::sum);
                }
            }
            indice = (indice + 1) % jugadores.size();
        }

        Jugador ganador = buscarSuperviviente();
        ganador.registrarVictoria();
        ganador.sumarPuntos(PUNTOS_VICTORIA_RONDA);
        puntosGanados.merge(ganador.getNombre(), PUNTOS_VICTORIA_RONDA, Integer::sum);

        RegistroRonda registro = new RegistroRonda(numeroPartida, rondasDePartida.size() + 1,
                ganador.getNombre(), participantes, turnos, puntosGanados);
        rondasDePartida.add(registro);
        if (rondasDePartida.size() == totalRondas) {
            finalizarPartida();
        }
        return registro;
    }

    
    public boolean hayPartidaEnCurso() {
        return partidaEnCurso;
    }

    public int getNumeroPartida() {
        return numeroPartida;
    }

    public int getNumeroRondaActual() {
        return partidaEnCurso ? rondasDePartida.size() + 1 : 0;
    }

    public int getTotalRondas() {
        return totalRondas;
    }

    public int getPartidasFinalizadas() {
        return historial.size();
    }

    public List<Jugador> getJugadores() {
        return Collections.unmodifiableList(jugadores);
    }

    public List<Jugador> getClasificacion() {
        List<Jugador> copia = new ArrayList<>(jugadores);
        copia.sort(MotorJuego::compararClasificacion);
        return Collections.unmodifiableList(copia);
    }

    public List<ResultadoPartida> getHistorial() {
        return Collections.unmodifiableList(historial);
    }


    static int compararClasificacion(Jugador a, Jugador b) {
        int resultado = Integer.compare(b.getPuntos(), a.getPuntos());
        if (resultado == 0) {
            resultado = Integer.compare(b.getVictorias(), a.getVictorias());
        }
        return resultado;
    }

    private void finalizarPartida() {
        List<Jugador> clasificacion = getClasificacion();
        Jugador lider = clasificacion.get(0);
        List<String> campeones = new ArrayList<>();
        Map<String, Integer> puntosFinales = new LinkedHashMap<>();
        Map<String, Integer> victoriasFinales = new LinkedHashMap<>();
        for (Jugador jugador : clasificacion) {
            puntosFinales.put(jugador.getNombre(), jugador.getPuntos());
            victoriasFinales.put(jugador.getNombre(), jugador.getVictorias());
            if (compararClasificacion(jugador, lider) == 0) {
                campeones.add(jugador.getNombre());
            }
        }
        historial.add(new ResultadoPartida(numeroPartida, rondasDePartida, campeones,
                puntosFinales, victoriasFinales));
        partidaEnCurso = false;
    }

    private int contarVivos() {
        int vivos = 0;
        for (Jugador jugador : jugadores) {
            if (jugador.isVivo()) {
                vivos++;
            }
        }
        return vivos;
    }

    private Jugador buscarSuperviviente() {
        for (Jugador jugador : jugadores) {
            if (jugador.isVivo()) {
                return jugador;
            }
        }
        throw new IllegalStateException("No quedó ningún jugador vivo.");
    }

    private String validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        String limpio = nombre.trim();
        if (limpio.length() > LONGITUD_MAX_NOMBRE) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los " + LONGITUD_MAX_NOMBRE + " caracteres.");
        }
        for (Jugador jugador : jugadores) {
            if (jugador.getNombre().equalsIgnoreCase(limpio)) {
                throw new IllegalArgumentException("Ya existe un jugador llamado \"" + jugador.getNombre() + "\".");
            }
        }
        return limpio;
    }

    private void exigirSinPartida(String accion) {
        if (partidaEnCurso) {
            throw new IllegalStateException("No se puede " + accion + " mientras hay una partida en curso.");
        }
    }
}
