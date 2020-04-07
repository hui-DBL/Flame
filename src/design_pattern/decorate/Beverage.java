package design_pattern.decorate;

public abstract class Beverage {

    String description = "Unknown Beverage";

    String getDescription() {
        return description;
    }

    public abstract Double getCost();
}
