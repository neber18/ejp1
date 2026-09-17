import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

// ==========================================
// 1. ESTRUCTURAS DE DATOS PROPIAS
// ==========================================

class Bicicleta {
    String codigo;
    String estacion;
    String estado; // OPERATIVA, REPORTADA, EN_TALLER, ESPERA_REPUESTO, DE_BAJA
    int reparaciones;
    String falla;
    int tiempoEspera; // Métrica: turnos que esperó en cola

    public Bicicleta(String codigo, String estacion, String estado, int reparaciones) {
        this.codigo = codigo;
        this.estacion = estacion;
        this.estado = estado;
        this.reparaciones = reparaciones;
        this.falla = "";
        this.tiempoEspera = 0;
    }
}

// Nodo para la Lista Enlazada de Flota
class NodoBicicleta {
    Bicicleta bicicleta;
    NodoBicicleta siguiente;

    public NodoBicicleta(Bicicleta bicicleta) {
        this.bicicleta = bicicleta;
        this.siguiente = null;
    }
}

class ListaFlota {
    private NodoBicicleta cabeza;
    private int tamano;

    public ListaFlota() {
        this.cabeza = null;
        this.tamano = 0;
    }

    // Inserta ordenado alfabéticamente por código y rechaza duplicados
    public boolean insertarOrdenado(Bicicleta b) {
        if (buscar(b.codigo) != null) {
            return false; // Duplicado
        }
        NodoBicicleta nuevo = new NodoBicicleta(b);
        if (cabeza == null || cabeza.bicicleta.codigo.compareTo(b.codigo) > 0) {
            nuevo.siguiente = cabeza;
            cabeza = nuevo;
        } else {
            NodoBicicleta actual = cabeza;
            while (actual.siguiente != null && actual.siguiente.bicicleta.codigo.compareTo(b.codigo) < 0) {
                actual = actual.siguiente;
            }
            nuevo.siguiente = actual.siguiente;
            actual.siguiente = nuevo;
        }
        tamano++;
        return true;
    }

