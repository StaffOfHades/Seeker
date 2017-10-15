package grapher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Export {

  public void exportToCSV(Map<String, List<? extends Serializable>> map, File file) {

    try {
      FileWriter writer = new FileWriter(file);
      String string = "";
      final List<String> keys = new ArrayList<>();
      for( String key : map.keySet() ) {
          string += key + ", ";
          keys.add(key);
      }
      string = string.substring(0, (string.length() - 2));
      string += "\n";
      writer.write( string );

      int max = 0;
      for( String key : keys ) {
        if( max < map.get(key).size() ) {
          max = map.get(key).size();
        }
      }

      for( int i = 0; i < max; i++ ) {
        string = "";
        for( String key : keys) {
          final List<? extends Serializable> list = map.get(key);
          if(i < list.size()) {
            string += list.get(i) + ", ";
          }
        }
        string = string.substring(0, (string.length() - 2));
        string += "\n";
        writer.write( string );
      }
      
      //writer.write( string );
      writer.close();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

}

/*

Los Fuertes
Variedad de Generos Musicales


*/