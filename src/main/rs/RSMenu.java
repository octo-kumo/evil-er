package main.rs;

import com.google.gson.stream.JsonReader;
import model.er.Entity;
import model.serializers.Serializer;
import shapes.lines.Line;
import utils.Chooser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static utils.Prompts.report;

public class RSMenu extends JMenuBar {
    public RSMenu(EvilRs evilRs) {
        add(new JMenu("File") {{
            setMnemonic('F');
            add(new JMenuItem(new AbstractAction("Open ER") {
                public void actionPerformed(ActionEvent ae) {
                    if (JFileChooser.APPROVE_OPTION == Chooser.jsonChooser.showOpenDialog(evilRs)) {
                        try (JsonReader reader = new JsonReader(new FileReader(Chooser.jsonChooser.getFinal()))) {
                            ArrayList<Entity> deserialized = Serializer.deserialize(reader);
                            evilRs.diagram.tables.clear();
                            Converter.convert(deserialized, evilRs.diagram.tables);
                        } catch (IOException e) {
                            report(e);
                            e.printStackTrace();
                        }
                    }
                }
            }));
            add(new JMenuItem(new AbstractAction("Export...") {
                public void actionPerformed(ActionEvent ae) {
                    if (JFileChooser.APPROVE_OPTION == Chooser.imageChooser.showSaveDialog(evilRs)) try {
                        ImageIO.write(evilRs.diagram.export(), "PNG", Chooser.imageChooser.getFinal());
                    } catch (IOException e) {
                        report(e);
                        e.printStackTrace();
                    }
                }
            }));
        }});
        add(new JMenu("Edit") {{
            setMnemonic('E');
            add(new JMenuItem(new AbstractAction("New Table") {
                public void actionPerformed(ActionEvent ae) {
                    evilRs.diagram.newTable();
                }
            }));
            add(new JMenuItem("New Table") {{
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                addActionListener(e -> {
                    evilRs.diagram.delete();
                });
            }});
            add(new JMenuItem("Revalidate") {{
                addActionListener(e -> evilRs.diagram.revalidate());
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
                    buttons[i].addActionListener(evt -> evilRs.diagram.lineStyle.set(type));
                }
                evilRs.diagram.lineStyle.addListener(s -> group.setSelected(buttons[Arrays.binarySearch(values, s)].getModel(), true));
            }});
            add(new JCheckBoxMenuItem("Grid") {{
                evilRs.diagram.grid.addListener(this::setState);
                addActionListener(e -> {
                    evilRs.diagram.grid.set(getState());
                    evilRs.diagram.repaint();
                });
            }});
            add(new JCheckBoxMenuItem("AABB") {{
                evilRs.diagram.aabb.addListener(this::setState);
                addActionListener(e -> {
                    evilRs.diagram.aabb.set(getState());
                    evilRs.diagram.repaint();
                });
            }});
            add(new JCheckBoxMenuItem("Brackets") {{
                evilRs.diagram.showBrackets.addListener(this::setState);
                addActionListener(e -> {
                    evilRs.diagram.showBrackets.set(getState());
                    evilRs.diagram.repaint();
                });
            }});
            add(new JCheckBoxMenuItem("Jump Intersects") {{
                evilRs.diagram.jumpLines.addListener(this::setState);
                addActionListener(e -> {
                    evilRs.diagram.jumpLines.set(getState());
                    evilRs.diagram.repaint();
                });
            }});
            add(new JCheckBoxMenuItem("Avoid Overlap") {{
                evilRs.diagram.avoidOverlap.addListener(this::setState);
                addActionListener(e -> {
                    evilRs.diagram.avoidOverlap.set(getState());
                    evilRs.diagram.repaint();
                });
            }});
        }});
    }
}
