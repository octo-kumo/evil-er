package utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class IconPreprocess {
    public static void main(String... args) throws IOException, URISyntaxException {
        BufferedImage image = ImageIO.read(Objects.requireNonNull(IconPreprocess.class.getResourceAsStream("/images/icons/icon.png")));
        ImageIO.write(pad(image, 16, 16), "PNG", new File("./res/icon/iconx16.png"));
        ImageIO.write(pad(image, 32, 32), "PNG", new File("./res/icon/iconx32.png"));
        ImageIO.write(pad(image, 64, 64), "PNG", new File("./res/icon/iconx64.png"));
        ImageIO.write(pad(image, 128, 128), "PNG", new File("./res/icon/iconx128.png"));
        ImageIO.write(pad(image, 256, 256), "PNG", new File("./res/icon/iconx256.png"));
    }

    static BufferedImage pad(BufferedImage image, double width, double height) {
        double ratioW = image.getWidth() / width;
        double ratioH = image.getHeight() / height;
        double newWidth = width, newHeight = height;
        int fitW = 0, fitH = 0;
        BufferedImage resultImage;
        Image resize;

        //padding width
        if (ratioW < ratioH) {
            newWidth = image.getWidth() / ratioH;
            newHeight = image.getHeight() / ratioH;
            fitW = (int) ((width - newWidth) / 2.0);

        }//padding height
        else if (ratioH < ratioW) {
            newWidth = image.getWidth() / ratioW;
            newHeight = image.getHeight() / ratioW;
            fitH = (int) ((height - newHeight) / 2.0);
        }

        resize = image.getScaledInstance((int) newWidth, (int) newHeight, Image.SCALE_SMOOTH);
        resultImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = resultImage.getGraphics();
        g.drawImage(resize, fitW, fitH, null);
        g.dispose();
        return resultImage;
    }
}
