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
public class LRTest {

    private static final Config driverConfig = Config.build().withoutEncryption().toConfig();
    private ServerControls embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = TestServerBuilders
                .newInProcessBuilder()
                .withProcedure(LR.class)
                .withFunction(LR.class)
                .newServer();
    }

    @Test
    public void testMult() {

        // In a try-block, to make sure we close the driver and session after the test
        try(Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
            Session session = driver.session())
        {
            // Then I can call the multiplyBy procedure
            StatementResult result = session.run("RETURN neoplus.mult(2, 10.0)");
            Record r = result.single();
            assertThat(r.get(0).asDouble()).isEqualTo( 20.0 );
        }
    }

    @Test
    public void shouldMultiplyBy() {

        // In a try-block, to make sure we close the driver and session after the test
        try(Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
            Session session = driver.session())
        {
            // Given I've started Neo4j with the neoplus.LR procedure class
            //       which my 'neo4j' rule above does.
            // And given I have a node in the database
            long nodeId = session.run( "CREATE (p:Label {property:1.0}) RETURN id(p)" )
                    .single()
                    .get( 0 ).asLong();

            // Then I can call the multiplyBy procedure
            StatementResult result = session.run("CALL neoplus.multiplyBy('Label', 'property', 10.0)");
            Record r = result.single();
            assertThat(r.get( "nodeId" ).asLong()).isEqualTo( nodeId );
            assertThat(r.get( "newValue" ).asDouble()).isEqualTo( 10.0 );
        }
    }

    @Test
    public void shouldMultiplyByWrite() {

        // In a try-block, to make sure we close the driver and session after the test
        try(Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
            Session session = driver.session())
        {
            // Given I've started Neo4j with the neoplus procedure class
            //       which my 'neo4j' rule above does.
            // And given I have a node in the database
            long nodeId = session.run( "CREATE (p:Label {property:1.0}) RETURN id(p)" )
                    .single()
                    .get( 0 ).asLong();

            // Then I can call the multiplyByWrite procedure
            session.run( "CALL neoplus.multiplyByWrite('Label', 'property', 10.0)");

            // and it adds a new property 'newValue' to my graph
            Value parameters = parameters(
                    "nodeId", nodeId
            );
            StatementResult result = session.run(
                    "MATCH (n:Label) WHERE id(n) = {nodeId} RETURN n.newValue as newValue",
                    parameters);
            assertThat(result.single().get( "newValue" ).asDouble()).isEqualTo( 10.0 );
        }
    }
}