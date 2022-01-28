package main.ui;

import main.EvilEr;
import main.renderer.DiagramGraphics;
import model.Vector;
import model.entities.Entity;
import model.lines.Line;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static main.renderer.DiagramGraphics.flatten;

public class ControlPanel extends JPanel {

    private final EvilEr evilEr;
    public final JCheckBox lock;
    public final JComboBox<Entity.Types> typeBox;
    public final JComboBox<Line.LineStyle> styleBox;

    public ControlPanel(EvilEr evilEr) {
        this.evilEr = evilEr;
        setLayout(new FlowLayout());
        add(lock = new JCheckBox("Lock"));
        add(typeBox = new JComboBox<Entity.Types>(Entity.Types.values()) {{
            addActionListener(evt -> evilEr.diagramPanel.diagram.setAddingType((Entity.Types) getSelectedItem()));
        }});
        add(styleBox = new JComboBox<Line.LineStyle>(Line.LineStyle.values()) {{
            addActionListener(evt -> evilEr.diagramPanel.diagram.setStyle((Line.LineStyle) getSelectedItem()));
        }});
        add(new JCheckBox("Connect") {{
            addActionListener(evt -> {
                evilEr.diagramPanel.diagram.setConnecting(isSelected());
            });
        }});
        add(new JButton("Regress Positions") {{
            addActionListener(evt -> {
                double total;
                int ops = 0;
                do {
                    total = Entity.applyForces(evilEr.diagramPanel.diagram.entities, 100);
                    ops++;
                } while (total > 0.1);
                System.out.println("Regression Complete, Operations = " + ops);
                evilEr.diagramPanel.diagram.repaint();
            });
        }});
        add(new JButton("Delete") {{
            addActionListener(evt -> evilEr.diagramPanel.diagram.delete());
        }});
        add(new JButton("Save...") {{
            addActionListener(evt -> {
                JFileChooser fileChooser = new JFileChooser();
                FileFilter imageFilter = new FileNameExtensionFilter(
                        "Image files", ImageIO.getReaderFileSuffixes());
                fileChooser.setFileFilter(imageFilter);
                fileChooser.setDialogTitle("Specify a file to save");

                int userSelection = fileChooser.showSaveDialog(SwingUtilities.getWindowAncestor(this));

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    try {
                        saveTo(fileToSave);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }});
    }

    private void saveTo(File file) throws IOException {
        int padding = 20;
        if (!file.getName().contains(".")) file = new File(file.getAbsolutePath() + ".png");
        Rectangle2D.Double aabb = evilEr.diagramPanel.diagram.getAABB();
        aabb.x -= padding;
        aabb.y -= padding;
        aabb.width += 2 * padding;
        aabb.height += 2 * padding;
        BufferedImage img = new BufferedImage((int) aabb.width, (int) aabb.height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = img.createGraphics();
        g.translate(-aabb.x, -aabb.y);
        evilEr.diagramPanel.diagram.draw(new DiagramGraphics(g));
        g.dispose();
        ImageIO.write(img, "png", file);
    }
}
