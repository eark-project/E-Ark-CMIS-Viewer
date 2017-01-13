package dk.magenta.eark.cmis.system;

import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service
@Singleton
public class PropertiesHandlerImpl implements PropertiesHandler {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesHandlerImpl.class);

    private Properties properties;

    public PropertiesHandlerImpl() {

        InputStream in = getClass().getClassLoader().getResourceAsStream("settings.properties");

        properties = new Properties();
        try {
            properties.load(in);
            in.close();
        } catch (IOException e) {
            logger.error("******** Error Reading from properties file ********\n");
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
