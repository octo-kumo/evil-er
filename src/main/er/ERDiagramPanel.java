package main.er;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.Theme;
import com.github.weisj.darklaf.theme.event.ThemeChangeEvent;
import com.github.weisj.darklaf.theme.event.ThemeChangeListener;
import com.github.weisj.darklaf.theme.info.ColorToneRule;
import model.Vector;
import model.er.Entity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class ERDiagramPanel extends JPanel implements ComponentListener {
    public final ERDiagram diagram;
    private final JTextField input;
    private Entity inputTarget = null;

    public ERDiagramPanel() {
        setLayout(null);
        addComponentListener(this);
        setPreferredSize(new Dimension(1280, 720));

        add(input = new JTextField() {{
            setVisible(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
            setHorizontalAlignment(CENTER);
            addActionListener(e -> requestNameEdit(null));
        }});
        add(diagram = new ERDiagram(this));

        diagram.darkMode.set(LafManager.getTheme().getColorToneRule() == ColorToneRule.DARK);
        LafManager.addThemeChangeListener(new ThemeChangeListener() {
            public void themeChanged(ThemeChangeEvent themeChangeEvent) {
                diagram.darkMode.set(Theme.isDark(themeChangeEvent.getNewTheme()));
                ERMenu.saveThemeToPreference();
            }

            public void themeInstalled(ThemeChangeEvent themeChangeEvent) {
            }
        });
    }

    public void requestNameEdit(Entity entity) {
        if (inputTarget != null) inputTarget.setName(input.getText());

        if ((inputTarget = entity) == null) {
            input.setVisible(false);
            diagram.acceptingKeys = true;
        } else {
            input.setVisible(true);
            input.setText(entity.getName());
            input.setForeground(diagram.foreground());
            input.setFont(diagram.getFont().deriveFont((float) (diagram.getFont().getSize() * diagram.scale)));
            entity.setName("");
            Vector pos = diagram.project(entity.minus(Entity.WIDTH / 2, Entity.HEIGHT / 4));
            input.setBounds((int) pos.getX(), (int) pos.getY(), (int) (Entity.WIDTH * diagram.scale), (int) (Entity.HEIGHT * diagram.scale / 2));
            input.grabFocus();
            input.selectAll();
            diagram.acceptingKeys = false;
        }
        repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        diagram.setSize(getWidth(), getHeight());
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        requestNameEdit(null);
    }
}
