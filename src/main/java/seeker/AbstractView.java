package seeker;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.MenuElement;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import manager.Connect;
import manager.Constants;

public abstract class AbstractView extends JFrame implements Constants {

   // Constants
   private final static String TYPEFACE = "Tahoma";
   private final static int TEXT_TITLE = 36;
   private final static int TEXT_HEADER = 18;
   private final static int TEXT_BODY = 14;
   static final long serialVersionUID = 25L;

   // View Variables
   protected ActionListener listener;
   protected JCheckBoxMenuItem toggleSearch;
   protected JComboBox<String> querySelector;
   protected JMenu compare;
   protected JTextArea resultArea;
   protected JTextField queryField;

   private JMenu history;
   private JMenuItem clearHistory;
   private JMenuItem noHistory;
   
   // Inner Variables
   protected final Connect connect;
   protected final LinkedList<Boolean> feedbackHistory;
   protected LinkedList<String> queryHistory;
   protected LinkedList<String> idHistory;
   protected LinkedList<List<Similar>> similarsHistory;
   protected LinkedList<List<Integer>> relevantsHistory;
   protected LinkedList<List<Double>> precisionHistory;
   protected LinkedList<List<Double>> recallHistory;

   AbstractView() {

      connect = Connect.getInstance();
      feedbackHistory = new LinkedList<>();
      queryHistory = new LinkedList<>();
      idHistory = new LinkedList<>();
      similarsHistory = new LinkedList<List<Similar>>();
      relevantsHistory = new LinkedList<List<Integer>>();
      precisionHistory = new LinkedList<List<Double>>();
      recallHistory = new LinkedList<List<Double>>();
      initComponents();
   }

   protected abstract void exportToCsv();
   protected abstract void exportRelevantToCsv();
   protected abstract void graphPrecisionAndRecall();
   protected abstract void graphRelevantPrecisionAndRecall();
   protected abstract void graphFMeasure( double grapheme );
   protected abstract void graphRelevantFMeasure( double grapheme );
   protected abstract void showResults( String id );
   protected abstract void buscarTermino() throws Exception; 

   private void initComponents() {
      
      final GroupLayout layout = new GroupLayout( getContentPane() );
      final JButton searchTerm = new JButton();
      toggleSearch = new JCheckBoxMenuItem( "Use Relevance Feedback" );
      querySelector = new JComboBox<>( connect.getQueries() );
      final JLabel resultLabel = new JLabel();
      final JLabel titleLabel = new JLabel();
      compare = new JMenu( "Compare" );
      final JMenu edit = new JMenu( "Edit" );
      final JMenu file = new JMenu( "File" );
      final JMenu graph = new JMenu( "Graph" );
      history = new JMenu( "History" );
      final JMenu relevant = new JMenu( "Relevant" );
      final JMenuBar menuBar = new JMenuBar();
      clearHistory = new JMenuItem( "Clear History" );
      final JMenuItem export = new JMenuItem( "Export to CSV" );
      final JMenuItem exportR = new JMenuItem( "Export (Relevant) to CSV" );
      final JMenuItem graphRP = new JMenuItem( "Recall & Precision" );
      final JMenuItem graphRelevantRP = new JMenuItem( "Recall & Precision" );
      final JMenuItem graphF = new JMenuItem( "F-Measure" );
      final JMenuItem graphFR = new JMenuItem( "F-Measure" );
      noHistory = new JMenuItem( "No Search History" );
      final JMenuItem[] items = {toggleSearch, exportR, graphF, graphRP, relevant};
      resultArea = new JTextArea();
      queryField = new JTextField();

      final JScrollPane scroll = new JScrollPane( resultArea );

      listener = new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {

            if( !(e.getSource() instanceof JMenuItem) )
               return;

            final JMenuItem item = (JMenuItem) e.getSource();
            if( item == export )
               exportToCsv();
            else if( item == exportR )
               exportRelevantToCsv();
            else if( item == graphRP )
               graphPrecisionAndRecall();
            else if( item == graphRelevantRP )
               graphRelevantPrecisionAndRecall();
            else if( item == graphF )
               graphFMeasure( selectGrapheme() );
            else if( item == graphFR )
               graphRelevantFMeasure( selectGrapheme() );
            else if( item == clearHistory ) {

               disableMenu( items );
               clearHistory();
            } else if(
               item.getParent() instanceof JPopupMenu &&
               ( (JPopupMenu) item.getParent() ).getInvoker() == history ) {

               showResults( item.getText() );
            }
         }
      };
      
