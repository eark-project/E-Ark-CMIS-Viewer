package dk.magenta.eark.cmis.system;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface PropertiesHandler {
	String getProperty(String key);
	void setProperty(String key, String value);
}
