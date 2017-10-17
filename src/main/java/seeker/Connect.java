package seeker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Connect implements Constants {

   private static Connect sConnect;

   // Only allow the class from being accesed through a 
   // static method, and not from a xconstructor.
   private Connect() {

      try {
         // Add driver
         Class.forName( DRIVER ).newInstance();
      } catch( Exception e ) {
         e.printStackTrace();
      }
   }

   // Get an instance of connect.
   public static Connect getInstance() {

      if( sConnect == null )
         sConnect = new Connect();
      return sConnect;
   }

   public boolean checkConnection() {

      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null)
            return false;

         System.out.println( SUCCESS );
         System.out.println( DIVIDER + DIVIDER );
         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return true;
   }

   // Retrieve all default queries
   public String[] getQueries() {
    
      final int default_queries = 93;
      final String[] queries = new String[default_queries + 1];
      int i = 0;
      queries[i++] = DIVIDER + "Seleccionar" + DIVIDER;

      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null)
            return null;

         System.out.println( SUCCESS );
         System.out.println( DIVIDER + "Retrieving all queries" + DIVIDER );

         // SQL Query to find text for given idquery.
         final String sqlQuery = "select `text` from `queries` where `id` <= ?;";
         final PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.setInt( 1, default_queries );

         // Execute query & parse result.
         final ResultSet result = statement.executeQuery();

         // Add queries to array
         while( result.next() )
            queries[i++] = result.getString( 1 );
         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return queries;
   }

   // Given an idquery, attempt to find the query
   public String getQuery( int idquery ) {

      // Initialize query to null to return in case of mistake
      String query = null;

      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null)
            return null;

         System.out.println( SUCCESS );
         System.out.println( DIVIDER + "Retrieve query" + DIVIDER );

         // SQL Query to find text for given idquery.
         final String sqlQuery = "select `text` from `queries` where `id` = ?;";
         final PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.setInt( 1, idquery );

         // Execute query & parse result.
         final ResultSet result = statement.executeQuery();

         // If query exists for an id, save its text;
         // Otherwise, return empty text.
         if( result.next() )
            query = result.getString( 1 );
         else
            query = "";
         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return query;
   }

   // Given an array of terms and an specific iddoc,
   // find the dfs for all given terms. 
   public int[] getDFs( int iddoc, String[] terms ) {
      
      int[] dfs = new int[0];

      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null )
            return new int[0];

         System.out.println( SUCCESS );
         System.out.println(
            DIVIDER +
            "Consult df for terms in specific document" +
            DIVIDER
         );

         dfs = new int[terms.length];

         // Iterate over each term, and find its df for an iddoc
         for( int i = 0; i < terms.length; i++ ) {

            // Remove all punctuation and whitespaces in the term
            terms[i] = terms[i].trim().replaceAll("\\p{Punct}+", "");
            
            // SQL Query to check the Term Frequency in a given document
            // for a given term.
            final String sqlQuery =
               "select `tf` " +
               "from `contains` " +
               "where `iddoc` = ? " +
               "and `term` = ?;";

            final PreparedStatement statement = connection.prepareStatement( sqlQuery );
            statement.setInt( 1, iddoc ); 
            statement.setString( 2, terms[i] );

            // Execute query & parse result.
            final ResultSet result = statement.executeQuery();

            // Get df if there is a result.
            dfs[i] = result.next() ? result.getInt( 1 ) : 0;
         }
         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return dfs;
   }

   // Return a list of all documents with a similarity
   // in order descending given an idquery.
   public List<Similar> getSimilarity(int idquery) {
    
      List<Similar> similars = null;
      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null )
            return null;
         

         System.out.println( SUCCESS );
         System.out.println(
            DIVIDER +
            "Use dot product to get similarity for query: #" +
            idquery +
            DIVIDER
         );

         similars = new ArrayList<>();

         // SQL Query to determine similarity using Vector Space Model.
         final String sqlQuery = 
            "select `documents`.`id`, " +
            "sum(`contains`.`tf` * `terms`.`idf` * `made`.`tf` * `terms`.`idf`) / " +
            "(`documents`.`weight` * `queries`.`weight`) as `similar` " +
            "from `queries`, `documents`, `contains`, `made`, `terms` " +
            "where `documents`.`id` = `contains`.`iddoc` " +
            "and `contains`.`term` = `terms`.`term` " +
            "and `made`.`term` = `terms`.`term` " +
            "and `made`.`idquery` = `queries`.`id` " +
            "and `queries`.`id` = ? " +
            "group by `documents`.`id` " +
            "order by `similar` desc;"
         ;
         final PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.setInt( 1, idquery );

         // Execute query & parse result.
         final ResultSet result = statement.executeQuery();

         // Add all results to list
         while( result.next() ) {

            // Save into custom class for later retrieval,
            final Similar similar = new Similar(
               result.getInt( 1 ),
               result.getDouble( 2 )
            );
            // Add to list for a more permanent storage.
            similars.add( similar );
         }
         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return similars;
   }


   // Return a list of all documents with a similarity
   // in order descending for a given idquery
   // after calculating query1 using Rocchio weights.
   public List<Similar> getSimilarityQ1( int idquery ) {
      
      List<Similar> similars = null;
      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null )
            return null;

         System.out.println( SUCCESS );
         System.out.println(
            DIVIDER +
            "Calculating new Q1 using Rocchio weights from query: #" +
            idquery +
            DIVIDER
         );

         // Drop table `temp` if exists.
         String sqlQuery = "drop table if exists `temp`;";
         PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.executeQuery();

         // Add table `temp` if not exists.
         sqlQuery = "CREATE TEMPORARY TABLE IF NOT EXISTS `temp` (" +
            "`idquery` int(8) unsigned NOT NULL, " +
            "`term` varchar(32) NOT NULL, " +
            "`tf1` double(20,15) unsigned NOT NULL, " +
            "PRIMARY KEY (`term`) " +
         ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
         statement = connection.prepareStatement( sqlQuery );
         statement.executeQuery();

         // SQL Query to generate new tfs using Rochio weights.
         sqlQuery =
         "insert into `temp` (`idquery`, `term`,`tf1`) select " +
            "`made`.`idquery`, " +
            "`made`.`term`, " +
            "`made`.`tf` + 0.8 / " +
            "(select " +
               "count(`documents`.`id`) " +
               "from `documents`, `relevant` " +
               "where `documents`.`id` = `relevant`.`iddoc` " +
               "and `relevant`.`idquery` = `made`.`idquery` " +
            ") * sum(`contains`.`tf`) - 0.1 / " +
            "(select " +
               "(select count(`documents`.`id`) " +
                  "from `documents` " +
               ") - count(`documents`.`id`) " +
               "from `documents`, `relevant` " +
               "where `documents`.`id` = `relevant`.`iddoc` " +
               "and `relevant`.`idquery` = `made`.`idquery` " +
            ") * ( " +
               "(select sum(`contains`.`tf`) " +
                  "from `contains` " +
                  "where `contains`.`term` = `made`.`term` " +
               ") - sum(`contains`.`tf`) " +
            ") as `tf1` " +
            "from `contains`, `relevant`, `made` " +
            "where `contains`.`term` = `made`.`term` " +
            "and `contains`.`iddoc` = `relevant`.`iddoc` " +
            "and `relevant`.`idquery` = `made`.`idquery` " +
            "and `made`.`idquery` = ? " +
            "group by `made`.`term`;";
         statement = connection.prepareStatement( sqlQuery );
         statement.setInt( 1, idquery );
         statement.executeQuery();

         sqlQuery =
            "update `made` " +
            "inner join `temp` " +
            "on `made`.`term` = `temp`.`term` " +
            "and `made`.`idquery` = `temp`.`idquery` " +
            "set `made`.`tf1` = `temp`.`tf1`";
         statement = connection.prepareStatement( sqlQuery );
         statement.executeQuery();
      
         System.out.println( SUCCESS );
         System.out.println(
            DIVIDER +
            "Use dot product to get similarity for new query q1 from query: #" +
            idquery +
            DIVIDER
         );

         similars = new ArrayList<>();

         // SQL Query to determine similarity using Vector Space Model.
         sqlQuery = 
            "select `documents`.`id`, " +
            "sum(`contains`.`tf` * `terms`.`idf` * `made`.`tf1` * `terms`.`idf`) / " +
            "(`documents`.`weight` * `queries`.`weight`) as `similar` " +
            "from `queries`, `documents`, `contains`, `made`, `terms` " +
            "where `documents`.`id` = `contains`.`iddoc` " +
            "and `contains`.`term` = `terms`.`term` " +
            "and `made`.`term` = `terms`.`term` " +
            "and `made`.`idquery` = `queries`.`id` " +
            "and `queries`.`id` = ? " +
            "group by `documents`.`id` " +
            "order by `similar` desc;"
         ;
         statement = connection.prepareStatement( sqlQuery );
         statement.setInt( 1, idquery );

         // Execute query & parse result.
         final ResultSet result = statement.executeQuery();

         // Add all results to list
         while( result.next() ) {

            // Save into custom class for later retrieval,
            final Similar similar = new Similar(
               result.getInt( 1 ),
               result.getDouble( 2 )
            );
            // Add to list for a more permanent storage.
            similars.add( similar );
         }
         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return similars;
  }

   // Return a list of all iddoc that are relevant
   // for a given idquery.
   public List<Integer> getRelevants( int idquery ) {
     
      List<Integer> relevants = null;
      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null )
            return null;
         
         System.out.println( SUCCESS );
         System.out.println(
            DIVIDER +
            "Find all relevant documents for query: #" +
            idquery +
            DIVIDER
         );

         // SQL Query to find all relevant documents for an idquery.
         final String sqlQuery = "select `iddoc` from `relevant` where `idquery` = ?;";
         final PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.setInt( 1, idquery );

         // Execute query & parse result.
         final ResultSet result = statement.executeQuery();
         relevants = new ArrayList<>();

         // Add to list all relevant documents.
         while( result.next() ) 
            relevants.add( result.getInt( 1 ) );

         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return relevants;
   }

   // 
   public int addQuery( String query ) {

      query = query.trim();
      int idquery = -1;

      // Open connection to Database && Run query.
      try {
         
         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null )
            return idquery;
         
         System.out.println( SUCCESS );
         System.out.println( DIVIDER + "Add query if not exists" + DIVIDER );

         // SQL Query to check if a query allready exists in db.
         String sqlQuery = "select `id` from `queries` where `text` = ?;";
         PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.setString( 1, query );

         // Execute query & parse result.
         ResultSet result = statement.executeQuery();

         // Check if query exists, otherwise, add it to db.
         if( result.next() ) {

            idquery = result.getInt( 1 );
         } else {

            // SQL Query to add a new query to db.
            sqlQuery = "insert into `queries` (`text`) values(?);";
            statement = connection.prepareStatement( sqlQuery );
            statement.setString( 1, query );

            // Execute query
            statement.executeQuery();
   
            // SQL Query to find an idquery for a given query.
            sqlQuery = "select `id` from `queries` where `text` = ?;";
            statement = connection.prepareStatement( sqlQuery );
            statement.setString( 1, query );

            // Execute query & parse result.
            result = statement.executeQuery();

            // Get the idquery
            if( result.next() )
               idquery = result.getInt( 1 );
            else
               throw new SQLException( "Added query does not exist" );
   
            // Split the query into words
            final String[] words = query.split( "\\s+" );
            final Map<String, Integer> map = new HashMap<>();

            // Iterate over all the words to find df for each word.
            for( String word: words ) {
       
               // Remove all punctuation and whitespaces in a word
               word = word.trim().replaceAll( "\\p{Punct}+", "" );

               // If word exits in hash, add one to count, otherwise initiliaze at 1,
               final int count = ( map.containsKey( word ) ? map.get( word ) : 0 ) + 1;
               // and save it into list.
               map.put( word, count );
            }
            final int id_query = idquery;
       
            // For each word, add to db, along with its tf and idquery.
            map.forEach(
               ( word, count ) -> {
                  try {

                     // SQL Query to insert a word into terms, ignoring duplicates.
                     String sql_query = "insert ignore into `terms` (`term`) values(?);";
                     PreparedStatement prepared = connection.prepareStatement( sql_query );
                     prepared.setString( 1, word );

                     // Execute query
                     prepared.execute();
      
                     // SQL Query to add into made the word, tf, and idquery.
                     sql_query = "insert into `made` (`idquery`, `term`, `tf`, `tf1`) values (?, ?, ?, ?);";
                     prepared = connection.prepareStatement( sql_query );
                     prepared.setInt( 1, id_query );
                     prepared.setString( 2, word );
                     prepared.setInt( 3, count );
                     prepared.setInt( 4, count );
                     
                     // Execute query
                     prepared.execute();
                  } catch( Exception e ) {
                     e.printStackTrace();
                  }
               }
            );
         }
         connection.close();
      } catch( Exception e ) {
         e.printStackTrace();
      }
      return idquery;
   }

   public int findQuery( String query ) {

      query = query.trim();
      int idquery = -1;

      // Open connection to Database && Run query.
      try {
         
         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null )
            return idquery;
         
         System.out.println( SUCCESS );
         System.out.println( DIVIDER + "Find query if exists" + DIVIDER );

         // SQL Query to find an idquery for a given query.
         String sqlQuery = "select `id` from `queries` where `text` = ?;";
         PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.setString( 1, query );

         // Execute query & parse result.
         ResultSet result = statement.executeQuery();

         // If idquery is matched, save it.
         if( result.next() ) {
            idquery = result.getInt( 1 );
         }
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return idquery;
   }
}