      final Font title = new Font(
         TYPEFACE,
         0,
         TEXT_TITLE
      );

      final Font header = new Font(
         TYPEFACE,
         0,
         TEXT_HEADER
      );

      final Font body = new Font(
         TYPEFACE,
         0,
         TEXT_BODY
      );

      noHistory.setEnabled( false );
      toggleSearch.setEnabled( false );
      clearHistory.setEnabled( false );

      menuBar.add( file );
      menuBar.add( edit );
      menuBar.add( history );
      menuBar.add( graph );
      edit.add( toggleSearch );
      file.add( export );
      file.add( exportR );
      graph.add( graphF );
      graph.add( graphRP );
      graph.add( relevant );
      history.add( compare );
      history.addSeparator();
      history.add( noHistory );
      history.addSeparator();
      history.add( clearHistory );
      relevant.add( graphFR );
      relevant.add( graphRelevantRP );
      this.setJMenuBar( menuBar );
      
      scroll.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

      setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      setTitle( TITLE );

      export.addActionListener( listener );
      exportR.addActionListener( listener );
      graphRP.addActionListener( listener );
      graphRelevantRP.addActionListener( listener );
      graphF.addActionListener( listener );
      graphFR.addActionListener( listener );
      noHistory.addActionListener( listener );

      titleLabel.setFont( title );
      titleLabel.setText( "Buscador v.2" );

      queryField.setFont( header );

      searchTerm.setFont( body );
      searchTerm.setText( "Buscar" );
      searchTerm.addActionListener(
         new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent event ) {

               try {
                  buscarTermino();
               } catch ( Exception e ) {
                  e.printStackTrace();
               }
            }
         }
      );

      resultArea.setEditable(false);
      resultArea.setFont( body ); // NOI18N

      resultLabel.setFont( body ); // NOI18N
      resultLabel.setText( "Documentos Relevantes" );

      querySelector.addActionListener(
         new ActionListener() {
         
            @Override
            public void actionPerformed(ActionEvent e) {

               if( querySelector.getSelectedIndex() > 0 ) {

                  queryField.setText("");
                  setMenuEnabled(items, true);
               }
            }
         }
      );
            
      queryField.getDocument().addDocumentListener(
         new DocumentListener() {
         
            @Override
            public void removeUpdate(DocumentEvent e) {

               if( queryField.getText().trim().length() >  0 )
                  disableMenu( items );
            }
         
            @Override
            public void insertUpdate(DocumentEvent e) {

               if( queryField.getText().trim().length() >  0 )
                  disableMenu( items );
            }
         
            @Override
            public void changedUpdate(DocumentEvent e) {
               
               if( queryField.getText().trim().length() >  0 )
                  disableMenu( items );
            }
         }
      );
      
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
               GroupLayout.Alignment.TRAILING,
               layout.createSequentialGroup()
                  .addGap(20, 20, 20)
                  .addGroup(
                     layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                           GroupLayout.Alignment.TRAILING,
                           layout.createSequentialGroup()
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 81, GroupLayout.PREFERRED_SIZE)
                              .addComponent(titleLabel)
                              .addGap(72, 72, 72)
                        ).addComponent(querySelector, GroupLayout.PREFERRED_SIZE, 359, GroupLayout.PREFERRED_SIZE)
                        .addComponent(queryField, GroupLayout.PREFERRED_SIZE, 359, GroupLayout.PREFERRED_SIZE)
                        .addGroup(
                           layout.createSequentialGroup()
                              .addGap(145, 145, 145)
                              .addComponent(searchTerm)
                        )
                  ).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                  .addGroup(
                     layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(resultLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(scroll, GroupLayout.PREFERRED_SIZE, 141, GroupLayout.PREFERRED_SIZE)
                  ).addGap(70, 70, 70)
            )
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
               layout.createSequentialGroup()
                  .addGap(30, 30, 30)
                  .addGroup(
                     layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(
                           layout.createSequentialGroup()
                              .addComponent(titleLabel)
                              .addGap(38, 38, 38)
                              .addComponent(querySelector, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                              .addComponent(queryField, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                              .addComponent(searchTerm)
                              .addGap(25, 25, 25)
                        ).addGroup(
                           layout.createSequentialGroup()
                              .addComponent(resultLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(scroll, GroupLayout.PREFERRED_SIZE, 186, GroupLayout.PREFERRED_SIZE)
                        )
                  ).addContainerGap(29, Short.MAX_VALUE)
            )
      );
      
      titleLabel.getAccessibleContext().setAccessibleName("titleLabel");

      pack();
   }

   private double selectGrapheme() {

      double grapheme = 0;
      while( grapheme <= 0 ) {

         String answer = (String) JOptionPane.showInputDialog(
            this,
            "Input your given grapheme: ",
            "Select F-Measure",
            JOptionPane.QUESTION_MESSAGE
         );
         try {
            grapheme = Double.parseDouble( answer );
            if( grapheme <= 0 ) {
               System.err.println( "Grapheme must be a positive number" );
            }
         } catch( NumberFormatException e) {
            System.err.println( "Input was not a number" );
         }         
      }
      return grapheme;
   }

   private void setMenuEnabled( final JMenuItem[] items, boolean enabled ) {
      for( JMenuItem item : items )
         item.setEnabled( enabled );
   }

   private void disableMenu( final JMenuItem[] items ) {

      querySelector.setSelectedIndex( 0 );
      toggleSearch.setSelected( false );
      setMenuEnabled( items, false );
   }

   private void clearHistory() {

      feedbackHistory.clear();
      idHistory.clear();
      queryHistory.clear();
      similarsHistory.clear();
      relevantsHistory.clear();
      precisionHistory.clear();
      recallHistory.clear();
      updateHistory();
   }
   
   protected void updateHistory() {

      for( MenuElement element : history.getSubElements() )
         if( element instanceof JMenuItem )
            ( (JMenuItem) element).removeActionListener( listener );

      clearHistory.addActionListener( listener );
      history.removeAll();
      compare.removeAll();
      history.add( compare );
      history.addSeparator();
      if( similarsHistory.size() > 0 ) {

         clearHistory.setEnabled( true );
         for(int i = 0; i < idHistory.size() && i < feedbackHistory.size(); i++ ) {
            
            final String text = idHistory.get( i );
            final JMenuItem item = new JMenuItem( text );
            item.addActionListener( listener );
            history.add( item );
            compare.add( new JSelectorMenuItem( text ) );
         }
      } else {
         
         clearHistory.setEnabled( false );
         history.add( noHistory );
      }
      history.addSeparator();
      history.add( clearHistory );
   } 
}

