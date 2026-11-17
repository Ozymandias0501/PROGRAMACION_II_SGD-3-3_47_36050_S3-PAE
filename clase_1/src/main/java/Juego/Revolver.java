package Juego;

import java.util.Random;

public final class Revolver {

    public static final int CAPACIDAD = 6;

    private final Random random;
    private int posicionActual;
    private int posicionBala;

    public Revolver() {
        this(new Random());
    }

    public Revolver(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("La fuente de aleatoriedad no puede ser nula.");
        }
        this.random = random;
        girar();
    }

    public void girar() {
        posicionActual = random.nextInt(CAPACIDAD);
        posicionBala = random.nextInt(CAPACIDAD);
    }

    public boolean disparar() {
        boolean hayBala = (posicionActual == posicionBala);
        avanzar();
        return hayBala;
    }

    private void avanzar() {
        posicionActual = (posicionActual + 1) % CAPACIDAD;
    }

    @Override
    public String toString() {
        return "Revolver{posicion=" + posicionActual + "}";
    }
}
