package by.bsuir.lab1.model.domain;

public class Band {

  private Double roughness;
  private Double pressSpeed;

  public Band(Double roughness, Double pressSpeed) {
    this.roughness = roughness;
    this.pressSpeed = pressSpeed;
  }

  public Double getRoughness() {
    return roughness;
  }

  public void setRoughness(Double roughness) {
    this.roughness = roughness;
  }

  public Double getPressSpeed() {
    return pressSpeed;
  }

  public void setPressSpeed(Double pressSpeed) {
    this.pressSpeed = pressSpeed;
  }
}
