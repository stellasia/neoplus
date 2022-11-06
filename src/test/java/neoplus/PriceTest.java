package neoplus;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.neo4j.driver.*;
import org.neo4j.driver.Config;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.value.NullValue;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PriceTest {

    private static final Config driverConfig = Config.builder().withoutEncryption().build();
    private Neo4j embeddedDatabaseServer; // <2>

    @BeforeAll // <3>
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withFunction(Price.class)
                .withProcedure(Price.class)
                .withFixture("CREATE (:OldProduct {id: 0, price: 100.0, discount: 10})")
                .withFixture("CREATE (:Product {id: 1, fullPrice: 100.0, discount: 20})")
                .withFixture("CREATE (:Product {id: 2, fullPrice: 1.0})")
                .withDisabledServer()
                .build();
    }

    @Test
    public void testDiscountedPrice() {

        // In a try-block, to make sure we close the driver and session after the test
        try(Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
            Session session = driver.session())
        {
            Result result = session.run("RETURN neoplus.discountedPrice(100.0, 10)");
            Record r = result.single();
            assertThat(r.get(0).asDouble()).isEqualTo( 90.0 );
        }
    }

    @Test
    public void testDiscountedPriceWithNulls() {

        try(Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
            Session session = driver.session())
        {
            Result result = session.run("RETURN neoplus.discountedPrice(100, null)");
            Record r = result.single();
            assertThat(r.get(0)).isInstanceOf(NullValue.class);

            result = session.run("RETURN neoplus.discountedPrice(null, 1)");
            r = result.single();
            assertThat(r.get(0)).isInstanceOf(NullValue.class);

            result = session.run("RETURN neoplus.discountedPrice(null, null)");
            r = result.single();
            assertThat(r.get(0)).isInstanceOf(NullValue.class);

        }
    }

    @Test
    public void testDiscountPriceForNode() {

        try(Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
            Session session = driver.session())
        {
            Result records = session.run(
                    "MATCH (p:OldProduct {id: 0})" +
                            "WITH neoplus.discountedPriceForNode(p) AS value " +
                            "RETURN value"
            );

            double result = records.single().get(0).asDouble();
            assertThat(result).isEqualTo(90.0);
        }
    }

    @Test
    public void testDiscountPriceForLabel() {

        try(Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
            Session session = driver.session())
        {
            Result records = session.run("CALL neoplus.discountedPriceForLabel('Product', 'fullPrice', 'discount', 10)");
            var result = records.stream()
                    .map(r -> r.get("discountedPrice"))
                    .map(Value::asDouble)
                    .sorted()
                    .collect(Collectors.toList());
            List<Double> lst = Arrays.asList(0.9, 80.0);
            assertThat(result).isEqualTo(lst);
        }
    }
}