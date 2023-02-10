package main;

import com.github.weisj.darklaf.components.OverlayScrollPane;
import com.github.weisj.darklaf.iconset.AllIcons;
import com.google.common.io.Files;
import images.Icons;
import main.er.ERMenu;
import utils.Chooser;
import utils.Prompts;
import utils.Utils;
import utils.callbacks.ListAction;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class FileList extends JPanel {
    private static final String PROJECT_PATH = "project_path";
    private final TitledBorder title;
    private final JList<File> files;
    private final EvilEr evilEr;
    private String projectPath;

    private static boolean showFolders = true;
    private static boolean showAllFiles = false;

    public FileList(EvilEr evilEr) {
        this.evilEr = evilEr;
        setLayout(new BorderLayout());
        add(createToolbar(), BorderLayout.NORTH);
        add(new OverlayScrollPane(files = new JList<>(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        files.setCellRenderer(new FileListItem());
        ListAction.apply(files, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                File file = files.getSelectedValue();
                open(file);
            }
        });
        setBorder(title = new TitledBorder("Loading..."));
        setPreferredSize(new Dimension(200, 200));

        setProjectPath(projectPath);

        files.setComponentPopupMenu(new PopupMenu());
    }

    private void open(File file) {
        if (file == null) return;
        if (file.isDirectory()) setProjectPath(file.toPath().normalize().toString());
        else if (Objects.equals(evilEr.diagramPanel.diagram.getCurrentFile(), file)) rename(file);
        else evilEr.diagramPanel.diagram.setCurrentFile(file);
        repaint();
    }

    private void delete(File file) {
        if (file == null) return;
//        if (file.isDirectory()) return;

        File folder = new File(getProjectPath(), ".evil-trash");
        if (!folder.exists() && !folder.mkdirs()) {
            JOptionPane.showMessageDialog(this, "Deletion not executed", "Failed: mkdirs", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File newFile = new File(folder, file.getName());
        if (newFile.exists()) newFile = new File(folder, file.getName() + System.currentTimeMillis());
        try {
            Files.move(file, newFile);
            refresh();
        } catch (Exception e) {
            Prompts.report(e);
        }
    }

    private void rename(File file) {
        Object input = JOptionPane.showInputDialog(evilEr, "What is the new name?", "Rename", JOptionPane.QUESTION_MESSAGE, null, null, file.getName());
        if (input == null) return;
        String newName = input.toString();
        if (!newName.endsWith(".dig")) newName += ".dig";
        File newFile = new File(getProjectPath(), newName);
        try {
            Files.move(file, newFile);
            if (file.equals(evilEr.diagramPanel.diagram.getCurrentFile()))
                evilEr.diagramPanel.diagram.setCurrentFileQuietly(newFile);
            refresh();
            files.setSelectedValue(newFile, true);
        } catch (Exception e) {
            Prompts.report(e);
        }
    }

    private JToolBar createToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setMargin(new Insets(0, 0, 0, 0));
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.add(new AbstractAction("Open Directory", Icons.MenuOpen) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Chooser.folderChooser.setCurrentDirectory(new File(getProjectPath()));
                if (JFileChooser.APPROVE_OPTION == Chooser.folderChooser.showOpenDialog(evilEr)) {
                    setProjectPath(Chooser.folderChooser.getSelectedFile().getPath());
                }
            }
        });
//        toolBar.add(new AbstractAction("Up Folder", Icons.UpFolder) {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                setProjectPath(new File(getProjectPath()).getParent());
//            }
//        });
        toolBar.add(new AbstractAction("New Folder", Icons.AddFolder) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object input = JOptionPane.showInputDialog(FileList.this, "New Folder Name", "New Folder", JOptionPane.QUESTION_MESSAGE, Icons.AddFolder, null, null);
                if (input != null) {
                    File file = new File(projectPath, input.toString());
                    if (file.mkdir()) refresh();
                    else Prompts.report(new FileSystemException("Unable to create folder"));
                }
            }
        });
        toolBar.add(new AbstractAction("New Diagram", Icons.AddFile) {
            @Override
            public void actionPerformed(ActionEvent e) {
                evilEr.diagramPanel.diagram.setCurrentFile(findAvailableFile());
                refresh();
            }
        });
        return toolBar;
    }

    public File findAvailableFile() {
        for (int i = 0; ; i++) {
            File file = new File(getProjectPath(), "Untitled" + (i == 0 ? "" : "-" + i) + ".dig");
            if (!file.exists()) return file;
        }
    }

    public void setProjectPath(String projectPath) {
        if (projectPath == null)
            projectPath = ERMenu.SETTINGS.get(PROJECT_PATH, FileSystemView.getFileSystemView().getDefaultDirectory().getPath());
        this.projectPath = projectPath;
        ERMenu.SETTINGS.put(PROJECT_PATH, projectPath);
        refresh();
    }

    public void refresh() {
        File folder = new File(projectPath);
        title.setTitle(folder.getAbsolutePath());
        boolean valid = folder.exists() && folder.isDirectory() && folder.canRead() && folder.canWrite();
        if (!valid) setProjectPath(FileSystemView.getFileSystemView().getDefaultDirectory().getPath());
        else {
            File[] listData = folder.listFiles(FileList::accept);
            if (listData == null) listData = new File[]{};
            Arrays.sort(listData, (f1, f2) -> {
                if (f1.isDirectory() != f2.isDirectory()) return f1.isDirectory() ? 1 : -1;
                return f1.compareTo(f2);
            });
            listData = Utils.prepend(listData, new File(projectPath, ".."));
            files.setListData(listData);
        }
        repaint();
    }

    public String getProjectPath() {
        return projectPath;
    }

    private static boolean accept(File file) {
        return (showFolders && file.isDirectory()) ||
                (showAllFiles || file.getName().endsWith(".dig") ||
                        file.getName().endsWith(".json"));
    }

    private class FileListItem extends Box implements ListCellRenderer<File> {
        private final JLabel name;
        private final JLabel lastEdit;

        public FileListItem() {
            super(BoxLayout.Y_AXIS);
            add(name = new JLabel());
            add(lastEdit = new JLabel());
            setBorder(new LineBorder(getForeground()));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends File> list, File file, int index, boolean isSelected, boolean cellHasFocus) {
            name.setText(file.getName());
            name.setFont(new Font(null, file.equals(evilEr.diagramPanel.diagram.getCurrentFile()) ? Font.BOLD : Font.PLAIN, 14));
            name.setIcon(file.isFile() ? AllIcons.Files.Text.get() : file.isDirectory() ? AllIcons.Files.Folder.get() : AllIcons.Files.Unknown.get());
            Date date = new Date(file.lastModified());
            SimpleDateFormat format = new SimpleDateFormat("MMM dd HH:mm:ss");
            lastEdit.setText("Last edited " + format.format(date));

            lastEdit.setVisible(file.isFile());
            return this;
        }
    }

    private class PopupMenu extends JPopupMenu {
        private File selectedFile;

        public PopupMenu() {
            add(new JMenuItem("Open") {{
                addActionListener(e -> open(selectedFile));
            }});
            add(new JMenuItem("Open via System") {{
                addActionListener(e -> {
                    try {
                        Desktop.getDesktop().open(Optional.ofNullable(selectedFile).orElseGet(() -> new File(projectPath)));
                    } catch (IOException ex) {
                        Prompts.report(ex);
                    }
                });
            }});
            add(new JMenuItem("Show in System") {{
                addActionListener(e -> {
                    try {
                        Desktop.getDesktop().open(selectedFile.getParentFile());
                    } catch (IOException ex) {
                        Prompts.report(ex);
                    }
                });
            }});
            add(new JSeparator());
            add(new JCheckBoxMenuItem("Show Folders", true) {{
                addActionListener(e -> {
                    showFolders = isSelected();
                    refresh();
                });
            }});
            add(new JCheckBoxMenuItem("Show All Files") {{
                addActionListener(e -> {
                    showAllFiles = isSelected();
                    refresh();
                });
            }});
            add(new JSeparator());
            add(new JMenuItem("Rename") {{
                setIcon(AllIcons.Files.Text.get());
                addActionListener(e -> {
                    rename(selectedFile);
                });
            }});
            add(new JMenuItem("Delete") {{
                setIcon(AllIcons.Action.Delete.get());
                addActionListener(e -> {
                    if (JOptionPane.showConfirmDialog(null, "Are you sure?", "Delete", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        return;
                    if (selectedFile.equals(evilEr.diagramPanel.diagram.getCurrentFile()))
                        evilEr.diagramPanel.diagram.setCurrentFile(null);
                    delete(selectedFile);
                });
            }});
        }

        @Override
        public void show(Component invoker, int x, int y) {
            int row = files.locationToIndex(new Point(x, y));
            if (row != -1) files.setSelectedIndex(row);
            super.show(invoker, x, y);

            selectedFile = files.getSelectedValue();
        }
    }
}
