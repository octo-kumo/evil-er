package utils.callbacks;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/*
 *  Add an Action to a JList that can be invoked either by using
 *  the keyboard or a mouse.
 *
 *  By default, the Enter will be used to invoke the Action
 *  from the keyboard, although you can specify and KeyStroke you wish.
 *
 *  A double click with the mouse will invoke the same Action.
 *
 *  The Action can be reset at any time.
 */
public class ListAction {
    private static final KeyStroke ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

    public static void apply(JList<?> list, Action action) {
        apply(list, action, ENTER);
    }

    public static void apply(JList<?> list, Action action, KeyStroke keyStroke) {
        InputMap im = list.getInputMap();
        im.put(keyStroke, keyStroke);

        list.getActionMap().put(keyStroke, action);
        list.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Action action = list.getActionMap().get(keyStroke);

                    if (action != null) {
                        ActionEvent event = new ActionEvent(list, ActionEvent.ACTION_PERFORMED, "");
                        action.actionPerformed(event);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }
}