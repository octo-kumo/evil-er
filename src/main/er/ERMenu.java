package main.er;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.settings.ThemeSettings;
import com.github.weisj.darklaf.theme.Theme;
import com.github.weisj.darklaf.theme.info.AccentColorRule;
import com.github.weisj.darklaf.theme.info.FontSizeRule;
import com.google.gson.stream.JsonReader;
import main.EvilEr;
import main.rs.Converter;
import main.rs.EvilRs;
import main.rs.RSMenu;
import model.Vector;
import model.er.Entity;
import model.serializers.Serializer;
import shapes.lines.Line;
import utils.Chooser;
import utils.JFontChooser;
import utils.Prompts;
import utils.models.TransferableImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static utils.Prompts.report;

public class ERMenu extends JMenuBar {

    public static final Preferences THEME = Preferences.userRoot().node("theme");
    public static final Preferences DRAWING_FONT = Preferences.userRoot().node("drawingFont");
    private final EvilEr evilEr;
    private final JFontChooser fontChooser;
    private JDialog fontChooserFrame;

    public ERMenu(EvilEr evilEr) {
        this.evilEr = evilEr;
        this.fontChooser = new JFontChooser();
        resetFontSelector();

        ERDiagram diagram = evilEr.diagramPanel.diagram;
        add(new JMenu("File") {{
            setMnemonic('F');
            add(new JMenuItem(new AbstractAction("Open") {
                public void actionPerformed(ActionEvent ae) {
                    if (JFileChooser.APPROVE_OPTION == Chooser.jsonChooser.showOpenDialog(evilEr)) {
                        try (JsonReader reader = new JsonReader(new FileReader(Chooser.jsonChooser.getFinal()))) {
                            ArrayList<Entity> deserialized = Serializer.deserialize(reader);
                            diagram.entities.clear();
                            diagram.entities.addAll(deserialized);
                            diagram.repaint();
                        } catch (IOException e) {
                            report(e);
                            e.printStackTrace();
                        }
                    }
                }
            }));
            add(new JMenuItem(new AbstractAction("Save") {
                public void actionPerformed(ActionEvent ae) {
                    String json = Serializer.serialize(diagram.entities);
                    try (FileWriter fw = new FileWriter("session.json")) {
                        fw.write(json);
                    } catch (IOException e) {
                        report(e);
                        e.printStackTrace();
                    }
                    ArrayList<Entity> deserialized = Serializer.deserialize(json);
                    diagram.entities.clear();
                    diagram.entities.addAll(deserialized);
                    diagram.repaint();
                }
            }) {{
                setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JMenuItem(new AbstractAction("Save as...") {
                public void actionPerformed(ActionEvent ae) {
                    if (JFileChooser.APPROVE_OPTION == Chooser.jsonChooser.showSaveDialog(evilEr)) {
                        try (Writer writer = new FileWriter(Chooser.jsonChooser.getFinal())) {
                            Serializer.serialize(diagram.entities, writer);
                        } catch (IOException e) {
                            report(e);
                            e.printStackTrace();
                        }
                    }
                }
            }));
            add(new JMenuItem(new AbstractAction("To clipboard") {
                public void actionPerformed(ActionEvent ae) {
                    TransferableImage transferable = new TransferableImage(diagram.export());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
                }
            }));
            add(new JMenuItem(new AbstractAction("To schema") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    EvilRs rs = new EvilRs();
                    rs.diagram.darkMode.set(diagram.darkMode);
                    JDialog dialog = new JDialog(evilEr.frame, "Relational Schema");
                    dialog.setJMenuBar(new RSMenu(rs));
                    dialog.setContentPane(rs);
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);

                    try {
                        Converter.convert(diagram.entities, rs.diagram.tables);
                    } catch (Exception ex) {
                        Prompts.report(ex);
                    }
                }
            }));
            add(new JSeparator());
            add(new JMenuItem(new AbstractAction("Export...") {
                public void actionPerformed(ActionEvent ae) {
                    if (JFileChooser.APPROVE_OPTION == Chooser.imageChooser.showSaveDialog(evilEr)) try {
                        ImageIO.write(diagram.export(), "PNG", Chooser.imageChooser.getFinal());
                    } catch (IOException e) {
                        report(e);
                        e.printStackTrace();
                    }
                }
            }));
        }});
        add(new JMenu("Edit") {{
            setMnemonic('E');
            add(new JMenuItem("Copy") {{
                addActionListener(e -> diagram.copy());
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JMenuItem("Cut") {{
                addActionListener(e -> diagram.copy());
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JMenuItem("Paste") {{
                addActionListener(e -> diagram.paste());
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JMenuItem("Delete") {{
                addActionListener(e -> diagram.delete());
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JMenuItem("Select All") {{
                addActionListener(e -> {
                    diagram.selection.clear();
                    diagram.selection.addAll(diagram.entities);
                    diagram.repaint();
                });
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JSeparator());
            add(new JMenuItem("Undo") {{
                addActionListener(e -> diagram.undo());
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JMenuItem("Redo") {{
                addActionListener(e -> diagram.redo());
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                        KeyEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JSeparator());
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
                        total = Entity.applyForces(diagram.entities, 100);
                        ops++;
                    } while (total > 1 && ops < 100);
                    center(null);
                    System.out.println("Regression Complete, Operations = " + ops);
                    diagram.repaint();
                });
                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                        InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JMenuItem(new AbstractAction("Clear") {
                public void actionPerformed(ActionEvent ae) {
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(evilEr, "Do you really want to clear all entities?", "Warning", JOptionPane.YES_NO_OPTION)) {
                        diagram.entities.clear();
                        diagram.repaint();
                    }
                }
            }));
//            add(new JCheckBoxMenuItem("Connecting") {{
//                evilEr.diagramPanel.diagram.connecting.addListener(this::setState);
//                addActionListener(e -> evilEr.diagramPanel.diagram.connecting.set(getState()));
//                setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_MASK));
//            }});
        }});
        add(new JMenu("Add") {{
            setMnemonic('A');
            char[] mnemonics = new char[]{'e', 'r', 'a', 's'};
            Entity.Type[] values = Entity.Type.values();
            for (int i = 0; i < values.length; i++) {
                Entity.Type type = values[i];
                final char mnemonic = mnemonics[i];
                add(new JMenuItem(type.name()) {{
                    addActionListener(e -> diagram.setAddingType(type));
                    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar(mnemonic), 0));
                }});
            }
            add(new JCheckBoxMenuItem("Locked") {{
                diagram.locked.addListener(this::setState);
                addActionListener(e -> diagram.locked.set(getState()));
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
                    buttons[i].addActionListener(evt -> diagram.lineStyle.set(type));
                }
                diagram.lineStyle.addListener(s -> group.setSelected(buttons[Arrays.binarySearch(values, s)].getModel(), true));
            }});
            add(new JCheckBoxMenuItem("AABB") {{
                diagram.aabb.addListener(this::setState);
                addActionListener(e -> {
                    diagram.aabb.set(getState());
                    diagram.repaint();
                });
            }});
            add(new JCheckBoxMenuItem("Grid") {{
                diagram.grid.addListener(this::setState);
                addActionListener(e -> {
                    diagram.grid.set(getState());
                    diagram.repaint();
                });
            }});
            add(new JMenuItem("Toggle Info Panel") {{
                addActionListener(e -> {
                    int width = evilEr.splitPane.getWidth() - evilEr.splitPane.getDividerSize();
                    double pos = 1d * evilEr.splitPane.getDividerLocation() / width;
                    evilEr.splitPane.setDividerLocation(pos == 1 ? width - 256 : width);
                });
            }});
            add(new JMenuItem("Font...") {{
                addActionListener(e -> openFontChooser());
            }});
            add(new JMenuItem("Theme...") {{
                addActionListener(e -> ThemeSettings.showSettingsDialog(this));
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

    public static void saveThemeToPreference() {
        try {
            ThemeSettings instance = ThemeSettings.getInstance();
            System.out.printf("Saving theme to preferences :: %s%n", instance.getTheme().getName());
            THEME.putBoolean("isThemeFollowsSystem", instance.isThemeFollowsSystem());
            THEME.putBoolean("isFontSizeFollowsSystem", instance.isFontSizeFollowsSystem());
            THEME.putBoolean("isAccentColorFollowsSystem", instance.isAccentColorFollowsSystem());
            THEME.putBoolean("isSelectionColorFollowsSystem", instance.isSelectionColorFollowsSystem());
            THEME.putBoolean("isSystemPreferencesEnabled", instance.isSystemPreferencesEnabled());

            Color accentColor = instance.getAccentColorRule().getAccentColor();
            Color selectionColor = instance.getAccentColorRule().getSelectionColor();
            if (accentColor != null) THEME.putInt("accentColor", accentColor.getRGB());
            if (selectionColor != null) THEME.putInt("selectionColor", selectionColor.getRGB());
            ERDiagram.HIGHLIGHT = Theme.isDark(instance.getTheme()) ?
                    (accentColor == null ? selectionColor == null ? Color.GRAY : selectionColor : accentColor) :
                    (selectionColor == null ? accentColor == null ? Color.LIGHT_GRAY : accentColor : selectionColor);
            THEME.putInt("fontSize", instance.getFontSizeRule().getPercentage());

            THEME.put("theme", instance.getTheme().getName());
            THEME.flush();
        } catch (Exception e) {
            e.printStackTrace();
            Prompts.report(e);
        }
    }

    public static void loadThemeFromPreference() {
        try {
            System.out.println("Loading theme from preferences");
            ThemeSettings instance = ThemeSettings.getInstance();

            instance.setThemeFollowsSystem(THEME.getBoolean("isThemeFollowsSystem", true));
            instance.setFontSizeFollowsSystem(THEME.getBoolean("isFontSizeFollowsSystem", true));
            instance.setAccentColorFollowsSystem(THEME.getBoolean("isAccentColorFollowsSystem", true));
            instance.setSelectionColorFollowsSystem(THEME.getBoolean("isSelectionColorFollowsSystem", true));
            instance.setSystemPreferencesEnabled(THEME.getBoolean("isSystemPreferencesEnabled", true));

            instance.setAccentColorRule(AccentColorRule.fromColor(
                    new Color(THEME.getInt("accentColor", 0xFF0063B1)),
                    new Color(THEME.getInt("selectionColor", 0xFF7FAFD4))
            ));
            instance.setFontSizeRule(FontSizeRule.relativeAdjustment(THEME.getInt("fontSize", 100)));
            String theme = THEME.get("theme", null);
            instance.setTheme(Arrays.stream(LafManager.getRegisteredThemes()).filter(r -> Objects.equals(r.getName(), theme))
                    .findAny().orElse(LafManager.themeForPreferredStyle(LafManager.getPreferredThemeStyle())));
            instance.apply();
        } catch (Exception e) {
            e.printStackTrace();
            Prompts.report(e);
        }
        saveThemeToPreference();
    }

    private void resetFontSelector() {
        String name = DRAWING_FONT.get("fontName", EvilEr.DEFAULT_FONT.getFamily());
        int size = DRAWING_FONT.getInt("fontSize", EvilEr.DEFAULT_FONT.getSize());
        int style = DRAWING_FONT.getInt("fontStyle", EvilEr.DEFAULT_FONT.getStyle());

        fontChooser.setSelectedFontFamily(name);
        fontChooser.setSelectedFontSize(size);
        fontChooser.setSelectedFontStyle(style);
        evilEr.diagramPanel.diagram.setFont(fontChooser.getSelectedFont());
    }

    private void persistFontSelector() {
        DRAWING_FONT.put("fontName", fontChooser.getSelectedFontFamily());
        DRAWING_FONT.getInt("fontSize", fontChooser.getSelectedFontSize());
        DRAWING_FONT.getInt("fontStyle", fontChooser.getSelectedFontStyle());
        evilEr.diagramPanel.diagram.setFont(fontChooser.getSelectedFont());
    }

    private void center(ActionEvent evt) {
        Vector com = evilEr.diagramPanel.diagram.entities.parallelStream().map(e -> (Vector) e).reduce(Vector::add).orElse(Vector.ZERO).div(evilEr.diagramPanel.diagram.entities.size());
        evilEr.diagramPanel.diagram.origin.set(com.negate()
                .add(evilEr.diagramPanel.diagram.getWidth() / 2d / evilEr.diagramPanel.diagram.scale,
                        evilEr.diagramPanel.diagram.getHeight() / 2d / evilEr.diagramPanel.diagram.scale));
        evilEr.diagramPanel.diagram.repaint();
    }

    public void openFontChooser() {
        if (fontChooserFrame == null) {
            fontChooserFrame = new JDialog(evilEr.frame, "Font Chooser");
            fontChooserFrame.setContentPane(new JPanel() {{
                setLayout(new BorderLayout());
                add(fontChooser, BorderLayout.CENTER);
                add(new JPanel() {{
                    setLayout(new FlowLayout(FlowLayout.RIGHT));
                    add(new JButton("Cancel") {{
                        addActionListener(e -> {
                            resetFontSelector();
                            fontChooserFrame.setVisible(false);
                        });
                    }});
                    add(new JButton("Reset") {{
                        addActionListener(e -> {
                            try {
                                DRAWING_FONT.clear();
                                resetFontSelector();
                            } catch (BackingStoreException ex) {
                                Prompts.report(ex);
                            }
                        });
                    }});
                    add(new JButton("Apply") {{
                        addActionListener(e -> {
                            persistFontSelector();
                            fontChooserFrame.setVisible(false);
                        });
                    }});
                }}, BorderLayout.SOUTH);
            }});
            fontChooserFrame.pack();
        }
        fontChooserFrame.setLocationRelativeTo(evilEr);
        fontChooserFrame.setVisible(true);
    }
}
