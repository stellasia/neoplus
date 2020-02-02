package neoplus;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;

import java.util.stream.Stream;


public class LR {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;


    /**First test procedure: multiply a node property by a given factor
     * and streams the results
     *
     * @param label the label name to query by
     * @param property the property to be multiplied
     * @param factor the multiplication factor
     * @return the nodes id + the multiplied property
     */
    @Procedure(name = "neoplus.multiplyBy")
    @Description("Computes n.property*2 for all nodes with a given label")
    public Stream<Result> multiplyBy(
            @Name("label") String label,
            @Name("property") String property,
            @Name("factor") Double factor
    ) {
        return db.findNodes(Label.label(label)).stream().map(
                n -> new Result(n, property, factor)
        );
    }

    /**First test procedure: multiply a node property by a given factor
     *
     * @param label the label name to query by
     * @param property the property to be multiplied
     * @param factor the multiplication factor
     * @return the nodes id + the multiplied property
     */
    @Procedure(name = "neoplus.multiplyByWrite", mode=Mode.WRITE)
    @Description("Computes n.property*2 for all nodes with a given label")
    public void multiplyByWrite(
            @Name("label") String label,
            @Name("property") String property,
            @Name("factor") Double factor
    ) {
        Stream<Result> results = multiplyBy(label, property, factor);

        try (Transaction tx = db.beginTx()) {
            results.forEach(r -> {
                Node n = db.getNodeById(r.nodeId);
                n.setProperty("newValue", r.newValue);
            });
            tx.success();
        }
    }

    public static class Result {
        public long nodeId;
        public double newValue;

        public Result(Node node, String property, Double factor) {
            this.nodeId = node.getId();
            this.newValue = ((double) node.getProperty(property)) * factor;
        }
    }
}


