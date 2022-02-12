package main.rs;

import model.rs.Column;
import model.rs.Table;
import utils.callbacks.ChangeListener;
import utils.callbacks.Consumer;
import utils.components.ButtonColumn;
import utils.components.PlaceholderTextField;
import utils.models.Tuple;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.function.Function;

import static main.er.ERInfoPanel.CLOSE_ICON;

public class RSInfoPanel extends JPanel implements ChangeListener<Table> {

    private final JPanel tableControl;
    private final RSDiagram diagram;

    public RSInfoPanel(RSDiagram diagram) {
        this.diagram = diagram;
        setPreferredSize(new Dimension(333, 0));
        setMaximumSize(new Dimension(456, 0));
        setMinimumSize(new Dimension(256, 0));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new TitledBorder("Info Panel"));
        add(tableControl = new JPanel() {{
            setBorder(new TitledBorder("Table Control"));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }});
        diagram.target.addListener(this);
    }

    public JPanel tableWindow(Table table) {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Table"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new PlaceholderTextField(table.getName()) {{
            setPlaceholder("Name");
            addTextListener(this, table::setName, Function.identity());
        }});
        panel.add(new PlaceholderTextField(String.valueOf(table.getX())) {{
            setPlaceholder("X");
            addTextListener(this, table::setX, Double::parseDouble);
        }});
        panel.add(new PlaceholderTextField(String.valueOf(table.getY())) {{
            setPlaceholder("Y");
            addTextListener(this, table::setY, Double::parseDouble);
        }});
        panel.add(new JButton("Revalidate") {{
            addActionListener(e -> {
                table.revalidate();
                diagram.repaint();
            });
        }});
        return panel;
    }

    public JPanel attributesWindow(Table table) {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Attributes"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        ArrayList<Column> values = new ArrayList<>(table.attributeMap.values());
        Object[][] data = values.parallelStream()
                .map(a -> new Object[]{a.getName(), a.isKey(), CLOSE_ICON}).toArray(Object[][]::new);

        DefaultTableModel tableModel;
        panel.add(new JScrollPane(new JTable(tableModel = new DefaultTableModel(data, new String[]{"Name", "Key", ""}) {
            public void setValueAt(Object value, int row, int column) {
                super.setValueAt(value, row, column);
                if (column == 0) values.get(row).setName((String) value);
                else if (column == 1) values.get(row).setKey((boolean) value);
                diagram.repaint();
            }
        }) {
            {
                new ButtonColumn(this, new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        JTable source = (JTable) e.getSource();
                        int row = Integer.parseInt(e.getActionCommand());
                        if (table.remove(values.get(row))) {
                            values.remove(row);
                            ((DefaultTableModel) source.getModel()).removeRow(row);
                        }
                        diagram.repaint();
                    }
                }, 2);
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 1) return Boolean.class;
                else return String.class;
            }
        }, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));

        panel.add(new JPanel() {{
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            JTextField name;
            JCheckBox key;
            add(name = new PlaceholderTextField() {{
                setPlaceholder("Name");
            }});
            add(key = new JCheckBox());
            ActionListener action = evt -> {
                Column column = new Column(name.getText(), key.isSelected());
                if (table.add(column)) {
                    values.add(column);
                    tableModel.addRow(new Object[]{name.getText(), key.isSelected(), CLOSE_ICON});
                }
                diagram.repaint();
                name.setText("");
                key.setSelected(false);
            };
            name.addActionListener(action);
            add(new JButton("Add") {{
                addActionListener(action);
            }});
        }});
        return panel;
    }

    public JPanel foreignWindow(Table table) {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Foreign Keys"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Object[][] data = table.foreign.parallelStream()
                .map(a -> new Object[]{a.c.getName(), a.a, a.b, CLOSE_ICON}).toArray(Object[][]::new);

        DefaultTableModel tableModel;
        panel.add(new JScrollPane(new JTable(tableModel = new DefaultTableModel(data, new String[]{"Name", "Required", "Role", ""}) {
            public void setValueAt(Object value, int row, int column) {
                super.setValueAt(value, row, column);
                if (column == 0) table.foreign.get(row).c.setName((String) value);
                else if (column == 1) table.foreign.get(row).a = (Boolean) value;
                else if (column == 2) table.foreign.get(row).b = (String) value;
                diagram.repaint();
            }
        }) {
            {
                new ButtonColumn(this, new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        JTable source = (JTable) e.getSource();
                        int row = Integer.parseInt(e.getActionCommand());
                        table.foreign.remove(row);
                        ((DefaultTableModel) source.getModel()).removeRow(row);
                        diagram.repaint();
                    }
                }, 3);
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 1) return Boolean.class;
                else return String.class;
            }
        }, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));

        panel.add(new JPanel() {{
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            JTextField name, role;
            JCheckBox needed;
            add(name = new PlaceholderTextField() {{
                setPlaceholder("Name");
            }});
            add(needed = new JCheckBox());
            add(role = new PlaceholderTextField() {{
                setPlaceholder("Role");
            }});
            ActionListener action = evt -> {
                diagram.tables.stream().filter(t -> t.getName().equals(name.getText())).findAny().ifPresent(other -> {
                    table.foreign.add(new Tuple<>(needed.isSelected(), role.getText(), other));
                    tableModel.addRow(new Object[]{other.getName(), needed.isSelected(), role.getText(), CLOSE_ICON});
                    diagram.repaint();
                    name.setText("");
                    role.setText("");
                    needed.setSelected(false);
                });
            };
            name.addActionListener(action);
            add(new JButton("Add") {{
                addActionListener(action);
            }});
        }});

        return panel;
    }

    @Override
    public void onChange(Table table) {
        tableControl.removeAll();
        tableControl.revalidate();
        if (table == null) {
            tableControl.repaint();
            return;
        }
        tableControl.add(tableWindow(table));
        tableControl.add(attributesWindow(table));
        tableControl.add(foreignWindow(table));
        tableControl.repaint();
    }

    public <T> void addTextListener(JTextField field, Consumer<T> consumer, Function<String, T> transformer) {
        field.addActionListener(e -> {
            try {
                consumer.consume(transformer.apply(field.getText()));
                diagram.repaint();
            } catch (NumberFormatException ignored) {
            }
        });
    }
}
