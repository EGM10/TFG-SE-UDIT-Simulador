package seudit.simulador;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Panel gráfico que representa de forma visual el chip/contactless
 * asociado al monedero SE-UDIT simulado.
 */
class ChipPanel extends JPanel {

    ChipPanel() {
        setPreferredSize(new Dimension(140, 240));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        int x = 18;
        int y = 58;
        int w = 102;
        int h = 82;

        g2.setColor(new Color(238, 196, 70));
        g2.fillRect(x, y, w, h);

        g2.setColor(new Color(90, 70, 20));
        g2.drawRect(x, y, w, h);

        for (int i = 1; i < 3; i++) {
            g2.drawLine(x + i * w / 3, y + 10, x + i * w / 3, y + h - 10);
            g2.drawLine(x + 10, y + i * h / 3, x + w - 10, y + i * h / 3);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        g2.drawString(")))", x + 38, y + 135);

        g2.dispose();
    }
}