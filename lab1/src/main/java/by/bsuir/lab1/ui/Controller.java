package by.bsuir.lab1.ui;

import by.bsuir.lab1.model.domain.Band;
import by.bsuir.lab1.model.parse.CsvBandParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public class Controller implements Initializable {

  @FXML
  private LineChart<Double, Double> linearRegressionChart;
  @FXML
  private Label correlationValueLabel;

  public void initialize(URL url, ResourceBundle resourceBundle) {
    try (InputStream stream = getClass().getResourceAsStream("/18-bands.csv")) {
      List<Band> bands = new CsvBandParser().parseAsList(stream);
      linearRegressionChart.getData().add(createBandsDataSeries(bands));
      SimpleRegression regression = new SimpleRegression();
      for (Band band : bands) {
        regression.addData(band.getRoughness(), band.getPressSpeed());
      }
      linearRegressionChart.getData().add(createLinearRegressionSeries(regression));
      correlationValueLabel.setText(Double.toString(regression.getR()));
    } catch (IOException e) {
      System.err.println("Data file not found.");
      System.exit(1);
    }
  }

  private Series<Double, Double> createBandsDataSeries(List<Band> bands) {
    Series<Double, Double> series = new Series<>();
    series.getData()
        .addAll(bands.stream().map(b -> new Data<>(b.getRoughness(), b.getPressSpeed())).collect(
            Collectors.toList()));
    series.setName("Bands data");
    return series;
  }

  private Series<Double, Double> createLinearRegressionSeries(SimpleRegression regression) {
    Series<Double, Double> series = new Series<>();
    for (double i = 0; i < 1.5; i += 0.1) {
      series.getData().add(new Data<>(i, regression.predict(i)));
    }
    series.setName("Linear regression");
    return series;
  }
}
