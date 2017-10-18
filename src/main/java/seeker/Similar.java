package seeker;

public class Similar {
  
  private int id;
  private double similarity;

  public Similar( int id, double similarity ) {
      this.id = id;
      this.similarity = similarity;
  }

  public int getId() {
      return id;
  }

  public double getSimilarity() {
      return similarity;
  }
}