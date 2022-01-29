package main.ui.components;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NoneSelectedButtonGroup extends ButtonGroup {

    private final ActionListener actionListener;

    public NoneSelectedButtonGroup(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @Override
    public void setSelected(ButtonModel model, boolean selected) {
        if (selected) {
            super.setSelected(model, true);
        } else {
            clearSelection();
        }
        actionListener.actionPerformed(new ActionEvent(this, 0, selected ? model.getActionCommand() : null));
    }
}