package main.rs;

import com.google.gson.stream.JsonReader;
import model.er.Entity;
import model.serializers.Serializer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
    }
}
