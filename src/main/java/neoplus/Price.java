package neoplus;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;

import java.util.stream.Stream;


public class Price {

    @Context
    public Transaction tx;

    Double computeDiscountedPrice(
            Double price,
            Long discount
    ) {
        if (price == null || discount == null) {
            return null;
        }
        return price * (1 - discount/100.0);
    }

    /** A user-defined function, that multiplies two numbers together.
     *
     * @param price: the initial price
     * @param discount: the discount in %
     * @return
     */
    @UserFunction
    @Description("neoplus.discountedPrice(value, value)")
    public Double discountedPrice(
            @Name("price") Double price,
            @Name("discount") Long discount
    ) {
        return computeDiscountedPrice(price, discount);
    }

    /**
     *
     * @param node: node with price and discount properties
     * @return
     */
    @UserFunction(name = "neoplus.discountedPriceForNode")
    @Description("Compute discounted price from price and discount in percentage")
    public Double getDiscountedPrice(
            @Name("node") Node node
    ) {
        Double price = (Double) node.getProperty("price");
        Long discount = (Long) node.getProperty("discount");
        return this.discountedPrice(price, discount);
    }

    @UserFunction
    @Description("neoplus.setDiscountedPrice(node)")
    public Double setPrice(
            @Name("node") Node node
    ) {
        Double newPrice = this.getDiscountedPrice(node);
        if (newPrice != null) {
            node.setProperty("discountPrice", newPrice);
        }
        return newPrice;
    }

    /**
     * A user-defined procedure that computes the discounted prices and
     * streams the results
     *
     * @param label                the label name to query by
     * @param priceProperty        the name of the property holding the price
     * @param discountProperty     the name of the property containing the discount
     * @param defaultDiscountValue default value for the discount
     * @return the node ids + the discounted price
     */
    @Procedure(name = "neoplus.discountedPriceForLabel", mode=Mode.READ)
    @Description("Computes discounted prices for all nodes with a given label")
    public Stream<Result> discountedPriceForLabel(
            @Name("label") String label,
            @Name("priceProperty") String priceProperty,
            @Name("discountProperty") String discountProperty,
            @Name("defaultDiscountValue") Long defaultDiscountValue
    ) {
        return tx.findNodes(Label.label(label)).stream().map(
                n -> {
                    Double price = (Double) n.getProperty(priceProperty);
                    Long discount = defaultDiscountValue;
                    try {
                        discount = (Long) n.getProperty(discountProperty);
                    } catch (Exception ignored) {
                    }
                    return new Result(n, this.discountedPrice(price, discount));
                });
    }

    public static class Result {
        public Node node;
        public double discountedPrice;

        public Result(Node node, Double discountedPrice) {
            this.node = node;
            this.discountedPrice = discountedPrice;
        }
    }
}


