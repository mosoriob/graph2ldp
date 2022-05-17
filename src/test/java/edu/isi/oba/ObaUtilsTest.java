package edu.isi.oba;

import static edu.isi.oba.ObaUtils.get_yaml_data;
import edu.isi.oba.config.YamlConfig;
import java.io.File;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class ObaUtilsTest {

    @Test
    public void read_json_file() throws IOException {
        JSONObject actual = ObaUtils.read_json_file("json_one.json");
        Assert.assertNotNull(actual.get("@context"));
    }

    @Test
    public void mergeJSONObjects() throws IOException {
        JSONObject one = ObaUtils.read_json_file("json_one.json");
        JSONObject two = ObaUtils.read_json_file("json_two.json");
        JSONObject merge = ObaUtils.mergeJSONObjects(one, two);
        Assert.assertNotNull(merge.get("@context"));
        Assert.assertNotNull(merge.get("@context"));
    }

    @Test
    public void concat_json_common_key() throws IOException {
        JSONObject one = ObaUtils.read_json_file("json_one.json");
        JSONObject two = ObaUtils.read_json_file("json_two.json");
        JSONObject three = ObaUtils.read_json_file("json_three.json");
        JSONObject[] jsons = new JSONObject[]{ one, two, three};
        JSONObject merge = ObaUtils.concat_json_common_key(jsons, "@context");
        JSONObject o = (JSONObject) merge.get("@context");
        assertNotNull(o.get("Entity"));
        assertNotNull(o.get("Model"));
        assertNotNull(o.get("Setup"));
    }
    
    @Test
    public void getDescription () throws OWLOntologyCreationException{
        String example_remote = "src/test/config/pplan.yaml";
        YamlConfig config_data = get_yaml_data(example_remote);
        try {
            Mapper mapper = new Mapper(config_data);
            OWLClass planClass = mapper.manager.getOWLDataFactory().getOWLClass("http://purl.org/net/p-plan#Plan");
            String desc = ObaUtils.getDescription(planClass, mapper.ontologies.get(0));
            assertNotEquals(desc, "");
        }catch(Exception e){
            fail();
        }
    }

    /**
     * This test will try to load a file that does not exits. The exception is captured and reported.
     * This test will pass IF you see an error on the output terminal
     * @throws OWLOntologyCreationException
     */
    @Test
    public void missing_file () throws OWLOntologyCreationException{
        String missing_file = "src/test/config/missing_file.yaml";
        YamlConfig config_data = get_yaml_data(missing_file);
        try {
            Mapper mapper = new Mapper(config_data);
            mapper.getClass();
            //if no exception is launched, fail test
            fail();
        }catch(Exception e){
            //pass test if there is an exception
        }
    }

    @Test
    public void run() {
        String ontology1 = "https://mintproject.github.io/Mint-ModelCatalog-Ontology/release/1.4.0/ontology.xml";
        String ontology2 = "https://knowledgecaptureanddiscovery.github.io/SoftwareDescriptionOntology/release/1.5.0/ontology.xml";
        File ont1 = new File("ontology1");
        File ont2 = new File("ontology2");
        ObaUtils.downloadOntology(ontology1, ont1.getPath());
        ObaUtils.downloadOntology(ontology2, ont2.getPath());
        String[] ontologies = new String[]{"ontology1", "ontology2"};
        JSONObject context = null;

    }

}