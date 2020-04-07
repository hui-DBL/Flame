package design_pattern.decorate;

public class BeverageTest {
    public static void main(String[] args) {
        Beverage beverage = new Espresso();
        System.out.println(beverage.getDescription() + " $" + beverage.getCost());

        Beverage beverage1 = new Mocha(beverage);
        beverage1 = new Mocha(beverage1);
        beverage1 = new Whip(beverage1);
        System.out.println(beverage1.getDescription() + " $" + beverage1.getCost());
        // getDescription()与description不同
        System.out.println(beverage1.description+ " $" + beverage1.getCost());
    }
}
