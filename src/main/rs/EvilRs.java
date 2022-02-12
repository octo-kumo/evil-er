package main.rs;

import javax.swing.*;
import java.awt.*;

import static utils.Prompts.report;

public class EvilRs extends JPanel {

    public final RSDiagram diagram;

    public EvilRs() {
        setLayout(new BorderLayout());

        diagram = new RSDiagram();
        JSplitPane splitpane;
        add(splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, diagram, new RSInfoPanel(diagram)), BorderLayout.CENTER);
        splitpane.setResizeWeight(1);
    }

    public static void main(String... args) {
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            report(e);
            e.printStackTrace();
        }
        JFrame frame = new JFrame("Evil RS");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        EvilRs evilRs = new EvilRs();
        frame.setContentPane(evilRs);
        frame.setJMenuBar(new RSMenu(evilRs));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
