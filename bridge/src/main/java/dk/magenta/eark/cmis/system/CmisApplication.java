package dk.magenta.eark.cmis.system;

import dk.magenta.eark.cmis.bridge.authentication.AuthenticationService;
import dk.magenta.eark.cmis.bridge.authentication.AuthenticationServiceImpl;
import dk.magenta.eark.cmis.bridge.db.DatabaseConnectionStrategy;
import dk.magenta.eark.cmis.bridge.db.DatabaseWorker;
import dk.magenta.eark.cmis.bridge.db.DatabaseWorkerImpl;
import dk.magenta.eark.cmis.bridge.db.JDBCConnectionStrategy;
import dk.magenta.eark.cmis.repository.CmisSessionWorker;
import dk.magenta.eark.cmis.repository.CmisSessionWorkerImpl;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Singleton;

/**
 * @author DarkStar1.
 */
public class CmisApplication extends ResourceConfig{

    public CmisApplication(){
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(DatabaseWorkerImpl.class).to(DatabaseWorker.class);
                bind(CmisSessionWorkerImpl.class).to(CmisSessionWorker.class);
                bind(AuthenticationServiceImpl.class).to(AuthenticationService.class);
                bind(JDBCConnectionStrategy.class).to(DatabaseConnectionStrategy.class);
                bind(PropertiesHandlerImpl.class).to(PropertiesHandler.class).in(Singleton.class);
            }
        });
        packages(true, "dk.magenta.eark.cmis");
    }
}
