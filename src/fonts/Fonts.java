package fonts;

import main.EvilEr;
import utils.JFontChooser;
import utils.Prompts;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Fonts {
    private static Fonts instance;
    public static final Preferences DRAWING_FONT = Preferences.userRoot().node("drawingFont");

    private final JFontChooser fontChooser;
    private final EvilEr evilEr;
    private JDialog fontChooserFrame;

    public Fonts(EvilEr evilEr) {
        this.evilEr = evilEr;
        this.fontChooser = new JFontChooser();
        resetFontSelector();

        instance = this;
    }

    private void resetFontSelector() {
        String name = DRAWING_FONT.get("fontName", EvilEr.DEFAULT_FONT.getFamily());
        int size = DRAWING_FONT.getInt("fontSize", EvilEr.DEFAULT_FONT.getSize());
        int style = DRAWING_FONT.getInt("fontStyle", EvilEr.DEFAULT_FONT.getStyle());

        fontChooser.setSelectedFontFamily(name);
        fontChooser.setSelectedFontSize(size);
        fontChooser.setSelectedFontStyle(style);
        evilEr.diagramPanel.diagram.setFont(fontChooser.getSelectedFont());
    }

    private void persistFontSelector() {
        DRAWING_FONT.put("fontName", fontChooser.getSelectedFontFamily());
        DRAWING_FONT.getInt("fontSize", fontChooser.getSelectedFontSize());
        DRAWING_FONT.getInt("fontStyle", fontChooser.getSelectedFontStyle());
        evilEr.diagramPanel.diagram.setFont(fontChooser.getSelectedFont());
    }

    public static void open() {
        if (instance != null) instance.openFontChooser();
    }

    private void openFontChooser() {
        if (fontChooserFrame == null) {
            fontChooserFrame = new JDialog(evilEr.frame, "Choose Font");
            fontChooserFrame.setContentPane(new JPanel() {{
                setLayout(new BorderLayout());
                add(fontChooser, BorderLayout.CENTER);
                add(new JPanel() {{
                    setLayout(new FlowLayout(FlowLayout.RIGHT));
                    add(new JButton("Cancel") {{
                        addActionListener(e -> {
                            resetFontSelector();
                            fontChooserFrame.setVisible(false);
                        });
                    }});
                    add(new JButton("Reset") {{
                        addActionListener(e -> {
                            try {
                                DRAWING_FONT.clear();
                                resetFontSelector();
                            } catch (BackingStoreException ex) {
                                Prompts.report(ex);
                            }
                        });
                    }});
                    add(new JButton("Apply") {{
                        addActionListener(e -> {
                            persistFontSelector();
                            fontChooserFrame.setVisible(false);
                        });
                    }});
                }}, BorderLayout.SOUTH);
            }});
            fontChooserFrame.pack();
        }
        SwingUtilities.updateComponentTreeUI(fontChooserFrame);
        fontChooserFrame.setLocationRelativeTo(evilEr);
        fontChooserFrame.setVisible(true);
    }

    public static void loadFonts() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Fonts.class.getResourceAsStream("/fonts/zh-cn.ttf"))));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Fonts.class.getResourceAsStream("/fonts/roboto.ttf"))));
        } catch (IOException | FontFormatException e) {
            //Handle exception
        }
    }
}
