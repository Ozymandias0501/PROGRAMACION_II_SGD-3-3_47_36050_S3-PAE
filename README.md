Juego de Revólver — proyecto corregido y ampliado (Java nativo, consola)
Evolución del proyecto original eje_1_T.zip (NetBeans). El código fuente usa únicamente sintaxis y APIs de Java 8, por lo que funciona con cualquier JDK 8 o superior.
Estructura
eje_1_T_corregido/
├── clase_1/                      <- proyecto corregido (se compila desde aquí)
│   ├── pom.xml                   <- original, sin cambios; NO se usa para compilar
│   └── src/
│       ├── main/java/Juego/                  Jugador, Revolver, MotorJuego, RegistroRonda, ResultadoPartida
│       ├── main/java/com/mycompany/clase_1/  InterfazConsola   (punto de entrada)
│       └── test/java/Juego/                  PruebasMotor      (pruebas sin JUnit)
├── respaldo_original_clase_1/    <- copia intacta del ZIP recibido (no se compila)
├── docs/                         <- informe técnico (PDF) y diagrama UML
└── evidencias/                   <- salidas reales de ejecución (ver tabla al final)
Compilar y ejecutar (PowerShell, desde la carpeta clase_1)
cd .\clase_1

# 1) Versiones instaladas
java -version
javac -version

# 2) Compilar el programa (la salida va a .\out, separada del código fuente)
$fuentes = Get-ChildItem .\src\main\java -Recurse -Filter *.java | ForEach-Object { $_.FullName }
New-Item -ItemType Directory -Force .\out | Out-Null
javac -encoding UTF-8 -d .\out $fuentes

# 3) Ejecutar
java -cp .\out com.mycompany.clase_1.InterfazConsola

# 4) Compilar y ejecutar las pruebas automáticas
$pruebas = Get-ChildItem .\src\test\java -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp .\out -d .\out $pruebas
java -cp .\out Juego.PruebasMotor
Equivalente en Linux/macOS (bash):
cd clase_1 && mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -cp out com.mycompany.clase_1.InterfazConsola
javac -encoding UTF-8 -cp out -d out $(find src/test/java -name "*.java")
java -cp out Juego.PruebasMotor
Cambio respecto al original: la clase principal pasó de com.mycompany.clase_1.Clase_1 a com.mycompany.clase_1.InterfazConsola (mismo paquete, mismo rol de punto de entrada).
Tildes en la consola. Si en PowerShell las tildes se ven como ?, es solo la codificación de la consola, no un error del programa. Ejecute antes [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 o inicie Java con java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp .\out com.mycompany.clase_1.InterfazConsola.
Cómo se juega
Menú: 1 registrar jugadores (2 a 6, nombres únicos), 2 jugar una partida (1 a 10 rondas; Enter inicia cada ronda), 3 ver clasificación, 4 ver historial, 0 salir.
Reglas (todas en MotorJuego):
Cada ronda: todos vivos y tambor girado. Los vivos disparan por turnos; el primer turno rota de una ronda a otra.
Disparo sin bala: el jugador supera el turno y suma 1 punto. Disparo con bala: queda eliminado de la ronda y el tambor se vuelve a girar si la ronda continúa.
La ronda termina cuando queda un solo jugador vivo: gana 3 puntos y 1 victoria. La ronda nunca termina en empate.
Campeón de la partida: más puntos; si empatan, más victorias; si persiste la igualdad, campeones compartidos.
Entre partidas se conservan jugadores e historial; puntos y victorias se reinician.
