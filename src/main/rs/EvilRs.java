package main.rs;

import javax.swing.*;
import java.awt.*;

import static main.ui.Prompts.report;

public class EvilRs extends JPanel {

    private final RSDiagram diagram;

    public EvilRs() {
        setLayout(new BorderLayout());

        diagram = new RSDiagram();
        add(diagram, BorderLayout.CENTER);
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
        frame.setContentPane(new EvilRs());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}