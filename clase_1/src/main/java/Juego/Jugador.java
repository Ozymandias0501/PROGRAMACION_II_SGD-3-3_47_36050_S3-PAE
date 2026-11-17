package Juego;

public class Jugador {

    private final int id;
    private final String nombre;
    private boolean vivo;
    private int puntos;
    private int victorias;

    Jugador(int id, String nombre) {
        if (id <= 0) {
            throw new IllegalArgumentException("El identificador debe ser un número positivo.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío.");
        }
        this.id = id;
        this.nombre = nombre.trim();
        this.vivo = true;
    }
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isVivo() {
        return vivo;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getVictorias() {
        return victorias;
    }

    void sumarPuntos(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("No se pueden sumar puntos negativos.");
        }
        puntos += cantidad;
    }

    void registrarVictoria() {
        victorias++;
    }

    void eliminar() {
        if (!vivo) {
            throw new IllegalStateException("El jugador " + nombre + " ya estaba eliminado.");
        }
        vivo = false;
    }

    void reiniciarEstado() {
        vivo = true;
    }

    void reiniciarEstadisticas() {
        puntos = 0;
        victorias = 0;
        vivo = true;
    }

    @Override
    public String toString() {
        return "Jugador{id=" + id + ", nombre=" + nombre + ", vivo=" + vivo
                + ", puntos=" + puntos + ", victorias=" + victorias + '}';
    }
}
