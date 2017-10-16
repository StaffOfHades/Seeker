package grapher;

import java.awt.Color;
import java.awt.BasicStroke;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Graph<E extends Number> extends JFrame{

  static final long serialVersionUID = 13L;

  /** The Constant COUNT. */
  static final int COUNT = 20;
 
  /** The Constant WINDOW. */
  public static final int WINDOW = 5;

  /** The Constant FIRST. */
  public static final int FIRST = 0;

  private final static Color[] colors = {Color.BLACK, Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.PINK, Color.ORANGE, Color.MAGENTA, Color.YELLOW };

  public Graph(
    String appTitle,
    String chartTitle,
    Map<String, List<E>> data) {
    super(appTitle);

    JFreeChart chart = ChartFactory.createXYLineChart(
      chartTitle,
      "Documents (#)",
      "Percetage (%)",
      createDataset(data),
      PlotOrientation.VERTICAL,
      true,
      true,
      false
    );

    ChartPanel panel = new ChartPanel( chart );
    panel.setPreferredSize(new java.awt.Dimension(1000, 540));
    panel.setMouseZoomable(true, false);
    panel.setRangeZoomable(true);
    panel.setDomainZoomable(true);
    final XYPlot plot = chart.getXYPlot();

    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    shuffleColor();

    for(int i = 0; i < data.size(); i++) {
      renderer.setSeriesPaint(
        i,
        colors[i % colors.length]
      );
      final float[] dash = {10.0f};
      renderer.setSeriesStroke(
        i,
        new BasicStroke(
          1.0f,
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER,
          10.0f,
          dash,
          0.0f
        )
      );
    }
    plot.setRenderer( renderer );
    setContentPane( panel );

    setDefaultCloseOperation( DISPOSE_ON_CLOSE );
  }

  private XYDataset createDataset(Map<String, List<E>> data) {
    final XYSeriesCollection dataset = new XYSeriesCollection( );
    for( Map.Entry<String, List<E>> entry : data.entrySet() ) {
      final XYSeries series = new XYSeries( entry.getKey() );
      final List<E> list = entry.getValue();
      final int max = list.size();
      for( int i = 0; i < max; i++ ) {
        series.add(
          i,
          list.get(i)
        );
      }
      dataset.addSeries( series );
    }
    return dataset;
  }

  private void shuffleColor() {
    int index;
    final Random random = new Random();
    for (int i = colors.length - 1; i > 0; i--) {
        index = random.nextInt(i + 1);
        if (index != i) {
          final Color temp = colors[index];
          colors[index] = colors[i];
          colors[i] = temp;
        }
    }
  }

}