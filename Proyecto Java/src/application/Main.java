package application;

import java.time.LocalDate;


import java.util.*;


public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final Sistema sistema = new Sistema();

    public static void main(String[] args) {
        seedData(); // Carga de datos de ejemplo
        System.out.println("Sistema de Gestión de Recetas: ");
        boolean salir = false;
        while (!salir) {
            try {
                showMainMenu();
                int opt = readInt("Elegir opción: ");
                switch (opt) {
                    case 1:
                        menuSecretaria();
                        break;
                    case 2:
                        menuMedico();
                        break;
                    case 3:
                        menuConsultas();
                        break;
                    case 4:
                        sistema.generarAlertas();
                    case 0:
                        salir = true;
                        break;
                    default:
                        System.out.println("Opción inválida. Intente nuevamente.");
                }
            } catch (InputMismatchException ime) {
                System.out.println("Entrada inválida. Debe ingresar un número.");
                sc.nextLine(); 
            } catch (Exception e) {
                System.out.println("Error inesperado: " + e.getMessage());
            }
            System.out.println();
        }
        System.out.println("Saliendo. ¡Hasta luego!");
    }

    // Creo el Munú Despegable:
    private static void showMainMenu() {
        System.out.println("\n--- Menú Principal ---");
        System.out.println("1. Acciones de Secretaria");
        System.out.println("2. Acciones de Médica");
        System.out.println("3. Consultas (historial / búsqueda)");
        System.out.println("4. Generar alertas de pendientes (sistema)");
        System.out.println("0. Salir");
    }


    private static void menuSecretaria() {
        System.out.println("\n--- Menú Secretaria ---");
        System.out.println("1. Registrar solicitud (manual)");
        System.out.println("2. Listar solicitudes pendientes");
        System.out.println("3. Cambiar estado de solicitud");
        System.out.println("0. Volver");
        int opt = readInt("Elegir: ");
        switch (opt) {
            case 1:
                registrarSolicitudManual();
                break;
            case 2:
                listarPorEstado(EstadoSolicitud.PENDIENTE);
                break;
            case 3:
                actualizarEstadoSolicitud();
                break;
            case 0:
                return;
            default:
                System.out.println("Opción inválida.");
        }
    }

    private static void menuMedico() {
        System.out.println("\n--- Menú Médica ---");
        System.out.println("1. Confeccionar receta (emitir)");
        System.out.println("2. Ver solicitudes en proceso");
        System.out.println("0. Volver");
        int opt = readInt("Elegir: ");
        switch (opt) {
            case 1:
                confeccionarReceta();
                break;
            case 2:
                listarPorEstado(EstadoSolicitud.EN_PROCESO);
                break;
            case 0:
                return;
            default:
                System.out.println("Opción inválida.");
        }
    }

    private static void menuConsultas() {
        System.out.println("\n--- Menú Consultas ---");
        System.out.println("1. Buscar solicitudes por DNI del paciente");
        System.out.println("2. Listar todas las solicitudes (ordenadas por fecha)");
        System.out.println("0. Volver");
        int opt = readInt("Elegir: ");
        switch (opt) {
            case 1:
                buscarPorDni();
                break;
            case 2:
                sistema.listarSolicitudesOrdenadasPorFecha();
                break;
            case 0:
                return;
            default:
                System.out.println("Opción inválida.");
        }
    }

    // Operaciones:
    private static void registrarSolicitudManual() {
        try {
            System.out.println("\nRegistrar nueva solicitud:");
            String dni = readString("DNI paciente: ");
            String nombre = readString("Nombre paciente: ");
            String apellido = readString("Apellido paciente: ");
            String med = readString("Medicamento (nombre y dosis): ");
            LocalDate fecha = LocalDate.now();
            Paciente p = sistema.findOrCreatePaciente(dni, nombre, apellido);
            Solicitud s = new Solicitud(p, med, fecha);
            sistema.agregarSolicitud(s);
            System.out.println("Solicitud registrada con ID: " + s.getId());
        } catch (DataValidationException dve) {
            System.out.println("Validación: " + dve.getMessage());
        }
    }

    private static void confeccionarReceta() {
        try {
            int id = readInt("ID de solicitud a confeccionar: ");
            Solicitud s = sistema.buscarSolicitudPorId(id);
            if (s.getEstado() != EstadoSolicitud.PENDIENTE && s.getEstado() != EstadoSolicitud.EN_PROCESO) {
                System.out.println("La solicitud no está en estado procesable. Estado actual: " + s.getEstado());
                return;
            }
            s.setEstado(EstadoSolicitud.EN_PROCESO);
            String texto = readString("Ingrese texto de la receta (prescripción): ");
            Receta r = new Receta(s.getPaciente(), texto, LocalDate.now());
            sistema.emitirRecetaParaSolicitud(s, r);
            System.out.println("Receta emitida y vinculada a la solicitud. Estado actualizado a: " + s.getEstado());
        } catch (NotFoundException nfe) {
            System.out.println("No encontrado: " + nfe.getMessage());
        } catch (Exception e) {
            System.out.println("Error al confeccionar receta: " + e.getMessage());
        }
    }

    private static void listarPorEstado(EstadoSolicitud estado) {
        List<Solicitud> lista = sistema.listarSolicitudesPorEstado(estado);
        if (lista.isEmpty()) {
            System.out.println("No hay solicitudes con estado: " + estado);
            return;
        }
        for (Solicitud s : lista) {
            System.out.println(s);
        }
    }

    private static void actualizarEstadoSolicitud() {
        try {
            int id = readInt("ID solicitud: ");
            Solicitud s = sistema.buscarSolicitudPorId(id);
            System.out.println("Estado actual: " + s.getEstado());
            System.out.println("1. PENDIENTE  2. EN_PROCESO  3. EMITIDA  4. ENTREGADA");
            int opt = readInt("Elegir nuevo estado: ");
            EstadoSolicitud nuevo;
            switch (opt) {
                case 1: nuevo = EstadoSolicitud.PENDIENTE; break;
                case 2: nuevo = EstadoSolicitud.EN_PROCESO; break;
                case 3: nuevo = EstadoSolicitud.EMITIDA; break;
                case 4: nuevo = EstadoSolicitud.ENTREGADA; break;
                default: System.out.println("Opción inválida."); return;
            }
            sistema.actualizarEstadoSolicitud(id, nuevo);
            System.out.println("Estado actualizado.");
        } catch (NotFoundException nfe) {
            System.out.println("No encontrado: " + nfe.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void buscarPorDni() {
        String dni = readString("DNI a buscar: ");
        List<Solicitud> resultados = sistema.buscarSolicitudesPorDni(dni);
        if (resultados.isEmpty()) {
            System.out.println("No se encontraron solicitudes para DNI " + dni);
            return;
        }
        for (Solicitud s : resultados) {
            System.out.println(s);
        }
    }

    // Lectura segura:
    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!sc.hasNextInt()) {
            System.out.print("Por favor, ingrese un número válido. " + prompt);
            sc.next();
        }
        int val = sc.nextInt();
        sc.nextLine();
        return val;
    }
    private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    // Datos de ejemplo:
    private static void seedData() {
        try {
            Paciente p1 = new Paciente("12345678", "Rocío", "Ortiz");
            Paciente p2 = new Paciente("87654321", "Juan", "Pérez");
            sistema.agregarPaciente(p1);
            sistema.agregarPaciente(p2);
            Solicitud s1 = new Solicitud(p1, "Enalapril 10mg", LocalDate.now().minusDays(2));
            Solicitud s2 = new Solicitud(p2, "Metformin 500mg", LocalDate.now().minusDays(1));
            sistema.agregarSolicitud(s1);
            sistema.agregarSolicitud(s2);

            // Usuarios: demostración de herencia / polimorfismo.
            Secretaria sec = new Secretaria("Rocío Alen", "Ortiz");
            Medica med = new Medica("Alejandra", "Fernandez", "Clínica General");
            sistema.addUsuario(sec);
            sistema.addUsuario(med);
        } catch (Exception e) {
            System.out.println("Error seed: " + e.getMessage());
        }
    }
}

