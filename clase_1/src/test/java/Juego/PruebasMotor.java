package Juego;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class PruebasMotor {

    private static int ejecutadas;
    private static int fallidas;

    private PruebasMotor() {
    }

    public static void main(String[] args) {
        System.out.println("=== Pruebas del Juego de Revólver ===");

        titulo("Revolver (corrección de los defectos del original)");
        pruebasRevolver();
        titulo("Jugador");
        pruebasJugador();
        titulo("Registro de jugadores");
        pruebasRegistro();
        titulo("Inicio de partida y validaciones");
        pruebasInicioDePartida();
        titulo("Partida de 2 rondas con 3 jugadores (empate de campeones)");
        MotorJuego motor = pruebasPartidaConEmpate();
        titulo("Segunda partida: reinicio de estadísticas, historial y contador");
        pruebasSegundaPartida(motor);
        titulo("Criterios de desempate");
        pruebasDesempate();
        titulo("Invariantes en 1000 partidas simuladas (semilla fija)");
        pruebaDeInvariantes();

        System.out.println();
        System.out.println("Resumen: " + (ejecutadas - fallidas) + " superadas, " + fallidas
                + " fallidas, " + ejecutadas + " verificaciones en total.");
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    private static void pruebasRevolver() {
        for (int bala = 0; bala < Revolver.CAPACIDAD; bala++) {
            Revolver revolver = new Revolver(new Guion(0, bala));
            verificarIgual("Posición actual 0, bala en " + bala + ": dispara en el disparo " + (bala + 1),
                    bala + 1, disparosHastaBala(revolver));
        }

        Revolver conGiro = new Revolver(new Guion(3, 0));
        verificarIgual("Posición actual 3, bala en 0: el tambor da la vuelta y dispara en el 4.º disparo "
                + "(el original nunca disparaba)", 4, disparosHastaBala(conGiro));

        Revolver ciclo = new Revolver(new Guion(0, 2));
        disparosHastaBala(ciclo);
        boolean cincoSeguros = true;
        for (int i = 0; i < Revolver.CAPACIDAD - 1; i++) {
            cincoSeguros &= !ciclo.disparar();
        }
        verificar("Tras un disparo con bala, los 5 siguientes son seguros (el original repetía el disparo con bala)",
                cincoSeguros);
        verificar("...y el 6.º, al completar la vuelta, vuelve a tener bala", ciclo.disparar());

        Revolver reiniciado = new Revolver(new Guion(0, 0, 0, 3));
        verificar("Antes de girar: bala en la posición actual", reiniciado.disparar());
        reiniciado.girar();
        verificarIgual("girar() vuelve a sortear: ahora la bala sale en el 4.º disparo", 4, disparosHastaBala(reiniciado));

        verificarExcepcion("Fuente de aleatoriedad nula se rechaza", IllegalArgumentException.class,
                () -> new Revolver(null));

        int giros = 100000;
        int[] porDisparo = new int[Revolver.CAPACIDAD + 1];
        boolean dentroDelLimite = true;
        Revolver real = new Revolver();
        for (int i = 0; i < giros; i++) {
            real.girar();
            int disparos = disparosHastaBala(real);
            if (disparos > Revolver.CAPACIDAD) {
                dentroDelLimite = false;
            } else {
                porDisparo[disparos]++;
            }
        }
        verificar("Con azar real, la bala sale siempre en " + Revolver.CAPACIDAD + " disparos o menos (" + giros + " giros)",
                dentroDelLimite);
        boolean uniforme = true;
        for (int k = 1; k <= Revolver.CAPACIDAD; k++) {
            double porcentaje = 100.0 * porDisparo[k] / giros;
            uniforme &= Math.abs(porcentaje - 100.0 / Revolver.CAPACIDAD) < 1.5;
        }
        verificar("...y el disparo con bala es equiprobable entre 1 y 6 (±1,5 %)", uniforme);
    }

    private static int disparosHastaBala(Revolver revolver) {
        int disparos = 1;
        while (!revolver.disparar() && disparos <= Revolver.CAPACIDAD) {
            disparos++;
        }
        return disparos;
    }

    private static void pruebasJugador() {
        Jugador jugador = new Jugador(1, "  Ana ");
        verificarIgual("El nombre se recorta", "Ana", jugador.getNombre());
        verificar("Un jugador nuevo está vivo, con 0 puntos y 0 victorias (nunca nulo)",
                jugador.isVivo() && jugador.getPuntos() == 0 && jugador.getVictorias() == 0);
        verificarExcepcion("Identificador no positivo se rechaza", IllegalArgumentException.class,
                () -> new Jugador(0, "Ana"));
        verificarExcepcion("Nombre nulo se rechaza", IllegalArgumentException.class, () -> new Jugador(1, null));
        verificarExcepcion("Nombre en blanco se rechaza", IllegalArgumentException.class, () -> new Jugador(1, "   "));
        verificarExcepcion("Sumar puntos negativos se rechaza", IllegalArgumentException.class,
                () -> jugador.sumarPuntos(-1));

        jugador.sumarPuntos(5);
        jugador.registrarVictoria();
        jugador.eliminar();
        verificar("Eliminar deja al jugador fuera de la ronda", !jugador.isVivo());
        verificarExcepcion("Eliminar dos veces se rechaza", IllegalStateException.class, jugador::eliminar);
        jugador.reiniciarEstado();
        verificar("reiniciarEstado() lo reactiva SIN borrar puntos ni victorias",
                jugador.isVivo() && jugador.getPuntos() == 5 && jugador.getVictorias() == 1);
        jugador.reiniciarEstadisticas();
        verificar("reiniciarEstadisticas() pone a cero puntos y victorias",
                jugador.getPuntos() == 0 && jugador.getVictorias() == 0 && jugador.isVivo());
    }

    private static void pruebasRegistro() {
        MotorJuego motor = new MotorJuego();
        Jugador ana = motor.registrarJugador("  Ana ");
        verificar("Registrar un jugador válido lo incorpora con el nombre recortado",
                motor.getJugadores().size() == 1 && "Ana".equals(ana.getNombre()));
        verificarExcepcion("Nombre nulo se rechaza", IllegalArgumentException.class, () -> motor.registrarJugador(null));
        verificarExcepcion("Nombre vacío se rechaza", IllegalArgumentException.class, () -> motor.registrarJugador(""));
        verificarExcepcion("Nombre solo con espacios se rechaza", IllegalArgumentException.class,
                () -> motor.registrarJugador("    "));
        verificarExcepcion("Nombre duplicado se rechaza", IllegalArgumentException.class,
                () -> motor.registrarJugador("Ana"));
        verificarExcepcion("Duplicado sin distinguir mayúsculas se rechaza", IllegalArgumentException.class,
                () -> motor.registrarJugador("ANA"));
        verificarExcepcion("Nombre demasiado largo se rechaza", IllegalArgumentException.class,
                () -> motor.registrarJugador("123456789012345X"));
        verificarIgual("Los intentos inválidos no agregaron jugadores", 1, motor.getJugadores().size());

        for (String nombre : new String[] {"Beto", "Caro", "Dani", "Eva", "Fran"}) {
            motor.registrarJugador(nombre);
        }
        verificarIgual("Se llegó al máximo de jugadores", MotorJuego.MAX_JUGADORES, motor.getJugadores().size());
        verificarExcepcion("Superar el máximo se rechaza", IllegalStateException.class,
                () -> motor.registrarJugador("Gaby"));

        boolean idsUnicos = true;
        for (int i = 0; i < motor.getJugadores().size(); i++) {
            idsUnicos &= motor.getJugadores().get(i).getId() == i + 1;
        }
        verificar("Los identificadores son únicos y correlativos (1..6), asignados por el motor", idsUnicos);
        verificarExcepcion("La lista expuesta es de solo lectura", UnsupportedOperationException.class,
                () -> motor.getJugadores().clear());

        motor.limpiarJugadores();
        verificarIgual("limpiarJugadores() deja la lista vacía", 0, motor.getJugadores().size());
        verificarIgual("...y los identificadores vuelven a empezar en 1", 1, motor.registrarJugador("Zoe").getId());
    }

    private static void pruebasInicioDePartida() {
        MotorJuego motor = new MotorJuego();
        verificar("Sin jugadores no se puede iniciar", !motor.puedeIniciarPartida());
        verificarExcepcion("Iniciar con 0 jugadores se bloquea", IllegalStateException.class,
                () -> motor.iniciarPartida(3));
        motor.registrarJugador("Ana");
        verificarExcepcion("Iniciar con 1 jugador se bloquea", IllegalStateException.class,
                () -> motor.iniciarPartida(3));
        motor.registrarJugador("Beto");
        verificar("Con 2 jugadores ya se puede iniciar", motor.puedeIniciarPartida());
        verificarExcepcion("0 rondas se rechaza", IllegalArgumentException.class, () -> motor.iniciarPartida(0));
        verificarExcepcion("Rondas negativas se rechazan", IllegalArgumentException.class, () -> motor.iniciarPartida(-2));
        verificarExcepcion("Más del máximo de rondas se rechaza", IllegalArgumentException.class,
                () -> motor.iniciarPartida(MotorJuego.MAX_RONDAS + 1));
        verificarExcepcion("Jugar una ronda sin partida iniciada se rechaza", IllegalStateException.class,
                motor::jugarRonda);

        motor.iniciarPartida(2);
        verificar("Tras iniciar hay partida en curso, ronda 1 de 2",
                motor.hayPartidaEnCurso() && motor.getNumeroRondaActual() == 1 && motor.getTotalRondas() == 2);
        verificarExcepcion("Registrar jugadores con la partida en curso se rechaza", IllegalStateException.class,
                () -> motor.registrarJugador("Caro"));
        verificarExcepcion("Limpiar jugadores con la partida en curso se rechaza", IllegalStateException.class,
                motor::limpiarJugadores);
        verificarExcepcion("Iniciar otra partida con una en curso se rechaza", IllegalStateException.class,
                () -> motor.iniciarPartida(3));
    }

    private static MotorJuego pruebasPartidaConEmpate() {
        MotorJuego motor = motorConGiros(0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0);
        motor.registrarJugador("Ana");
        motor.registrarJugador("Beto");
        motor.registrarJugador("Caro");
        motor.iniciarPartida(2);

        RegistroRonda ronda1 = motor.jugarRonda();
        verificarIgual("Ronda 1: gana Ana", "Ana", ronda1.getGanador());
        verificarIgual("Ronda 1: 3 disparos en total", 3, ronda1.getTurnos().size());
        verificarIgual("Ronda 1: empieza Ana", "Ana", ronda1.getTurnos().get(0).getJugador());
        verificar("Ronda 1: Ana supera su turno y Beto queda eliminado",
                !ronda1.getTurnos().get(0).isEliminado() && ronda1.getTurnos().get(1).isEliminado());
        verificarIgual("Ronda 1: orden de eliminación Beto, Caro", Arrays.asList("Beto", "Caro"), ronda1.getEliminadosEnOrden());
        verificarIgual("Ronda 1: participantes registrados", Arrays.asList("Ana", "Beto", "Caro"), ronda1.getParticipantes());
        verificarIgual("Ronda 1: puntos de la ronda Ana=4 (1 turno + 3 por ganar), Beto=0, Caro=0",
                Arrays.asList(4, 0, 0), valores(ronda1));
        verificarIgual("Ana acumula 4 puntos y 1 victoria",
                Arrays.asList(4, 1), Arrays.asList(jugador(motor, "Ana").getPuntos(), jugador(motor, "Ana").getVictorias()));
        verificarIgual("La ronda actual pasa a la 2", 2, motor.getNumeroRondaActual());
        verificar("La partida sigue en curso", motor.hayPartidaEnCurso());

        RegistroRonda ronda2 = motor.jugarRonda();
        verificarIgual("Ronda 2: el primer turno rota y empieza Beto", "Beto", ronda2.getTurnos().get(0).getJugador());
        verificarIgual("Ronda 2: gana Caro", "Caro", ronda2.getGanador());
        verificarIgual("Ronda 2: 4 disparos", 4, ronda2.getTurnos().size());
        verificar("Reiniciar el estado temporal no borró los puntos de Ana (siguen en 4)",
                jugador(motor, "Ana").getPuntos() == 4);
        verificarIgual("Puntos finales Ana, Beto, Caro",
                Arrays.asList(4, 1, 4),
                Arrays.asList(jugador(motor, "Ana").getPuntos(), jugador(motor, "Beto").getPuntos(),
                        jugador(motor, "Caro").getPuntos()));
        verificar("La partida finalizó", !motor.hayPartidaEnCurso());
        verificarIgual("Contador de partidas finalizadas", 1, motor.getPartidasFinalizadas());

        ResultadoPartida resultado = motor.getHistorial().get(0);
        verificarIgual("Campeones compartidos Ana y Caro", Arrays.asList("Ana", "Caro"), resultado.getCampeones());
        verificar("El resultado marca empate", resultado.isEmpate());
        verificarIgual("El historial guarda las 2 rondas", 2, resultado.getRondas().size());
        verificarIgual("El historial guarda la clasificación final (puntos)",
                Arrays.asList("Ana", "Caro", "Beto"), Arrays.asList(resultado.getPuntosFinales().keySet().toArray()));
        verificarIgual("Beto tiene 0 victorias", 0, resultado.getVictoriasFinales().get("Beto"));
        verificarExcepcion("Jugar una ronda con la partida finalizada se rechaza", IllegalStateException.class,
                motor::jugarRonda);
        verificarExcepcion("El historial expuesto es de solo lectura", UnsupportedOperationException.class,
                () -> motor.getHistorial().clear());
        return motor;
    }

    private static void pruebasSegundaPartida(MotorJuego motor) {
        verificar("Se puede iniciar otra partida con los mismos jugadores", motor.puedeIniciarPartida());
        motor.iniciarPartida(1);
        verificarIgual("Es la partida número 2", 2, motor.getNumeroPartida());
        verificar("Puntos y victorias se reiniciaron con la partida nueva",
                jugador(motor, "Ana").getPuntos() == 0 && jugador(motor, "Ana").getVictorias() == 0
                        && jugador(motor, "Caro").getPuntos() == 0);
        RegistroRonda ronda = motor.jugarRonda();
        verificarIgual("Gana Caro", "Caro", ronda.getGanador());
        verificarIgual("Caro suma 3 puntos (ganó sin disparar)", 3, jugador(motor, "Caro").getPuntos());
        verificarIgual("El historial acumula 2 partidas", 2, motor.getHistorial().size());
        verificarIgual("Campeón único de la partida 2", Arrays.asList("Caro"), motor.getHistorial().get(1).getCampeones());
        verificar("La partida 1 del historial no cambió",
                motor.getHistorial().get(0).getCampeones().size() == 2 && motor.getHistorial().get(0).getRondas().size() == 2);
        verificarIgual("Contador de partidas finalizadas = 2", 2, motor.getPartidasFinalizadas());
        motor.limpiarJugadores();
        verificar("limpiarJugadores() conserva el historial", motor.getHistorial().size() == 2);
    }

    private static void pruebasDesempate() {
        Jugador masPuntos = jugadorCon(1, "A", 7, 0);
        Jugador menosPuntos = jugadorCon(2, "B", 6, 2);
        verificar("Primer criterio: más puntos gana aunque tenga menos victorias",
                MotorJuego.compararClasificacion(masPuntos, menosPuntos) < 0
                        && MotorJuego.compararClasificacion(menosPuntos, masPuntos) > 0);
        Jugador conVictoria = jugadorCon(3, "C", 6, 1);
        Jugador sinVictoria = jugadorCon(4, "D", 6, 0);
        verificar("Segundo criterio: con los mismos puntos gana quien tiene más victorias",
                MotorJuego.compararClasificacion(conVictoria, sinVictoria) < 0
                        && MotorJuego.compararClasificacion(sinVictoria, conVictoria) > 0);
        verificarIgual("Igualdad total: el comparador devuelve 0 (campeones compartidos)", 0,
                MotorJuego.compararClasificacion(jugadorCon(5, "E", 4, 1), jugadorCon(6, "F", 4, 1)));
    }

    private static void pruebaDeInvariantes() {
        Random azar = new Random(2024);
        int violaciones = 0;
        int rondasJugadas = 0;
        int partidas = 1000;
        for (int i = 0; i < partidas; i++) {
            MotorJuego motor = new MotorJuego(new Revolver(new Random(azar.nextLong())));
            int cantidad = MotorJuego.MIN_JUGADORES + azar.nextInt(MotorJuego.MAX_JUGADORES - MotorJuego.MIN_JUGADORES + 1);
            int rondas = MotorJuego.MIN_RONDAS + azar.nextInt(MotorJuego.MAX_RONDAS);
            for (int j = 1; j <= cantidad; j++) {
                motor.registrarJugador("J" + j);
            }
            motor.iniciarPartida(rondas);
            while (motor.hayPartidaEnCurso()) {
                motor.jugarRonda();
            }
            rondasJugadas += rondas;
            if (!cumpleInvariantes(motor, cantidad, rondas)) {
                violaciones++;
            }
        }
        verificarIgual(partidas + " partidas (" + rondasJugadas + " rondas) terminan y cumplen todas las invariantes",
                0, violaciones);
    }

    private static boolean cumpleInvariantes(MotorJuego motor, int cantidad, int rondas) {
        ResultadoPartida resultado = motor.getHistorial().get(motor.getHistorial().size() - 1);
        if (resultado.getRondas().size() != rondas || resultado.getCampeones().isEmpty()) {
            return false;
        }
        int victoriasTotales = 0;
        for (Jugador jugador : motor.getJugadores()) {
            int esperadoPuntos = 0;
            int esperadoVictorias = 0;
            for (RegistroRonda ronda : resultado.getRondas()) {
                esperadoPuntos += ronda.getPuntosGanados().get(jugador.getNombre());
                if (ronda.getGanador().equals(jugador.getNombre())) {
                    esperadoVictorias++;
                }
                int superados = 0;
                for (RegistroRonda.Turno turno : ronda.getTurnos()) {
                    if (turno.getJugador().equals(jugador.getNombre()) && !turno.isEliminado()) {
                        superados++;
                    }
                }
                int bonificacion = ronda.getGanador().equals(jugador.getNombre()) ? MotorJuego.PUNTOS_VICTORIA_RONDA : 0;
                if (ronda.getPuntosGanados().get(jugador.getNombre())
                        != superados * MotorJuego.PUNTOS_TURNO_SUPERADO + bonificacion) {
                    return false;
                }
            }
            if (jugador.getPuntos() != esperadoPuntos || jugador.getVictorias() != esperadoVictorias) {
                return false;
            }
            victoriasTotales += jugador.getVictorias();
        }
        for (RegistroRonda ronda : resultado.getRondas()) {
            boolean unaSolaEliminacionPorJugador = ronda.getEliminadosEnOrden().size() == cantidad - 1
                    && !ronda.getEliminadosEnOrden().contains(ronda.getGanador());
            boolean dentroDelLimite = ronda.getTurnos().size() <= Revolver.CAPACIDAD * (cantidad - 1);
            if (!unaSolaEliminacionPorJugador || !dentroDelLimite) {
                return false;
            }
        }
        if (victoriasTotales != rondas) {
            return false;
        }
        int maximo = 0;
        for (Jugador jugador : motor.getJugadores()) {
            maximo = Math.max(maximo, jugador.getPuntos());
        }
        for (String campeon : resultado.getCampeones()) {
            if (jugador(motor, campeon).getPuntos() != maximo) {
                return false;
            }
        }
        return true;
    }

    private static final class Guion extends Random {
        private static final long serialVersionUID = 1L;
        private final int[] valores;
        private int siguiente;

        Guion(int... valores) {
            this.valores = valores;
        }

        @Override
        public int nextInt(int limite) {
            if (siguiente >= valores.length) {
                throw new IllegalStateException("Se agotó el guion de la prueba.");
            }
            int valor = valores[siguiente++];
            if (valor < 0 || valor >= limite) {
                throw new IllegalStateException("Valor de guion fuera de rango: " + valor);
            }
            return valor;
        }
    }

    private static MotorJuego motorConGiros(int... valores) {
        int[] guion = new int[valores.length + 2];
        System.arraycopy(valores, 0, guion, 2, valores.length);
        return new MotorJuego(new Revolver(new Guion(guion)));
    }

    private static Jugador jugador(MotorJuego motor, String nombre) {
        for (Jugador jugador : motor.getJugadores()) {
            if (jugador.getNombre().equals(nombre)) {
                return jugador;
            }
        }
        throw new IllegalStateException("La prueba busca un jugador inexistente: " + nombre);
    }

    private static List<Integer> valores(RegistroRonda ronda) {
        return Arrays.asList(ronda.getPuntosGanados().values().toArray(new Integer[0]));
    }

    private static Jugador jugadorCon(int id, String nombre, int puntos, int victorias) {
        Jugador jugador = new Jugador(id, nombre);
        jugador.sumarPuntos(puntos);
        for (int i = 0; i < victorias; i++) {
            jugador.registrarVictoria();
        }
        return jugador;
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("-- " + texto);
    }

    private static void verificar(String caso, boolean condicion) {
        ejecutadas++;
        if (condicion) {
            System.out.println("  [OK]     " + caso);
        } else {
            fallidas++;
            System.out.println("  [FALLO]  " + caso);
        }
    }

    private static void verificarIgual(String caso, Object esperado, Object obtenido) {
        ejecutadas++;
        if (Objects.equals(esperado, obtenido)) {
            System.out.println("  [OK]     " + caso);
        } else {
            fallidas++;
            System.out.println("  [FALLO]  " + caso + " -> esperado: " + esperado + ", obtenido: " + obtenido);
        }
    }

    private static void verificarExcepcion(String caso, Class<? extends RuntimeException> tipo, Runnable accion) {
        ejecutadas++;
        try {
            accion.run();
            fallidas++;
            System.out.println("  [FALLO]  " + caso + " -> no lanzó " + tipo.getSimpleName());
        } catch (RuntimeException e) {
            if (tipo.isInstance(e)) {
                System.out.println("  [OK]     " + caso);
            } else {
                fallidas++;
                System.out.println("  [FALLO]  " + caso + " -> lanzó " + e.getClass().getSimpleName());
            }
        }
    }
}
