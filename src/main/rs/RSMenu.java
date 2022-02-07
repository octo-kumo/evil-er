package main.rs;

import com.google.gson.stream.JsonReader;
import model.er.Entity;
import model.serializers.Serializer;
import shapes.lines.Line;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static main.ui.Prompts.report;

public class RSMenu extends JMenuBar {
    public RSMenu(EvilRs evilRs) {
        add(new JMenu("File") {{
            add(new JMenuItem(new AbstractAction("Open ER") {
                public void actionPerformed(ActionEvent ae) {
                    if (JFileChooser.APPROVE_OPTION == main.ui.Chooser.jsonChooser.showOpenDialog(evilRs)) {
                        try (JsonReader reader = new JsonReader(new FileReader(main.ui.Chooser.jsonChooser.getFinal()))) {
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
                    if (JFileChooser.APPROVE_OPTION == main.ui.Chooser.imageChooser.showSaveDialog(evilRs)) try {
                        ImageIO.write(evilRs.diagram.export(), "PNG", main.ui.Chooser.imageChooser.getFinal());
                    } catch (IOException e) {
                        report(e);
                        e.printStackTrace();
                    }
                }
            }));
        }});
        add(new JMenu("View") {{
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
        }});
    }
}