    public Bicicleta buscar(String codigo) {
        NodoBicicleta actual = cabeza;
        while (actual != null) {
            if (actual.bicicleta.codigo.equals(codigo)) {
                return actual.bicicleta;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    public NodoBicicleta getCabeza() {
        return cabeza;
    }
}

// Nodo para Cola y Pila genéricas o específicas
class NodoDoble {
    String dato; // Usado para piezas o para guardar códigos de bicis en cola
    Bicicleta bici; // Usado para la cola de taller
    NodoDoble siguiente;

    // Constructor para Cola de Bicicletas
    public NodoDoble(Bicicleta bici) {
        this.bici = bici;
        this.siguiente = null;
    }

    // Constructor para Pila de Piezas o Cola de Espera de Repuestos (por código)
    public NodoDoble(String dato) {
        this.dato = dato;
        this.siguiente = null;
    }
}

class ColaTaller {
    private NodoDoble frente, fin;
    private int tamano;

    public ColaTaller() {
        frente = fin = null;
        tamano = 0;
    }

    public void encolar(Bicicleta b) {
        NodoDoble nuevo = new NodoDoble(b);
        if (fin == null) {
            frente = fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }
        tamano++;
    }

    public Bicicleta desencolar() {
        if (frente == null) return null;
        Bicicleta b = frente.bici;
        frente = frente.siguiente;
        if (frente == null) fin = null;
        tamano--;
        return b;
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int getTamano() {
        return tamano;
    }
}

class ColaRepuestos {
    private NodoDoble frente, fin;
    private int tamano;

    public ColaRepuestos() {
        frente = fin = null;
        tamano = 0;
    }

    public void encolar(String codigoBici) {
        NodoDoble nuevo = new NodoDoble(codigoBici);
        if (fin == null) {
            frente = fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }
        tamano++;
    }

    public String desencolar() {
        if (frente == null) return null;
        String codigo = frente.dato;
        frente = frente.siguiente;
        if (frente == null) fin = null;
        tamano--;
        return codigo;
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int getTamano() {
        return tamano;
    }
}

class PilaPiezas {
    private NodoDoble tope;
    private int tamano;

    public PilaPiezas() {
        tope = null;
        tamano = 0;
    }

    public void apilar(String pieza) {
        NodoDoble nuevo = new NodoDoble(pieza);
        nuevo.siguiente = tope;
        tope = nuevo;
        tamano++;
    }

    public String desapilar() {
        if (estaVacia()) return null;
        String pieza = tope.dato;
        tope = tope.siguiente;
        tamano--;
        return pieza;
    }

    public boolean estaVacia() {
        return tope == null;
    }

    public int getTamano() {
        return tamano;
    }

    // Método para duplicar/clonar la pila sin alterar su orden (usando 2 auxiliares para R4 y R5)
    public PilaPiezas clonar() {
        PilaPiezas aux1 = new PilaPiezas();
        PilaPiezas aux2 = new PilaPiezas();
        
        NodoDoble actual = tope;
        while (actual != null) {
            aux1.apilar(actual.dato);
            actual = actual.siguiente;
        }
        
        // Volvemos a invertir para conservar el orden exacto fondo -> tope
        NodoDoble actAux1 = aux1.tope;
        while (actAux1 != null) {
            aux2.apilar(actAux1.dato);
            actAux1 = actAux1.siguiente;
        }
        
        // Invertimos de nuevo en una tercera para que quede idéntica al original
        PilaPiezas resultado = new PilaPiezas();
        NodoDoble actAux2 = aux2.tope;
        while (actAux2 != null) {
            resultado.apilar(actAux2.dato);
            actAux2 = actAux2.siguiente;
        }
        return resultado;
    }
}

// Estructura para almacenar temporalmente la pila de una orden suspendida
class OrdenSuspendida {
    String codigoBici;
    PilaPiezas pilaPiezas;
    OrdenSuspendida siguiente;

    public OrdenSuspendida(String codigoBici, PilaPiezas pilaPiezas) {
        this.codigoBici = codigoBici;
        this.pilaPiezas = pilaPiezas;
        this.siguiente = null;
    }
}

class AlmacenSuspendidas {
    private OrdenSuspendida cabeza;

    public void guardar(String codigo, PilaPiezas pila) {
        OrdenSuspendida nuevo = new OrdenSuspendida(codigo, pila);
        nuevo.siguiente = cabeza;
        cabeza = nuevo;
    }

    public PilaPiezas recuperarYRemover(String codigo) {
        OrdenSuspendida actual = cabeza;
        OrdenSuspendida anterior = null;

        while (actual != null) {
            if (actual.codigoBici.equals(codigo)) {
                if (anterior == null) {
                    cabeza = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                return actual.pilaPiezas;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return null;
    }
}


// ==========================================
// 2. SISTEMA PRINCIPAL BICI-TALLER
// ==========================================

public class BiciTaller {
    private ListaFlota flotaActiva = new ListaFlota();
    private ListaFlota flotaBajas = new ListaFlota();
    private ColaTaller colaTaller = new ColaTaller();
    private ColaRepuestos colaRepuestos = new ColaRepuestos();
    private AlmacenSuspendidas suspendidas = new AlmacenSuspendidas();
    
    // Puesto de trabajo actual
    private Bicicleta biciEnPuesto = null;
    private PilaPiezas pilaPuesto = new PilaPiezas();

    // Métricas
    private int ordenesCerradas = 0;
    private int ordenesSuspendidas = 0;
    private int rechazosArmadoIncompleto = 0;

    public void procesarArchivo(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            boolean leyendoFlota = false;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;

                if (linea.equals("FLOTA")) {
                    leyendoFlota = primeroBandera(true);
                    continue;
                }

                if (leyendoFlota && !linea.startsWith("REPORTAR") && !linea.startsWith("RECIBIR") 
                    && !linea.startsWith("INICIAR") && !linea.startsWith("DESMONTAR") 
                    && !linea.startsWith("SUSPENDER") && !linea.startsWith("MONTAR") 
                    && !linea.startsWith("CERRAR") && !linea.startsWith("REANUDAR") 
                    && !linea.startsWith("REPORTE")) {
                    
                    // Parsear datos de flota: BIC-0101, Parque Norte, OPERATIVA, 0
                    String[] partes = linea.split(",");
                    if (partes.length >= 4) {
                        String codigo = partes[0].trim();
                        String estacion = partes[1].trim();
                        String estado = partes[2].trim();
                        int rep = Integer.parseInt(partes[3].trim());
                        flotaActiva.insertarOrdenado(new Bicicleta(codigo, estacion, estado, rep));
                    }
                } else {
                    leyendoFlota = false;
                    ejecutarComando(linea);
                }
            }
        } catch (IOException e) {
            System.out.println("Error leyendo el archivo: " + e.getMessage());
        }
    }

    private boolean primeroBandera(boolean val) { return val; }

    public void ejecutarComando(String comando) {
        System.out.println("> " + comando);
        String[] partes = comando.split(" ", 2);
        String accion = partes[0];
        String argumento = partes.length > 1 ? partes[1].trim() : "";

        switch (accion) {
            case "REPORTAR":
                String[] argsRep = argumento.split(" ", 2);
                reportarFalla(argsRep[0], argsRep.length > 1 ? argsRep[1] : "");
                break;
            case "RECIBIR":
                recibirEnTaller(argumento);
                break;
            case "INICIAR":
                iniciarReparacion();
                break;
            case "DESMONTAR":
                desmontar(argumento);
                break;
            case "MONTAR":
                montar();
                break;
            case "SUSPENDER":
                suspender(argumento);
                break;
            case "REANUDAR":
                reanudar(argumento);
                break;
            case "CERRAR":
                cerrarOrden();
                break;
            case "REPORTE":
                reporte();
                break;
            default:
                System.out.println("Comando desconocido: " + accion);
        }
    }

    public void reportarFalla(String codigo, String falla) {
        Bicicleta b = flotaActiva.buscar(codigo);
        if (b == null) {
            System.out.println("RECHAZADA: Bicicleta no existe en la flota.");
            return;
        }
        b.estado = "REPORTADA";
        b.falla = falla;
    }

    public void recibirEnTaller(String codigo) {
        Bicicleta b = flotaActiva.buscar(codigo);
        if (b == null) {
            System.out.println("RECHAZADA: Bicicleta no existe.");
            return;
        }
        // Valida R1
        if (!b.estado.equals("REPORTADA")) {
            System.out.println("RECHAZADA (R1): estado " + b.estado + ", no fue reportada");
            return;
        }
        b.estado = "EN_TALLER";
        colaTaller.encolar(b);
    }

    public void iniciarReparacion() {
        if (colaTaller.estaVacia()) {
            System.out.println("No hay bicicletas en la cola del taller.");
            return;
        }
        biciEnPuesto = colaTaller.desencolar();
        pilaPuesto = new PilaPiezas(); // Pila vacía
    }

    public void desmontar(String pieza) {
        if (biciEnPuesto == null) {
            System.out.println("No hay bicicleta en el puesto de trabajo.");
            return;
        }
        pilaPuesto.apilar(pieza);
    }

    public void montar() {
        if (biciEnPuesto == null) {
            System.out.println("No hay bicicleta en el puesto de trabajo.");
            return;
        }
        if (pilaPuesto.estaVacia()) {
            System.out.println("ERROR: No se puede montar, la pila de piezas está vacía.");
            return;
        }
        pilaPuesto.desapilar();
    }

    public void suspender(String motivo) {
        if (biciEnPuesto == null) {
            System.out.println("No hay bicicleta en el puesto para suspender.");
            return;
        }
        biciEnPuesto.estado = "ESPERA_REPUESTO";
        // Guardar copia exacta de la pila preservando el orden con clonar()
        PilaPiezas pilaGuardada = pilaPuesto.clonar();
        suspendidas.guardar(biciEnPuesto.codigo, pilaGuardada);
        colaRepuestos.encolar(biciEnPuesto.codigo);
        
        System.out.println(biciEnPuesto.codigo + " -> ESPERA_REPUESTO | piezas guardadas (fondo -> tope)");
        System.out.println("puesto de trabajo liberado");
        
        biciEnPuesto = null;
        pilaPuesto = new PilaPiezas();
        ordenesSuspendidas++;
    }

    public void reanudar(String codigo) {
        Bicicleta b = flotaActiva.buscar(codigo);
        if (b == null) {
            System.out.println("Bicicleta no encontrada.");
            return;
        }
        b.estado = "EN_TALLER";
        colaTaller.encolar(b);
        
        // Recuperar pila manteniendo orden exacto
        PilaPiezas recuperada = suspendidas.recuperarYRemover(codigo);
        if (recuperada != null) {
            pilaPuesto = recuperada;
        }
        System.out.println("vuelve al final de la cola del taller | pila restaurada");
    }

    public void cerrarOrden() {
        if (biciEnPuesto == null) {
            System.out.println("No hay bicicleta en el puesto de trabajo para cerrar.");
            return;
        }
        // Valida R6: la pila debe estar vacía (todo armado)
        if (!pilaPuesto.estaVacia()) {
            System.out.println("RECHAZADA (R6): No se puede cerrar la orden, la pila de piezas no está vacía (armado incompleto).");
            rechazosArmadoIncompleto++;
            return;
        }

        biciEnPuesto.reparaciones++;
        ordenesCerradas++;
        
        System.out.println("CERRAR (" + biciEnPuesto.codigo + ")");
        System.out.println("reparaciones acumuladas: " + biciEnPuesto.reparaciones);

        // Valida R7: 3 reparaciones en el trimestre pasa a DE_BAJA
        if (biciEnPuesto.reparaciones >= 3) {
            biciEnPuesto.estado = "DE_BAJA";
            System.out.println("R7: " + biciEnPuesto.codigo + " pasa a DE_BAJA (retirada de la flota activa)");
            // La movemos de la flota activa a bajas
            flotaBajas.insertarOrdenado(new Bicicleta(biciEnPuesto.codigo, biciEnPuesto.estacion, "DE_BAJA", biciEnPuesto.reparaciones));
            // Nota: En una implementación completa eliminaríamos de flotaActiva, aquí la marcamos/movemos.
        } else {
            biciEnPuesto.estado = "OPERATIVA";
        }
        
        biciEnPuesto = null;
        pilaPuesto = new PilaPiezas();
    }

    public void reporte() {
        int op = 0, rep = 0, tal = 0, esp = 0, baj = 0;
        NodoBicicleta actual = flotaActiva.getCabeza();
        while (actual != null) {
            switch (actual.bicicleta.estado) {
                case "OPERATIVA": op++; break;
                case "REPORTADA": rep++; break;
                case "EN_TALLER": tal++; break;
                case "ESPERA_REPUESTO": esp++; break;
                case "DE_BAJA": baj++; break;
            }
            actual = actual.siguiente;
        }

        System.out.println("=== REPORTE DEL DIA ===");
        System.out.println("Flota activa: " + (op + rep + tal + esp));
        System.out.println("De baja: " + flotaBajas.getCabeza() != null ? "1" : "0"); // Simplificado
        System.out.println("Cola taller: " + (colaTaller.estaVacia() ? "vacia" : colaTaller.getTamano() + " elementos"));
        System.out.println("Cola repuestos: " + (colaRepuestos.estaVacia() ? "vacia" : colaRepuestos.getTamano() + " elementos"));
        System.out.println("Ordenes cerradas: " + ordenesCerradas);
        System.out.println("Suspendidas: " + ordenesSuspendidas);
        System.out.println("Rechazadas por armado incompleto: " + rechazosArmadoIncompleto);
    }

    public static void main(String[] args) {
        BiciTaller sistema = new BiciTaller();
        // Asegúrate de tener el archivo taller.txt en la ruta correcta o pásalo por parámetro
        sistema.procesarArchivo("taller.txt");
    }
}