package main.er;

import com.google.gson.stream.JsonReader;
import main.EvilEr;
import main.rs.Converter;
import main.rs.EvilRs;
import main.rs.RSMenu;
import main.ui.Prompts;
import model.Vector;
import model.er.Entity;
import model.others.TransferableImage;
import model.serializers.Serializer;
import shapes.lines.Line;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import static main.ui.Prompts.report;

public class ERMenu extends JMenuBar {

    private final EvilEr evilEr;

    public ERMenu(EvilEr evilEr) {
        this.evilEr = evilEr;
        add(new JMenu("File") {{
            setMnemonic('F');
            add(new JMenuItem(new AbstractAction("Open") {
                public void actionPerformed(ActionEvent ae) {
                    if (JFileChooser.APPROVE_OPTION == main.ui.Chooser.jsonChooser.showOpenDialog(evilEr)) {
                        try (JsonReader reader = new JsonReader(new FileReader(main.ui.Chooser.jsonChooser.getFinal()))) {
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
                    if (JFileChooser.APPROVE_OPTION == main.ui.Chooser.jsonChooser.showSaveDialog(evilEr)) {
                        try (Writer writer = new FileWriter(main.ui.Chooser.jsonChooser.getFinal())) {
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
            add(new JMenuItem(new AbstractAction("To schema") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    EvilRs rs = new EvilRs();
                    JDialog dialog = new JDialog(evilEr.frame, "Relational Schema");
                    dialog.setJMenuBar(new RSMenu(rs));
                    dialog.setContentPane(rs);
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);

                    try {
                        Converter.convert(evilEr.diagramPanel.diagram.entities, rs.diagram.tables);
                    } catch (Exception ex) {
                        Prompts.report(ex);
                    }
                }
            }));
            add(new JSeparator());
            add(new JMenuItem(new AbstractAction("Export...") {
                public void actionPerformed(ActionEvent ae) {
                    if (JFileChooser.APPROVE_OPTION == main.ui.Chooser.imageChooser.showSaveDialog(evilEr)) try {
                        ImageIO.write(evilEr.diagramPanel.diagram.export(), "PNG", main.ui.Chooser.imageChooser.getFinal());
                    } catch (IOException e) {
                        report(e);
                        e.printStackTrace();
                    }
                }
            }));
        }});
        add(new JMenu("Edit") {{
            setMnemonic('E');
            add(new JMenuItem("Delete") {{
                addActionListener(e -> evilEr.diagramPanel.diagram.delete());
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JMenuItem("Center") {{
                addActionListener(ERMenu.this::center);
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
            setMnemonic('A');
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
            setMnemonic('V');
            add(new JMenu("Line Style") {{
                ButtonGroup group = new ButtonGroup();
                Line.LineStyle[] values = Line.LineStyle.values();
                Arrays.sort(values);
                JRadioButtonMenuItem[] buttons = new JRadioButtonMenuItem[values.length];
                for (int i = 0; i < values.length; i++) {
                    Line.LineStyle type = values[i];
                    group.add(add(buttons[i] = new JRadioButtonMenuItem(type.toString())));
                    buttons[i].addActionListener(evt -> evilEr.diagramPanel.diagram.lineStyle.set(type));
                }
                evilEr.diagramPanel.diagram.lineStyle.addListener(s -> group.setSelected(buttons[Arrays.binarySearch(values, s)].getModel(), true));
            }});
            add(new JCheckBoxMenuItem("AABB") {{
                evilEr.diagramPanel.diagram.aabb.addListener(this::setState);
                addActionListener(e -> {
                    evilEr.diagramPanel.diagram.aabb.set(getState());
                    evilEr.diagramPanel.diagram.repaint();
                });
            }});
            add(new JCheckBoxMenuItem("Grid") {{
                evilEr.diagramPanel.diagram.grid.addListener(this::setState);
                addActionListener(e -> {
                    evilEr.diagramPanel.diagram.grid.set(getState());
                    evilEr.diagramPanel.diagram.repaint();
                });
            }});
        }});
        add(new JMenu("Help") {{
            setMnemonic('H');
            add(new JMenuItem("About") {{
                addActionListener(e -> {
                    if (JOptionPane.showConfirmDialog(null,
                            "EVIL ER\nMade by octo-kumo (***REMOVED***)\nVisit repository?", "About", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                        try {
                            Desktop.getDesktop().browse(new URI("https://github.com/octo-kumo/evil-er"));
                        } catch (IOException | URISyntaxException ex) {
                            ex.printStackTrace();
                        }
                });
            }});
        }});
    }

    private void center(ActionEvent evt) {
        Vector com = evilEr.diagramPanel.diagram.entities.stream().map(e -> (Vector) e).reduce(Vector::add).orElse(Vector.ZERO).div(evilEr.diagramPanel.diagram.entities.size());
        evilEr.diagramPanel.diagram.origin.set(com.negate()
                .add(evilEr.diagramPanel.diagram.getWidth() / 2d / evilEr.diagramPanel.diagram.scale,
                        evilEr.diagramPanel.diagram.getHeight() / 2d / evilEr.diagramPanel.diagram.scale));
        evilEr.diagramPanel.diagram.repaint();
    }
}
