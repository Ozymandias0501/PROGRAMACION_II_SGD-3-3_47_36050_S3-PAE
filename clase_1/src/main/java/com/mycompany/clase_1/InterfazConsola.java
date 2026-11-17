package com.mycompany.clase_1;

import Juego.Jugador;
import Juego.MotorJuego;
import Juego.RegistroRonda;
import Juego.ResultadoPartida;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class InterfazConsola {

    private final MotorJuego motor;
    private final Scanner entrada;

    public InterfazConsola(MotorJuego motor, Scanner entrada) {
        this.motor = motor;
        this.entrada = entrada;
    }

    public static void main(String[] args) {
        try (Scanner sr = new Scanner(System.in)) {
            new InterfazConsola(new MotorJuego(), sr).iniciar();
        }
    }

    public void iniciar() {
        try {
            boolean salir = false;
            while (!salir) {
                mostrarMenu();
                switch (leerLinea("Opción: ").trim()) {
                    case "1":
                        registrarJugadores();
                        break;
                    case "2":
                        jugarPartida();
                        break;
                    case "3":
                        mostrarClasificacion();
                        break;
                    case "4":
                        mostrarHistorial();
                        break;
                    case "0":
                        salir = true;
                        break;
                    default:
                        System.out.println("Error: opción no válida.");
                }
            }
            System.out.println("Hasta pronto.");
        } catch (NoSuchElementException e) {
            System.out.println("\nEntrada finalizada. Hasta pronto.");
        }
    }
    private void mostrarMenu() {
        System.out.println();
        System.out.println("===== JUEGO DE REVÓLVER =====");
        System.out.println("1. Registrar jugadores");
        System.out.println("2. Jugar una partida");
        System.out.println("3. Ver clasificación");
        System.out.println("4. Ver historial");
        System.out.println("0. Salir");
    }

    private void registrarJugadores() {
        try {
            motor.limpiarJugadores();
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }
        int cantidad = leerEntero("Ingrese un número entre " + MotorJuego.MIN_JUGADORES
                + " y " + MotorJuego.MAX_JUGADORES + ": ", MotorJuego.MIN_JUGADORES, MotorJuego.MAX_JUGADORES);
        for (int i = 1; i <= cantidad; i++) {
            registrarUnJugador(i);
        }
        System.out.println("Jugadores registrados:");
        for (Jugador jugador : motor.getJugadores()) {
            System.out.println("  #" + jugador.getId() + " " + jugador.getNombre());
        }
    }
    private void registrarUnJugador(int numero) {
        while (true) {
            String nombre = leerLinea("Nombre del jugador " + numero + ": ");
            try {
                motor.registrarJugador(nombre);
                return;
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void jugarPartida() {
        if (!motor.puedeIniciarPartida()) {
            System.out.println("Error: primero registre al menos " + MotorJuego.MIN_JUGADORES
                    + " jugadores (opción 1).");
            return;
        }
        int rondas = leerEntero("Número de rondas (" + MotorJuego.MIN_RONDAS + " a "
                + MotorJuego.MAX_RONDAS + "): ", MotorJuego.MIN_RONDAS, MotorJuego.MAX_RONDAS);
        motor.iniciarPartida(rondas);
        System.out.println();
        System.out.println("##### PARTIDA " + motor.getNumeroPartida() + ": " + rondas + " ronda(s), "
                + motor.getJugadores().size() + " jugadores #####");

        while (motor.hayPartidaEnCurso()) {
            System.out.println();
            System.out.println("--- RONDA " + motor.getNumeroRondaActual() + " de " + motor.getTotalRondas() + " ---");
            leerLinea("Presione Enter para iniciar la ronda...");
            mostrarRonda(motor.jugarRonda());
            mostrarClasificacion();
        }
        mostrarResultadoFinal(motor.getHistorial().get(motor.getHistorial().size() - 1));
    }

    private void mostrarClasificacion() {
        List<Jugador> clasificacion = motor.getClasificacion();
        if (clasificacion.isEmpty()) {
            System.out.println("No hay jugadores registrados.");
            return;
        }
        System.out.println();
        System.out.println("Clasificación:");
        System.out.println(String.format("  %-4s %-15s %8s %10s", "Pos.", "Jugador", "Puntos", "Victorias"));
        int posicion = 1;
        for (Jugador jugador : clasificacion) {
            System.out.println(String.format("  %-4d %-15s %8d %10d",
                    posicion++, jugador.getNombre(), jugador.getPuntos(), jugador.getVictorias()));
        }
    }

    private void mostrarHistorial() {
        List<ResultadoPartida> historial = motor.getHistorial();
        System.out.println();
        if (historial.isEmpty()) {
            System.out.println("Aún no hay partidas finalizadas.");
            return;
        }
        System.out.println("Historial (" + motor.getPartidasFinalizadas() + " partida(s) finalizada(s)):");
        for (ResultadoPartida partida : historial) {
            System.out.println();
            System.out.println("Partida " + partida.getNumeroPartida() + " - "
                    + descripcionCampeones(partida) + " (" + partida.getRondas().size() + " ronda(s))");
            for (RegistroRonda ronda : partida.getRondas()) {
                System.out.println("  Ronda " + ronda.getNumeroRonda() + ": gana " + ronda.getGanador()
                        + " | eliminados: " + String.join(", ", ronda.getEliminadosEnOrden())
                        + " | disparos: " + ronda.getTurnos().size());
                System.out.println("    Participantes: " + String.join(", ", ronda.getParticipantes()));
                System.out.println("    Puntos de la ronda: " + formatear(ronda.getPuntosGanados()));
            }
            System.out.println("  Resultado final: " + formatear(partida.getPuntosFinales()) + " puntos");
        }
    }

    private void mostrarRonda(RegistroRonda ronda) {
        int numero = 1;
        for (RegistroRonda.Turno turno : ronda.getTurnos()) {
            if (turno.isEliminado()) {
                System.out.println("  Disparo " + numero + ": " + turno.getJugador() + " dispara... BANG! Eliminado.");
            } else {
                System.out.println("  Disparo " + numero + ": " + turno.getJugador()
                        + " dispara... clic. Supera el turno (+" + MotorJuego.PUNTOS_TURNO_SUPERADO + " punto).");
            }
            numero++;
        }
        System.out.println(">> Gana la ronda " + ronda.getNumeroRonda() + ": " + ronda.getGanador()
                + " (+" + MotorJuego.PUNTOS_VICTORIA_RONDA + " puntos)");
        System.out.println("   Puntos de la ronda: " + formatear(ronda.getPuntosGanados()));
    }

    private void mostrarResultadoFinal(ResultadoPartida resultado) {
        System.out.println();
        System.out.println("########## FIN DE LA PARTIDA " + resultado.getNumeroPartida() + " ##########");
        System.out.println(descripcionCampeones(resultado));
        if (resultado.isEmpate()) {
            System.out.println("(Igualados en puntos y en victorias: se declaran campeones compartidos.)");
        }
        System.out.println("Partidas finalizadas en esta ejecución: " + motor.getPartidasFinalizadas());
    }

    private static String descripcionCampeones(ResultadoPartida partida) {
        if (partida.isEmpate()) {
            return "EMPATE, campeones compartidos: " + String.join(", ", partida.getCampeones());
        }
        return "CAMPEÓN: " + partida.getCampeones().get(0);
    }

    private static String formatear(Map<String, Integer> valores) {
        StringBuilder texto = new StringBuilder();
        for (Map.Entry<String, Integer> entrada : valores.entrySet()) {
            if (texto.length() > 0) {
                texto.append(", ");
            }
            texto.append(entrada.getKey()).append(' ').append(entrada.getValue());
        }
        return texto.toString();
    }

    private String leerLinea(String mensaje) {
        System.out.print(mensaje);
        return entrada.nextLine();
    }

    private int leerEntero(String mensaje, int minimo, int maximo) {
        while (true) {
            String texto = leerLinea(mensaje).trim();
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
                System.out.println("Error: el número debe estar entre " + minimo + " y " + maximo + ".");
            } catch (NumberFormatException e) {
                System.out.println("Error: ingrese un número entero.");
            }
        }
    }
}
