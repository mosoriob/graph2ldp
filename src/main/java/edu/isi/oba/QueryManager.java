package edu.isi.oba;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;


public class QueryManager {
    /**
     * Query an Endpoint using the given SPARQl query
     * @param query
     * @param endpoint
     * @throws Exception
     */
    public void queryEndpoint(String queryString, String endpoint)
    throws Exception
    {
        // Create a Query with the given String
        Query query = QueryFactory.create(queryString);
        Model model = ModelFactory.createDefaultModel();
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        Model resultModel = qexec.execConstruct();
        RDFDataMgr.write(System.out, resultModel, Lang.TRIG) ;
    } // End of Method: queryEndpoint()

    
}
