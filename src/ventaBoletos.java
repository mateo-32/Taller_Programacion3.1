
import javax.swing.*;
import java.math.BigDecimal;
import java.util.*;

public class ventaBoletos {


    private JPanel panelPrincipal;
    private JLabel VENTABOLETOS;
    private JComboBox cmbruta;
    private JTextField txtCedula;
    private JTextField txtNombre;
    private JTextField txtBoletos;
    private JButton btnComprar;
    private JTextArea txtHistorial;
    private JButton btnVerCola;
    private JButton btnAtender;
    private JButton btnPrimero;
    private JTextArea txtcola;
    private JLabel lblGuayaquil;
    private JLabel lblCuenca;
    private JLabel lblLoja;
    private JLabel lblTotal;
    private JLabel lblPendientes;

    private static final int CAPACIDAD = 20;
    private static final int MAX_BOLETOS = 5;


    private final Map<String, BigDecimal> precios =
            new LinkedHashMap<>();


    private final Map<String, Integer> vendidos =
            new HashMap<>();


    private final Map<String, Integer> porCedula =
            new HashMap<>();


    private final Queue<Compra> cola =
            new LinkedList<>();


    private final ArrayList<Compra> historial =
            new ArrayList<>();

    private BigDecimal recaudacion = BigDecimal.ZERO;


    static class Compra {
        String ruta;
        String cedula;
        String nombre;
        int cantidad;
        BigDecimal total;

        public Compra(String ruta, String cedula,
                      String nombre, int cantidad,
                      BigDecimal total) {

            this.ruta = ruta;
            this.cedula = cedula;
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.total = total;
        }

        public String detalle() {
            return "Ruta: " + ruta +
                    "\nCedula: " + cedula +
                    "\nPasajero: " + nombre +
                    "\nBoletos: " + cantidad +
                    "\nTotal: $" + total.toPlainString();
        }
    }


    public ventaBoletos() {


        precios.put("QUITO - GUAYAQUIL",
                new BigDecimal("10.50"));

        precios.put("QUITO - CUENCA",
                new BigDecimal("12.75"));

        precios.put("QUITO - LOJA",
                new BigDecimal("15.00"));


        for (String ruta : precios.keySet()) {
            vendidos.put(ruta, 0);
        }


        cmbruta.removeAllItems();

        for (String ruta : precios.keySet()) {
            cmbruta.addItem(ruta);
        }

        cmbruta.setSelectedIndex(-1);


        txtHistorial.setEditable(false);
        txtcola.setEditable(false);
        txtHistorial.setLineWrap(true);
        txtcola.setLineWrap(true);


        btnComprar.addActionListener(e -> comprar());


        btnVerCola.addActionListener(e -> mostrarCola());


        btnPrimero.addActionListener(e -> primeraCompra());


        btnAtender.addActionListener(e -> atenderCompra());

        actualizarLabels();
        mostrarCola();
    }


