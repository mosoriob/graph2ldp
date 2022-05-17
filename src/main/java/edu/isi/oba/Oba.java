package edu.isi.oba;
import edu.isi.oba.config.*;
import java.io.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

class Oba {
  public static final String SERVERS_ZIP = "/servers.zip";
  public static final String SERVERS_DIRECTORY = "servers";
  static Logger logger = null;
  public enum LANGUAGE {
    PYTHON_FLASK
  }

  public static void main(String[] args) throws Exception {
    /*
    TODO: we are supporting one language. Issue #42
    */
	
   InputStream stream = Oba.class.getClassLoader().getResourceAsStream("logging.properties");
    try {
      LogManager.getLogManager().readConfiguration(stream);
      logger = Logger.getLogger(Oba.class.getName());
      logger.setUseParentHandlers(false);//remove double logging

    } catch (IOException e) {
      e.printStackTrace();
    }

    logger.setLevel(Level.FINE);
    logger.addHandler(new ConsoleHandler());
	  
    //parse command line
    String config_yaml = ObaUtils.get_config_yaml(args);
    //read the config yaml from command line
    YamlConfig config_data = new YamlConfig();

    try {
      config_data = ObaUtils.get_yaml_data(config_yaml);
    } catch (Exception e){
      logger.severe("Error parsing the configuration file. Please make sure it is valid \n " + e);
      System.exit(1);
    }

    String destination_dir = config_data.getOutput_dir() + File.separator + config_data.getName();
    try {
        Mapper mapper = new Mapper(config_data);
        mapper.createBasicContainers(destination_dir, config_data);
    }catch (Exception e){
        logger.severe("Error while creating the API specification: "+e.getLocalizedMessage());
        e.printStackTrace();
        System.exit(1);
    }
  }
}
