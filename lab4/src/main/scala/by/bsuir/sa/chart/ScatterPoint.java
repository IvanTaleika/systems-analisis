package by.bsuir.sa.chart;

import java.io.Serializable;
import org.jzy3d.colors.Color;

public class ScatterPoint implements Serializable {

  private double x;
  private double y;
  private double z;
  private Color color;

  public ScatterPoint(double x, double y, double z, Color color) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.color = color;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getZ() {
    return z;
  }

  public Color getColor() {
    return color;
  }
}
