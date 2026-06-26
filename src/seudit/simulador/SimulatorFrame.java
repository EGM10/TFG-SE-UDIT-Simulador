package seudit.simulador;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

// Ventana principal de la aplicación. Contiene los dos monederos simulados
// y el registro inferior de eventos donde se muestran las fases de los protocolos:
// WalletPanel a y WalletPanel b 
class SimulatorFrame extends JFrame {
    JTextArea log = new JTextArea(6, 40);

    SimulatorFrame() {
        setTitle("SEUDITSimulador - Transferencia offline");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1320, 720));

        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        WalletPanel a = new WalletPanel(new WalletModel("Dispositivo A"), log);
        WalletPanel b = new WalletPanel(new WalletModel("Dispositivo B"), log);

        a.peer = b;
        b.peer = a;

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, a, b);
        split.setResizeWeight(.5);

        JLabel lt = new JLabel("Registro del protocolo de transferencia offline", SwingConstants.CENTER);
        lt.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        JPanel south = new JPanel(new BorderLayout());
        south.add(lt, BorderLayout.NORTH);
        south.add(new JScrollPane(log), BorderLayout.CENTER);

        add(split, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        setLocationRelativeTo(null);

        log("Simulador iniciado. Introduzca el PIN para acceder al monedero.");
    }

    void log(String s) {
        log.append("[" + new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + s + "\n");
    }
}