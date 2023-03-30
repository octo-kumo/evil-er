package main.er;

import javax.swing.*;

public class ERPopupMenu extends JPopupMenu {
    public ERPopupMenu(ERDiagram diagram) {
        add(new ERMenu.ERAddMenu(diagram));
        add(new ERMenu.EREditMenu(diagram));
        add(new ERMenu.ERViewMenu(diagram));
    }
}
