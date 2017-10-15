package buscador;


import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

interface Constants {

   String DRIVER = "org.mariadb.jdbc.Driver";
   String DB =
      "jdbc:mariadb://localhost:3306/information_retrieval?user=root";
   String DIVIDER = " ------ ";
   String SUCCESS = "SUCCESS: Connected to DB";
   String FAILURE = "FAILURE: Unable to connect to DB";
}
