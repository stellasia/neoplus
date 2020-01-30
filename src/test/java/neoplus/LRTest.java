package neoplus;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import static org.assertj.core.api.Assertions.assertThat;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LRTest {

    private static final Config driverConfig = Config.build().withoutEncryption().toConfig();
    private ServerControls embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {

        this.embeddedDatabaseServer = TestServerBuilders
                .newInProcessBuilder()
                .withProcedure(LR.class)
                .newServer();
    }

    @Test
    public void shouldAllowIndexingAndFindingANode() {

        // In a try-block, to make sure we close the driver and session after the test
        try(Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
            Session session = driver.session())
        {
            // Given I've started Neo4j with the FullTextIndex procedure class
            //       which my 'neo4j' rule above does.
            // And given I have a node in the database
            long nodeId = session.run( "CREATE (p:Label {property:1}) RETURN id(p)" )
                    .single()
                    .get( 0 ).asLong();

            // Then I can search for that node with lucene query syntax
            StatementResult result = session.run( "CALL neoplus.multiplyBy(Label, 'property', 10)");
            assertThat(result.single().get( "nodeId" ).asLong()).isEqualTo( nodeId );
            assertThat(result.single().get( "newValue" ).asDouble()).isEqualTo( 10.0 );
        }
    }
}