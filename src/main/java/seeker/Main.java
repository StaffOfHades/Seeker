package seeker;

import loader.Load;

public class Main implements Constants {

  public static void main( String[] args ) {

    new Load();
    /*
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