/* ---------- CLASES DEL DOMINIO Y SISTEMA --------------- */

class Sistema {
    private final Map<String, Paciente> pacientesByDni = new HashMap<>();
    private final Map<Integer, Solicitud> solicitudesById = new HashMap<>();
    private final List<Usuario> usuarios = new ArrayList<>();
    private int nextSolicitudId = 1;

    public void agregarPaciente(Paciente p) throws DataValidationException {
        if (p == null || p.getDni() == null || p.getDni().isEmpty())
            throw new DataValidationException("Paciente o DNI inválido");
        pacientesByDni.put(p.getDni(), p);
    }

    public Paciente findOrCreatePaciente(String dni, String nombre, String apellido) throws DataValidationException {
        if (dni == null || dni.isBlank()) throw new DataValidationException("DNI requerido");
        Paciente p = pacientesByDni.get(dni);
        if (p == null) {
            p = new Paciente(dni, nombre, apellido);
            agregarPaciente(p);
        }
        return p;
    }

    public void agregarSolicitud(Solicitud s) throws DataValidationException {
        if (s == null) throw new DataValidationException("Solicitud nula");
        s.setId(nextSolicitudId++);
        solicitudesById.put(s.getId(), s);
    }

    public List<Solicitud> listarSolicitudesPorEstado(EstadoSolicitud estado) {
        List<Solicitud> res = new ArrayList<>();
        for (Solicitud s : solicitudesById.values()) {
            if (s.getEstado() == estado) res.add(s);
        }
        res.sort(Comparator.comparing(Solicitud::getFecha));
        return res;
    }

