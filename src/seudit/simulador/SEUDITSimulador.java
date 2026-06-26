package seudit.simulador;

import javax.swing.SwingUtilities;

/**
 * Clase principal de arranque del simulador SE-UDIT.
 * 
 * Esta aplicación de Java Swing representa dos monederos SE-UDIT virtuales,
 * pero no implementa un Secure Element físico real.
 * 
 * La simulación implementa el comportamiento lógico definido en la arquitectura:
 * estados, transiciones, conservación del valor y trazabilidad.
 */
public class SEUDITSimulador {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimulatorFrame().setVisible(true));
    }
}
