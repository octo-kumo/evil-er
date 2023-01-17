package fonts;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class Fonts {
    public static void loadFonts() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Fonts.class.getResourceAsStream("/fonts/zh-cn.ttf"))));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Fonts.class.getResourceAsStream("/fonts/roboto.ttf"))));
        } catch (IOException | FontFormatException e) {
            //Handle exception
        }
    }
}
