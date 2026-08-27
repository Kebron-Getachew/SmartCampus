import javax.swing.*;
import java.awt.*;

public class PillLabel extends JLabel {
    private Color bgColor;

    public PillLabel(String text, Color bgColor, Color fgColor) {
        super(text);
        this.bgColor = bgColor;
        setForeground(fgColor);
        setFont(new Font("SansSerif", Font.BOLD, 11));
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}