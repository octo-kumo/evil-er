package main.ui;

import main.EvilEr;
import model.entities.Entity;
import model.lines.Line;

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
        add(new JButton("Copy") {{
            addActionListener(evt -> {
                saveToClipboard();
            });
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

    private static class TransferableImage implements Transferable {
        private final Image i;

        public TransferableImage(Image i) {
            this.i = i;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (flavor.equals(DataFlavor.imageFlavor) && i != null) return i;
            else throw new UnsupportedFlavorException(flavor);
        }

        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = new DataFlavor[1];
            flavors[0] = DataFlavor.imageFlavor;
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            DataFlavor[] flavors = getTransferDataFlavors();
            for (DataFlavor dataFlavor : flavors) if (flavor.equals(dataFlavor)) return true;
            return false;
        }
    }

}
