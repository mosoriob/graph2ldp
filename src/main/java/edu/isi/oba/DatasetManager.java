package edu.isi.oba;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;

public class DatasetManager {
    public String graphIRI = null;
    Model model;
    Model newModel = ModelFactory.createDefaultModel();
    Property rdf_type;
    private static String[] split;
    String uriSourcePattern = "https://w3id.org/okn/i/mint/";

    List<String> ignoreClasses = Arrays.asList("https://w3id.org/okn/o/sdm#Theory-GuidedModel",
            "https://w3id.org/okn/o/sd#SoftwareConfiguration");

    public DatasetManager(String triplesFilePath, String graphIRI) {
        this.graphIRI = graphIRI;
        File file = new File(triplesFilePath);

        if (file.exists()) {
            model = RDFDataMgr.loadModel(triplesFilePath);
            rdf_type = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
            System.out.println("ok");
        } else {
            System.out.println("not found");
        }
    }

    // get the number of triples in the graph
    public long getNumTriples() {
        return model.size();
    }

    // get resource
    public void getSubjectToRDF() {
        model.listSubjects().toList().forEach(subject -> {
            System.out.println(subject.toString());
            Model subjectModel = subject.getModel();
            String name = subject.getLocalName();
            writeModel2File(subjectModel, name);

        });
    }

    public void renameStatemenets() {
        ResIterator iter = model.listSubjects();
        while (iter.hasNext()) {
            Resource subject = iter.nextResource();
            StmtIterator stmtIter = subject.listProperties();
            while (stmtIter.hasNext()) {
                Statement stmt = stmtIter.nextStatement();
                Property predicate = stmt.getPredicate();
                RDFNode object = stmt.getObject();
                List<String> subjectResourceClasses = getRdfClasses(subject);
                String pattern = "https://w3id.org/okn";
                List<String> subjectClasses = filterRdfClasses(subjectResourceClasses, pattern, subject.getURI());
                String subjectClass = getLocalNameClass(subjectClasses);
                String newSubjectName = renameURI(subjectClass, subject);
                Resource newSubject = newModel.createResource(newSubjectName);
                if (object.isResource()) {
                    Resource objectResource = object.asResource();
                    List<String> objectResourceClasses = getRdfClasses(objectResource);
                    try {
                        List<String> objectClass = filterRdfClasses(objectResourceClasses, pattern,
                                objectResource.getURI());
                        String objectClassName = getLocalNameClass(objectClass);
                        String newObjectName = objectResource.getURI();
                        if (!predicate.equals(rdf_type))
                            newObjectName = renameURI(objectClassName, objectResource);
                        Resource newObject = newModel.createResource(newObjectName);
                        Statement newStmt = newModel.createStatement(newSubject, predicate, newObject);
                        newModel.add(newStmt);
                    } catch (IllegalArgumentException e) {
                        System.out.println("error");
                    }
                } else {
                    Statement newStmt = newModel.createStatement(newSubject, predicate, object);
                    newModel.add(newStmt);
                }
            }
        }
        writeModel2File(newModel, "test.ttl");
    }

    private void writeModel2File(Model subjectModel, String fileName) {
        File file = new File(fileName + ".ttl");
        try (FileWriter myWriter = new FileWriter(fileName + ".ttl")) {
            OutputStream out = new FileOutputStream(file);
            subjectModel.write(out, "TURTLE");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<String> getRdfClasses(Resource resource) {
        StmtIterator propsIter = resource.listProperties(this.rdf_type);
        List<String> rdfClasses = new ArrayList<String>();
        while (propsIter.hasNext()) {
            Statement stmt = propsIter.nextStatement();
            RDFNode object = stmt.getObject();
            if (object instanceof Resource) {
                Resource objectResource = object.asResource();
                rdfClasses.add(objectResource.getURI());
            }
        }
        return rdfClasses;
    }

    public static List<String> filterRdfClasses(List<String> rdfClasses, String pattern, String resourceUri) {

        List<String> ignoreClasses = Arrays.asList(
                "https://w3id.org/okn/o/sdm#Theory-GuidedModel",
                "https://w3id.org/okn/o/sd#SoftwareConfiguration",
                "https://w3id.org/okn/o/sd#ConfigurationSetup",
                "https://w3id.org/okn/o/sdm#EmpiricalModel",
                "https://w3id.org/okn/o/sdm#SpatiallyDistributedGrid",
                "https://w3id.org/okn/o/sdm#PointBasedGrid",
                "https://w3id.org/okn/o/sd#CatalogIdentifier",
                "https://w3id.org/okn/o/sdm#CoupledModel",
                "https://w3id.org/okn/o/sdm#TheoryAndEmpiricalModel",
                "https://w3id.org/okn/o/sd#SampleCollection");
        List<String> filteredRdfClasses = rdfClasses.stream().filter(s -> s.contains(pattern))
                .collect(Collectors.toList());
        for (String s : ignoreClasses) {
            filteredRdfClasses.remove(s);
        }
        if (filteredRdfClasses.size() > 1) {
            System.out.println("Multiple classes " + resourceUri);
        }
        return filteredRdfClasses;
    }

    public String getLocalNameClass(List<String> subjectClasses) {
        if (subjectClasses.size() > 0) {
            if (subjectClasses.size() > 1) {
                System.out.println(subjectClasses.toString());
            }
            return getLocalNameCustom(subjectClasses.get(0), "#");
        }
        return "";
    }

    public String renameURI(String className, Resource resource) {
        String uriResourceString = resource.getURI();
        if (uriResourceString.contains(uriSourcePattern)) {
            return getLocalNamespaceCustom(uriResourceString, "/") + className + '/'
                    + getLocalNameCustom(uriResourceString, "/");
        }
        return uriResourceString;
    }

    public static String getLocalNameCustom(String iri, String pattern) {
        split = iri.split(pattern);
        return split[split.length - 1];
    }

    public static String getLocalNamespaceCustom(String iri, String pattern) {
        String[] splitArray = iri.split(pattern);
        return Arrays.stream(splitArray)
                .limit(splitArray.length - 1)
                .collect(Collectors.joining(pattern)) + '/';
    }

}
