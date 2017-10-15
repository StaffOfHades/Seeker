package buscador;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.swing.*;
import javax.swing.event.*;

import org.jfree.ui.RefineryUtilities;
import org.renjin.eval.EvalException;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.Vector;

import grapher.Export;
import grapher.Graph;

public class Ventana extends JFrame implements Constants {

   // Constants
   private final static String TYPEFACE = "Tahoma";
   private final static int TEXT_TITLE = 36;
   private final static int TEXT_HEADER = 18;
   private final static int TEXT_BODY = 14;
   private final static String PATH =
      "/Users/mauriciog/Downloads/Recuperacion de la Información/Buscador/src/main/java/grapher/";

   // Variables
   private JButton searchDocument;
   private JButton searchTerm;
   private JTextField queryField;
   private JCheckBox specificDocumentToggle;
   private JLabel titleLabel;
   private Label resultLabel;
   private JTextField documentNumberField;
   private TextArea resultArea;

   private final Connect connect;
   private List<Similar> similars;
   private List<Integer> relevants;
   private List<Double> precision;
   private List<Double> recall;

   public Ventana() {

      initComponents();
      searchDocument.setVisible(false);

      connect = Connect.getInstance();
   }

   private void initComponents() {

      titleLabel = new JLabel();
      queryField = new JTextField();
      searchDocument = new JButton();
      specificDocumentToggle = new JCheckBox();
      documentNumberField = new JTextField();
      searchTerm = new JButton();
      resultArea = new TextArea();
      resultLabel = new Label();

      setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      setTitle( "HW5" );

      titleLabel.setFont(
         new Font(
            TYPEFACE,
            0,
            TEXT_TITLE
         )
      );
      titleLabel.setText( "Buscador v.1" );

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

      searchDocument.setFont( body );
      searchDocument.setText( "Buscar" );
      searchDocument.addActionListener(
         new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent event ) {

               try {
                  searchDocumentActionPerformed( event );
               } catch ( Exception e ) {
                  e.printStackTrace();
               }
            }
         }
      );

      specificDocumentToggle.setFont( body );
      specificDocumentToggle.setText( "Buscar en documento específico" );
      specificDocumentToggle.addChangeListener(
         new ChangeListener() {

            @Override
            public void stateChanged( ChangeEvent event ) {
               specificDocumentStateChanged( event );
            }
         }
      );

      documentNumberField.setEditable( false );
      documentNumberField.setFont( header );
      documentNumberField.setToolTipText( "" );

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

      GroupLayout layout = new GroupLayout( getContentPane() );
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(GroupLayout.Alignment.LEADING)
         .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGap(20, 20, 20)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
               .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 81, GroupLayout.PREFERRED_SIZE)
                  .addComponent(titleLabel)
                  .addGap(72, 72, 72))
               .addComponent(queryField, GroupLayout.PREFERRED_SIZE, 359, GroupLayout.PREFERRED_SIZE)
               .addGroup(layout.createSequentialGroup()
                  .addGap(145, 145, 145)
                  .addComponent(searchTerm))
               .addGroup(layout.createSequentialGroup()
                  .addGap(68, 68, 68)
                  .addComponent(specificDocumentToggle))
               .addGroup(layout.createSequentialGroup()
                  .addGap(125, 125, 125)
                  .addComponent(documentNumberField, GroupLayout.PREFERRED_SIZE, 108, GroupLayout.PREFERRED_SIZE))
               .addGroup(layout.createSequentialGroup()
                  .addGap(143, 143, 143)
                  .addComponent(searchDocument)))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(resultLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                  .addComponent(resultArea, GroupLayout.PREFERRED_SIZE, 141, GroupLayout.PREFERRED_SIZE))
            .addGap(70, 70, 70))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addGap(30, 30, 30)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(titleLabel)
                  .addGap(38, 38, 38)
                  .addComponent(queryField, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                  .addComponent(searchTerm)
                  .addGap(25, 25, 25)
                  .addComponent(specificDocumentToggle)
                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(documentNumberField, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                  .addComponent(searchDocument))
               .addGroup(layout.createSequentialGroup()
                  .addComponent(resultLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(resultArea, GroupLayout.PREFERRED_SIZE, 186, GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(29, Short.MAX_VALUE))
      );

      titleLabel.getAccessibleContext().setAccessibleName("titleLabel");

      pack();
   }

   // Toggle view change whenever searching for a specific document.
   private void specificDocumentStateChanged( ChangeEvent event ) {

      final boolean selected = specificDocumentToggle.isSelected();
      searchDocument.setVisible( selected );
      searchTerm.setVisible( !selected );
      documentNumberField.setEditable( selected );
      if( !selected )
         documentNumberField.setText( "" );
   }

   // Search for term in specific document.
   private void searchDocumentActionPerformed( ActionEvent event ) throws Exception {
      
      // Document number as string,
      final String document = documentNumberField.getText();

      // Convert to number.
      final int iddoc = Integer.parseInt( document );

      // Retrieve query from textfield,
      final String query = queryField.getText();

      // Split into individual terms
      final String[] terms = query.split( "\\s+" );

      // Retrieve dfs from db,
      final int[] dfs = connect.getDFs( iddoc, terms );

      // And print results.
      for(int i = 0; i < terms.length; i++) {

         terms[i] = terms[i].trim().replaceAll("\\p{Punct}+", "");
         System.out.println(
            "'" +
            terms[i] +
            "' has a df of " +
            dfs[i] +
            " for iddoc #" +
            iddoc 
         );
      }
   }

   // Search for term in collection, and return relevant documents in order.
   private void buscarTerminoActionPerformed( ActionEvent event ) throws Exception {

      // Retrieve query from textfield,
      final String query = queryField.getText();
      final DecimalFormat format = new DecimalFormat("#0.00");

      final int idquery;
      // If its a user query, add it to db and get its id;
      // Otherwise, just find the idquery;
      if( query.trim().length() > 0 )
         idquery = connect.addQuery( query );
      else
         idquery = connect.findQuery( query );

      if( idquery == -1 )
         throw new Exception( "An idquery was uncessfuly found or created" );

      // Get similarity for a given idquery
      similars = connect.getSimilarity( idquery );

      if( similars == null )
         throw new Exception( "An error occured when getting similarity list" );
      
      System.out.println(
         DIVIDER +
         "First 100 Similar Documents" +
         DIVIDER
      );

      // Iterate over the first 100 documents found
      for( int i = 0; i < similars.size() && i < 100; i++ ) {

         final Similar similar = similars.get(i);
         System.out.println(
            "Documento #" +
            similar.getId() +
            ": " +
            similar.getSimilarity()
         );
         resultArea.setText(
            resultArea.getText() +
            similar.getId() +
            "\n"
         );
      }

      // Scroll back to top of text area
      resultArea.setCaretPosition( 0 );

      // Get relevant documents
      relevants = connect.getRelevants(idquery);

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

         precision = new ArrayList<>();
         recall = new ArrayList<>();

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
            ", of which" +
            found +
            " where relvant"
         );
      }
   }

   private void graphPrecisionAndRecall() {

      final Map<String, List<Double>> map = new HashMap<String, List<Double>>();
      map.put("Precision", precision);
      map.put("Recall", recall);
      Graph<Double> graph = new Graph<>("Grafica", "Recall vs Precision", map);
      graph.pack();    
      RefineryUtilities.centerFrameOnScreen( graph );          
      graph.setVisible( true );
   }

   private void exportToCsv() {

      final List<Integer> ids = new ArrayList<>();
      final List<Double> similarities = new ArrayList<>();
      for( Similar pair : similars ) {
         ids.add( pair.getId() );
         similarities.add( pair.getSimilarity() );
      }
      final Map<String, List<? extends Serializable>> csv =
         new HashMap<String, List<? extends Serializable>>();

      csv.put("Id", ids);
      csv.put("Precision", precision);
      csv.put("Recall", recall);
      csv.put("Similarity", similarities);

      Export export = new Export();
      export.exportToCSV(csv, new File(PATH + "data.csv"));
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
               new Ventana().setVisible(true);
            }
         }
      );
   }
}