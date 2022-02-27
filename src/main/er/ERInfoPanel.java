package main.er;

import com.github.weisj.darklaf.icons.IconLoader;
import images.ImageRoot;
import main.EvilEr;
import model.er.Attribute;
import model.er.Entity;
import model.er.Relationship;
import model.er.Specialization;
import model.rs.Column;
import utils.callbacks.ChangeListener;
import utils.callbacks.Consumer;
import utils.callbacks.Getter;
import utils.components.ButtonColumn;
import utils.components.PlaceholderTextField;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ERInfoPanel extends JPanel implements ChangeListener<Entity> {
    private final JPanel entityControls;
    private final EvilEr evilEr;
    public static final Icon CLOSE_ICON;

    static {
        CLOSE_ICON = IconLoader.get(ImageRoot.class).getIcon("icons/close_black_24dp.svg");
    }

    public ERInfoPanel(EvilEr evilEr) {
        this.evilEr = evilEr;
        setPreferredSize(new Dimension(333, 0));
        setMaximumSize(new Dimension(456, 0));
        setMinimumSize(new Dimension(256, 0));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new TitledBorder("Info Panel"));
        add(entityControls = new JPanel() {{
            setBorder(new TitledBorder("Entity Control"));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }});
        evilEr.diagramPanel.diagram.target.addListener(this);
    }

    public JPanel entityWindow(Entity entity) {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Entity"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new PlaceholderTextField(entity.getName()) {{
            setPlaceholder("Name");
            addTextListener(this, entity::setName, Function.identity());
        }});
        panel.add(new PlaceholderTextField(String.valueOf(entity.getX())) {{
            setPlaceholder("X");
            addTextListener(this, entity::setX, Double::parseDouble);
        }});
        panel.add(new PlaceholderTextField(String.valueOf(entity.getY())) {{
            setPlaceholder("Y");
            addTextListener(this, entity::setY, Double::parseDouble);
        }});
        panel.add(new JCheckBox("Weak") {{
            setAlignmentX(LEFT_ALIGNMENT);
            addBooleanListener(this, entity::setWeak, entity::isWeak);
        }});
        return panel;
    }

    public JPanel attributeWindow(Attribute attribute) {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Attribute"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JCheckBox("Key") {{
            addBooleanListener(this, attribute::setKey, attribute::isKey);
        }});
        panel.add(new JCheckBox("Derived") {{
            addBooleanListener(this, attribute::setDerived, attribute::isDerived);
        }});
        panel.add(new JComboBox<Column.DataType>(Column.DataType.values()) {{
            setSelectedItem(attribute.getDataType());
            addActionListener(evt -> attribute.setDataType((Column.DataType) getSelectedItem()));
        }});
        panel.add(new PlaceholderTextField() {{
            setPlaceholder("Param");
            setText(attribute.getDataParam());
            addActionListener(evt -> attribute.setDataParam(getText()));
        }});
        panel.setAlignmentX(LEFT_ALIGNMENT);
        return panel;
    }

    public JPanel relationWindow(Relationship relationship) {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Relation"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        if (relationship instanceof Specialization)
            panel.add(new JCheckBox("Disjoint", ((Specialization) relationship).isDisjoint()) {{
                setAlignmentX(Component.LEFT_ALIGNMENT);
                addActionListener(e -> {
                    ((Specialization) relationship).setDisjoint(isSelected());
                    evilEr.diagramPanel.diagram.repaint();
                });
            }});

        Object[][] data = IntStream.range(0, relationship.nodes.size()).mapToObj(i -> {
            Entity t = relationship.nodes.get(i);
            Relationship.RelationshipSpec spec = relationship.specs.get(i);
            return new Object[]{t.getName(), spec.amm, spec.total, spec.role, CLOSE_ICON};
        }).toArray(Object[][]::new);

        DefaultTableModel tableModel;
        /* Table */
        panel.add(new JScrollPane(new JTable(tableModel = new DefaultTableModel(data, new String[]{"Name", "Amount", "Total", "Role", ""}) {
            public void setValueAt(Object value, int row, int column) {
                super.setValueAt(value, row, column);
                if (column == 0) relationship.nodes.get(row).setName((String) value);
                else if (column == 1) relationship.specs.get(row).amm = (String) value;
                else if (column == 2) relationship.specs.get(row).total = (boolean) value;
                else if (column == 3) relationship.specs.get(row).role = (String) value;
                evilEr.diagramPanel.diagram.repaint();
            }
        }) {
            {
                new ButtonColumn(this, new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        JTable table = (JTable) e.getSource();
                        int row = Integer.parseInt(e.getActionCommand());
                        if (relationship.remove(row)) {
                            ((DefaultTableModel) table.getModel()).removeRow(row);
                            evilEr.diagramPanel.diagram.repaint();
                        }
                    }
                }, 4);
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 2) return Boolean.class;
                else return String.class;
            }
        }, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        /* Add new entity or link to entity */
        panel.add(new JPanel() {{
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            JTextField name, amm;
            JCheckBox total;
            add(name = new PlaceholderTextField() {{
                setPlaceholder("Name");
            }});
            add(amm = new PlaceholderTextField() {{
                setPlaceholder("N");
            }});
            add(total = new JCheckBox());
            ActionListener action = evt -> {
                Entity entity = evilEr.diagramPanel.diagram.entities.parallelStream()
                        .filter(e -> e.getName().equals(name.getText())).findAny().orElseGet(() -> {
                            Entity n = new Entity().setName(name.getText());
                            evilEr.diagramPanel.diagram.entities.add(n);
                            double angle = Math.random() * Math.PI * 2;
                            return (Entity) n.set(relationship.add(100 * Math.cos(angle), 100 * Math.sin(angle)));
                        });
                relationship.addNode(entity, new Relationship.RelationshipSpec(amm.getText(), total.isSelected()));

                tableModel.addRow(new Object[]{entity.getName(), amm.getText(), total.isSelected(), CLOSE_ICON});
                evilEr.diagramPanel.diagram.repaint();
                name.setText("");
                amm.setText("");
                total.setSelected(false);
            };
            name.addActionListener(action);
            add(new JButton("Add") {{
                addActionListener(action);
            }});
        }});
        return panel;
    }

    public JPanel attributesWindow(Entity parent) {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Attributes"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Object[][] data = parent.attributes.parallelStream().map(a -> new Object[]{a.getName(), a.isDerived(), a.isKey(), CLOSE_ICON}).toArray(Object[][]::new);

        DefaultTableModel tableModel;
        panel.add(new JScrollPane(new JTable(tableModel = new DefaultTableModel(data, new String[]{"Name", "Derived", "Key", ""}) {
            public void setValueAt(Object value, int row, int column) {
                super.setValueAt(value, row, column);
                if (column == 0) parent.attributes.get(row).setName((String) value);
                else if (column == 1) parent.attributes.get(row).setDerived((boolean) value);
                else if (column == 2) parent.attributes.get(row).setKey((boolean) value);
                evilEr.diagramPanel.diagram.repaint();
            }
        }) {
            {
                new ButtonColumn(this, new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        JTable table = (JTable) e.getSource();
                        int row = Integer.parseInt(e.getActionCommand());
                        parent.attributes.remove(row);
                        ((DefaultTableModel) table.getModel()).removeRow(row);
                        evilEr.diagramPanel.diagram.repaint();
                    }
                }, 3);
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 1 || column == 2) return Boolean.class;
                else return String.class;
            }
        }, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));

        panel.add(new JPanel() {{
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            JTextField name;
            JCheckBox derived, key;
            add(name = new PlaceholderTextField() {{
                setPlaceholder("Name");
            }});
            add(derived = new JCheckBox());
            add(key = new JCheckBox());
            ActionListener action = evt -> {
                double angle = Math.random() * Math.PI * 2;
                parent.addAttribute((Attribute) new Attribute()
                        .setDerived(derived.isSelected())
                        .setKey(key.isSelected()).setName(name.getText())
                        .set(100 * Math.cos(angle), 100 * Math.sin(angle)));
                tableModel.addRow(new Object[]{name.getText(), derived.isSelected(), key.isSelected(), CLOSE_ICON});
                evilEr.diagramPanel.diagram.repaint();
                name.setText("");
                derived.setSelected(false);
                key.setSelected(false);
            };
            name.addActionListener(action);
            add(new JButton("Add") {{
                addActionListener(action);
            }});
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
        if (newEntity instanceof Relationship) entityControls.add(relationWindow((Relationship) newEntity));
        entityControls.add(attributesWindow(newEntity));
        entityControls.revalidate();
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

    public void addBooleanListener(JToggleButton field, Consumer<Boolean> consumer, Getter<Boolean> getter) {
        field.setSelected(getter.get());
        field.addActionListener(e -> {
            try {
                consumer.consume(field.isSelected());
                evilEr.diagramPanel.diagram.repaint();
            } catch (NumberFormatException ignored) {
            }
        });
    }
}
