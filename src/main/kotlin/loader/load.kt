package loader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import kotlin.collections.MutableList;

open class Load {
  
  val DRIVER = "org.mariadb.jdbc.Driver";
  val DB =
     "jdbc:mariadb://localhost:3306/information_retrieval?user=root";
  val PATH =
    "/Users/mauriciog/Downloads/Recuperacion de la Información/Buscador/src/loader/";
  val TERMS = PATH + "term-vocab.txt";
  val DOCUMENTS = PATH + "doc-text.txt";
  val QUERIES = PATH + "query-text.txt";
  val CONTAINS = PATH + "doc-vecs.txt";
  val MADE = PATH + "queries-vec.txt";
  val RELEVANT = PATH + "rlv-ass.txt";

  fun checkConnection() {

    try {
      // Enable driver,
      Class.forName( DRIVER ).newInstance();
      
      // Open connection,
      var connection = DriverManager.getConnection( DB );

      // Check if succesful.
      if( connection != null ) {

        println( "SUCCESS: Connected to DB" );
        println( "Removing all data" );
      } else
        return;

      // TODO
      var sqlQuery = "select * from terms";
      var statement = connection.prepareStatement( sqlQuery );
      statement.execute();
      statement.close();
      
    } catch(e: SQLException) {
        e.printStackTrace();
    } catch(e: Exception) {
      e.printStackTrace();
    }
  }

}