    public Solicitud buscarSolicitudPorId(int id) throws NotFoundException {
        Solicitud s = solicitudesById.get(id);
        if (s == null) throw new NotFoundException("Solicitud con ID " + id + " no encontrada.");
        return s;
    }

    public void emitirRecetaParaSolicitud(Solicitud s, Receta r) throws DataValidationException {
        if (s == null) throw new DataValidationException("Solicitud nula");
        if (r == null) throw new DataValidationException("Receta nula");
        s.setReceta(r);
        s.setEstado(EstadoSolicitud.EMITIDA);
    }

    public void actualizarEstadoSolicitud(int id, EstadoSolicitud nuevoEstado) throws NotFoundException {
        Solicitud s = buscarSolicitudPorId(id);
        if (s.getEstado() == EstadoSolicitud.ENTREGADA && nuevoEstado != EstadoSolicitud.ENTREGADA) {
            System.out.println("No se puede cambiar estado después de ENTREGADA.");
            return;
        }
        s.setEstado(nuevoEstado);
    }

    public List<Solicitud> buscarSolicitudesPorDni(String dni) {
        List<Solicitud> res = new ArrayList<>();
        for (Solicitud s : solicitudesById.values()) {
            if (s.getPaciente().getDni().equals(dni)) res.add(s);
        }
        res.sort(Comparator.comparing(Solicitud::getFecha));
        return res;
    }

    public void listarSolicitudesOrdenadasPorFecha() {
        List<Solicitud> lista = new ArrayList<>(solicitudesById.values());
        Collections.sort(lista, Comparator.comparing(Solicitud::getFecha));
        if (lista.isEmpty()) {
            System.out.println("No hay solicitudes registradas.");
            return;
        }
        for (Solicitud s : lista) System.out.println(s);
    }

    public void generarAlertas() {
        LocalDate hoy = LocalDate.now();
        System.out.println("=== Alertas: solicitudes pendientes > 24 horas ===");
        boolean alguna = false;
        for (Solicitud s : solicitudesById.values()) {
            if (s.getEstado() == EstadoSolicitud.PENDIENTE) {
                if (s.getFecha().isBefore(hoy.minusDays(1))) {
                    System.out.printf("ALERTA: Solicitud ID %d de %s (fecha: %s) está pendiente > 24hs%n",
                            s.getId(), s.getPaciente().getDni(), s.getFecha());
                    alguna = true;
                }
            }
        }
        if (!alguna) System.out.println("No hay alertas.");
    }

