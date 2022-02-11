package main;

import com.github.weisj.darklaf.LafManager;
import main.er.ERControlPanel;
import main.er.ERDiagramPanel;
import main.er.ERInfoPanel;
import main.er.ERMenu;
import utils.Prompts;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class EvilEr extends JPanel {
    public static final Font DEFAULT_FONT = new Font(null);
    public static Map<?, ?> RENDER_HINTS;
    public final ERControlPanel controlPanel;
    public final ERDiagramPanel diagramPanel;
    public final ERInfoPanel infoPanel;
    public final JSplitPane splitPane;
    public JFrame frame;

    public EvilEr(JFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());

        diagramPanel = new ERDiagramPanel();
        infoPanel = new ERInfoPanel(this);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, diagramPanel, infoPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(0.9);
        splitPane.setResizeWeight(1);
        add(splitPane, BorderLayout.CENTER);

        controlPanel = new ERControlPanel(this);
        add(controlPanel, BorderLayout.NORTH);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(diagramPanel.diagram.keyManager);
    }

    public static void main(String... args) {
        RENDER_HINTS = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        LafManager.install(LafManager.themeForPreferredStyle(LafManager.getPreferredThemeStyle()));
        ERMenu.loadThemeFromPreference();
        JFrame frame = new JFrame("Evil ER :: " + Version.CURRENT);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        EvilEr evilEr;
        frame.setContentPane(evilEr = new EvilEr(frame));
        frame.setJMenuBar(new ERMenu(evilEr));
        frame.setAutoRequestFocus(true);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.requestFocus();

        Version.asyncUpdate();
        SwingUtilities.invokeLater(() -> {
            try {
                frame.setIconImages(Arrays.asList(
                        ImageIO.read(Objects.requireNonNull(EvilEr.class.getResourceAsStream("/icon/iconx16.png"))),
                        ImageIO.read(Objects.requireNonNull(EvilEr.class.getResourceAsStream("/icon/iconx32.png"))),
                        ImageIO.read(Objects.requireNonNull(EvilEr.class.getResourceAsStream("/icon/iconx64.png"))),
                        ImageIO.read(Objects.requireNonNull(EvilEr.class.getResourceAsStream("/icon/iconx128.png"))),
                        ImageIO.read(Objects.requireNonNull(EvilEr.class.getResourceAsStream("/icon/iconx256.png")))
                ));
            } catch (IOException e) {
                Prompts.report(e);
                e.printStackTrace();
            }
        });
    }
}
