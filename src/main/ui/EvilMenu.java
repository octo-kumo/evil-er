package main.ui;

import com.google.gson.stream.JsonReader;
import main.EvilEr;
import model.Vector;
import model.entities.Entity;
import model.entities.Specialization;
import model.lines.Line;
import model.others.TransferableImage;
import model.serializers.Serializer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import static main.ui.Prompts.report;

public class EvilMenu extends JMenuBar {

    private final EvilEr evilEr;

    public EvilMenu(EvilEr evilEr) {
        this.evilEr = evilEr;
        add(new JMenu("File") {{
            add(new JMenuItem(new AbstractAction("Open") {
                public void actionPerformed(ActionEvent ae) {
                    if (JFileChooser.APPROVE_OPTION == jsonChooser.showOpenDialog(evilEr)) {
                        try (JsonReader reader = new JsonReader(new FileReader(jsonChooser.getFinal()))) {
                            ArrayList<Entity> deserialized = Serializer.deserialize(reader);
                            evilEr.diagramPanel.diagram.entities.clear();
                            evilEr.diagramPanel.diagram.entities.addAll(deserialized);
                            evilEr.diagramPanel.diagram.repaint();
                        } catch (IOException e) {
                            report(e);
                            e.printStackTrace();
                        }
                    }
                }
            }));
            add(new JMenuItem(new AbstractAction("Save") {
                public void actionPerformed(ActionEvent ae) {
                    String json = Serializer.serialize(evilEr.diagramPanel.diagram.entities);
                    try (FileWriter fw = new FileWriter("session.json")) {
                        fw.write(json);
                    } catch (IOException e) {
                        report(e);
                        e.printStackTrace();
                    }
                    ArrayList<Entity> deserialized = Serializer.deserialize(json);
                    evilEr.diagramPanel.diagram.entities.clear();
                    evilEr.diagramPanel.diagram.entities.addAll(deserialized);
                    evilEr.diagramPanel.diagram.repaint();
                }
            }) {{
                setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JMenuItem(new AbstractAction("Save as...") {
                public void actionPerformed(ActionEvent ae) {
                    if (JFileChooser.APPROVE_OPTION == jsonChooser.showSaveDialog(evilEr)) {
                        try (Writer writer = new FileWriter(jsonChooser.getFinal())) {
                            Serializer.serialize(evilEr.diagramPanel.diagram.entities, writer);
                        } catch (IOException e) {
                            report(e);
                            e.printStackTrace();
                        }
                    }
                }
            }));
            add(new JMenuItem(new AbstractAction("To clipboard") {
                public void actionPerformed(ActionEvent ae) {
                    TransferableImage transferable = new TransferableImage(evilEr.diagramPanel.diagram.export());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
                }
            }) {{
                setAccelerator(KeyStroke.getKeyStroke('C', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JSeparator());
            add(new JMenuItem(new AbstractAction("Export...") {
                public void actionPerformed(ActionEvent ae) {
                    if (JFileChooser.APPROVE_OPTION == imageChooser.showSaveDialog(evilEr)) try {
                        ImageIO.write(evilEr.diagramPanel.diagram.export(), "PNG", imageChooser.getFinal());
                    } catch (IOException e) {
                        report(e);
                        e.printStackTrace();
                    }
                }
            }));
        }});
        add(new JMenu("Edit") {{
            add(new JMenuItem("Delete") {{
                addActionListener(e -> evilEr.diagramPanel.diagram.delete());
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JMenuItem("Center") {{
                addActionListener(EvilMenu.this::center);
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                        InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JMenuItem("Regress") {{
                addActionListener(evt -> {
                    double total;
                    int ops = 0;
                    do {
                        total = Entity.applyForces(evilEr.diagramPanel.diagram.entities, 100);
                        ops++;
                    } while (total > 1 && ops < 100);
                    center(null);
                    System.out.println("Regression Complete, Operations = " + ops);
                    evilEr.diagramPanel.diagram.repaint();
                });
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                        InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JMenuItem(new AbstractAction("Clear") {
                public void actionPerformed(ActionEvent ae) {
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(evilEr, "Do you really want to clear all entities?", "Warning", JOptionPane.YES_NO_OPTION)) {
                        evilEr.diagramPanel.diagram.entities.clear();
                        evilEr.diagramPanel.diagram.repaint();
                    }
                }
            }));
            add(new JSeparator());

            add(new JCheckBoxMenuItem("Connecting") {{
                evilEr.diagramPanel.diagram.connecting.addListener(this::setState);
                addActionListener(e -> evilEr.diagramPanel.diagram.connecting.set(getState()));
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_MASK));
            }});
        }});
        add(new JMenu("Add") {{
            add(new JMenu("Type...") {{
                ButtonGroup group = new ButtonGroup();
                char[] mnemonics = new char[]{'v', 'e', 'r', 'a', 's'};
                Entity.Type[] values = Entity.Type.values();
                ButtonModel[] models = new ButtonModel[values.length];
                evilEr.diagramPanel.diagram.addingType.addListener(t ->
                        group.setSelected(models[Arrays.asList(values).indexOf(t)], true));
                for (int i = 0; i < values.length; i++) {
                    Entity.Type type = values[i];
                    JRadioButtonMenuItem button;
                    add(button = new JRadioButtonMenuItem(type.toString()));

                    group.add(button);
                    button.setMnemonic(mnemonics[i]);
                    button.addActionListener(evt -> evilEr.diagramPanel.diagram.setAddingType(type));
                    models[i] = button.getModel();
                }
            }});
            add(new JCheckBoxMenuItem("Locked") {{
                evilEr.diagramPanel.diagram.locked.addListener(this::setState);
                addActionListener(e -> evilEr.diagramPanel.diagram.locked.set(getState()));
            }});
        }});
        add(new JMenu("View") {{
            add(new JMenu("Line Style") {{
                ButtonGroup group = new ButtonGroup();
                Line.LineStyle[] values = Line.LineStyle.values();
                for (Line.LineStyle type : values) {
                    JRadioButtonMenuItem button;
                    group.add(add(button = new JRadioButtonMenuItem(type.toString())));
                    button.addActionListener(evt -> evilEr.diagramPanel.diagram.lineStyle.set(type));
                }
            }});
            add(new JCheckBoxMenuItem("AABB") {{
                evilEr.diagramPanel.diagram.aabb.addListener(this::setState);
                addActionListener(e -> {
                    evilEr.diagramPanel.diagram.aabb.set(getState());
                    evilEr.diagramPanel.diagram.repaint();
                });
            }});
        }});
    }

    private static final Chooser jsonChooser = new Chooser() {

        public File getFinal() {
            File file = getSelectedFile();
            if (getDialogType() == SAVE_DIALOG && !getFileFilter().accept(file) && file != null)
                file = new File(file.getAbsolutePath() + ".dig");
            return file;
        }

        {
            FileFilter imageFilter = new FileNameExtensionFilter("Diagram File", "json", "dig");
            setDialogTitle("Select...");
            setFileFilter(imageFilter);
        }
    };

    private static final Chooser imageChooser = new Chooser() {

        public File getFinal() {
            File file = getSelectedFile();
            if (getDialogType() == SAVE_DIALOG && !getFileFilter().accept(file) && file != null)
                file = new File(file.getAbsolutePath() + ".png");
            return file;
        }

        {
            FileFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
            setDialogTitle("Select...");
            setFileFilter(imageFilter);
        }
    };

    private static class Chooser extends JFileChooser {
        public File getFinal() {
            return getSelectedFile();
        }
    }

    private void center(ActionEvent evt) {
        Vector com = evilEr.diagramPanel.diagram.entities.stream().map(e -> (Vector) e).reduce(Vector::add).orElse(Vector.ZERO).div(evilEr.diagramPanel.diagram.entities.size());
        Vector diff = com.minus(evilEr.diagramPanel.diagram.getWidth() / 2d, evilEr.diagramPanel.diagram.getHeight() / 2d);
        evilEr.diagramPanel.diagram.entities.stream().filter(e -> !(e instanceof Specialization)).forEach(e -> e.decre(diff));
        evilEr.diagramPanel.diagram.repaint();
    }
}
