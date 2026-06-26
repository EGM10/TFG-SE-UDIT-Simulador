package seudit.simulador;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de visualización del monedero SE-UDIT simulado.
 *
 * Representa la pantalla interna del monedero, mostrando el estado operativo,
 * la entrada de PIN, las operaciones de carga, transferencia, fragmentación,
 * selección de UDIT, historial y mensajes temporales de estado.
 */
class DisplayPanel extends JPanel {

    private static final DecimalFormat MONEY = new DecimalFormat("0.00");

    final WalletModel model;
    Mode mode = Mode.NORMAL;
    String flash = null;
    long flashUntil = 0;

    DisplayPanel(WalletModel m) {
        model = m;
        setPreferredSize(new Dimension(230, 290));
        setBackground(new Color(20, 20, 20));
        setBorder(new CompoundBorder(
            new LineBorder(Color.BLACK, 5),
            new EmptyBorder(10, 10, 10, 10)
        ));
    }

    void flashMessage(String msg) {
        flash = msg;
        flashUntil = System.currentTimeMillis() + 1100;
        repaint();

        Timer t = new Timer(1150, e -> repaint());
        t.setRepeats(false);
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        g2.setColor(new Color(18, 18, 18));
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (flash != null && System.currentTimeMillis() < flashUntil) {
            g2.setColor(new Color(120, 210, 255));
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));

            int yy = 180;
            for (String l : flash.split("\\n")) {
                center(g2, l, yy);
                yy += 42;
            }

            g2.dispose();
            return;
        }

        if (mode == Mode.INITIAL_LOAD_INPUT ||
            mode == Mode.TRANSFER_INPUT ||
            mode == Mode.FRAGMENT_INPUT) {

            String title =
                mode == Mode.INITIAL_LOAD_INPUT ? "Carga inicial" :
                mode == Mode.TRANSFER_INPUT ? "Importe a transferir" :
                "Fragmentar";

            input(g2, title);
            g2.dispose();
            return;
        }

        if (mode == Mode.SELECT_UDIT_TRANSFER) {
            selectUditForTransfer(g2);
            g2.dispose();
            return;
        }

        if (mode == Mode.HISTORY) {
            hist(g2);
            g2.dispose();
            return;
        }

        if (model.accessGranted) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
            center(g2, "UDIT activas", 70);

            g2.setColor(new Color(120, 210, 255));
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));

            int yy = 130;

            if (model.udits.isEmpty()) {
                center(g2, "Vacío", yy);
            } else {
                for (Udit u : model.udits) {
                    center(
                        g2,
                        u.id + ": " + MONEY.format(u.value).replace(".", ","),
                        yy
                    );
                    yy += 35;
                }
            }

            g2.setColor(new Color(140, 255, 220));
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            center(g2, "OPERATIVO", 260);

            g2.dispose();
            return;
        }

        g2.setColor(new Color(140, 255, 120));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        center(g2, "Introduzca PIN", 150);

        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 36));
        String pinView = model.selectedAmountText.isEmpty()
            ? "_ _ _ _"
            : model.selectedAmountText;
        center(g2, pinView, 230);

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        center(g2, "Use las flechas para escribir", 330);
        center(g2, "✓ para confirmar", 355);

        g2.dispose();
    }

    void input(Graphics2D g2, String title) {
        g2.setColor(new Color(140, 255, 220));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        center(g2, title, 70);

        String s = model.selectedAmountText.isEmpty()
            ? "_"
            : model.selectedAmountText;

        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 34));
        int x = (getWidth() - g2.getFontMetrics().stringWidth(s)) / 2;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean blink =
                i == model.cursor &&
                ((System.currentTimeMillis() / 350) % 2 == 0);

            g2.setColor(
                blink
                    ? new Color(255, 214, 72)
                    : new Color(120, 210, 255)
            );

            g2.drawString(String.valueOf(c), x, 128);
            x += g2.getFontMetrics().charWidth(c);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        center(g2, "Este: añadir 0", 178);
        center(g2, "Este largo: coma decimal", 196);
        center(g2, "Norte/Sur: modificar dígito", 214);
        center(g2, "Centro: confirmar", 232);
    }

    void selectUditForTransfer(Graphics2D g2) {
        g2.setColor(new Color(120, 210, 255));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        center(g2, "Seleccionar UDIT", 65);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));

        int yy = 120;

        for (int i = 0; i < model.udits.size(); i++) {
            Udit u = model.udits.get(i);

            if (i == model.selectedUditIndex) {
                g2.setColor(new Color(255, 214, 72));
            } else {
                g2.setColor(Color.WHITE);
            }

            center(
                g2,
                u.id + ": " + MONEY.format(u.value).replace(".", ",") + " UDIT",
                yy
            );

            yy += 38;
        }

        g2.setColor(new Color(140, 255, 220));
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        center(g2, "Norte/Sur: elegir · Centro: confirmar", 345);
    }

    void hist(Graphics2D g2) {
        g2.setColor(new Color(120, 210, 255));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        center(g2, "Historial", 68);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        wrap(g2, model.history.get(model.historyIndex).text, 115, 190);

        g2.setColor(new Color(140, 255, 220));
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        center(g2, (model.historyIndex + 1) + " / " + model.history.size(), 245);
        center(g2, "Norte/Sur: navegar · Centro: salir", 265);
    }

    void center(Graphics2D g2, String s, int y) {
        g2.drawString(
            s,
            (getWidth() - g2.getFontMetrics().stringWidth(s)) / 2,
            y
        );
    }

    void wrap(Graphics2D g2, String text, int y, int max) {
        List<String> lines = new ArrayList<>();
        String line = "";

        for (String w : text.split(" ")) {
            String t = line.isEmpty() ? w : line + " " + w;

            if (g2.getFontMetrics().stringWidth(t) > max) {
                lines.add(line);
                line = w;
            } else {
                line = t;
            }
        }

        if (!line.isEmpty()) {
            lines.add(line);
        }

        int yy = y;

        for (String l : lines) {
            center(g2, l, yy);
            yy += 22;
        }
    }
}