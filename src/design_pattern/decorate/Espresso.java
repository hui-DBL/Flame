package design_pattern.decorate;

public class Espresso extends Beverage {

    public Espresso() {
        description = "Espresso";
    }

    @Override
    public Double getCost() {
        // 可以不用写死
        return 1.99;
    }
}
