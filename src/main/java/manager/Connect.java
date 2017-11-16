package manager;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mysql.jdbc.Driver;

import seeker.Similar;

public class Connect implements Constants  {

   private static Connect sConnect;

   // Only allow the class from being accesed through a 
   // static method, and not from a xconstructor.
   Connect() {

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
         final Connection connection = DriverManager.getConnection( DB , USER , PASSWORD );

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
    
      //final int default_queries = 2;
      final int default_queries = 111;
      final String[] queries = new String[default_queries + 1];
      int i = 0;
      queries[i++] = DIVIDER + "Seleccionar" + DIVIDER;

      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB , USER , PASSWORD );

         // Check if succesful.
         if( connection == null)
            return null;

         System.out.println( SUCCESS );
         System.out.println( DIVIDER + "Retrieving all queries" + DIVIDER );

         // SQL Query to find text for given idquery.
         //final String sqlQuery = "select `text` from `queries` where `id` <= ?;";
         final String sqlQuery = "select `text` from `queries` where `id` >= ? and `id` <= ?;";
         final PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.setInt( 1, default_queries );
         statement.setInt( 2, default_queries + 2 );

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
         final Connection connection = DriverManager.getConnection( DB , USER , PASSWORD );

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
         final Connection connection = DriverManager.getConnection( DB , USER , PASSWORD );

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
   
   public List<Similar> getSimilarity(int idquery) {
    
      System.out.println("getSimilarity");
       
      List<Similar> similars = null;
      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB , USER , PASSWORD );

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
            "select `contains_web`.`iddoc`, " +
            "sum(`contains_web`.`tf` * `terms_web`.`idf` * `made`.`tf1` * `terms_web`.`idf`) as `similar`"   +
            "from `contains_web`, `made`, `terms_web` " +
            "where `made`.`idquery` = ? " +
            "and `made`.`term` = `terms_web`.`term` " +
            "and `contains_web`.`term` = `terms_web`.`term` " +
            
            "group by `contains_web`.`iddoc` " +
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
            
