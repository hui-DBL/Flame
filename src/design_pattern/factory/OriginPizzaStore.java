package design_pattern.factory;

public class OriginPizzaStore {

    SimplePizzaFactory factory;

    public OriginPizzaStore(SimplePizzaFactory factory) {
        this.factory = factory;
    }

    public Pizza orderPizza(String type) {
//        Pizza pizza = null;
        // *********************
//        if (type.equals("cheese")) {
//            pizza = new CheesePizza();
//        } else if (type.equals("greek")) {
//            pizza = new GreekPizza();
//        }
        // *********************
        Pizza pizza = factory.createPizza(type);
        pizza.prepare();
        pizza.bake();
        pizza.cut();
        pizza.box();
        return pizza;
    }
}
