package main.ui;

import main.EvilEr;
import main.renderer.ChangeListener;
import main.renderer.Consumer;
import model.entities.Attribute;
import model.entities.Entity;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.function.Function;

public class InfoPanel extends JPanel implements ChangeListener<Entity> {
    private final JPanel entityControls;
    private EvilEr evilEr;

    public InfoPanel(EvilEr evilEr) {
        this.evilEr = evilEr;
        setPreferredSize(new Dimension(200, 0));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new TitledBorder("Info Panel"));
        add(entityControls = new JPanel() {{
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            setBorder(new TitledBorder("Entity Control"));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }});
        add(new JLabel("Outline"));
        add(new JLabel("Dotted"));
        evilEr.diagramPanel.diagram.listeners.add(this);
    }

    public JPanel entityWindow(Entity entity) {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Entity"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new PlaceholderTextField(entity.name) {{
            setPlaceholder("Name");
            addTextListener(this, s -> entity.name = s, Function.identity());
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        }});
        panel.add(new PlaceholderTextField(String.valueOf(entity.x)) {{
            setPlaceholder("X");
            addTextListener(this, s -> entity.x = s, Double::parseDouble);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        }});
        panel.add(new PlaceholderTextField(String.valueOf(entity.y)) {{
            setPlaceholder("Y");
            addTextListener(this, s -> entity.x = s, Double::parseDouble);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        }});

        panel.add(new JCheckBox("Weak") {{
            addBooleanListener(this, "weak", entity);
        }});
        return panel;
    }

    public JPanel attributeWindow(Attribute attribute) {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Attribute"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JCheckBox("Key") {{
            addBooleanListener(this, "key", attribute);
        }});
        panel.add(new JCheckBox("Derived") {{
            addBooleanListener(this, "derived", attribute);
        }});
        return panel;
    }

    @Override
    public void onChange(Entity newEntity) {
        entityControls.removeAll();
        entityControls.revalidate();
        if (newEntity == null) {
            entityControls.repaint();
            return;
        }
        entityControls.add(entityWindow(newEntity));
        if (newEntity instanceof Attribute) entityControls.add(attributeWindow((Attribute) newEntity));
        entityControls.repaint();
    }


    public <T> void addTextListener(JTextField field, Consumer<T> consumer, Function<String, T> transformer) {
        field.addActionListener(e -> {
            try {
                consumer.consume(transformer.apply(field.getText()));
                evilEr.diagramPanel.diagram.repaint();
            } catch (NumberFormatException ignored) {
            }
        });
    }

    public void addBooleanListener(JToggleButton field, String name, Entity entity) {
        field.addActionListener(e -> {
            try {
                Field prop = entity.getClass().getDeclaredField(name);
                prop.setAccessible(true);
                prop.set(entity, field.isSelected());
                evilEr.diagramPanel.diagram.repaint();
            } catch (NumberFormatException | NoSuchFieldException | IllegalAccessException ignored) {
            }
        });
    }
}
