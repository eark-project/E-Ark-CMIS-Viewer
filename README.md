# E-Ark-CMIS-Viewer
A Browser based viewer for CMIS 1.0 complaint repositories.

This is the code repository for the E-Ark, browser based, CMIS viewer project
All issues concerning features and issues with UI should to be reported here.

## Requirements
For the backend<br/>
1. A servlet container such as Tomcat(7+)<br/>
2. Java 1.8+<br/>
3. Apache Maven 3.3+<br/>

For the frontend<br/>
1. Angular JS - 1.4.12+<br/>
2. Gulp build tool (requires Node.js and npm)<br/>
3. A webserver like apache2 or Nginx<br/>

## Get up and running
Preparing the Server:<br/>
1. Install Java [1.8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)<br/>
2. Install a relational database such as [Mariadb](https://downloads.mariadb.org/)/[PostgreSql](https://www.postgresql.org/download/) using the appropriate method(s) for the operating system.<br/>
3. Create the database  on the database server using this script located in the: [bridge/src/main/sql/setup_db.sql](https://github.com/magenta-aps/E-Ark-CMIS-Viewer/blob/master/bridge/src/main/sql/setup_db.sql) directory<br/>
4. Install [Node.js and Npm](https://docs.npmjs.com/getting-started/installing-node)<br/>
5. Install [Apache Maven 3.3+](https://maven.apache.org/install.html)<br/>
6. Install a **Java Servlet Container** such as [Apache Tomcat 7+](https://tomcat.apache.org/tomcat-7.0-doc/appdev/installation.html)<br/>
7. Install a HTML webserver such as [Apache webserver](https://httpd.apache.org/docs/2.4/install.html) or [Nginx](https://www.nginx.com/resources/wiki/start/topics/tutorials/install/)<br/>
 
Installing the viewer:
 - Clone the project
 - For the bridge/backend:<br/>
1 - In the root of the bridge folder, build the project using maven by execuing the following command in a terminal: `mvn clean package`.<br/>There should now be a resulting `*.war` file in [bridge/target]() directory named `cmis-bridge.war`<br/>
2 - Install the warfile in your java servlet container<br/>
3 - Browse to [http://server-address:port/app-context/webapi/system/check]() and it should result in a simple HTML page twith the following message: `CMIS Bridge is up and running!`<br/>(Note that the "app-context" portion of the url is optional. In case the war isn't deployed as the ROOT context in the servlet container. The default app-context will be `cmis-bridge`, unless of course the WAR file is renamed to something else)<br/>

- Installing the UI:
## ToDo

**Good to go**

- [x] Repository Profile creation - maybe add some documentation
- [x] Mapping.xml addition - Some proper error reporting to be done
- [x] Repository browsing - Some documentation needed
- [x] Translations and local selection (i18n) - Some parts of the UI still require additional translation

**Outstanding**
- [ ] export (ongoing)
- [ ] search
