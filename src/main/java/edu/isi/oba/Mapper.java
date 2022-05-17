package edu.isi.oba;

import static edu.isi.oba.Oba.logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import edu.isi.oba.config.YamlConfig;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

class Mapper {
    public static final String DEFAULT_DIR_QUERY = "_default_";
    public final Map<IRI, String> schemaNames = new HashMap<>(); //URI-names of the schemas
    public final Map<IRI, String> schemaDescriptions = new HashMap<>(); //URI-description of the schemas
    public Map<String, Schema> schemas = new HashMap<>();
    final Paths paths = new Paths();
    List<String> selected_paths;
    List<OWLOntology> ontologies;
    List<OWLClass> selected_classes;
    List<OWLClass> mappedClasses;
    YamlConfig config_data;

    public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private Boolean follow_references;

    public Mapper(YamlConfig config_data) throws OWLOntologyCreationException, IOException {
        this.config_data = config_data;
        this.selected_paths = config_data.getPaths();
        this.mappedClasses = new ArrayList<>();
        this.follow_references = config_data.getFollow_references();

        List<String> config_ontologies = config_data.getOntologies();
        String destination_dir = config_data.getOutput_dir() + File.separator + config_data.getName();
        File outputDir = new File(destination_dir);
        if (!outputDir.exists()){
            outputDir.mkdirs();
        }
        //Load the ontology into the manager
        int i = 0;
        List<String> ontologyPaths = new ArrayList<>();
        download_ontologies(config_ontologies, destination_dir, i, ontologyPaths);
        //set ontology paths in YAML to the ones we have downloaded (for later reference by owl2jsonld)
        this.config_data.setOntologies(ontologyPaths);
        ontologies = this.manager.ontologies().collect(Collectors.toList());

        //Create a temporal Map<IRI, String> schemaNames with the classes
        for (OWLOntology ontology : ontologies) {
            Set<OWLClass> classes = ontology.getClassesInSignature();
            setSchemaNames(classes);
            setSchemaDrescriptions(classes,ontology);
        }
        if (config_data.getClasses() != null)
            this.selected_classes = filter_classes();
    }

    private void download_ontologies(List<String> config_ontologies, String destination_dir, int i, List<String> ontologyPaths) throws OWLOntologyCreationException, IOException {
        for (String ontologyPath : config_ontologies) {
            //copy the ontologies used in the destination folder
            String destinationPath = destination_dir + File.separator +"ontology"+i+".owl";
            File ontologyFile = new File (destinationPath);
            //content negotiation + download in case a URI is added
            if(ontologyPath.startsWith("http://") || ontologyPath.startsWith("https://")){
                //download ontology to local path
                ObaUtils.downloadOntology(ontologyPath, destinationPath);
            }
            else{
                try {
                    //copy to the right folder
                    Files.copy(new File(ontologyPath).toPath(), ontologyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    Logger.getLogger(Mapper.class.getName()).log(Level.SEVERE, "ERROR while loading file: "+ontologyPath, ex);
                    throw ex;
                }
            }
            System.out.println(destinationPath);
            ontologyPaths.add(destinationPath);
            // Set to silent so missing imports don't make the program fail.
            OWLOntologyLoaderConfiguration loadingConfig = new OWLOntologyLoaderConfiguration();
            loadingConfig = loadingConfig.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
            this.manager.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(destinationPath)), loadingConfig);
            i++;
        }
    }

    /**
     * Obtain Schemas using the ontology classes
     * The schemas includes the properties
     *
     * @param destination_dir directory to write the final results
     * @param config_data yaml configuration
     */
    public void createBasicContainers(String destination_dir, YamlConfig config_data) {
        for (OWLOntology ontology : this.ontologies) {
            Set<OWLClass> classes = ontology.getClassesInSignature();
            for (OWLClass cls : classes) {
                //filter if the class prefix does not have the default ontology prefix
                if (cls.getIRI() != null) {
                    if (selected_classes == null || selected_classes.contains(cls)){
                        logger.log(Level.SEVERE,"Could not parse class "+cls.getIRI().toString());
                        String containerDirectoryPath = config_data.getOutput_dir() + File.separator + cls.getIRI().getShortForm();
                        File containerDirectory = new File(containerDirectoryPath);
                        if (!containerDirectory.exists()){
                            containerDirectory.mkdirs();
                        }
                    }
                }
            }
        }
    }

    
    /**
     * Given a set of classes from an ontology, this method initializes
     * schemaDescriptions with the definitions used to describe an ontology (if provided)
     * @param classes the classes you want the description for
     * @param ontology the ontology from where we will extract the descriptions
     */
    private void setSchemaDrescriptions(Set<OWLClass> classes,OWLOntology ontology){
       for (OWLClass cls : classes) {
           System.out.println(cls);
           schemaDescriptions.put(cls.getIRI(), ObaUtils.getDescription(cls, ontology));
       }
    }


    public List<OWLClass> filter_classes() {
        List<String> selected_classes_iri = this.config_data.getClasses();
        ArrayList<OWLClass> filtered_classes = new ArrayList();
        for (OWLOntology ontology : this.ontologies) {
            for (OWLClass cls : ontology.getClassesInSignature()) {
                if (selected_classes_iri.contains(cls.getIRI().toString())) {
                    filtered_classes.add(cls);
                }
            }
        }
        return filtered_classes;
    }

    private void setSchemaNames(Set<OWLClass> classes) {
        for (OWLClass cls : classes) {
            schemaNames.put(cls.getIRI(), cls.getIRI().getShortForm());
        }
    }
    
}
