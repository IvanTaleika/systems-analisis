package by.bsuir.sa.chart;

import java.util.List;
import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory.Toolkit;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class ScatterPlot extends AbstractAnalysis {

  private List<ScatterPoint> points;


  public ScatterPlot(List<ScatterPoint> points) {
    this.points = points;
  }

  @Override
  public void init() {
    Coord3d[] coords = new Coord3d[points.size()];
    Color[] colors = new Color[points.size()];
    int i = 0;
    for (ScatterPoint point : points) {
      coords[i] = new Coord3d(point.getX(), point.getY(), point.getZ());
      colors[i] = point.getColor();
      i++;
    }

    Scatter scatter = new Scatter(coords, colors);

    scatter.setWidth(10);
    chart = AWTChartComponentFactory.chart(Quality.Advanced, Toolkit.newt);
    chart.getScene().add(scatter);
    chart.getAxeLayout().setXAxeLabel("graphics");
    chart.getAxeLayout().setYAxeLabel("medicine");
    chart.getAxeLayout().setZAxeLabel("atheism");
  }

}