            //IMPRIMIR TEXTO DE DOCUMENTOS
            PreparedStatement pst_doc_text = connection.prepareStatement("SELECT `text`, `url` FROM documents_web where id = ? limit 1;");
            pst_doc_text.setInt(1, result.getInt(1));
            ResultSet result_doc_tex = pst_doc_text.executeQuery();          
            if( result_doc_tex.next() ){
                System.out.println("--------  documento: " +  result.getInt(1) + "------------");
                System.out.println(result_doc_tex.getString(1));
                System.out.println(result_doc_tex.getString(2));
                
            }
            similar.setText(result_doc_tex.getString(1));
            similar.setURL(result_doc_tex.getString(2));
            
            
            similars.add( similar );
         }
         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return similars;
   }

   

   // Return a list of all documents with a similarity
   // in order descending given an idquery.
   public List<Similar> getSimilarityClusterQuery(int idquery) {
    
      System.out.println("getSimilarity");
       
      List<Similar> similars = null;
      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB , USER , PASSWORD );

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
         
         

         //reiniciar las 2 tablas
         PreparedStatement statement_truncate_table = connection.prepareStatement("truncate temporal_cluster;" );     
         statement_truncate_table.execute();
         statement_truncate_table = connection.prepareStatement("create TEMPORARY table IF NOT EXISTS temp_table (doc int(8) not null) ENGINE=InnoDB DEFAULT CHARSET=utf8;" );     
         statement_truncate_table.execute();
           

         
         
         // SQL Query to determine similarity using Vector Space Model.
         final String sqlQuery = 
            "select `contains_cluster`.`iddoc`, " +
            "sum(`contains_cluster`.`tf` * `terms_cluster`.`idf` * `made`.`tf1` * `terms_cluster`.`idf`) as `similar`"   +
            "from `contains_cluster`, `made`, `terms_cluster` " +
            "where `made`.`idquery` = ? " +
            "and `made`.`term` = `terms_cluster`.`term` " +
            "and `contains_cluster`.`term` = `terms_cluster`.`term` " +
            
            "group by `contains_cluster`.`iddoc` " +
            "order by `similar` desc limit 1;"
         ;
         

         final PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.setInt( 1, idquery );

         // Execute query & parse result.
         int doc_mayor = 0;
         
         ResultSet result = statement.executeQuery();

         if(result.next())
         {
            //obtiene el documento con mayor silimaridad.
            doc_mayor = result.getInt( 1 );
                   
            PreparedStatement pst = connection.prepareStatement("select `dad` from groups_cluster where `group` = ? ;");
            pst.setInt( 1, doc_mayor );
            ResultSet result3 = pst.executeQuery();

            //obtiene el papa del documento mas similar
            if(result3.next())
            {
                doc_mayor = result3.getInt( 1 );
            }
            
            System.out.println("Grupo relevante: " + doc_mayor);
         }
        

        //aplica el procedure que te mande para llenar la tabla temporal_cluster donde estarán
        //los documentos obtenidos conforme a la similitud y los clusters
         CallableStatement cstmt = connection.prepareCall("{ CALL get_all_path(?) }");
         cstmt.setInt(1, doc_mayor);
         ResultSet result2 = cstmt.executeQuery();
              
         //obtiene esos documentos ya ingresados en dicha tabla
         PreparedStatement pst = connection.prepareStatement("SELECT * FROM temporal_cluster;");
         result2 = pst.executeQuery();
              
         // Add all results to list
         while( result2.next() ) {

            // Save into custom class for later retrieval.
            final Similar similar = new Similar(
               result2.getInt(1), 0   //sa similtud le puse 0 porque solo regresa los documentos.
            );
            // Add to list for a more permanent storage.
            //similars.add( similar );
            

            
            similars.add( similar );

         }

         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return similars;
   }
   
   
   public List<Similar> getSimilarityToCluster(int idquery) {
    
      System.out.println("getSimilarity to CLUSTER!!");
       
      List<Similar> similars = null;
      
      //QUE DOCUMENTOS ESTAN Y SIGUEN SIN GRUPO
      ArrayList<Integer> documentos_cluster = new ArrayList<Integer>();
      
       for (int i = 1; i <= 15; i++) {
           documentos_cluster.add(i);
       }
      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB , USER , PASSWORD );

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
            "select `contains_cluster`.`iddoc`, " +
            "sum(`contains_cluster`.`tf` * `terms_cluster`.`idf` * `contains_cluster2`.`tf` * `terms_cluster`.`idf`) as `similar`"   +
            "from `contains_cluster`, `contains_cluster2`, `terms_cluster` " +
            "where `contains_cluster2`.`iddoc` = ? " +
            "and `contains_cluster2`.`term` = `terms_cluster`.`term` " +
            "and `contains_cluster`.`term` = `terms_cluster`.`term` " +
            
            "group by `contains_cluster`.`iddoc` " +
            "order by `contains_cluster`.`iddoc`;"
         ;
         
         //MATRIZ DE SIMILITUD 
         
         
         //iterar por el numero de documentos

         
         //mientras haya algun documento que no haya sido evaluado, esto seguira.
         while(documentos_cluster.size() >= 1) 
         {
             //matriz de similitud
             double [][] matriz_de_simlitud = new double[16][16]; 
             
             System.out.println("!!!!!!!!! NUEVO CICLO !!!!!!!!!");
             for( Integer i:documentos_cluster)
             {
                 System.out.println(i);
             }
         
             //CALCULAR LA SIMILITUD DE UN DOCUMENTOS CONTRA TODOS. PARA HACER LA MATRIZ COMPLETA 15x15
            for (int i = 1; i <= 15; i++) {

               final PreparedStatement statement = connection.prepareStatement( sqlQuery );
               statement.setInt( 1, i );

               // Execute query & parse result.
               final ResultSet result = statement.executeQuery();

               // Add all results to list

               int columna = 1;

               while( result.next() ) {

                  // Save into custom class for later retrieval,
                  final Similar similar = new Similar(
                     result.getInt( 1 ), //iddoc
                     result.getDouble( 2 ) //similitud
                  );
                  // Agregar similitud a matriz de similitud
                  matriz_de_simlitud[i][columna] = similar.getSimilarity();

                  columna +=1;
               }

             }

             //VEREMOS CUAL DE TODOS LOS PARES TIENE MAYOR SIMILITUD
             double masSimilitud = 0;
             int doc1 = 0;
             int doc2 = 0;
             for (int i = 1; i <= 15; i++) {
                 if(!documentos_cluster.contains(i))
                     {
                         continue;
                     }

                 for (int j = 1; j <= 15; j++) {

                     if(!documentos_cluster.contains(j))
                     {
                         continue;
                     }

                     if(matriz_de_simlitud[i][j] > masSimilitud && i!=j)
                     {
                         //cuando encuentra una similitud grande, guarda esos documentos y su simiitud
                         masSimilitud = matriz_de_simlitud[i][j];   
                         doc1 = i;     
                         doc2 = j;
                     }

                     //System.out.print(matriz_de_simlitud[i][j] + " ");
                     //System.out.println();
                 }
                 System.out.println("-------");

             }

             //checar cual ya estaba en grupo
             int grupo_doc1 = 0;
             int eliminar1 = 0;
             int grupo_doc2 = 0;
             int eliminar2 = 0;

             //Doc1
             
             PreparedStatement pst = connection.prepareStatement("CALL get_last_group(?);");
               pst.setInt( 1, doc1 );
               ResultSet result = pst.executeQuery();

               if(result.next())
               {
                   grupo_doc1= result.getInt(1);
                   eliminar1 = result.getInt(2);
               }
               
               pst = connection.prepareStatement("CALL get_last_group_bottom_up(?);");
               pst.setInt( 1, eliminar1 );
               result = pst.executeQuery();

               if(result.next())
               {
                   //cual doc se eliminara de la lista de documentos para no tomarlo en cuenta para la sig iteracion
                   eliminar1 = result.getInt(1);
               }
               
               

               //Doc 2
              pst = connection.prepareStatement("CALL get_last_group(?);");
               pst.setInt( 1, doc2 );
               result = pst.executeQuery();

               if(result.next())
               {
                   grupo_doc2= result.getInt(1);
                   eliminar2 = result.getInt(2);
               }
               
               pst = connection.prepareStatement("CALL get_last_group_bottom_up(?);");
               pst.setInt( 1, eliminar2 );
               result = pst.executeQuery();

               if(result.next())
               {
                   //cual doc se eliminara de la lista de documentos para no tomarlo en cuenta para la sig iteracion
                   eliminar2 = result.getInt(1);
               }


            //INSERTAR EL NUEVO GRUPO   
             pst = connection.prepareStatement("insert into `groups_cluster` (`idgroup1`,`idgroup2`) values (?, ?);");

             if(grupo_doc1 >= grupo_doc2)
             {
                 System.out.println( grupo_doc1 + " > " + grupo_doc2);
                 pst.setInt( 1, grupo_doc2 );
                 pst.setInt( 2, grupo_doc1 );
                 
                 //remueve el doc de la lista de documentos_cluster
                 documentos_cluster.remove(new Integer(eliminar1));
             }
             else{
                 System.out.println( grupo_doc2 + " > " + grupo_doc1);
                 pst.setInt( 1, grupo_doc1 );
                 pst.setInt( 2, grupo_doc2 );
                 
                 //remueve el doc de la lista de documentos_cluster
                 documentos_cluster.remove(new Integer(eliminar2));
             }

               pst.execute();
               
               int papa = 0;
               
               //OBTENER EL PAPA
               pst = connection.prepareStatement("select max(`group`) from groups_cluster;");
               result = pst.executeQuery();
               System.out.println("!!!!!!!! " + papa + "!!!!!!!!1" );
               System.out.println("!!!!!!!! " + grupo_doc1 + "!!!!!!!!1" );
               System.out.println("!!!!!!!! " + grupo_doc2 + "!!!!!!!!1" );
               
               if(result.next())
               {
                   papa = result.getInt(1);
               }
               
               
               //AGREGAR PAPA A DOCUMENTO 1
               pst = connection.prepareStatement("UPDATE groups_cluster\n" +
                                                 "SET dad = ? \n" +
                                                 "WHERE  `group` = ? ;");
               pst.setInt( 1, papa );
               pst.setInt( 2, grupo_doc1 );
               pst.execute();
               
               //AGREGAR PAPA A DOCUMENTO 2
               pst = connection.prepareStatement("UPDATE groups_cluster\n" +
                                                 "SET dad = ? \n" +
                                                 "WHERE  `group` = ? ;");
               pst.setInt( 1, papa );
               pst.setInt( 2, grupo_doc2 );
               pst.execute();


               
               
               
               
          
         }
          
         //////////
         
         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return similars;
   }


   // Return a list of all documents with a similarity
   // in order descending for a given idquery
   // after calculating query1 using Rocchio weights.
   public List<Similar> getSimilarityQ1( int idquery, List<Similar> similars ) {
      
      System.out.println("getSimilarityQ1");
       
      List<Similar> similarsQ1 = null;
      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB , USER , PASSWORD );

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
         List<Similar> n1 = similars.subList( 0, N1 );
         List<Similar> n2 = similars.subList( similars.size() - N2, similars.size() );
         
         // Drop table `n1` if exists.
         String sqlQuery = "truncate n1;";
         PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.executeQuery();

          /*
         // Add table `n1` if not exists.
         sqlQuery = "CREATE TEMPORARY TABLE IF NOT EXISTS `n1` (" +
            "`iddoc` int(8) unsigned NOT NULL, " +
            "PRIMARY KEY (`iddoc`) " +
         ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
         statement = connection.prepareStatement( sqlQuery );
         statement.executeQuery();
         
         */

         for( Similar s : n1 ) {
            sqlQuery = "insert into `n1` (`iddoc`) values(?);";
            statement = connection.prepareStatement( sqlQuery );
            statement.setInt( 1, s.getId() );
            statement.executeQuery();
         }

         // Drop table `n2` if exists.
         sqlQuery = "truncate n2;";
         statement = connection.prepareStatement( sqlQuery );
         statement.executeQuery();

          /*
         // Add table `n1` if not exists.
         sqlQuery = "CREATE TEMPORARY TABLE IF NOT EXISTS `n2` (" +
            "`iddoc` int(8) unsigned NOT NULL," +
            "PRIMARY KEY (`iddoc`) " +
         ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
         statement = connection.prepareStatement( sqlQuery );
         statement.executeQuery();
         */

         for( Similar s : n2 ) {
            sqlQuery = "insert into `n2` (`iddoc`) values(?);";
            statement = connection.prepareStatement( sqlQuery );
            statement.setInt(1, s.getId() );
            statement.executeQuery();
         }

          // Drop table `temp` if exists.
          sqlQuery = "drop table if exists `temp`;";
          statement = connection.prepareStatement( sqlQuery );
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

         sqlQuery =
         "insert into `temp` (`idquery`, `term`,`tf1`) select " +
            "`made`.`idquery`, " +
            "`made`.`term`, " +
            "? * `made`.`tf` + ? / (select " +
               "count(`n1`.`iddoc`) " +
               "from `n1`"  +
            ") * coalesce(" +
               "(select " +
                  "sum(`contains`.`tf`) " +
                  "from `contains`, `n1` " +
                  "where `contains`.`term` = `made`.`term` " +
                  "and `contains`.`iddoc` = `n1`.`iddoc`" +
               "), 0" +
            ") - ? / (select " +
               "count(`n2`.`iddoc`) " +
               "from `n2`"  +
            ") * coalesce(" +
               "(select " +
                  "sum(`contains`.`tf`) " +
                  "from `contains`, `n2` " +
                  "where `contains`.`term` = `made`.`term` " +
                  "and `contains`.`iddoc` = `n2`.`iddoc`" +
               "), 0" +
            ") as `tf1` " +
            "from `made` " +
            "where `made`.`idquery` = ?;";
         statement = connection.prepareStatement( sqlQuery );
         statement.setDouble( 1, ALPHA );
         statement.setDouble( 2, BETA );
         statement.setDouble( 3, GAMMA );
         statement.setInt( 4, idquery );
         statement.executeQuery();

         sqlQuery = "update `temp`, `made` " +
            "set `temp`.`tf1` = `made`.`tf`" +
            "where `temp`.`idquery` = `made`.`idquery`" +
            "and `temp`.`tf1` is null";
         statement = connection.prepareStatement( sqlQuery );
         statement.executeQuery();
               
         System.out.println( SUCCESS );
         System.out.println(
            DIVIDER +
            "Use dot product to get similarity for new query q1 from query: #" +
            idquery +
            DIVIDER
         );

         similarsQ1 = new ArrayList<>();

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
            similarsQ1.add( similar );
         }
         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return similarsQ1;
  }
   
   
   
   
  public List<Similar> getSimilarityQ1StopWords( int idquery, List<Similar> similars ) {
      
      System.out.println("getSimilarityQ1StopWords");
      List<Similar> similarsQ1 = null;
      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB , USER , PASSWORD );

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
         List<Similar> n1 = similars.subList( 0, N1 );
         List<Similar> n2 = similars.subList( similars.size() - N2, similars.size() );
         
         // Drop table `n1` if exists.
         String sqlQuery = "truncate n1;";
         PreparedStatement statement = connection.prepareStatement( sqlQuery );
         statement.executeQuery();

           /*
         // Add table `n1` if not exists.
         sqlQuery = "CREATE TEMPORARY TABLE IF NOT EXISTS `n1` (" +
            "`iddoc` int(8) unsigned NOT NULL, " +
            "PRIMARY KEY (`iddoc`) " +
         ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
         statement = connection.prepareStatement( sqlQuery );
         statement.executeQuery();
         
         */

         for( Similar s : n1 ) {
            sqlQuery = "insert into `n1` (`iddoc`) values(?);";
            statement = connection.prepareStatement( sqlQuery );
            statement.setInt( 1, s.getId() );
            statement.executeQuery();
         }

         // Drop table `n2` if exists.
         sqlQuery = "truncate n2;";
         statement = connection.prepareStatement( sqlQuery );
         statement.executeQuery();
         
         /*
         // Add table `n1` if not exists.
         sqlQuery = "CREATE TEMPORARY TABLE IF NOT EXISTS `n2` (" +
            "`iddoc` int(8) unsigned NOT NULL," +
            "PRIMARY KEY (`iddoc`) " +
         ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
         statement = connection.prepareStatement( sqlQuery );
         statement.executeQuery();    
         */

         for( Similar s : n2 ) {
            sqlQuery = "insert into `n2` (`iddoc`) values(?);";
            statement = connection.prepareStatement( sqlQuery );
            statement.setInt(1, s.getId() );
            statement.executeQuery();
         }

          // Drop table `temp` if exists.
          sqlQuery = "drop table if exists `temp`;";
          statement = connection.prepareStatement( sqlQuery );
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

         sqlQuery =
         "insert into `temp` (`idquery`, `term`,`tf1`) select " +
            "`made_stopwords`.`idquery`, " +
            "`made_stopwords`.`term`, " +
            "? * `made_stopwords`.`tf` + ? / (select " +
               "count(`n1`.`iddoc`) " +
               "from `n1`"  +
            ") * coalesce(" +
               "(select " +
                  "sum(`contains_stopwords`.`tf`) " +
                  "from `contains_stopwords`, `n1` " +
                  "where `contains_stopwords`.`term` = `made_stopwords`.`term` " +
                  "and `contains_stopwords`.`iddoc` = `n1`.`iddoc`" +
               "), 0" +
            ") - ? / (select " +
               "count(`n2`.`iddoc`) " +
               "from `n2`"  +
            ") * coalesce(" +
               "(select " +
                  "sum(`contains_stopwords`.`tf`) " +
                  "from `contains_stopwords`, `n2` " +
                  "where `contains_stopwords`.`term` = `made_stopwords`.`term` " +
                  "and `contains_stopwords`.`iddoc` = `n2`.`iddoc`" +
               "), 0" +
            ") as `tf1` " +
            "from `made_stopwords` " +
            "where `made_stopwords`.`idquery` = ?;";
         statement = connection.prepareStatement( sqlQuery );
         statement.setDouble( 1, ALPHA );
         statement.setDouble( 2, BETA );
         statement.setDouble( 3, GAMMA );
         statement.setInt( 4, idquery );
         statement.executeQuery();

         sqlQuery = "update `temp`, `made_stopwords` " +
            "set `temp`.`tf1` = `made_stopwords`.`tf`" +
            "where `temp`.`idquery` = `made_stopwords`.`idquery`" +
            "and `temp`.`tf1` is null";
         statement = connection.prepareStatement( sqlQuery );
         statement.executeQuery();
               
         System.out.println( SUCCESS );
         System.out.println(
            DIVIDER +
            "Use dot product to get similarity for new query q1 from query: #" +
            idquery +
            DIVIDER
         );

         similarsQ1 = new ArrayList<>();

         // SQL Query to determine similarity using Vector Space Model.
         sqlQuery = 
            "select `documents`.`id`, " +
            "sum(`contains_stopwords`.`tf` * `terms`.`idf` * `made_stopwords`.`tf1` * `terms`.`idf`) / " +
            "(`documents`.`weight` * `queries`.`weight`) as `similar` " +
            "from `queries`, `documents`, `contains_stopwords`, `made_stopwords`, `terms` " +
            "where `documents`.`id` = `contains_stopwords`.`iddoc` " +
            "and `contains_stopwords`.`term` = `terms`.`term` " +
            "and `made_stopwords`.`term` = `terms`.`term` " +
            "and `made_stopwords`.`idquery` = `queries`.`id` " +
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
            similarsQ1.add( similar );
         }
         connection.close();
      } catch( Exception e ) {

         e.printStackTrace();
      }
      return similarsQ1;
  }

   // Return a list of all iddoc that are relevant
   // for a given idquery.
   public List<Integer> getRelevants( int idquery ) {
     
      List<Integer> relevants = null;
      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB , USER , PASSWORD );

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
         final Connection connection = DriverManager.getConnection( DB , USER , PASSWORD );

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
         final Connection connection = DriverManager.getConnection( DB , USER , PASSWORD );

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

