# neoplus

A test project to build user defined procedures to be run against a Neo4j database from Cypher.

> Except for learning purposes, this repo does not contain any useful stuff.

Project built from: https://github.com/neo4j-examples/neo4j-procedure-template/

## Content

Functions to compute a discount price from a full price and a discount factor. They come in different flavours:

- `neoplus.discountPrice(price, discount)` is a simple function that takes two (double) numbers as input, and returns the discounted price
- `neoplus.discountPriceForNode(node)` is called for a given node with properties "price" and "discount" and returns the discounted price
- `neoplus.setDiscountPrice(node)` : update the node to set the discounted price based on "price" and "discount" properties
- `neoplus.discountPriceForLabel(label, priceProperty, discountProperty, defaultDiscountValue)` is a slightly more complex procedure
   streaming the discounted price for all nodes with a given label, using configured price and discount
   property names, with the ability to have a default value for discount if the node property
   is not set.

Another type of functions are related to shortest path algorithms:
- `neoplus.shortestPathLength(node1, node2)`: returns the length of the shortest path
   between node1 and node2.

## Usage

0. Copy built jar to plugins directory
0. Add `neoplus.*` to `dbms.security.procedures.unrestricted` in graph settings
0. Restart graph
0. Check the procedures are there:

       CALL dbms.procedures() YIELD name, signature, description
       WITH name, signature, description WHERE name CONTAINS 'neoplus'
       RETURN name, signature, description
       
0. Call the new procedure:

       CALL neoplus.discountPriceForLabel("MyLabel", "price", "discount", 1.0) 
       YIELD newValue, nodeId 
       RETURN nodeId, newValue
 
 *NB* ensure `myProperty` contains floats:
 
    MATCH (n:MyLabel) SET n.myProperty = toFloat(n.myProperty)


## Developers

### Build

    mvn clean package

### Test

    mvn test
    
    