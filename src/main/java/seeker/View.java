package seeker;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jfree.ui.RefineryUtilities;

import processer.Export;
import processer.Graph;

public class View extends AbstractView {

   static final long serialVersionUID = 53L;
   private int currentIndex;
   
   public View() {
      super();
   }

   // Search for term in collection, and return relevant documents in order.
   @Override
   protected void buscarTermino() throws Exception {

      // Retrieve query from textfield,
      final String query = queryField.getText();

      int idquery;
      // If its a user query, add it to db and get its id;
      // Otherwise, just find the idquery;
      if( query.trim().length() > 0 )
         idquery = connect.addQuery( query );
      else if ( querySelector.getSelectedIndex() > 0 )
         idquery = querySelector.getSelectedIndex();
      else {
                  
         JOptionPane.showMessageDialog( this, "Please select a query or add your own" );
         return;
      }

      if( idquery == -1 )
         throw new Exception( "An idquery was uncessfuly found or created" );
      
      // Get similarity for a given idquery
      List<Similar> similars = connect.getSimilarity(idquery ); //original
      //List<Similar> similars = connect.getSimilarityClusterQuery(idquery );   // con cluster
       
      //ARTURO 
      //escribre the walking dead en el buscador
      // Get relevant documents
      List<Integer> relevants = connect.getRelevants( idquery );
      
      resultArea.setText( "" );

      // Itera
      String texto = "";
      for( int i = 0; i < similars.size() && i < MAX_DOC; i++ ) {

         final Similar similar = similars.get(i);
         System.out.println(
            "Documento #" +
            similar.getId() +
            ": " +
            similar.getSimilarity()
         );
         
         if( relevants.contains(similar.getId())){
               texto = texto + "---RELEVANTE!--- <br>\n";
           }
         
         texto += "#Doc: " + similar.getId() + " - " + similar.getText().substring(0, 140) 
                           + "<br>\n" + "<br>\n" + makeLinksInText(similar.getURL()) + "<br>\n";
         texto += "<br>\n";
    
      }
      resultArea.setText( texto );  
      
      //ARTURO FIN
      if( toggleSearch.isSelected() )
            similars = connect.getSimilarityQ1( idquery, similars );
            //getSimilarityQ1StopWords
      if( similars == null )
         throw new Exception( "An error occured when getting similarity list" );
      
      System.out.println(similars.size());
      List<Double> precision = new ArrayList<>();
      List<Double> recall = new ArrayList<>();
      
      if( relevants == null )
         throw new Exception( "An error occured when getting relevants list" );
      
      // Only try to find precision and recall if there are relevant documents;
      if( relevants.size() > 0 ) {
         
         int counter = 1;
         int found = 0;
         final int correct = relevants.size();
         

         // Iterate over all relevant documents,
         for( Similar pair : similars ) {

            found += relevants.contains( pair.getId() ) ? 1 : 0;
            final double p = (double) found / counter; 
            final double r = (double) found / correct;
            precision.add( p );
            recall.add( r );

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
      System.out.println( DIVIDER + "End" + DIVIDER + "\n" );
      if( similarsHistory.size() > 5)
         similarsHistory.removeFirst();         
      similarsHistory.add( similars );

      if( relevantsHistory.size() > 5)
         relevantsHistory.removeFirst();         
      relevantsHistory.add( relevants );

      if( recallHistory.size() > 5)
         recallHistory.removeFirst();
      recallHistory.add( recall );

      if( precisionHistory.size() > 5)
         precisionHistory.removeFirst();         
      precisionHistory.add( precision );

      if( idHistory.size() > 5 )
         idHistory.removeFirst();
      final String text = idquery + ( toggleSearch.isSelected() ? " w/ Feedback" : "" ) ;
      idHistory.add( text );
      currentIndex = idHistory.indexOf( text );

      if( feedbackHistory.size() > 5 )
         feedbackHistory.removeFirst();
      feedbackHistory.add( toggleSearch.isSelected() );
     
      updateHistory();
      
      //showResults();
   }

   private void showResults() {
      showResults( idHistory.get( currentIndex ) );
   }

   @Override
   protected void showResults( String id ) {

      currentIndex = idHistory.indexOf( id );
      System.out.println("--------------------------------------");
      System.out.println(currentIndex);
      if( currentIndex < 0 )
         return;
      final String[] ids = id.split( "\\s+" );
      final int idquery = Integer.parseInt( ids[0] );
      toggleSearch.setSelected( feedbackHistory.get( currentIndex ) );
      querySelector.setSelectedIndex( idquery );

      final List<Similar> similars = similarsHistory.get( currentIndex );
      final List<Integer> relevants = relevantsHistory.get( currentIndex );
      final List<Double> precision = precisionHistory.get( currentIndex );
      final List<Double> recall = recallHistory.get( currentIndex );
      
      System.out.println( "Query: \"" + querySelector.getSelectedItem()  + "\"");

      System.out.println(
         DIVIDER +
         "First " + 
         MAX_DOC +
         " Similar Documents" +
         DIVIDER
      );

      resultArea.setText( "" );

      // Iterate over the first MAX_DOC documents found
      String text = "";
      for( int i = 0; i < similars.size() && i < MAX_DOC; i++ ) {

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
      
      final DecimalFormat format = new DecimalFormat("#0.00");

      if( relevants.size() > 0 ) {

         System.out.println(
            DIVIDER +
            "Precision & Recall" +
            DIVIDER
         );
      }

      for(  int i = 0;
            i < MAX_DOC && i < recall.size() && i < precision.size() && i < similars.size();
            i++
      ) {

         System.out.println(
            "Documento #" +
            similars.get( i ).getId() +
            ": Precision: " +
            format.format( 100 * precision.get( i ) ) +
            "%; Recall: " +
            format.format( 100 * recall.get( i ) ) +
            "%;"
         );
      }
      System.out.println( DIVIDER + "End" + DIVIDER + "\n" );
   }

   private void graph(
      Map<String, List<Double>> map,
      String title,
      String xLabel
   ) {    
      final Graph<Double> graph = new Graph<>("Grafica", title, xLabel, map);
      graph.pack();      
      RefineryUtilities.centerFrameOnScreen( graph );          
      graph.setVisible( true );
   }

   private void graphPrecisionAndRecall( int[] indeces, boolean onlyRelevant ) {

      final Map<String, List<Double>> data =
         new HashMap<String, List<Double>>();

      String title;
      for( int index : indeces ) {

         final int idquery = Integer.parseInt( idHistory.get( index ).split("\\s+")[0] );
         title = "";
         if( indeces.length > 1 ) {

            title += " #" + idquery;
            if( feedbackHistory.get( index ) )
               title += " w/ RF";
         }

         final List<Similar> similars = similarsHistory.get( index );
         final List<Integer> relevants = relevantsHistory.get( index );
         final List<Double> precision = precisionHistory.get( index );
         final List<Double> recall = recallHistory.get( index );
                  
         final List<Double> precisionR = new ArrayList<>();
         final List<Double> recallR = new ArrayList<>();
         for( int i = 0; i < similars.size() && i < precision.size() && i < recall.size(); i++ ) {

            final Similar s = similars.get( i );

            if( !onlyRelevant || relevants.contains( s.getId() ) ) {
                              
               precisionR.add( precision.get( i ) );
               recallR.add( recall.get( i ) );
            }
         }
                 
         data.put( "Precision" + title, precisionR);        
         data.put( "Recall" + title, recallR);
      }

      title = "";
      if ( indeces.length > 1 )
         title += "Comparison of ";

      title += "Recall vs Precision";
      if( indeces.length == 1 ) {
         
         final int idquery = Integer.parseInt( idHistory.get( 0 ).split("\\s+")[0] );
         title += " (Query #" + idquery;
         if( feedbackHistory.get( indeces[0] ) )
            title += " w/ Relevance Feedback";
         title += ")";
      }

      String xLabel = onlyRelevant ? "Relevants (#)" : "Documents (#)";

      graph(
         data,
         title,
         xLabel
      );
   }

   @Override
   protected void graphPrecisionAndRecall() {
      
      boolean checked = false;
      for( MenuElement element : compare.getSubElements() )
         if( !checked && element instanceof JPopupMenu)
            for( MenuElement item : element.getSubElements() )
               if( !checked && item instanceof JSelectorMenuItem )
                  checked = ( (JSelectorMenuItem) item).isSelected();

      int[] indeces = new int[0]; 
      if( !checked ) {

         if( similarsHistory.size() <= 0 ) {

            JOptionPane.showMessageDialog( this, "A query must be made first" );
            return;
         }
         indeces = new int[] { currentIndex };
      } else {

         MenuElement[] elements = compare.getSubElements()[0].getSubElements();
         List<Integer> ids = new ArrayList<>();
         for( int i = 0; i < elements.length; i++ ) {

            if( elements[i] instanceof JSelectorMenuItem ) {

               final JSelectorMenuItem item = (JSelectorMenuItem )elements[i];
               if( item.isSelected() )
                  ids.add( i );
            }
         }
         indeces = ids.stream().mapToInt( i -> i).toArray();
      }
      
      if( indeces.length > 0 ) {

         graphPrecisionAndRecall( indeces, false );
      } else
         System.err.println( "Nothing to graph" );
   }

   @Override
   protected void graphRelevantPrecisionAndRecall() {

      boolean checked = false;
      for( MenuElement element : compare.getSubElements() )
         if( !checked && element instanceof JPopupMenu)
            for( MenuElement item : element.getSubElements() )
               if( !checked && item instanceof JSelectorMenuItem )
                  checked = ( (JSelectorMenuItem) item).isSelected();

      int[] indeces = new int[0]; 
      if( !checked ) {

         if( recallHistory.get( currentIndex ).size() <= 0 ||
            precisionHistory.get( currentIndex ).size() <= 0 ) {

            JOptionPane.showMessageDialog( this, "A default query must be made" );
            return;
         }
        
         indeces = new int[] { currentIndex };
      } else {

         MenuElement[] elements = compare.getSubElements()[0].getSubElements();
         List<Integer> ids = new ArrayList<>();
         for( int i = 0; i < elements.length; i++ ) {

            if( elements[i] instanceof JSelectorMenuItem ) {

               final JSelectorMenuItem item = (JSelectorMenuItem )elements[i];
               if( item.isSelected() ) {

                  final int index = idHistory.indexOf( item.getText() );
                  if( currentIndex >= 0 && relevantsHistory.get( index ).size() > 0 )
                     ids.add( i );
                  else if( currentIndex >= 0 )
                     System.err.println( "Selected id query has no relevant documents" );
               }
            }
         }
         indeces = ids.stream().mapToInt( i -> i).toArray();
      }
      if( indeces.length > 0 ) {
         graphPrecisionAndRecall( indeces, true );
      } else
         System.err.println( "Nothing to graph" );
   }

   private void graphFMeasure( double grapheme, int[] indeces, boolean onlyRelevant ) {

      final Map<String, List<Double>> data =
         new HashMap<String, List<Double>>();
      
      String title;
      for( int index : indeces ) {
         
         final int idquery = Integer.parseInt( idHistory.get( index ).split("\\s+")[0] );
         title = "F" + ( grapheme % 1 == 0 ? (int) grapheme : grapheme );
         if( indeces.length > 1 ) {
            
            title += " #" + idquery;
            if( feedbackHistory.get( index ) )
               title += " w/ RF";
         }

         final List<Similar> similars = similarsHistory.get( index );
         final List<Integer> relevants = relevantsHistory.get( index );
         final List<Double> precision = precisionHistory.get( index );
         final List<Double> recall = recallHistory.get( index );

         final double graphemePow2 = grapheme * grapheme;
         final List<Double> fm = new ArrayList<>();
         for( int i = 0; i < similars.size() && i < precision.size() && i < recall.size(); i++ ) {

            final Similar s = similars.get( i );

            if( !onlyRelevant || relevants.contains( s.getId() ) ) {
               
               final double p = precision.get( i );
               final double r = recall.get( i );
               fm.add( ( 1 + graphemePow2 ) * ( p * r ) / ( graphemePow2 * p + r ) );
            }
         }
         data.put( title, fm );
      }
      title = "";
      if ( indeces.length > 1 )
         title += "Comparison of ";

      title += "F" + ( grapheme % 1 == 0 ? (int) grapheme : grapheme ) + " Measure";
      if( indeces.length == 1 ) {
         
         final int idquery = Integer.parseInt( idHistory.get( 0 ).split("\\s+")[0] );
         title += " (Query #" + idquery;
         if( feedbackHistory.get( indeces[0] ) )
            title += " w/ Relevance Feedback";
         title += ")";
      }

      String xLabel = onlyRelevant ? "Relevants (#)" : "Documents (#)";

      graph(
         data,
         title,
         xLabel
      );
   }

   @Override
   protected void graphFMeasure( double grapheme ) {

      boolean checked = false;
      for( MenuElement element : compare.getSubElements() )
         if( !checked && element instanceof JPopupMenu)
            for( MenuElement item : element.getSubElements() )
               if( !checked && item instanceof JSelectorMenuItem )
                  checked = ( (JSelectorMenuItem) item).isSelected();

      int[] indeces = new int[0]; 
      if( !checked ) {

         if( similarsHistory.size() <= 0 ) {

            JOptionPane.showMessageDialog( this, "A query must be made first" );
            return;
         }
         indeces = new int[] { currentIndex };
      } else {

         MenuElement[] elements = compare.getSubElements()[0].getSubElements();
         List<Integer> ids = new ArrayList<>();
         for( int i = 0; i < elements.length; i++ ) {

            if( elements[i] instanceof JSelectorMenuItem ) {

               final JSelectorMenuItem item = (JSelectorMenuItem )elements[i];
               if( item.isSelected() )
                  ids.add( i );
            }
         }
         indeces = ids.stream().mapToInt( i -> i).toArray();
      }
      
      if( indeces.length > 0 ) {

         graphFMeasure( grapheme, indeces, false );
      } else
         System.err.println( "Nothing to graph" );
   }

   @Override
   protected void graphRelevantFMeasure( double grapheme ) {

      boolean checked = false;
      for( MenuElement element : compare.getSubElements() )
         if( !checked && element instanceof JPopupMenu)
            for( MenuElement item : element.getSubElements() )
               if( !checked && item instanceof JSelectorMenuItem )
                  checked = ( (JSelectorMenuItem) item).isSelected();

      int[] indeces = new int[0]; 
      if( !checked ) {

         if( recallHistory.get( currentIndex ).size() <= 0 ||
            precisionHistory.get( currentIndex ).size() <= 0 ) {

            JOptionPane.showMessageDialog( this, "A default query must be made" );
            return;
         }
        
         indeces = new int[] { currentIndex };
      } else {

         MenuElement[] elements = compare.getSubElements()[0].getSubElements();
         List<Integer> ids = new ArrayList<>();
         for( int i = 0; i < elements.length; i++ ) {

            if( elements[i] instanceof JSelectorMenuItem ) {

               final JSelectorMenuItem item = (JSelectorMenuItem) elements[i];
               if( item.isSelected() ) {

                  final int index = idHistory.indexOf( item.getText() );
                  if( index >= 0 && relevantsHistory.get( index ).size() > 0 )
                     ids.add( i );
                  else if( index >= 0 )
                     System.err.println( "Selected id query has no relevant documents" );
               }
            }
         }
         indeces = ids.stream().mapToInt( i -> i).toArray();
      }
      if( indeces.length > 0 ) {
         graphFMeasure( grapheme, indeces, true );
      } else
         System.err.println( "Nothing to graph" );
   }

   private void exportCsv(
      Map<String, Map<String, List<? extends Serializable>>> csv ) {
         
      final Export export = new Export();

      for( String name: csv.keySet() ) {

         export.exportToCSV(
            csv.get( name ),
            new File( PATH + "/processer/" + name )
         );
      }             
   }

   private void exportToCsv( int[] indeces, boolean onlyRelevant ) {

      final Map<String, Map<String, List<? extends Serializable>>> csv =
         new HashMap<String, Map<String, List<? extends Serializable>>>();

      for(int index : indeces) {

         final int idquery = Integer.parseInt( idHistory.get( index ).split("\\s+")[0] );
         final List<Similar> similars = similarsHistory.get( index );
         final List<Integer> relevants = relevantsHistory.get( index );
         final List<Double> precision = precisionHistory.get( index );
         final List<Double> recall = recallHistory.get( index );

         final List<Integer> idsR = new ArrayList<>();
         final List<Double> similaritiesR = new ArrayList<>() ;
         final List<Double> precisionR = new ArrayList<>();
         final List<Double> recallR = new ArrayList<>();
         for( int i = 0; i < similars.size() && i < precision.size() && i < recall.size(); i++ ) {

            final Similar s = similars.get( i );

            if( !onlyRelevant || relevants.contains( s.getId() ) ) {

               idsR.add( s.getId() );
               similaritiesR.add( s.getSimilarity() );
               precisionR.add( precision.get( i ) );
               recallR.add( recall.get( i ) );
            }
         }
         final Map<String, List<? extends Serializable>> table =
            new HashMap<String, List<? extends Serializable>>();
   
         table.put( "Id", idsR );
         table.put("Precision", precisionR);        
         table.put("Recall", recallR);
         table.put("Similarity", similaritiesR);
         String title = "relevant-" + idquery;
         if( feedbackHistory.get( index ) )
            title += "-feedback";
         title += ".csv";
         csv.put( title, table);
      }
      exportCsv( csv );
   }
  

   @Override
   protected void exportToCsv() {

      boolean checked = false;
      for( MenuElement element : compare.getSubElements() )
         if( !checked && element instanceof JPopupMenu)
            for( MenuElement item : element.getSubElements() )
               if( !checked && item instanceof JSelectorMenuItem )
                  checked = ( (JSelectorMenuItem) item).isSelected();

      int[] indeces = new int[0]; 
      if( !checked ) {

         if( similarsHistory.size() <= 0 ) {

            JOptionPane.showMessageDialog( this, "A query must be made first" );
            return;
         }
         indeces = new int[] { currentIndex };
      } else {

         MenuElement[] elements = compare.getSubElements()[0].getSubElements();
         List<Integer> ids = new ArrayList<>();
         for( int i = 0; i < elements.length; i++ ) {

            if( elements[i] instanceof JSelectorMenuItem ) {

               final JSelectorMenuItem item = (JSelectorMenuItem )elements[i];
               if( item.isSelected() )
                  ids.add( i );
            }
         }
         indeces = ids.stream().mapToInt( i -> i).toArray();
      }
      
      if( indeces.length > 0 ) {

         exportToCsv( indeces, false );
         System.out.println( "Sucessfully exported all Documents to CSV" );
      } else
         System.err.println( "Nothing to exported" );
   }
   
   @Override
   protected void exportRelevantToCsv() {

      boolean checked = false;
      for( MenuElement element : compare.getSubElements() )
         if( !checked && element instanceof JPopupMenu)
            for( MenuElement item : element.getSubElements() )
               if( !checked && item instanceof JSelectorMenuItem )
                  checked = ( (JSelectorMenuItem) item).isSelected();
            
      int[] indeces = new int[0]; 
      if( !checked ) {

         if( recallHistory.get( currentIndex ).size() <= 0 ||
            precisionHistory.get( currentIndex ).size() <= 0 ) {

            JOptionPane.showMessageDialog( this, "A default query must be made" );
            return;
         }
        
         indeces = new int[] { currentIndex };
      } else {
        
         MenuElement[] elements = compare.getSubElements()[0].getSubElements();
         List<Integer> ids = new ArrayList<>();
         for( int i = 0; i < elements.length; i++ ) {

            if( elements[i] instanceof JSelectorMenuItem ) {

               final JSelectorMenuItem item = (JSelectorMenuItem )elements[i];
               if( item.isSelected() ) {

                  final int index = idHistory.indexOf( item.getText() );
                  if( index >= 0 && relevantsHistory.get( index ).size() > 0 )
                     ids.add( i );
                  else if( index >= 0 )
                     System.err.println( "Selected id query has no relevant documents" );
               }
            }
         }
         indeces = ids.stream().mapToInt( i -> i).toArray();
      }
      if( indeces.length > 0 ) {

         exportToCsv( indeces, true );
         System.out.println( "Sucessfully exported Relevant Documents to CSV" );
      } else
         System.err.println( "Nothing to exported" );
   }

   //Agregar un Link al texto encontrado
   public String makeLinksInText(String text) {
        text = "<a href=\"" + text + "\" target=\"_blank\" " + ">" + text + "</a>" ;
        return text;
    }
   
    //Menú
    public static void main( String args[] ) {
        //
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new View().setVisible(true);
            }
        });
        
        //
        try {
            for( UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
                if( "Nimbus".equals( info.getName() ) ) {
                    UIManager.setLookAndFeel( info.getClassName() );
                    break;
                }
            }
        } 
        catch( Exception e ) {
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