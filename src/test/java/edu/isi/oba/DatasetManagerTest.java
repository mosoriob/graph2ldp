package edu.isi.oba;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.rdf.api.Dataset;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.SplitIRI;
import org.junit.Test;

public class DatasetManagerTest {
    String pattern = "https://w3id.org/okn/o";
    @Test
    public void constructor() throws IOException {
        DatasetManager datasetManager = new DatasetManager("./src/test/resources/triples.ttl", null);
        datasetManager.getNumTriples();
        datasetManager.renameStatemenets();
        System.out.println("x");
    }

    @Test
    public void getRdfClassTest() throws IOException {
        DatasetManager datasetManager = new DatasetManager("./src/test/resources/multipleRdfClass.ttl", null);
        ResIterator iter = datasetManager.model.listSubjects();
        while (iter.hasNext()) {
            Resource subject = iter.nextResource();
            List<String> classes = datasetManager.getRdfClasses(subject);
            assertEquals(classes.size(), 2);
        }
    }

    @Test
    public void filterRdfClassTest() {
        List<String> list = Arrays.asList(
                "https://w3id.org/wings/export/MINT#CyclesAnnualSoilProfile",
                "https://w3id.org/okn/o/sd#DatasetSpecification");
        try {
            List<String> rdfClass = DatasetManager.filterRdfClasses(list, "https://w3id.org/okn/o", null);
            assertEquals(1, rdfClass.size());
        } catch (IllegalArgumentException e){
            System.out.println("ok");
        }
    }

    @Test
    public void getRdfClassPersonTest() throws IOException {
        DatasetManager datasetManager = new DatasetManager("./src/test/resources/person.ttl", null);
        ResIterator iter = datasetManager.model.listSubjects();
        while (iter.hasNext()) {
            Resource subject = iter.nextResource();
            List<String> classes = datasetManager.getRdfClasses(subject);
            assertEquals(classes.size(), 2);
            List<String> rdfClass = DatasetManager.filterRdfClasses(classes, pattern, null);
            assertEquals(rdfClass.size(), 1);
            assertEquals("https://w3id.org/okn/o/sd#Person", rdfClass.get(0));
            
        }
    }


    @Test
    public void getLocalName() {
        DatasetManager datasetManager = new DatasetManager("./src/test/resources/idHash.ttl", null);
        ResIterator iter = datasetManager.model.listSubjects();
        while (iter.hasNext()) {
            Resource subject = iter.nextResource();
            String localName = subject.getLocalName();
            String test = DatasetManager.getLocalNameCustom(subject.getURI(), "/");
            assertEquals("0567f5f6-9b9b-423d-94ef-af31e1064dee", test);
        }
    } 
}