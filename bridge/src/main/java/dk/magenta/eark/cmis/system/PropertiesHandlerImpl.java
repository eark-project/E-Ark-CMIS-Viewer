package dk.magenta.eark.cmis.system;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesHandlerImpl implements PropertiesHandler {

	private Properties properties;
	
	public PropertiesHandlerImpl(String pathToPropertiesFile) {
		
		InputStream in = getClass().getClassLoader().getResourceAsStream(pathToPropertiesFile);
		
		properties = new Properties();
		try {
			properties.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getProperty(String key) {
		// TODO Throw exception if invalid key is given
		return properties.getProperty(key); 
	}

	@Override
	public void setProperty(String key, String value) {
		// TODO: write properties to file also

	}

}