/*
package buscador;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Connect implements Constants {

   private static Connect sConnect;

   // Only allow the class from being accesed through a 
   // static method, and not from a constructor.
   private Connect() {
      
      try {
         // Enable driver.
         Class.forName( DRIVER ).newInstance();
      } catch( Exception e ) {
         e.printStackTrace();
      }
   }

   // Get an instance of connect.
   public static Connect getInstance() {

      if( sConnect == null )
         sConnect = new Connect();
      return sConnect;
   }

   public boolean checkConnection() {

      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null)
            return false;
         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return true;
   }

   // Given an idquery, attempt to find the query
   public String getQuery( int idquery ) {

      // Initialize query to null to return in case of mistake
      String query = null;

      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null)
            return null;

         System.out.println( SUCCESS );
         System.out.println( DIVIDER + "Retrieve query" + DIVIDER );

         // SQL Query to find text for given idquery.
         final String sqlQuery = "select `text` from `queries` where `id` = ?;";
         final PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.setInt( 1, idquery );

         // Execute query & parse result.
         final ResultSet result = statement.executeQuery();

         // If query exists for an id, save its text;
         // Otherwise, return empty text.
         if( result.next() )
            query = result.getString( 1 );
         else
            query = "";
         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return query;
   }

   // Given an array of terms and an specific iddoc,
   // find the dfs for all given terms. 
   public int[] getDFs( int iddoc, String[] terms ) {
      
      int[] dfs = new int[0];

      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null )
            return new int[0];

         System.out.println( SUCCESS );
         System.out.println(
            DIVIDER +
            "Consult df for terms in specific document" +
            DIVIDER
         );

         dfs = new int[terms.length];

         // Iterate over each term, and find its df for an iddoc
         for( int i = 0; i < terms.length; i++ ) {

            // Remove all punctuation and whitespaces in the term
            terms[i] = terms[i].trim().replaceAll("\\p{Punct}+", "");
            
            // SQL Query to check the Term Frequency in a given document
            // for a given term.
            final String sqlQuery =
               "select `tf` " +
               "from `contains` " +
               "where `iddoc` = ? " +
               "and `term` = ?;";

            final PreparedStatement statement = connection.prepareStatement( sqlQuery );
            statement.setInt( 1, iddoc ); 
            statement.setString( 2, terms[i] );

            // Execute query & parse result.
            final ResultSet result = statement.executeQuery();

            // Get df if there is a result.
            dfs[i] = result.next() ? result.getInt( 1 ) : 0;
         }
         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return dfs;
   }

   // Return a list of all documents with a similarity
   // in order descending given an idquery.
   public List<Similar> getSimilarity(int idquery) {
    
      List<Similar> similars = null;
      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null )
            return null;
         

         System.out.println( SUCCESS );
         System.out.println(
            DIVIDER +
            "Use dot product to get similarity for query: #" +
            idquery +
            DIVIDER
         );

         similars = new ArrayList<>();

         // SQL Query to determine similarity using Vector Space Model.
         final String sqlQuery = 
            "select `contains`.`iddoc`, " +
            "sum(`contains`.`tf` * `terms`.`idf` * `made`.`tf` * `terms`.`idf`) as `similar` " +
            "from `contains`, `made`, `terms` " +
            "where `contains`.`term` = `terms`.`term` " +
            "and `made`.`term` = `terms`.`term` " +
            "and `made`.`idquery` = ? " +
            "group by `contains`.`iddoc` " +
            "order by `similar` desc;"
         ;
         final PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.setInt( 1, idquery );

         // Execute query & parse result.
         final ResultSet result = statement.executeQuery();

         // Add all results to list
         while( result.next() ) {

            // Save into custom class for later retrieval,
            final Similar similar = new Similar(
               result.getInt( 1 ),
               result.getDouble( 2 )
            );
            // Add to list for a more permanent storage.
            similars.add( similar );
         }
         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return similars;
   }

   // Return a list of all iddoc that are relevant
   // for a given idquery.
   public List<Integer> getRelevants( int idquery ) {
     
      List<Integer> relevants = null;
      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null )
            return null;
         
         System.out.println( SUCCESS );
         System.out.println(
            DIVIDER +
            "Find all relevant documents for query: #" +
            idquery +
            DIVIDER
         );

         // SQL Query to find all relevant documents for an idquery.
         final String sqlQuery = "select `iddoc` from `relevant` where `idquery` = ?;";
         final PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.setInt( 1, idquery );

         // Execute query & parse result.
         final ResultSet result = statement.executeQuery();
         final List<Integer> relevants = new ArrayList<>();

         // Add to list all relevant documents.
         while( result.next() ) 
            relevants.add( result.getInt( 1 ) );

         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return relevants;
   }

   // 
   public int addQuery( String query ) {

      query = query.trim();
      int idquery = -1;

      // Open connection to Database && Run query.
      try {
         
         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null )
            return idquery;
         
         System.out.println( SUCCESS );
         System.out.println( DIVIDER + "Add query if not exists" + DIVIDER );

         // SQL Query to check if a query allready exists in db.
         String sqlQuery = "select `id` from `queries` where `text` = ?;";
         PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.setString( 1, query );

         // Execute query & parse result.
         ResultSet result = statement.executeQuery();

         // Check if query exists, otherwise, add it to db.
         if( result.next() ) {

            idquery = result.getInt( 1 );
         } else {

            // SQL Query to add a new query to db.
            sqlQuery = "insert into `queries` (`text`) values(?);";
            statement = connection.prepareStatement( sqlQuery );
            statement.setString( 1, query );

            // Execute query
            statement.executeQuery();
   
            // SQL Query to find an idquery for a given query.
            sqlQuery = "select `id` from `queries` where `text` = ?;";
            statement = connection.prepareStatement( sqlQuery );
            statement.setString( 1, query );

            // Execute query & parse result.
            result = statement.executeQuery();

            // Get the idquery
            if( result.next() )
               idquery = result.getInt( 1 );
            else
               throw new SQLException( "Added query does not exist" );
   
            // Split the query into words
            final String[] words = query.split( "\\s+" );
            final Map<String, Integer> map = new HashMap<>();

            // Iterate over all the words to find df for each word.
            for( String word: words ) {
       
               // Remove all punctuation and whitespaces in a word
               word = word.trim().replaceAll( "\\p{Punct}+", "" );

               // If word exits in hash, add one to count, otherwise initiliaze at 1,
               final int count = ( map.containsKey( word ) ? map.get( word ) : 0 ) + 1;
               // and save it into list.
               map.put( word, count );
            }
       
            // For each word, add to db, along with its tf and idquery.
            map.forEach(
               ( word, count ) -> {
                  try {

                     // SQL Query to insert a word into terms, ignoring duplicates.
                     String sql_query = "insert ignore into `terms` (`term`) values(?);";
                     PreparedStatement prepared = connection.prepareStatement( sql_query );
                     prepared.setString( 1, word );

                     // Execute query
                     prepared.execute();
      
                     // SQL Query to add into made the word, tf, and idquery.
                     sql_query = "insert into `made` (`idquery`, `term`, `tf`) values (?, ?, ?);";
                     prepared = connection.prepareStatement( sql_query );
                     prepared.setInt( 1, idquery );
                     prepared.setString( 2, word );
                     prepared.setInt( 3, count );
                     
                     // Execute query
                     prepared.execute();
                  } catch( Exception e ) {
                     e.printStackTrace();
                  }
               }
            );
         }
         connection.close();
      } catch( Exception e ) {
         e.printStackTrace();
      }
      return idquery;
   }

   public int findQuery( String query ) {

      query = query.trim();
      int idquery = -1;

      // Open connection to Database && Run query.
      try {
         
         // Open connection,
         final Connection connection = DriverManager.getConnection( DB );

         // Check if succesful.
         if( connection == null )
            return idquery;
         
         System.out.println( SUCCESS );
         System.out.println( DIVIDER + "Find query if exists" + DIVIDER );

         // SQL Query to find an idquery for a given query.
         String sqlQuery = "select `id` from `queries` where `text` = ?;";
         PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.setString( 1, query );

         // Execute query & parse result.
         ResultSet result = statement.executeQuery();

         // If idquery is matched, save it.
         if( result.next() ) {
            idquery = result.getInt( 1 );
         }
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return idquery;
   }
}



update `documents` set `weight` = 1;
update `queries` set `weight` = 1;

update `documents` set `weight` = (select sqrt(sum(`contains`.`tf`*`terms`.`idf`*`contains`.`tf`*`terms`.`idf`)) from `contains`, `terms` where `documents`.`id` = `contains`.`iddoc` and `contains`.`term` = `terms`.`term`);

update `queries` set `weight` = (select sqrt(sum(`made`.`tf`*`terms`.`idf`*`made`.`tf`*`terms`.`idf`)) from `made`, `terms` where `queries`.`id` = `made`.`idquery` and `made`.`term` = `terms`.`term`);

select `contains`.`iddoc`, sum(`contains`.`tf` * `terms`.`idf` * `made`.`tf` * `terms`.`idf`) as `similar` from `contains`, `made`, `terms` where `contains`.`term` = `terms`.`term` and `made`.`term` = `terms`.`term` and `made`.`idquery` = 1 group by `contains`.`iddoc` order by `similar` desc;

select `contains`.`iddoc`, sum(`contains`.`tf` * `terms`.`idf` * `made`.`tf` * `terms`.`idf`) / (`documents`.`weight` * `queries`.`weight`) as `similar` from `documents`, `queries`, `contains`, `made`, `terms` where `documents`.`id` = `contains`.`iddoc` and `contains`.`term` = `terms`.`term` and `queries`.`id` = `made`.`idquery` and `made`.`term` = `terms`.`term` and `made`.`idquery` = 1 group by `contains`.`iddoc` order by `similar` desc;



select `queries`.`id`, sum(`contains`.`tf` * `terms`.`idf` * `made`.`tf` * `terms`.`idf`) / (`documents`.`weight` * `queries`.`weight`) as `similar` from `queries`, `documents`, `contains`, `made`, `terms` where `documents`.`id` = `contains`.`iddoc` and `contains`.`term` = `terms`.`term` and `made`.`term` = `terms`.`term` and `made`.`idquery` = `queries`.`id` and `queries`.`id` = 1 group by `queries`.`id` order by `similar` desc;
*/