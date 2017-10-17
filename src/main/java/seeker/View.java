package seeker;

import grapher.Export;
import grapher.Graph;

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

import org.jfree.ui.RefineryUtilities;

public class View extends JFrame implements Constants {

   // Constants
   private final static String TYPEFACE = "Tahoma";
   private final static int TEXT_TITLE = 36;
   private final static int TEXT_HEADER = 18;
   private final static int TEXT_BODY = 14;
   static final long serialVersionUID = 25L;

   // View Variables
   private JComboBox<String> querySelector;
   private JMenuItem toggleSearch;
   private JTextArea resultArea;
   private JTextField queryField;
   
   // Inner Variables
   private final Connect connect;
   private List<Similar> similars;
   private List<Integer> relevants;
   private List<Double> precision;
   private List<Double> recall;
   private boolean useRelevanceFeedback;  

   public View() {

      connect = Connect.getInstance();
      similars = new ArrayList<>();
      relevants = new ArrayList<>();
      precision = new ArrayList<>();
      recall = new ArrayList<>();
      useRelevanceFeedback = false;
      initComponents();
   }

   private void initComponents() {
      
      final JButton searchTerm = new JButton();
      querySelector = new JComboBox<>( connect.getQueries() );
      final JLabel resultLabel = new JLabel();
      final JLabel titleLabel = new JLabel();
      final JMenu file = new JMenu( "File" );
      final JMenu edit = new JMenu( "Edit" );
      final JMenuBar menuBar = new JMenuBar();
      final JMenuItem export = new JMenuItem( "Export to CSV" );
      final JMenuItem graphRP = new JMenuItem( "Graph Recall & Precision" );
      final JMenuItem graphF = new JMenuItem( "Grap F1 & F2" );
      toggleSearch = new JMenuItem( "Use Relevance Feedback" );
      resultArea = new JTextArea();
      queryField = new JTextField();

      final JScrollPane scroll = new JScrollPane( resultArea );

      scroll.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

      setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      setTitle( "HW5" );

      export.addActionListener(
         new ActionListener(){
      
            @Override
            public void actionPerformed(ActionEvent e) {
               exportToCsv();
            }
         }
      );

      graphRP.addActionListener(
         new ActionListener(){
      
            @Override
            public void actionPerformed(ActionEvent e) {
               graphPrecisionAndRecall();
            }
         }
      );

      graphF.addActionListener(
         new ActionListener(){
      
            @Override
            public void actionPerformed(ActionEvent e) {
               graphFMeasure();
            }
         }
      );

      toggleSearch.addActionListener(
         new ActionListener(){
      
            @Override
            public void actionPerformed(ActionEvent e) {
               toggleSearch();
            }
         }
      );

      menuBar.add( file );
      menuBar.add( edit );
      file.add( export );
      file.add( graphRP );
      file.add( graphF );
      edit.add( toggleSearch );
      this.setJMenuBar( menuBar );

      titleLabel.setFont(
         new Font(
            TYPEFACE,
            0,
            TEXT_TITLE
         )
      );
      titleLabel.setText( "Buscador v.2" );

      final Font header = new Font(
         TYPEFACE,
         0,
         TEXT_HEADER
      );

      queryField.setFont( header );

      final Font body = new Font(
         TYPEFACE,
         0,
         TEXT_BODY
      );

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
               if( querySelector.getSelectedIndex() > 0 )
                  queryField.setText("");
            }
         }
      );

      queryField.getDocument().addDocumentListener(
         new DocumentListener() {
         
            @Override
            public void removeUpdate(DocumentEvent e) {
               if( queryField.getText().trim().length() >  0 )
                  querySelector.setSelectedIndex( 0 );
            }
         
            @Override
            public void insertUpdate(DocumentEvent e) {
               if( queryField.getText().trim().length() > 0 )
                  querySelector.setSelectedIndex( 0 );
            }
         
            @Override
            public void changedUpdate(DocumentEvent e) {
               if( queryField.getText().trim().length() > 0 )
                  querySelector.setSelectedIndex( 0 );
            }
         }
      );

      GroupLayout layout = new GroupLayout( getContentPane() );
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
      similars = useRelevanceFeedback ?
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
            " where relvant"
         );
      }
   }

   private void graphPrecisionAndRecall() {

      if( recall.size() <= 0 || precision.size() <= 0) {

         JOptionPane.showMessageDialog( this, "A default query must be made" );
         return;
      }

      final Map<String, List<Double>> map = new HashMap<String, List<Double>>();
      map.put("Precision", precision);
      map.put("Recall", recall);
      String title = "Recall vs Precision (Query #" +
         querySelector.getSelectedIndex();
      if( useRelevanceFeedback )
         title += " w/ Relevance Feedback";
      title += ")";
      Graph<Double> graph = new Graph<>("Grafica", title, map);
      graph.pack();
      RefineryUtilities.centerFrameOnScreen( graph );          
      graph.setVisible( true );
   }

   private void graphFMeasure() {
      if( recall.size() <= 0 || precision.size() <= 0) {

         JOptionPane.showMessageDialog( this, "A default query must be made" );
         return;
      }

      final Map<String, List<Double>> map = new HashMap<String, List<Double>>();
      final List<Double> f1 = new ArrayList<>();
      final List<Double> f2 = new ArrayList<>();
      final double graphemePow2 = 4.0;
      for( int i = 0; i < recall.size() && i < precision.size(); i++ ) {
         final double p = precision.get( i );
         final double r = recall.get( i );
         f1.add( 2 * ( p * r ) / ( p + r ) );
      }
      for( int i = 0; i < recall.size() && i < precision.size(); i++ ) {
         final double p = precision.get( i );
         final double r = recall.get( i );
         f2.add( (1 + graphemePow2 ) * ( p * r ) / ( graphemePow2 * p + r ) );
      }
      map.put("F1", f1);
      map.put("F2", f2);

      String title = "F1 vs F2 Measure (Query #" +
      querySelector.getSelectedIndex();
      if( useRelevanceFeedback )
         title += " w/ Relevance Feedback";
      title += ")";
      Graph<Double> graph = new Graph<>("Grafica", title, map);
      graph.pack();
      RefineryUtilities.centerFrameOnScreen( graph );          
      graph.setVisible( true );
   }

   private void graphFMeasure( int grapheme ) {
      if( recall.size() <= 0 || precision.size() <= 0) {

         JOptionPane.showMessageDialog( this, "A default query must be made" );
         return;
      }

      final Map<String, List<Double>> map = new HashMap<String, List<Double>>();
      final List<Double> f = new ArrayList<>();
      final double graphemePow2 = grapheme * grapheme * 1.0;
      for( int i = 0; i < recall.size() && i < precision.size(); i++ ) {
         final double p = precision.get( i );
         final double r = recall.get( i );
         f.add( (1 + graphemePow2 ) * ( p * r ) / ( graphemePow2 * p + r ) );
      }
      map.put("F" + grapheme, f);


      String title = 
         "F" +
         grapheme +
         " Measure (Query #" +
         querySelector.getSelectedIndex();
      if( useRelevanceFeedback )
         title += " w/ Relevance Feedback";
      title += ")";
      Graph<Double> graph = new Graph<>("Grafica", title, map);
      graph.pack();
      RefineryUtilities.centerFrameOnScreen( graph );          
      graph.setVisible( true );
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

      csv.put("Id", ids);
      if( precision.size() > 0 )
         csv.put("Precision", precision);
      if( recall.size() > 0 )
         csv.put("Recall", recall);
      csv.put("Similarity", similarities);

      final Export export = new Export();
      export.exportToCSV(csv, new File(PATH + "/grapher/data.csv"));
   }

   private void toggleSearch() {
      useRelevanceFeedback = !useRelevanceFeedback;
      final String text = useRelevanceFeedback ? "Stop using Relevance Feedback" : "Use Relevance Feedback";
      final String message = useRelevanceFeedback ? "Now using Relevance Feedback" : "No longer using Relevance Feedback";
      System.out.println( message );
      toggleSearch.setText( text );
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