package seeker;

import manager.Load;
import manager.Constants;

public class Main implements Constants {

   public static void main( String[] args ) {

      
     // Load load = Load.getInstance();
     //load.loadDocuments(true);
     //load.addDF();
     //load.addIDF();
      
    
      /*
      load.cleanDB();
      load.loadDocuments( true );
      load.loadQueries( true );
      load.addDF();
      load.addIDF();
      load.loadRelevant();
      */
      if( !Load.getInstance().checkConnection() )
          System.out.println( FAILURE );
   
      //Crea la interfez y muestrala
      View view = new View();
      view.setVisible( true );    
   }
}