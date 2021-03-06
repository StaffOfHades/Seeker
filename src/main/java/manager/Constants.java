package manager;

public interface Constants {

   double ALPHA = 1;
   double BETA = 0.8;
   double GAMMA = 0.1;
   int MAX_DOC = 10;
   int N1 = 100;
   int N2 = 10;
   String CONTAINS = "`contains`";
   String DB =
      "jdbc:mariadb://localhost:3306/information_retrieval?user=root";
   String DIVIDER = " ------ ";
   String DOCUMENTS = "`documents`";
   String DRIVER = "org.mariadb.jdbc.Driver";
   String FAILURE = "FAILURE: Unable to connect to DB";
   String MADE = "`made`";
   String PATH =
      "/Users/mauriciog/Downloads/Recuperacion de la Información/Seeker/src/main/java/";
   String QUERIES = "`queries`";
   String RELEVANT = "`relevant`";
   String SUCCESS = "SUCCESS: Connected to DB";
   String TERMS = "`terms`";
   String TITLE = "HW6";
}