    private void comprar() {

        String ruta = (String) cmbruta.getSelectedItem();

        String cedula = txtCedula.getText().trim();
        String nombre = txtNombre.getText().trim();
        String texto = txtBoletos.getText().trim();


        if (ruta == null) {
            alerta("Seleccione una ruta.");
            return;
        }


        if (!cedula.matches("\\d{10}")) {
            alerta("La cedula debe tener 10 digitos.");
            return;
        }


        if (nombre.isEmpty()) {
            alerta("Ingrese el nombre del pasajero.");
            return;
        }


        int cantidad;

        try {
            cantidad = Integer.parseInt(texto);
        } catch (NumberFormatException ex) {
            alerta("Ingrese una cantidad valida.");
            return;
        }

        if (cantidad < 1 || cantidad > MAX_BOLETOS) {
            alerta("Solo puede comprar de 1 a 5 boletos.");
            return;
        }


        int anteriores = porCedula.getOrDefault(
                cedula, 0);

        if (anteriores + cantidad > MAX_BOLETOS) {

            alerta("Esta cedula ya compro " +
                    anteriores + " boletos.\n" +
                    "El maximo permitido es 5.");

            return;
        }


        int ocupados = vendidos.get(ruta);
        int disponibles = CAPACIDAD - ocupados;

        if (cantidad > disponibles) {
            alerta("No hay suficientes asientos.\n" +
                    "Disponibles: " + disponibles);
            return;
        }


        BigDecimal total = precios.get(ruta)
                .multiply(BigDecimal.valueOf(cantidad));


        Compra compra = new Compra(
                ruta, cedula, nombre, cantidad, total);


        cola.offer(compra);


        historial.add(compra);


        vendidos.put(ruta, ocupados + cantidad);


        porCedula.put(
                cedula, anteriores + cantidad);


        recaudacion = recaudacion.add(total);


        txtHistorial.append(
                "COMPRA #" + historial.size() + "\n" +
                        compra.detalle() +
                        "\n-----------------------------\n");


        actualizarLabels();


        mostrarCola();


        cmbruta.setSelectedIndex(-1);
        txtCedula.setText("");
        txtNombre.setText("");
        txtBoletos.setText("");

        JOptionPane.showMessageDialog(
                panelPrincipal,
                "Compra realizada correctamente.\n" +
                        "Total: $" + total.toPlainString(),
                "Compra exitosa",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    private void mostrarCola() {

        txtcola.setText("");

        if (cola.isEmpty()) {
            txtcola.setText("No existen compras pendientes.");
            return;
        }

        int posicion = 1;

        for (Compra compra : cola) {

            txtcola.append(
                    "POSICION " + posicion++ + "\n" +
                            compra.detalle() +
                            "\n-----------------------------\n");
        }
    }


    private void primeraCompra() {

        Compra primera = cola.peek();

        if (primera == null) {
            alerta("La cola esta vacia.");
            return;
        }

        JOptionPane.showMessageDialog(
                panelPrincipal,
                "PRIMERA COMPRA EN COLA\n\n" +
                        primera.detalle());
    }


    private void atenderCompra() {

        Compra atendida = cola.poll();

        if (atendida == null) {
            alerta("No hay compras pendientes.");
            return;
        }

        JOptionPane.showMessageDialog(
                panelPrincipal,
                "COMPRA ATENDIDA\n\n" +
                        atendida.detalle());


        mostrarCola();
        actualizarLabels();
    }


    private void actualizarLabels() {

        int guayaquil =
                vendidos.get("QUITO - GUAYAQUIL");

        int cuenca =
                vendidos.get("QUITO - CUENCA");

        int loja =
                vendidos.get("QUITO - LOJA");

        lblGuayaquil.setText(
                "Guayaquil: " + guayaquil +
                        " vendidos / " +
                        (CAPACIDAD - guayaquil) +
                        " disponibles");

        lblCuenca.setText(
                "Cuenca: " + cuenca +
                        " vendidos / " +
                        (CAPACIDAD - cuenca) +
                        " disponibles");

        lblLoja.setText(
                "Loja: " + loja +
                        " vendidos / " +
                        (CAPACIDAD - loja) +
                        " disponibles");

        lblTotal.setText(
                "Total recaudado: $" +
                        recaudacion.toPlainString());

        lblPendientes.setText(
                "Compras pendientes: " + cola.size());
    }


    private void alerta(String mensaje) {

        JOptionPane.showMessageDialog(
                panelPrincipal,
                mensaje,
                "Validacion",
                JOptionPane.WARNING_MESSAGE);
    }


    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame ventana = new JFrame(
                    "Sistema de Venta de Boletos");

            ventaBoletos formulario =
                    new ventaBoletos();

            ventana.setContentPane(
                    formulario.panelPrincipal);

            ventana.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE);

            ventana.pack();
            ventana.setSize(800, 750);
            ventana.setLocationRelativeTo(null);
            ventana.setVisible(true);
        });
    }
}
