package utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class Chooser extends JFileChooser {
    public static final Chooser imageChooser = new Chooser() {

        {
            FileFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
            setDialogTitle("Select...");
            setFileFilter(imageFilter);
        }

        public File getFinal() {
            File file = getSelectedFile();
            if (getDialogType() == SAVE_DIALOG && !getFileFilter().accept(file) && file != null)
                file = new File(file.getAbsolutePath() + ".png");
            return file;
        }
    };

    public static final Chooser jsonChooser = new Chooser() {

        {
            FileFilter imageFilter = new FileNameExtensionFilter("Diagram File", "json", "dig");
            setDialogTitle("Select...");
            setFileFilter(imageFilter);
        }

        public File getFinal() {
            File file = getSelectedFile();
            if (getDialogType() == SAVE_DIALOG && !getFileFilter().accept(file) && file != null)
                file = new File(file.getAbsolutePath() + ".dig");
            return file;
        }
    };

    public static final Chooser folderChooser = new Chooser() {
        {
            setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
    };

    public File getFinal() {
        return getSelectedFile();
    }
}
