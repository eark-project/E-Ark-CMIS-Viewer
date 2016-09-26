package dk.magenta.eark.cmis.bridge;

public class Constants {

	// Accepted extraction formats

	// JSON keys
	public static final String SUCCESS = "success";
	public static final String MESSAGE = "message";
	public static final String ERRORMSG = "error";

	// File paths
	public static final String SETTINGS = "settings.properties";
	public static final String CMIS_SETTINGS_PATH = "cmis.properties";

	public static final String OBJECT_ID = "objectId";
	public static final String BASETYPE_ID = "type";
	public static final String PATH = "path";
	public static final String NAME = "name";
	public static final String PARENT_ID = "parentId";
	public static final String CREATED_BY = "createdBy";
	public static final String CREATION_DATE = "creationDate";
	public static final String OBJECT_TYPE_ID = "objectTypeId";
	public static final String DESCRIPTION = "description";
	public static final String LAST_MODIFIED = "lastModifiedBy";
	public static final String LAST_MOD_DATE = "lastModificationDate";

	//Some props that are for documents
	public static final String CONTENT_SIZE = "size";
	public static final String CONTENT_STREAM_LENGTH = "contentStreamLength";
	public static final String CONTENT_STREAM_FILENAME ="contentStreamFileName"; //Actual filename
	public static final String CONTENT_STREAM_ID = "contentStreamId";
	public static final String CONTENT_STREAM_MIMETYPE = "contentStreamMimeType";
}