    public void addUsuario(Usuario u) {
        usuarios.add(u);
        u.saludar();
    }
}

/* ----------------- MODELOS: ------------------ */

enum EstadoSolicitud {
    PENDIENTE, EN_PROCESO, EMITIDA, ENTREGADA
}

class Paciente {
    private final String dni;
    private String nombre;
    private String apellido;

    public Paciente(String dni, String nombre, String apellido) {
        if (dni == null || dni.isBlank()) throw new IllegalArgumentException("DNI requerido");
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public String getDni() { return dni; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    @Override
    public String toString() {
        return String.format("%s %s (DNI: %s)", nombre, apellido, dni);
    }
}

class Solicitud {
    private int id;
    private final Paciente paciente;
    private final String medicamento;
    private final LocalDate fecha;
    private EstadoSolicitud estado;
    private Receta receta;

    public Solicitud(Paciente paciente, String medicamento, LocalDate fecha) throws DataValidationException {
        if (paciente == null) throw new DataValidationException("Paciente requerido");
        if (medicamento == null || medicamento.isBlank()) throw new DataValidationException("Medicamento requerido");
        this.paciente = paciente;
        this.medicamento = medicamento;
        this.fecha = fecha != null ? fecha : LocalDate.now();
        this.estado = EstadoSolicitud.PENDIENTE;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Paciente getPaciente() { return paciente; }
    public String getMedicamento() { return medicamento; }
    public LocalDate getFecha() { return fecha; }
    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }
    public Receta getReceta() { return receta; }
    public void setReceta(Receta receta) { this.receta = receta; }

    @Override
    public String toString() {
        return String.format("Solicitud[id=%d, paciente=%s, medicamento=%s, fecha=%s, estado=%s, receta=%s]",
                id, paciente.getDni(), medicamento, fecha, estado, receta != null ? "SI" : "NO");
    }
}

class Receta {
    private final Paciente paciente;
    private final String texto;
    private final LocalDate fechaEmision;

    public Receta(Paciente paciente, String texto, LocalDate fechaEmision) {
        this.paciente = paciente;
        this.texto = texto;
        this.fechaEmision = fechaEmision != null ? fechaEmision : LocalDate.now();
    }

    public Paciente getPaciente() { return paciente; }
    public String getTexto() { return texto; }
    public LocalDate getFechaEmision() { return fechaEmision; }

    @Override
    public String toString() {
        return String.format("Receta[%s - %s (%s)]", paciente.getDni(), texto, fechaEmision);
    }
}

/* ------------- USUARIOS: abstracción, herencia y polimorfismo ------------- */

abstract class Usuario {
    private String nombre;
    private String apellido;

    public Usuario(String nombre, String apellido) {
        this.nombre = nombre;
        this.apellido = apellido;
    }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }

    public abstract void saludar();

    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}

class Secretaria extends Usuario {
    public Secretaria(String nombre, String apellido) {
        super(nombre, apellido);
    }
    @Override
    public void saludar() {
        System.out.println("Hola, soy la secretaria " + toString());
    }
}

class Medica extends Usuario {
    private String especialidad;

    public Medica(String nombre, String apellido, String especialidad) {
        super(nombre, apellido);
        this.especialidad = especialidad;
    }

    @Override
    public void saludar() {
        System.out.println("Hola, soy la médica " + toString() + " (" + especialidad + ")");
    }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String esp) { this.especialidad = esp; }
}

/* ---------------- Excepciones -------------- */
class DataValidationException extends Exception {
    private static final long serialVersionUID = 1L;  
    public DataValidationException(String msg) { 
        super(msg); 
    }
}

class NotFoundException extends Exception {
    private static final long serialVersionUID = 1L;  
    public NotFoundException(String msg) { 
        super(msg); 
    }
}
