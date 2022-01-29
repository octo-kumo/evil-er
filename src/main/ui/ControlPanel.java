package main.ui;

import main.EvilEr;
import model.entities.Entity;
import model.lines.Line;
import model.others.TransferableImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.io.IOException;

public class ControlPanel extends JPanel implements ClipboardOwner {

    private final EvilEr evilEr;
    public final JCheckBox lock;
    public final JComboBox<Entity.Type> typeBox;
    public final JComboBox<Line.LineStyle> styleBox;

    public ControlPanel(EvilEr evilEr) {
        this.evilEr = evilEr;
        setLayout(new FlowLayout());
        add(lock = new JCheckBox("Lock") {{
            evilEr.diagramPanel.diagram.locked.addListener(this::setSelected);
            addActionListener(e -> evilEr.diagramPanel.diagram.locked.set(isSelected()));
        }});
        add(typeBox = new JComboBox<Entity.Type>(Entity.Type.values()) {{
            evilEr.diagramPanel.diagram.addingType.addListener(this::setSelectedItem);
            addActionListener(evt -> evilEr.diagramPanel.diagram.setAddingType((Entity.Type) getSelectedItem()));
        }});
        add(styleBox = new JComboBox<Line.LineStyle>(Line.LineStyle.values()) {{
            evilEr.diagramPanel.diagram.lineStyle.addListener(this::setSelectedItem);
            addActionListener(evt -> evilEr.diagramPanel.diagram.lineStyle.set((Line.LineStyle) getSelectedItem()));
        }});
        add(new JCheckBox("Connect") {{
            evilEr.diagramPanel.diagram.connecting.addListener(this::setSelected);
            addActionListener(evt -> evilEr.diagramPanel.diagram.connecting.set(isSelected()));
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
        add(new JButton("Copy") {{
            addActionListener(evt -> saveToClipboard());
        }});
    }

    private void saveTo(File file) throws IOException {
        if (!file.getName().contains(".")) file = new File(file.getAbsolutePath() + ".png");
        ImageIO.write(evilEr.diagramPanel.diagram.export(), "png", file);
    }

    private void saveToClipboard() {
        TransferableImage transferable = new TransferableImage(evilEr.diagramPanel.diagram.export());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, this);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        System.out.println("Lost Clipboard Ownership");
    }
}
