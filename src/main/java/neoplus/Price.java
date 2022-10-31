package neoplus;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;

import java.util.stream.Stream;


public class Price {

    @Context
    public Transaction tx;

    /** A user-defined function, that multiplies two numbers together.
     *
     * @param price
     * @param discount
     * @return
     */
    @UserFunction
    @Description("neoplus.discountedPrice(value, value)")
    public Double discountedPrice(
            @Name("price") Double price,
            @Name("discount") Double discount
    ) {
        if (price == null || discount == null) {
            return null;
        }
        return price * discount;
    }

    /**
     *
     * @param node
     * @return
     */
    @UserFunction
    @Description("neoplus.discountPriceForNode(node)")
    public Double getPrice(
            @Name("node") Node node
    ) {
        Double price = (Double) node.getProperty("price");
        Double discount = (Double) node.getProperty("discount");
        if (price == null || discount == null)
            return null;
        return this.discountedPrice(price, discount);
    }

    @UserFunction
    @Description("neoplus.setDiscountPrice(node)")
    public Double setPrice(
            @Name("node") Node node
    ) {
        Double newPrice = this.getPrice(node);
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
    @Procedure(name = "neoplus.discountPriceForLabel", mode=Mode.WRITE)
    @Description("Computes n.property*2 for all nodes with a given label")
    public Stream<Result> priceForLabel(
            @Name("label") String label,
            @Name("priceProperty") String priceProperty,
            @Name("discountProperty") String discountProperty,
            @Name("defaultDiscountValue") Double defaultDiscountValue
    ) {
        return tx.findNodes(Label.label(label)).stream().map(
                n -> {
                    Double price = (Double) n.getProperty(priceProperty);
                    Double discount = defaultDiscountValue;
                    try {
                        discount = (Double) n.getProperty(discountProperty);
                    } catch (Exception ignored) {
                    }
                    if (discount == null) {
                        discount = defaultDiscountValue;
                    }
                    return new Result(n, this.discountedPrice(price, discount));
                });
    }

    public static class Result {
        public Node node;
        public double discountPrice;

        public Result(Node node, Double discountPrice) {
            this.node = node;
            this.discountPrice = discountPrice;
        }
    }
}


