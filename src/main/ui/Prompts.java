package main.ui;

import javax.swing.*;

public class Prompts {
    public static void report(Exception e) {
        JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
