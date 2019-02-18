module lab1 {
  requires javafx.fxml;
  requires javafx.controls;
  requires jackson.annotations;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.dataformat.csv;
  requires com.fasterxml.jackson.core;

  opens by.bsuir.lab1 to javafx.fxml;
  opens by.bsuir.lab1.ui to javafx.fxml;
  opens by.bsuir.lab1.model.domain to com.fasterxml.jackson.databind, com.fasterxml.jackson.dataformat.csv, com.fasterxml.jackson.core;
  exports by.bsuir.lab1.model.domain;
  exports by.bsuir.lab1;
  exports by.bsuir.lab1.ui;
}