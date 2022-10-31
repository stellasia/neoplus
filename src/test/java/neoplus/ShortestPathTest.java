package neoplus;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import static org.assertj.core.api.Assertions.assertThat;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ShortestPathTest {

    private static final Config driverConfig = Config.builder().withoutEncryption().build();
    private Neo4j embeddedDatabaseServer; // <2>

    @BeforeAll // <3>
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withFunction(ShortestPath.class) // <4>
                .withDisabledServer()
                .build(); // <6>
    }

    @Test
    public void testShortestPath() {

        // In a try-block, to make sure we close the driver and session after the test
        try(Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
            Session session = driver.session())
        {
            session.run("CREATE (p1:Label {id:1}) " +
                    "CREATE (p2:Label {id:2}) " +
                    "CREATE (p1)-[:LINKED_TO {w: 12.0}]->(p2);"
            );

            Result result = session.run(
                    "MATCH (p1:Label {id:1}) MATCH (p2:Label {id:2}) " +
                            "RETURN neoplus.shortestPathLength(p1, p2, 'w')"
            );
            Record r = result.single();
            assertThat(r.get(0).asDouble()).isEqualTo(12.0);
        }
    }

}