update `documents` set `weight` = 1;
update `queries` set `weight` = 1;

update `documents` set `weight` = (select sqrt(sum(`contains`.`tf`*`terms`.`idf`*`contains`.`tf`*`terms`.`idf`)) from `contains`, `terms` where `documents`.`id` = `contains`.`iddoc` and `contains`.`term` = `terms`.`term`);

update `queries` set `weight` = (select sqrt(sum(`made`.`tf`*`terms`.`idf`*`made`.`tf`*`terms`.`idf`)) from `made`, `terms` where `queries`.`id` = `made`.`idquery` and `made`.`term` = `terms`.`term`);

select `contains`.`iddoc`, sum(`contains`.`tf` * `terms`.`idf` * `made`.`tf` * `terms`.`idf`) as `similar` from `contains`, `made`, `terms` where `contains`.`term` = `terms`.`term` and `made`.`term` = `terms`.`term` and `made`.`idquery` = 1 group by `contains`.`iddoc` order by `similar` desc;

select `contains`.`iddoc`, sum(`contains`.`tf` * `terms`.`idf` * `made`.`tf` * `terms`.`idf`) / (`documents`.`weight` * `queries`.`weight`) as `similar` from `documents`, `queries`, `contains`, `made`, `terms` where `documents`.`id` = `contains`.`iddoc` and `contains`.`term` = `terms`.`term` and `queries`.`id` = `made`.`idquery` and `made`.`term` = `terms`.`term` and `made`.`idquery` = 1 group by `contains`.`iddoc` order by `similar` desc;



select `queries`.`id`, sum(`contains`.`tf` * `terms`.`idf` * `made`.`tf` * `terms`.`idf`) / (`documents`.`weight` * `queries`.`weight`) as `similar` from `queries`, `documents`, `contains`, `made`, `terms` where `documents`.`id` = `contains`.`iddoc` and `contains`.`term` = `terms`.`term` and `made`.`term` = `terms`.`term` and `made`.`idquery` = `queries`.`id` and `queries`.`id` = 1 group by `queries`.`id` order by `similar` desc;
*/






/*



public List<Similar> getSimilarity(int idquery) {
    
      System.out.println("getSimilarity");
       
      List<Similar> similars = null;
      // Open connection to Database && Run query.
      try {

         // Open connection,
         final Connection connection = DriverManager.getConnection( DB , USER , PASSWORD );

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
            "sum(`contains`.`tf` * `terms`.`idf` * `made`.`tf1` * `terms`.`idf`) as `similar`"   +
            "from `contains`, `made`, `terms` " +
            "where `made`.`idquery` = ? " +
            "and `made`.`term` = `terms`.`term` " +
            "and `contains`.`term` = `terms`.`term` " +
            
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



*/
