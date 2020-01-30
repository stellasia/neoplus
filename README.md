# neoplus

A test project to build user defined procedures to be run against a Neo4j database from Cypher.

Project built from: https://github.com/neo4j-examples/neo4j-procedure-template/

## Content

Procedure to multiply a node property by a given factor and return the node id and the product of the multiplication.

## Usage

0. Copy built jar to plugins directory
0. Add `neoplus.*` to `dbms.security.procedures.unrestricted` in graph settings
0. Restart graph
0. Check the procedures are there:

       CALL dbms.procedures() YIELD name, signature, description
       WITH name, signature, description WHERE name CONTAINS 'neoplus'
       RETURN name, signature, description
       
0. Call the new procedure:

       CALL neoplus.multiplyBy("MyLabel", "myProperty", 10.0) 
       YIELD newValue, nodeId 
       RETURN nodeId, newValue
 
 *NB* ensure `myProperty` contains floats:
 
    MATCH (n:MyLabel) SET n.myProperty = toFloat(n.myProperty)


## Developers

### Build

    mvn clean package

### Test

    mvn test
    
    