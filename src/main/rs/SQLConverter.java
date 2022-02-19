package main.rs;

import model.rs.Table;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class SQLConverter {
    public static String toSQLInit(ArrayList<Table> tables) {
        return ("DROP DATABASE IF EXISTS evilEr;\n" +
                "CREATE DATABASE evilEr;\n" +
                "USE evilEr;\n\n" +
                "##### TABLE CREATION #####\n" +
                tables.stream().map(Table::toSQL).collect(Collectors.joining("\n\n")) +
                "\n\n" +
                "##### TABLE ALTER #####\n" +
                tables.stream().map(Table::toSQLForeign).collect(Collectors.joining("\n\n"))).replace("\t", "    ");
    }

    public static void showSQLDialog(String sql, Window parent) {
        JDialog dialog = new JDialog(parent, "SQL");
        JTextArea area = new JTextArea(sql, 20, 64);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        dialog.setContentPane(new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
