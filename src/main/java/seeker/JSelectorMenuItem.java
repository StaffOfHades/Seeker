package seeker;

import java.awt.event.MouseEvent;

import javax.swing.JCheckBoxMenuItem;

class JSelectorMenuItem extends JCheckBoxMenuItem {

   static final long serialVersionUID = 73L;

   public JSelectorMenuItem() { }

   public JSelectorMenuItem(String text) {
       super(text);
   }

   @Override
   protected void processMouseEvent(MouseEvent e) {
      if( e.getID() == MouseEvent.MOUSE_RELEASED && contains( e.getPoint() ) ) {

         doClick();
         setArmed(true);
      } else
         super.processMouseEvent( e );
   }
}