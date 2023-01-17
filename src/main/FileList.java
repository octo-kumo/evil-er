package main;

import com.github.weisj.darklaf.components.OverlayScrollPane;
import com.github.weisj.darklaf.iconset.AllIcons;
import images.Icons;
import utils.Chooser;
import utils.Prompts;
import utils.callbacks.ListAction;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class FileList extends JPanel {
    private final TitledBorder title;
    private final JList<File> files;
    private String projectPath;
    private EvilEr evilEr;

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
        if (file.isDirectory()) setProjectPath(file.getPath());
        else evilEr.diagramPanel.diagram.setCurrentFile(file);
        repaint();
    }

    private void delete(File file) {
        if (file == null) return;
        if (file.isDirectory()) return;
        File folder = new File(getProjectPath(), ".evil-trash");
        if (!folder.exists()) folder.mkdirs();
        file.renameTo(new File(folder, file.getName()));
        refresh();
    }

    private JToolBar createToolbar() {
        JToolBar toolBar = new JToolBar();
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
        toolBar.add(new AbstractAction("Up Folder", Icons.UpFolder) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setProjectPath(new File(getProjectPath()).getParent());
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
        if (projectPath == null) projectPath = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
        this.projectPath = projectPath;
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
            files.setListData(listData);
        }
        repaint();
    }

    public String getProjectPath() {
        return projectPath;
    }

    private static boolean accept(File file) {
        return file.isDirectory() || file.getName().endsWith(".dig") || file.getName().endsWith(".json");
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
                        Desktop.getDesktop().open(selectedFile);
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
            add(new JMenuItem("Rename") {{
                setIcon(AllIcons.Files.Text.get());
                addActionListener(e -> {
                    String newName = String.valueOf(JOptionPane.showInputDialog(evilEr, "What is the new name?", "Rename", JOptionPane.QUESTION_MESSAGE, null, null, selectedFile.getName()));
                    if (newName == null) return;
                    File newFile = new File(getProjectPath(), newName);
                    if (selectedFile.renameTo(newFile)) {
                        if (selectedFile.equals(evilEr.diagramPanel.diagram.getCurrentFile()))
                            evilEr.diagramPanel.diagram.setCurrentFileQuietly(newFile);
                        refresh();
                        files.setSelectedValue(newFile, true);
                    }
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
