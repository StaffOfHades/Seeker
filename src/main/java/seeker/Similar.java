package seeker;

public class Similar {
  
  private int id;
  private double similarity;
  private String text;
  private String url;

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
  
  public void setText(String texto){
      this.text = texto;
  }
  
  public String getText(){
      return this.text;
  }
  
  public void setURL(String url){
      this.url = url;
  }
  
  public String getURL(){
      return this.url;
  }
}