package seeker;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;

import manager.*;

import processer.*;

import org.jfree.ui.RefineryUtilities;

public class View extends JFrame implements Constants {

   // Constants
   private final static String TYPEFACE = "Tahoma";
   private final static int TEXT_TITLE = 36;
   private final static int TEXT_HEADER = 18;
   private final static int TEXT_BODY = 14;
   static final long serialVersionUID = 25L;

   // View Variables
   private JCheckBoxMenuItem toggleSearch;
   private JComboBox<String> querySelector;
   private JTextArea resultArea;
   private JTextField queryField;
   
   // Inner Variables
   private final Connect connect;
   private List<Similar> similars;
   private List<Integer> relevants;
   private List<Double> precision;
   private List<Double> recall;

   public View() {

      connect = Connect.getInstance();
      similars = new ArrayList<>();
      relevants = new ArrayList<>();
      precision = new ArrayList<>();
      recall = new ArrayList<>();
      initComponents();
   }

   private void initComponents() {
      
      final JButton searchTerm = new JButton();
      toggleSearch = new JCheckBoxMenuItem( "Use Relevance Feedback" );
      querySelector = new JComboBox<>( connect.getQueries() );
      final JLabel resultLabel = new JLabel();
      final JLabel titleLabel = new JLabel();
      final JMenu file = new JMenu( "File" );
      final JMenu edit = new JMenu( "Edit" );
      final JMenu graph = new JMenu( "Graph" );
      final JMenu relevant = new JMenu( "Relevant" );
      final JMenuBar menuBar = new JMenuBar();
      final JMenuItem export = new JMenuItem( "Export to CSV" );
      final JMenuItem exportR = new JMenuItem( "Export (Relevant) to CSV" );
      final JMenuItem graphRP = new JMenuItem( "Recall & Precision" );
      final JMenuItem graphRelevantRP = new JMenuItem( "Recall & Precision" );
      final JMenuItem graphF = new JMenuItem( "F1 & F2" );
      final JMenuItem graphFR = new JMenuItem( "F1 & F2" );
      final JMenuItem[] items = {toggleSearch, exportR, graphF, graphRP, relevant};
      resultArea = new JTextArea();
      queryField = new JTextField();
      final JScrollPane scroll = new JScrollPane( resultArea );
      final GroupLayout layout = new GroupLayout( getContentPane() );

      final ActionListener listener = new ActionListener() {

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
               graphFMeasure( new int[] {1, 2} );
            else if( item == graphFR )
               graphRelevantFMeasure( new int[] {1, 2} );
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


      toggleSearch.setSelected( false );

      menuBar.add( file );
      menuBar.add( edit );
      menuBar.add( graph );
      file.add( export );
      file.add( exportR );
      edit.add( toggleSearch );
      graph.add( graphF );
      graph.add( graphRP );
      graph.add( relevant );
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
                  buscarTerminoActionPerformed( event );
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

   private void setMenuEnabled( final JMenuItem[] items, boolean enabled ) {
      for(JMenuItem item : items)
         item.setEnabled(enabled);
   }

   private void disableMenu( final JMenuItem[] items ) {

      querySelector.setSelectedIndex( 0 );
      toggleSearch.setSelected( false );
      setMenuEnabled(items, false);
   }

   // Search for term in collection, and return relevant documents in order.
   private void buscarTerminoActionPerformed( ActionEvent event ) throws Exception {

      // Retrieve query from textfield,
      final String query = queryField.getText();
      final DecimalFormat format = new DecimalFormat("#0.00");

      int idquery;
      // If its a user query, add it to db and get its id;
      // Otherwise, just find the idquery;
      if( query.trim().length() > 0 )
         idquery = connect.addQuery( query );
      else if ( querySelector.getSelectedIndex() > 0 ) {

         idquery = querySelector.getSelectedIndex();
         System.out.println( "Query: \"" + querySelector.getSelectedItem()  + "\"");
      } else {
         
         JOptionPane.showMessageDialog( this, "Please select a query or add your own" );
         return;
      }

      if( idquery == -1 )
         throw new Exception( "An idquery was uncessfuly found or created" );

      // Get similarity for a given idquery
      similars = toggleSearch.isSelected() ?
         connect.getSimilarityQ1( idquery ) :
         connect.getSimilarity( idquery );

      if( similars == null )
         throw new Exception( "An error occured when getting similarity list" );
      
      System.out.println(
         DIVIDER +
         "First 100 Similar Documents" +
         DIVIDER
      );

      resultArea.setText( "" );
      precision = new ArrayList<>();
      recall = new ArrayList<>();

      // Get relevant documents
      relevants = connect.getRelevants( idquery );

      // Iterate over the first 100 documents found
      String text = "";
      for( int i = 0; i < similars.size() && i < 100; i++ ) {

         final Similar similar = similars.get(i);
         System.out.println(
            "Documento #" +
            similar.getId() +
            ": " +
            similar.getSimilarity()
         );
         text += similar.getId();
         if( relevants.size() > 0 && relevants.contains( similar.getId() ) ) {
            text += "\tR";
         }
         text += "\n";
      }
      
      text = text.substring(0, text.length() - 1 );

      resultArea.setText( text );

      // Scroll back to top of text area
      resultArea.setCaretPosition( 0 );

      if( similars == null )
         throw new Exception( "An error occured when getting similarity list" );
      
      // Only try to find precision and recall if there are relevant documents;
      if( relevants.size() > 0 ) {
         
         int counter = 1;
         int found = 0;
         final int max = similars.size();
         final int correct = relevants.size();
         System.out.println(
            DIVIDER +
            "Precision & Recall" +
            DIVIDER
         );

         // Iterate over all relevant documents,
         for( Similar pair : similars ) {
            
            found += relevants.contains( pair.getId() ) ? 1 : 0;
            final double p = (double) found / counter; 
            final double r = (double) found / correct;
            precision.add( p );
            recall.add( r );

            // Print the first 100 calculated values.
            if( counter <= 100 ) {

               System.out.println(
                  "Documento #" +
                  pair.getId() +
                  ": Precision: " +
                  format.format( 100 * p ) +
                  "%; Recall: " +
                  format.format( 100 * r ) +
                  "%;"
               );
            }

            // Increment counter
            counter++;
         }
         System.out.println(
            "\nDocuments retrieved: " +
            counter +
            ", of which " +
            found +
            " where relevant"
         );
      }
   }

   private void graph( Map<String, List<Double>> map, String heading, String xLabel ) {

      String title = heading + " (Query #" + querySelector.getSelectedIndex();
      if( toggleSearch.isSelected() )
         title += " w/ Relevance Feedback";
      title += ")";
      final Graph<Double> graph = new Graph<>("Grafica", title, xLabel, map);
      graph.pack();
      RefineryUtilities.centerFrameOnScreen( graph );          
      graph.setVisible( true );
   }

   private void graphPrecisionAndRecall(
      List<Double> precision,
      List<Double> recall,
      String xLabel
   ) {
      if( recall.size() <= 0 || precision.size() <= 0) {

         JOptionPane.showMessageDialog( this, "A default query must be made" );
         return;
      }

      final Map<String, List<Double>> map = new HashMap<String, List<Double>>();
      map.put("Precision", precision);
      map.put("Recall", recall);
      graph( map, "Recall vs Precision", xLabel );
      
   }

   private void graphPrecisionAndRecall() {
      graphPrecisionAndRecall( precision, recall, "Documents (#)" );
   }

   private void graphRelevantPrecisionAndRecall() {

      if( recall.size() <= 0 || precision.size() <= 0) {

         JOptionPane.showMessageDialog( this, "A default query must be made" );
         return;
      }

      final List<Double> precisionR = new ArrayList<>();
      final List<Double> recallR = new ArrayList<>();
      for( int i = 0; i < similars.size() && i < precision.size() && i < recall.size(); i++ ) {

         final int id = similars.get( i ).getId();
         final double p = precision.get( i );
         final double r = recall.get( i );
         if( relevants.contains( id ) ) {

            precisionR.add( p );
            recallR.add( r );
         }
      }
      graphPrecisionAndRecall( precisionR, recallR, "Relevant (#)" );
   }

   private void graphFMeasure(
      int[] graphemes,
      List<Double> precision,
      List<Double> recall,
      String xLabel
   ) {
      
      if( recall.size() <= 0 || precision.size() <= 0) {

         JOptionPane.showMessageDialog( this, "A default query must be made" );
         return;
      }

      final Map<String, List<Double>> map = new HashMap<String, List<Double>>();
      for( int grapheme : graphemes ) {

         final double graphemePow2 = grapheme * grapheme;
         final List<Double> fn = new ArrayList<>();
         for( int i = 0; i < recall.size() && i < precision.size(); i++ ) {

            final double p = precision.get( i );
            final double r = recall.get( i );
            fn.add( ( 1 + graphemePow2 ) * ( p * r ) / ( graphemePow2 * p + r ) );
         }
         map.put( "F" + grapheme, fn );
      }
      graph( map, "F Measure Comparison", xLabel );
   }

   private void graphFMeasure( int[] graphemes ) {
      graphFMeasure( graphemes, precision, recall, "Documents (#)" );
   }

   private void graphRelevantFMeasure( int[] graphemes ) {

      if( recall.size() <= 0 || precision.size() <= 0) {

         JOptionPane.showMessageDialog( this, "A default query must be made" );
         return;
      }

      final List<Double> precisionR = new ArrayList<>();
      final List<Double> recallR = new ArrayList<>();
      for( int i = 0; i < similars.size() && i < precision.size() && i < recall.size(); i++ ) {

         final int id = similars.get( i ).getId();
         final double p = precision.get( i );
         final double r = recall.get( i );
         if( relevants.contains( id ) ) {

            precisionR.add( p );
            recallR.add( r );
         }
      }
      graphFMeasure( graphemes, precisionR, recallR, "Relevant (#)" );
   }

   private void exportToCsv() {

      if( similars.size() <= 0 ) {

         JOptionPane.showMessageDialog( this, "A query must be made first" );
         return;
      }

      final List<Integer> ids = new ArrayList<>();
      final List<Double> similarities = new ArrayList<>();
      for( Similar pair : similars ) {
         ids.add( pair.getId() );
         similarities.add( pair.getSimilarity() );
      }
      final Map<String, List<? extends Serializable>> csv =
         new HashMap<String, List<? extends Serializable>>();


      if( relevants.size() > 0 ) {

         final List<Character> isRelevant = new ArrayList<>();
         for( int id : ids )
            isRelevant.add( relevants.contains(id) ? 'R' : 'N' );

         csv.put("Relevant", isRelevant);
      }

      csv.put("Id", ids);
      if( precision.size() > 0 )
         csv.put("Precision", precision);
      if( recall.size() > 0 )
         csv.put("Recall", recall);
      csv.put("Similarity", similarities);

      final Export export = new Export();
      export.exportToCSV(csv, new File(PATH + "/processer/data.csv"));
      System.out.println( "Sucessfully exported all Documents to CSV" );
   }

   private void exportRelevantToCsv() {

      if( recall.size() <= 0 || precision.size() <= 0) {

         JOptionPane.showMessageDialog( this, "A default query must be made" );
         return;
      }

      final List<Integer> idsR = new ArrayList<>();
      final List<Double> similaritiesR = new ArrayList<>();
      final List<Double> precisionR = new ArrayList<>();
      final List<Double> recallR = new ArrayList<>();
      for( int i = 0; i < similars.size() && i < precision.size() && i < recall.size(); i++ ) {
         final int id = similars.get( i ).getId();
         final double s = similars.get( i ).getSimilarity();
         final double p = precision.get( i );
         final double r = recall.get( i );
         if( relevants.contains( id ) ) {
            idsR.add( id );
            similaritiesR.add( s );
            precisionR.add( p );
            recallR.add( r );
         }
      }
      final Map<String, List<? extends Serializable>> csv =
         new HashMap<String, List<? extends Serializable>>();

      csv.put("Id", idsR);
      if( precision.size() > 0 )
         csv.put("Precision", precisionR);
      if( recall.size() > 0 )
         csv.put("Recall", recallR);
      csv.put("Similarity", similaritiesR);

      final Export export = new Export();
      export.exportToCSV(csv, new File(PATH + "/processer/relevant.csv"));
      System.out.println( "Sucessfully exported Relevant Documents to CSV" );
   }

   /**
    * @param args the command line arguments
    */
   public static void main( String args[] ) {

      try {

         for( UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
            if( "Nimbus".equals( info.getName() ) ) {
               UIManager.setLookAndFeel( info.getClassName() );
               break;
            }
         }
      } catch( Exception e ) {

         e.printStackTrace();
      }

      /* Create and display the form */
      EventQueue.invokeLater(

         new Runnable() {

            @Override
            public void run() {
               new View().setVisible(true);
            }
         }
      );
   }
}