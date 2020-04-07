package design_pattern.decorate;

/**
 * 让外面看起来依旧是Beverage
 */
public abstract class CondimentDecorator extends Beverage {
    @Override
    public abstract String getDescription();
}
