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
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

public class Controller implements Initializable {

  @FXML
  private LineChart<Double, Double> linearRegressionChart;

  public void initialize(URL url, ResourceBundle resourceBundle) {
    try (InputStream stream = getClass().getResourceAsStream("/18-bands.csv")) {
      List<Band> bands = new CsvBandParser().parseAsList(stream);
//      linearRegressionChart.getData().add(createLinearRegressionSeries(bands));
      linearRegressionChart.getData().add(createBandsDataSeries(bands));
    } catch (IOException e) {
      e.printStackTrace();
      // TODO: 02/18/2019 process exception
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

//  private Series<Double, Double> createLinearRegressionSeries(List<Band> bands) {
//    Series<Double, Double> series = new Series<>();
//    series.getData()
//        .addAll(bands.stream().map(b -> new Data<>(b.getRoughness() + 1, b.getPressSpeed() + 1))
//            .collect(
//                Collectors.toList()));
//    series.setName("Linear regression");
//    return series;
//  }
}
