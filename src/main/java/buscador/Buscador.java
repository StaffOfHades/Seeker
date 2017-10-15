package buscador;

import java.sql.SQLException;
import loader.Load;

public class Buscador implements Constants {

  public static void main(String[] args) {

    /*
    Load load = new Load();
    load.cleanDB();
    load.loadDocuments( true );
    load.loadQueries( true );
    load.addDF();
    load.addIDF();
    load.loadRelevant();
    */
 
     //Crea la interfez y muestrala
    View view = new View();
    view.setVisible( true );

    if( !Connect.getInstance().checkConnection() )
      System.out.println( FAILURE );
  }
}