package main.er;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.components.help.HelpMenuItem;
import com.github.weisj.darklaf.iconset.AllIcons;
import com.github.weisj.darklaf.settings.ThemeSettings;
import com.github.weisj.darklaf.theme.Theme;
import com.github.weisj.darklaf.theme.spec.AccentColorRule;
import com.github.weisj.darklaf.theme.spec.FontSizeRule;
import fonts.Fonts;
import images.Icons;
import main.EvilEr;
import main.Onboarding;
import main.Version;
import main.rs.Converter;
import main.rs.EvilRs;
import main.rs.RSMenu;
import main.rs.SQLConverter;
import model.er.Entity;
import shapes.lines.Line;
import utils.Chooser;
import utils.Prompts;
import utils.models.Reactive;
import utils.models.TransferableImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.prefs.Preferences;

import static utils.Prompts.report;

public class ERMenu extends JMenuBar {

    public static final Preferences THEME = Preferences.userRoot().node("theme");
    public static final Preferences SETTINGS = Preferences.userRoot().node("settings");
    public static final Reactive<Boolean> DEV_MODE = new Reactive<>(false);

    public ERMenu(EvilEr evilEr) {
        ERDiagram diagram = evilEr.diagramPanel.diagram;
        add(new JMenu("File") {
            {
                setMnemonic('F');
                add(new JMenuItem(new AbstractAction("Open", AllIcons.Action.Search.get()) {
                    public void actionPerformed(ActionEvent ae) {
                        if (JFileChooser.APPROVE_OPTION == Chooser.jsonChooser.showOpenDialog(evilEr))
                            diagram.setCurrentFile(Chooser.jsonChooser.getFinal());
                    }
                }));
                add(new JMenuItem("Save", AllIcons.Action.Save.get()) {
                    {
                        addActionListener(e -> diagram.saveToFile(evilEr.fileList.getProjectPath()));
                        setAccelerator(
                                KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                    }
                });
                add(new JMenuItem("Save as...") {
                    {
                        addActionListener(e -> diagram.saveAsFile(evilEr.fileList.getProjectPath()));
                        setAccelerator(KeyStroke.getKeyStroke('S',
                                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));
                    }
                });
                add(new JCheckBoxMenuItem("File List", true) {
                    {
                        addActionListener(e -> evilEr.fileList.setVisible(getState()));
                    }
                });
                add(new JSeparator());
                if (DEV_MODE.get())
                    add(new JMenuItem(new AbstractAction("To schema") {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JDialog dialog = new JDialog(evilEr.frame, "Relational Schema");
                            EvilRs rs = new EvilRs(dialog);
                            rs.diagram.darkMode.set(diagram.darkMode);
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
                if (DEV_MODE.get())
                    add(new JMenuItem(new AbstractAction("To sql") {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            SQLConverter.showSQLDialog(Converter.convertToSQLInsert(diagram.entities, "[DID]"),
                                    evilEr.frame);
                        }
                    }));
                if (DEV_MODE.get())
                    add(new JCheckBoxMenuItem("Developer Mode", true) {
                        {
                            setEnabled(false);
                        }
                    });
                add(new JSeparator());
                add(new JMenuItem(new AbstractAction("Export...", Icons.Export) {
                    public void actionPerformed(ActionEvent ae) {
                        if (JFileChooser.APPROVE_OPTION == Chooser.imageChooser.showSaveDialog(evilEr))
                            try {
                                ImageIO.write(diagram.export(), "PNG", Chooser.imageChooser.getFinal());
                            } catch (IOException e) {
                                report(e);
                                e.printStackTrace();
                            }
                    }
                }));
                add(new JMenuItem(new AbstractAction("Copy Image") {
                    public void actionPerformed(ActionEvent ae) {
                        TransferableImage transferable = new TransferableImage(diagram.export());
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
                    }
                }));
            }
        });
        add(new EREditMenu(diagram));
        add(new ERAddMenu(diagram));
        add(new ERViewMenu(diagram));
        add(new JMenu("Help") {
            {
                setMnemonic('H');
                add(new JMenuItem("About") {
                    {
                        addActionListener(e -> {
                            if (JOptionPane.showConfirmDialog(null,
                                    "EVIL ER\nMade by octo-kumo (Yun)\nVisit repository?", "About",
                                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()
                                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                                try {
                                    Desktop.getDesktop().browse(new URI("https://github.com/octo-kumo/evil-er"));
                                } catch (IOException | URISyntaxException ex) {
                                    ex.printStackTrace();
                                }
                        });
                    }
                });
                add(new JCheckBoxMenuItem("Check for updates", SETTINGS.getBoolean(Version.CHECK_UPDATES, true)) {
                    {
                        addActionListener(e -> {
                            SETTINGS.putBoolean(Version.CHECK_UPDATES, isSelected());
                            if (isSelected())
                                Version.asyncUpdate();
                        });
                    }
                });
                add(new HelpMenuItem("Help") {
                    {
                        addActionListener(e -> {
                            if (JOptionPane.showConfirmDialog(null, "Visit online documentation?", "Help",
                                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()
                                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                                try {
                                    Desktop.getDesktop().browse(new URI("https://github.com/octo-kumo/evil-er/wiki"));
                                } catch (IOException | URISyntaxException ex) {
                                    ex.printStackTrace();
                                }
                        });
                    }
                });
                add(new JMenuItem("Report Issue") {
                    {
                        addActionListener(e -> {
                            if (JOptionPane.showConfirmDialog(null, "Found a bug? Want to report?", "Report",
                                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()
                                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                                try {
                                    Desktop.getDesktop()
                                            .browse(new URI("https://github.com/octo-kumo/evil-er/issues/new/choose"));
                                } catch (IOException | URISyntaxException ex) {
                                    ex.printStackTrace();
                                }
                        });
                    }
                });
                add(new JMenuItem("Onboarding") {
                    {
                        addActionListener(e -> {
                            Onboarding onboarding = (Onboarding) evilEr.frame.getGlassPane();
                            onboarding.doOnboarding();
                        });
                    }
                });
            }
        });
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
            if (accentColor != null)
                THEME.putInt("accentColor", accentColor.getRGB());
            if (selectionColor != null)
                THEME.putInt("selectionColor", selectionColor.getRGB());
            ERDiagram.HIGHLIGHT = Theme.isDark(instance.getTheme())
                    ? (accentColor == null ? selectionColor == null ? Color.GRAY : selectionColor : accentColor)
                    : (selectionColor == null ? accentColor == null ? Color.LIGHT_GRAY : accentColor : selectionColor);
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

            instance.setAccentColorRule(AccentColorRule.fromColor(new Color(THEME.getInt("accentColor", 0xFF0063B1)),
                    new Color(THEME.getInt("selectionColor", 0xFF7FAFD4))));
            instance.setFontSizeRule(FontSizeRule.relativeAdjustment(THEME.getInt("fontSize", 100)));
            String theme = THEME.get("theme", null);
            instance.setTheme(
                    Arrays.stream(LafManager.getRegisteredThemes()).filter(r -> Objects.equals(r.getName(), theme))
                            .findAny().orElse(LafManager.themeForPreferredStyle(LafManager.getPreferredThemeStyle())));
            instance.apply();
        } catch (Exception e) {
            e.printStackTrace();
            Prompts.report(e);
        }
        saveThemeToPreference();
    }

    public static class ERAddMenu extends JMenu {
        public ERAddMenu(ERDiagram diagram) {
            super("Add");
            setMnemonic('A');
            char[] mnemonics = new char[] { 'e', 'r', 'a', 's' };
            Entity.Type[] values = Entity.Type.values();
            for (int i = 0; i < values.length; i++) {
                Entity.Type type = values[i];
                final char mnemonic = mnemonics[i];
                add(new JMenuItem(type.name()) {
                    {
                        addActionListener(e -> diagram.setAddingType(type));
                        setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar(mnemonic), 0));
                    }
                });
            }
            add(new JCheckBoxMenuItem("Locked") {
                {
                    diagram.locked.addListener(this::setState);
                    addActionListener(e -> diagram.locked.set(getState()));
                }
            });
        }
    }

    public static class EREditMenu extends JMenu {
        public EREditMenu(ERDiagram diagram) {
            super("Edit");
            setMnemonic('E');
            add(new JMenuItem("Copy", AllIcons.Action.Copy.get()) {
                {
                    addActionListener(e -> diagram.copy());
                    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                }
            });
            add(new JMenuItem("Cut", AllIcons.Action.Cut.get()) {
                {
                    addActionListener(e -> diagram.cut());
                    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                }
            });
            add(new JMenuItem("Paste", AllIcons.Action.Paste.get()) {
                {
                    addActionListener(e -> diagram.paste());
                    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                }
            });
            add(new JMenuItem("Delete", AllIcons.Action.Delete.get()) {
                {
                    addActionListener(e -> diagram.delete());
                    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                }
            });
            add(new JMenuItem("Select All") {
                {
                    addActionListener(e -> {
                        diagram.selection.clear();
                        diagram.selection.addAll(diagram.entities);
                        diagram.repaint();
                    });
                    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                }
            });
            add(new JSeparator());
            add(new JMenuItem("Undo", AllIcons.Action.Undo.get()) {
                {
                    addActionListener(e -> diagram.undo());
                    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                }
            });
            add(new JMenuItem("Redo", AllIcons.Action.Redo.get()) {
                {
                    addActionListener(e -> diagram.redo());
                    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                            KeyEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                }
            });
            add(new JSeparator());
            add(new JMenuItem("Regress") {
                {
                    addActionListener(evt -> {
                        double total;
                        int ops = 0;
                        do {
                            total = Entity.applyForces(diagram.entities, 100);
                            ops++;
                        } while (total > 1 && ops < 100);
                        diagram.centralize();
                        System.out.println("Regression Complete, Operations = " + ops);
                        diagram.repaint();
                    });
                    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                            InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                }
            });
            add(new JMenuItem("Clear") {
                {
                    addActionListener(e -> {
                        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                                SwingUtilities.windowForComponent(this), "Do you really want to clear all entities?",
                                "Warning", JOptionPane.YES_NO_OPTION)) {
                            diagram.entities.clear();
                            diagram.repaint();
                        }
                    });
                }
            });
            add(new JMenuItem("Gridify") {
                {
                    addActionListener(e -> {
                        diagram.gridify();
                        diagram.repaint();
                    });
                }
            });
        }
    }

    public static class ERViewMenu extends JMenu {
        public ERViewMenu(ERDiagram diagram) {
            super("View");
            setMnemonic('V');
            add(new JMenuItem("Center") {
                {
                    addActionListener(evt -> diagram.centralize());
                    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                            InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                }
            });
            add(new JMenuItem("Fit") {
                {
                    addActionListener(evt -> diagram.fitScreen());
                    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                            InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                }
            });
            add(new JSeparator());
            add(new JMenu("Line Style") {
                {
                    ButtonGroup group = new ButtonGroup();
                    Line.LineStyle[] values = Line.LineStyle.values();
                    Arrays.sort(values);
                    JRadioButtonMenuItem[] buttons = new JRadioButtonMenuItem[values.length];
                    for (int i = 0; i < values.length; i++) {
                        Line.LineStyle type = values[i];
                        group.add(add(buttons[i] = new JRadioButtonMenuItem(type.toString())));
                        buttons[i].addActionListener(evt -> diagram.lineStyle.set(type));
                    }
                    diagram.lineStyle.addListener(
                            s -> group.setSelected(buttons[Arrays.binarySearch(values, s)].getModel(), true));
                }
            });
            add(new JCheckBoxMenuItem("Debug") {
                {
                    diagram.debug.addListener(this::setState);
                    addActionListener(e -> {
                        diagram.debug.set(getState());
                        diagram.repaint();
                    });
                }
            });
            add(new JCheckBoxMenuItem("AABB") {
                {
                    diagram.aabb.addListener(this::setState);
                    addActionListener(e -> {
                        diagram.aabb.set(getState());
                        diagram.repaint();
                    });
                }
            });
            add(new JCheckBoxMenuItem("Grid") {
                {
                    diagram.grid.addListener(this::setState);
                    addActionListener(e -> {
                        diagram.grid.set(getState());
                        diagram.repaint();
                    });
                }
            });
            add(new JMenuItem("Font...") {
                {
                    addActionListener(e -> Fonts.open());
                }
            });
            add(new JMenuItem("Theme...") {
                {
                    addActionListener(e -> ThemeSettings.showSettingsDialog(this));
                }
            });
        }
    }
}
