package utils.components;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;

public class PlaceholderTextField extends JTextField {

    private String placeholder;

    public PlaceholderTextField() {
        this(null, null, 0);
    }

    public PlaceholderTextField(
            final Document pDoc,
            final String pText,
            final int pColumns) {
        super(pDoc, pText, pColumns);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
    }

    public PlaceholderTextField(final int pColumns) {
        this(null, null, pColumns);
    }

    public PlaceholderTextField(final String pText) {
        this(null, pText, 0);
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(final String s) {
        placeholder = s;
    }

    @Override
    protected void paintComponent(final Graphics pG) {
        super.paintComponent(pG);

        if (placeholder == null || placeholder.length() == 0 || getText().length() > 0) {
            return;
        }

        final Graphics2D g = (Graphics2D) pG;
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(getDisabledTextColor());
        g.drawString(placeholder, getInsets().left, pG.getFontMetrics()
                .getMaxAscent() + getInsets().top);
    }

}