/*

CREATE TEMPORARY TABLE IF NOT EXISTS `n1` (`iddoc` int(8) unsigned NOT NULL,PRIMARY KEY (`iddoc`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TEMPORARY TABLE IF NOT EXISTS `n2` (`iddoc` int(8) unsigned NOT NULL,PRIMARY KEY (`iddoc`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into `n1`(`iddoc`) values
(3960),
(4136),
(4165),
(4341),
(5486),
(5639),
(5775),
(6967),
(6990),
(8891);
insert into `n2`(`iddoc`) values
(850),
(943),
(1049),
(1167),
(1809),
(1909),
(2283),
(2515),
(2733),
(2830);

select * from `n1`;
select * from `n2`;
select count(`n1`.`iddoc`) from `n1`;
select `contains`.`term`, sum(`contains`.`tf`) from `made`, `contains`, `n1` where `contains`.`term` = `made`.`term` and `contains`.`iddoc` = `n1`.`iddoc` and `made`.`idquery` = 2 group by `made`.`term`;
select count(`n2`.`iddoc`) from `n2`;
select `contains`.`term`, sum(`contains`.`tf`) from `made`, `contains`, `n2` where `contains`.`term` = `made`.`term` and `contains`.`iddoc` = `n2`.`iddoc` and `made`.`idquery` = 2;

select `made`.`idquery`, `made`.`term`, 1 * `made`.`tf` + 0.8 / (select count(`n1`.`iddoc`) from `n1`) * coalesce((select sum(`contains`.`tf`) from `contains`, `n1` where `contains`.`term` = `made`.`term` and `contains`.`iddoc` = `n1`.`iddoc`), 0) - 0.1 / (select count(`n2`.`iddoc`) from `n2`) * coalesce((select sum(`contains`.`tf`) from `contains`, `n2` where `contains`.`term` = `made`.`term` and `contains`.`iddoc` = `n2`.`iddoc`), 0) as `tf1` from `made` where `idquery` = 2;

drop table if exists `n1`;
drop table if exists `n2`;

*/