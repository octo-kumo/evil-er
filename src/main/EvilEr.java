package main;

import com.github.weisj.darklaf.LafManager;
import fonts.Fonts;
import main.er.ERControlPanel;
import main.er.ERDiagramPanel;
import main.er.ERInfoPanel;
import main.er.ERMenu;
import org.apache.commons.cli.*;
import utils.Prompts;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class EvilEr extends JPanel {
    public static final Font DEFAULT_FONT = new Font(null);
    public static Map<?, ?> RENDER_HINTS;
    public final ERControlPanel controlPanel;
    public final ERDiagramPanel diagramPanel;
    public final ERInfoPanel infoPanel;
    public final JSplitPane splitPane;
    public final FileList fileList;
    public JFrame frame;

    public EvilEr(JFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        diagramPanel = new ERDiagramPanel();
        infoPanel = new ERInfoPanel(this);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, diagramPanel, infoPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(0.9);
        splitPane.setResizeWeight(1);
        add(splitPane, BorderLayout.CENTER);

        controlPanel = new ERControlPanel(this);
        add(controlPanel, BorderLayout.NORTH);

        fileList = new FileList(this);
        add(fileList, BorderLayout.WEST);

        new Fonts(this);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(diagramPanel.diagram.keyManager);
    }

    public static void main(String... args) {
        Options options = new Options();
        Option input = new Option("d", "developer mode", false, "unlock hidden options and functions");
        input.setRequired(false);
        options.addOption(input);
        Option output = new Option("h", "help", false, "prints help");
        output.setRequired(false);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                formatter.printHelp("evil er command line options", options);
                return;
            }
            if (cmd.hasOption("d") || Objects.equals(System.getProperty("user.name"), "zy")) {
                ERMenu.DEV_MODE.set(true);
                System.out.println("Launching with extra features, enjoy~");
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }


        RENDER_HINTS = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        LafManager.install(LafManager.themeForPreferredStyle(LafManager.getPreferredThemeStyle()));
        ERMenu.loadThemeFromPreference();

        Fonts.loadFonts();

        JFrame frame = new JFrame("Evil ER :: " + Version.CURRENT);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        EvilEr evilEr = new EvilEr(frame);
        frame.setContentPane(evilEr);
        frame.setJMenuBar(new ERMenu(evilEr));
        frame.setAutoRequestFocus(true);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.requestFocus();

        Version.asyncUpdate();
        SwingUtilities.invokeLater(() -> {
            try {
                frame.setIconImages(Arrays.asList(
                        ImageIO.read(Objects.requireNonNull(EvilEr.class.getResourceAsStream("/images/icons/iconx16.png"))),
                        ImageIO.read(Objects.requireNonNull(EvilEr.class.getResourceAsStream("/images/icons/iconx32.png"))),
                        ImageIO.read(Objects.requireNonNull(EvilEr.class.getResourceAsStream("/images/icons/iconx64.png"))),
                        ImageIO.read(Objects.requireNonNull(EvilEr.class.getResourceAsStream("/images/icons/iconx128.png"))),
                        ImageIO.read(Objects.requireNonNull(EvilEr.class.getResourceAsStream("/images/icons/iconx256.png")))
                ));
            } catch (IOException e) {
                Prompts.report(e);
                e.printStackTrace();
            }
        });

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                String[] ObjButtons = {"Yes", "No"};
                if (JOptionPane.showOptionDialog(null, "Are you sure you want to exit?", "Evil ER", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, ObjButtons, ObjButtons[1]) == JOptionPane.YES_OPTION)
                    System.exit(0);
            }
        });

        frame.requestFocus();
    }
}
