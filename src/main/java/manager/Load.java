package manager;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Load extends Connect {
  
   private final static String TERMSTXT =
      PATH + "loader/term-vocab.txt";
   private final static String DOCUMENTSTXT =
      PATH + "loader/doc-text.txt";
   private final static String DOCUMENTS_CLUSTER_TXT =
      PATH + "manager/cluster_docs.txt";
   private final static String DOCUMENTS_WEB_TXT =
      PATH + "manager/docs-web.txt";
   private final static String QUERIESTXT =
      PATH + "loader/query-text.txt";
   private final static String CONTAINSTXT =
      PATH + "loader/doc-vecs.txt";
   private final static String MADETXT =
      PATH + "loader/queries-vec.txt";
   private final static String RELEVANTTXT =
      PATH + "loader/rlv-ass.txt";

   private static Load sLoad;
    
   Load() {
      super();
   }

   // Get an instance of connect.
   public static Load getInstance() {

      if( sLoad == null )
         sLoad = new Load();
      return sLoad;
   }

   public void cleanDB() {
      

      try {
         // Open connection,
         Connection connection = DriverManager.getConnection( DB , USER , PASSWORD );

         // Check if succesful.
         if( connection == null )
         return;
         
         System.out.println( SUCCESS );
         System.out.println( DIVIDER + "Cleaning Data in DB" + DIVIDER );

         final String[] queries = new String[6];
         queries[0] = "delete from `relevant`;";
         queries[1] = "delete from `contains`;";
         queries[2] = "delete from `made`;";
         queries[3] = "delete from `documents`;";
         queries[4] = "delete from `terms`;";
         queries[5] = "delete from `queries`;";
         
         for(String sqlQuery : queries) {

         final PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.execute();
         }
         connection.close();
      } catch( Exception e ) {
         e.printStackTrace();
      }
   }

   public void loadDocuments(boolean addTerms) {
       
       
      
      File file;
      FileReader input;
      LineNumberReader reader;
      Connection connection;
      List<String> list;
      String string;
      int j;

      try {

         // Open connection,
         connection = DriverManager.getConnection( DB , USER , PASSWORD );

         // Check if succesful.
         if( connection == null )
         return;

         System.out.println( SUCCESS );
         System.out.println( DIVIDER + "Adding Documents to DB" + DIVIDER );

         file = new File( DOCUMENTS_WEB_TXT );
         input = new FileReader( file );
         reader = new LineNumberReader( input );
         list = reader.lines().collect(Collectors.toCollection(ArrayList::new));

         int contador = 0;
         
         for(int i = 0; i < list.size(); i++) {

         string = list.get(i);
         list.remove(i);
         contador++;
         if(string.matches("^-?\\d+$")) {

            string += "#div";
            

            while(!string.contains("   /") ) {

                if(list.get(i).contains("UURRLL->"))
                {
                     string += "#div" + list.get(i);
                }
                else
                {
                    string += " " + list.get(i);
                }

               
               list.remove(i);
            }
            string = string.substring(0, string.length() - 1).trim();
            list.add(i, string);
         } else
            i++;
         }
         for(String line: list) {
         System.out.println(line);
         final String[] split = line.split("#div");
         final int id = Integer.parseInt( split[0].trim() );
         System.out.println(id);
         split[1] = split[1].trim();
         
         split[2] = split[2].trim();
         split[2] = split[2].replace("UURRLL->", "");
         
         System.out.println(split[1]);
         System.out.println(split[2]);

         System.out.println(id);

         final String sqlQuery = "insert into `documents_cluster` (`id`, `text`, `url`) values(?, ?,?);";
         try {

            final PreparedStatement statement = connection.prepareStatement( sqlQuery );
            statement.setInt(
               1,
               id
            );
            statement.setString(
               2,
               split[1]
            );
            
            statement.setString(
               3,
               split[2]
            );
            statement.execute();
            statement.close();
         } catch( Exception e ) {
            e.printStackTrace();
         }

         if( addTerms ) {

            addTerms(
               connection,
               id,
               split[1],
               true
            );
         }
         }

         reader.close();
         input.close();
         connection.close();
      } catch( Exception e ) {
         e.printStackTrace();
      }
   }

   public void loadQueries(boolean addTerms) {

      File file;
      FileReader input;
      LineNumberReader reader;
      Stream<String> stream;
      Connection connection;

      try {
         
         // Open connection,
         connection = DriverManager.getConnection( DB , USER , PASSWORD );

         // Check if succesful.
         if( connection == null )
         return;

         System.out.println( SUCCESS );
         System.out.println(
         DIVIDER +
         "Adding default queries to DB" +
         DIVIDER
         );

         file = new File( QUERIESTXT );
         input = new FileReader( file );
         reader = new LineNumberReader( input );
         stream = reader.lines();
         chunked(stream, 3)
         .forEach(
         string -> {

            final String sqlQuery = "insert into `queries` (`id`, `text`) values(?, ?);";
            final int id = Integer.parseInt( string.get(0).trim() );
            final String text = string.get(1).trim().toLowerCase();
            try {

               final PreparedStatement statement = connection.prepareStatement( sqlQuery );
               statement.setInt(
               1,
               id
               );
               statement.setString(
               2,
               text
               );
               statement.execute();
               statement.close();
            } catch(SQLException e) {
               e.printStackTrace();
            }
            if( addTerms ) {

               addTerms(
               connection,
               id,
               text,
               false
               );
            }
         }
         );
         stream.close();
         reader.close();
         input.close();
         connection.close();
      } catch( Exception e ) {
         e.printStackTrace();
      }
   }

   private void addTerms(
      Connection connection,
      int id,
      String text,
      boolean isDocument
   ) {
      
      final String[] words = text.split("\\s+");
      final Map<String, Integer> map = new HashMap<>();
      for(String word: words) {

         word = word.trim().replaceAll("\\p{Punct}+", "");
         final int count = ( map.containsKey( word ) ? map.get( word ) : 0 ) + 1;
         map.put(word, count);
      }

      map.forEach(
         (word, count) -> { 
         try {
            if( count > 1 )
               System.out.println("'" + word + "' has tf " + count  + " in doc " + id );

            String sqlQuery = "insert ignore into `terms_cluster` (`term`) values(?);";
            PreparedStatement statement = connection.prepareStatement( sqlQuery );
            statement.setString(
               1,
               word
            );
            statement.execute();

            if( isDocument )
               sqlQuery = "insert into `contains_cluster` (`iddoc`, `term`, `tf`) values (?, ?, ?);";
            else
               sqlQuery = "insert into `made` (`idquery`, `term`, `tf`, `tf1`) values (?, ?, ?, ?);";

            statement = connection.prepareStatement( sqlQuery );
            statement.setInt(
               1,
               id
            );
            statement.setString(
               2,
               word
            );
            statement.setInt(
               3,
               count
            );
            if( isDocument ) {
              statement.setInt(
                4,
                count
              );
            }
            statement.execute();
            statement.close();
         } catch( Exception e ) {
            e.printStackTrace();
         }
         }
      );

   }

   private void loadTerms() {
      File file;
      FileReader input;
      LineNumberReader reader;
      Stream<String> stream;
      Connection connection;

      try {
         
         // Open connection,
         connection = DriverManager.getConnection( DB , USER , PASSWORD );

         // Check if succesful.
         if( connection == null )
         return;

         System.out.println( SUCCESS );
         System.out.println( DIVIDER + "Adding Terms to DB" + DIVIDER );

         file = new File( TERMSTXT );
         input = new FileReader( file );
         reader = new LineNumberReader( input );
         stream = reader.lines();
         stream.forEach(
         string -> {
            final String[] split = string.split("\\s+");
            if (split.length < 2)
               return;
            final String sqlQuery = "insert into `terms` (`id`, `term`) values(?, ?);";
            try {
               final PreparedStatement statement = connection.prepareStatement( sqlQuery );
               statement.setInt(
               1,
               Integer.parseInt( split[1].trim() )
               );
               statement.setString(
               2,
               split[2].trim().toLowerCase()
               );
               statement.execute();
               statement.close();
            } catch( Exception e ) {
               e.printStackTrace();
            }
         }
         );
         stream.close();
         reader.close();
         input.close();
         connection.close();
         
      } catch( Exception e ) {
         e.printStackTrace();
      }
   }

   private void loadContains() {
      
      File file;
      FileReader input;
      LineNumberReader reader;
      Connection connection;
      List<String> list;
      String string;
      int j;

      try {
         
         // Open connection,
         connection = DriverManager.getConnection( DB , USER , PASSWORD );

         // Check if succesful.
         if( connection == null )
         return;

         System.out.println( SUCCESS );
         System.out.println(
         DIVIDER +
         "Adding relation between terms and documents" +
         DIVIDER
         );

         file = new File( CONTAINSTXT );
         input = new FileReader( file );
         reader = new LineNumberReader( input );
         list = reader.lines().collect(Collectors.toCollection(ArrayList::new));

         for(int i = 0; i < list.size() - 1; i++) {
         string = list.get(i);
         list.remove(i);
         while(!string.contains("/") ) {
            string += " " + list.get(i);
            list.remove(i);
         }
         string = string.substring(0, string.length() - 1).trim();
         list.add(i, string);
         }
         for(String line: list) {

         System.out.println(line);
         /*
         final String[] split = line.split(":");

         final String[] words = split[1].split("\\s+");
         for(String word: words) {
            final String sqlQuery = "insert into `documents` (`id`, `text`) values(?, ?);";
            try {
               final PreparedStatement statement = connection.prepareStatement( sqlQuery );
               statement.setInt(
               1,
               Integer.parseInt( split[0] )
               );
               statement.setString(
               2,
               split[1].trim()
               );
               statement.execute();
               statement.close();
            } catch(SQLException e) {
               e.printStackTrace();
            }
         }
         */
         }

         reader.close();
         input.close();
         connection.close();
      } catch( Exception e) {
         e.printStackTrace();
      }
   }

   public void loadRelevant() {
      
      File file;
      FileReader input;
      LineNumberReader reader;
      Connection connection;
      List<String> list;
      String string;

      try {
         
         // Open connection,
         connection = DriverManager.getConnection( DB , USER , PASSWORD );

         // Check if succesful.
         if( connection == null )
         return;

         System.out.println( SUCCESS );
         System.out.println( 
         DIVIDER +
         "Adding relation between documents and queries" +
         DIVIDER
         );

         file = new File( RELEVANTTXT );
         input = new FileReader( file );
         reader = new LineNumberReader( input );
         list = reader.lines().collect(Collectors.toCollection(ArrayList::new));

         for(int i = 0; i < list.size() - 1; i++) {
         string = list.get(i);
         list.remove(i);
         string += ":";
         while(!string.contains("/") ) {
            string += " " + list.get(i);
            list.remove(i);
         }
         string = string.substring(0, string.length() - 1).trim();
         list.add(i, string);
         }
         for(String line: list) {

         //System.out.println("\"" + line + "\"");
         
         final String[] split = line.split(":");

         final String[] words = split[1].trim().split("\\s+");
         for(String word: words) {
            //System.out.println("\"" + word + "\"");

            final String sqlQuery = "insert into `relevant` (`iddoc`,`idquery`) values (?, ?);";
            try {
               final PreparedStatement statement = connection.prepareStatement( sqlQuery );
               statement.setInt(
               1,
               Integer.parseInt( word.trim() )
               );
               statement.setInt(
               2,
               Integer.parseInt( split[0].trim() )
               );
               statement.execute();
               statement.close();
            } catch( Exception e ) {
               e.printStackTrace();
            }
         }
         }

         reader.close();
         input.close();
         connection.close();
      } catch( Exception e ) {
         e.printStackTrace();
      }
   }

   public void addDF() {
   
      Connection connection;

      try {
      
         // Open connection,
         connection = DriverManager.getConnection( DB , USER , PASSWORD );

         // Check if succesful.
         if( connection == null )
         return;

         System.out.println( "SUCCESS: Connected to DB" );
         System.out.println( "Adding df to terms" );

         final String sqlQuery = "update `terms_cluster` set `df` = (" +
         "select count(`tf`) as `df` from `contains_cluster` " +
         "where `terms_cluster`.`term` = `term` " +
         "group by `term`);";
         final PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.execute();

         connection.close();
      } catch( Exception e ) {
         e.printStackTrace();
      }
   }
   
   public void addIDF() {
      
      Connection connection;

      try {
      
         // Open connection,
         connection = DriverManager.getConnection( DB , USER , PASSWORD );

         // Check if succesful.
         if( connection == null )
         return;

         System.out.println( SUCCESS );
         System.out.println( DIVIDER + "Adding idf to terms" + DIVIDER );

         String sqlQuery = "CREATE TEMPORARY TABLE IF NOT EXISTS `temp` (" +
         "`term` varchar(500) NOT NULL, " +
         "`idf` double(20,15) unsigned DEFAULT NULL, " +
         "PRIMARY KEY (`term`) " +
         ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
         PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.execute();

         sqlQuery = "insert into `temp`(`term`, `idf`) " +
         "select `terms_cluster`.`term`, " +
         "log10( (select count(`text`) from `documents_cluster`) / `terms_cluster`.`df`) " +
         "from `terms_cluster` " +
         "where `terms_cluster`.`df` > 0;";
         statement = connection.prepareStatement( sqlQuery );
         statement.execute();

         sqlQuery = "update `terms_cluster` set `idf` = (" +
         "select `idf` from `temp` where `terms_cluster`.`term` = `term`" +
         ");";
         statement = connection.prepareStatement( sqlQuery );
         statement.execute();

         connection.close();
      } catch( Exception e ) {
         e.printStackTrace();
      }
   }

   

   public <T> Stream<List<T>> chunked(Stream<T> stream, int chunkSize) {
      
      if( chunkSize < 1 ) throw new IllegalArgumentException("chunkSize==" + chunkSize);
      if( chunkSize == 1 ) return stream.map(Collections::singletonList);
      Spliterator<T> src = stream.spliterator();
      long size = src.estimateSize();
      if( size != Long.MAX_VALUE )
         size = ( size + chunkSize - 1) /chunkSize;
      int ch = src.characteristics();
      ch &= Spliterator.SIZED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.IMMUTABLE;
      ch |= Spliterator.NONNULL;
      return StreamSupport.stream(
         new Spliterators.AbstractSpliterator<List<T>>(size, ch) {
         
            private List<T> current;

            @Override
            public boolean tryAdvance(Consumer<? super List<T>> action) {

               if( current == null ) current = new ArrayList<>(chunkSize);
               while( current.size() < chunkSize && src.tryAdvance(current::add) );
               if( !current.isEmpty() ) {

                  action.accept( current );
                  current = null;
                  return true;
               }
               return false;
            }
         },
         stream.isParallel()
      );
   }

  /*
  update `documents` set `weight` = (select sqrt(sum(`contains`.`tf`*`terms`.`idf`*`contains`.`tf`*`terms`.`idf`)) from `contains`, `terms` where `documents`.`id` = `contains`.`iddoc` and `contains`.`term` = `terms`.`term`);

  update `queries` set `weight` = (select sqrt(sum(`made`.`tf`*`terms`.`idf`*`made`.`tf`*`terms`.`idf`)) from `made`, `terms` where `queries`.`id` = `made`.`idquery` and `made`.`term` = `terms`.`term`);
  */
}