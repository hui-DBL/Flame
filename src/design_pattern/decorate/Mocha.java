package design_pattern.decorate;

public class Mocha extends CondimentDecorator {

    /**
     * 被装饰的对象
     */
    private Beverage beverage;

    public Mocha(Beverage beverage) {
        this.beverage = beverage;
    }

    @Override
    public String getDescription() {
        return beverage.getDescription() + ", Mocha";
    }

    @Override
    public Double getCost() {
        return beverage.getCost() + 0.20;
    }
}
