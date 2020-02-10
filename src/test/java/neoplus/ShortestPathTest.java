package neoplus;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.driver.v1.Values.parameters;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ShortestPathTest {

    private static final Config driverConfig = Config.build().withoutEncryption().toConfig();
    private ServerControls embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = TestServerBuilders
                .newInProcessBuilder()
                .withFunction(ShortestPath.class)
                .newServer();
    }

    @Test
    public void testShortestPath() {

        // In a try-block, to make sure we close the driver and session after the test
        try(Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
            Session session = driver.session())
        {
            // Given I've started Neo4j with the neoplus procedure class
            //       which my 'neo4j' rule above does.
            // And given I have a node in the database
            session.run("CREATE (p1:Label {id:1}) " +
                    "CREATE (p2:Label {id:2}) " +
                    "CREATE (p1)-[:LINKED_TO {w: 11.0}]->(p2);"
            );

            // Then I can call the shortestPath procedure
            StatementResult result = session.run("MATCH (p1:Label {id:1}) MATCH (p2:Label {id:2}) RETURN neoplus.shortestPath(p1, p2, 'w')");
            Record r = result.single();
            assertThat(r.get(0).asDouble() == 11.0);
        }
    }

}