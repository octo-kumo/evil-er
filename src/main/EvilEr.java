package main;

import main.er.ERControlPanel;
import main.er.ERDiagramPanel;
import main.er.ERInfoPanel;
import main.er.ERMenu;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static main.ui.Prompts.report;

public class EvilEr extends JPanel {
    public static Map<?, ?> DESKTOP_HINTS;
    public final ERControlPanel controlPanel;
    public final ERDiagramPanel diagramPanel;
    public final ERInfoPanel infoPanel;
    public JFrame frame;

    public EvilEr(JFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());

        diagramPanel = new ERDiagramPanel(this);
        add(diagramPanel, BorderLayout.CENTER);

        controlPanel = new ERControlPanel(this);
        add(controlPanel, BorderLayout.NORTH);

        infoPanel = new ERInfoPanel(this);
        add(infoPanel, BorderLayout.EAST);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(diagramPanel.diagram.keyManager);
    }

    public static void main(String... args) {
        try {
            DESKTOP_HINTS = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            report(e);
            e.printStackTrace();
        }
        JFrame frame = new JFrame("Evil ER :: " + Version.CURRENT);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        EvilEr evilEr;
        frame.setContentPane(evilEr = new EvilEr(frame));
        frame.setJMenuBar(new ERMenu(evilEr));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Version.asyncUpdate();
    }
}
