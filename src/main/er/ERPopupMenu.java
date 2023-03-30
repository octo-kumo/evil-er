package main.er;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class ERPopupMenu extends JPopupMenu {
    private final JSeparator sep;
    public final ERMenu.ERAddMenu add;
    public final ERMenu.EREditMenu edit;
    public final ERMenu.ERViewMenu view;

    public ERPopupMenu(ERDiagram diagram) {
        add(add = new ERMenu.ERAddMenu(diagram));
        add(view = new ERMenu.ERViewMenu(diagram));
        add(sep = new JSeparator());
        add(edit = new ERMenu.EREditMenu(diagram));
        addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                sep.setVisible(diagram.target.nonNull());
                edit.setVisible(diagram.target.nonNull());
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });
    }
}
