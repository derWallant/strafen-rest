package sport.strafenkatalog.service.data.DB.MySQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import sport.strafenkatalog.service.data.DB.DBConnector;

public class MySqlConnector extends DBConnector {

	private Statement statement = null;
	private Connection connObj = null;
	private ResultSet resultSet = null;
	private Boolean boolResult = null;

	private DbSchema schemaObj;
	private DbSpec specficationObj;

	private String tab_name;
	private DbTable table_name;
	private DbColumn column_1, column_2, column_3, column_4;

	private String database = "strafen"; //dbs4099800 //strafen
	private String host = "localhost"; //rdbms.strato.de //localhost
	private String user = "webApp"; //dbu4099800 //webApp
	private String pw = "init2021"; //Wallant1992+- //init2021
	private String conString;
	private String query;
	
	private String pattern = "yyyy-MM-dd";
	private DateFormat df = new SimpleDateFormat(pattern);

	public MySqlConnector(String tabName) throws Exception {
//		this.database = getDatabaseName();
//		this.host = getHostName();
//		this.user = getUserName();
//		this.pw = getPW();
		this.tab_name = tabName;
		this.conString = "jdbc:mysql://" + host + "/" + database + "?user=" + user + "&password=" + pw;
		connectDB();
	}
	
	public String getDatabaseName() {

		return "";
	}
	
	public String getHostName() {
		return "";
	}
	
	public String getUserName() {
		return "";
	}
	
	public String getPW() {
		return "";
	}
	
	@Override
	public void connectDB() throws Exception {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connObj = DriverManager.getConnection(this.conString);
		} catch (ClassNotFoundException | SQLException e) {
			Logger.getGlobal().log(Level.INFO, e.toString());
			throw new Exception(e.getMessage());
		}

	}

	@Override
	public void disconnectDB() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResultSet readData() throws SQLException, ClassNotFoundException {
		statement = connObj.createStatement();
		this.query = "select * from " + database + "." + tab_name;
		resultSet = statement.executeQuery(this.query);
		disconnectDB();
		return resultSet;
	}

	@Override
	public Boolean writeData(Entity newEntity) throws SQLException {
		Logger.getGlobal().log(Level.INFO, "hello");
		statement = connObj.createStatement();
		List<Property> proprties = newEntity.getProperties();
		String columns = "";
		String values = "";
		for (Property prop : proprties) {
			columns = columns + prop.getName() + ", ";

			if (prop.getValue() == null) {
				values = values + "NULL, ";
			} else {
				if (prop.getType().contains("Date")) {
					GregorianCalendar calendar = (GregorianCalendar) prop.getValue();
					df.setCalendar(calendar);
					values = values + "'" + df.format(calendar.getTime()) + "', ";
				} else {
					values = values + "'" + prop.getValue().toString() + "', ";
				}
			}

		}
		columns = columns.substring(0, columns.length() - 2);
		values = values.substring(0, values.length() - 2);

		this.query = "insert into " + database + "." + tab_name 
				+ " ( " + columns + " ) VALUES ( " + values + " )";

		boolResult = statement.execute(this.query);
		disconnectDB();
		return boolResult;
	}

	@Override
	public Boolean updateData(Entity newEntity, EdmEntityType edmEntityType) throws SQLException {
		statement = connObj.createStatement();

		List<Property> properties = newEntity.getProperties();
		String keys = "";
		String values = "";
		String keyPropertyName = "";
		String keyPropertyValue = "";
		List<EdmKeyPropertyRef> keyPropertyRefs = edmEntityType.getKeyPropertyRefs();
		for (EdmKeyPropertyRef propRef : keyPropertyRefs) {
			keyPropertyName = propRef.getName();
			keyPropertyValue = newEntity.getProperty(keyPropertyName).getValue().toString();
			keys = keys + "`" + keyPropertyName + "` = " + keyPropertyValue;
		}
		Logger.getGlobal().log(Level.INFO, keys);
		
		String value = "";
		for (Property prop : properties) {
			if (keyPropertyName == prop.getName()) {
				continue;
			}
			if (prop.getValue() == null) {
				value = "NULL"; //NULL
			} else {
				if (prop.getType().contains("Date")) {
					GregorianCalendar calendar = (GregorianCalendar) prop.getValue();
					df.setCalendar(calendar);
					value = "'" + df.format(calendar.getTime()) + "'";
				}else{
					value = "'" + prop.getValue().toString() + "'";
				}
			}
			values = values + "`" + prop.getName() + "` = " + value + ", ";
		}
		values = values.substring(0, values.length() - 2);
//		Logger.getGlobal().log(Level.INFO, values);
		
		this.query = "UPDATE " + database + "." + tab_name 
				+ " SET " + values + " WHERE " + keys;

//		Logger.getGlobal().log(Level.INFO, this.query);

		boolResult = statement.execute(this.query);
		disconnectDB();
		return boolResult;
	}

	@Override
	public Boolean deleteData(Entity deleteEntity, EdmEntityType edmEntityType) throws SQLException {
		// TODO Auto-generated method stub
		statement = connObj.createStatement();
		String keyPropertyName = "";
		String keyPropertyValue = "";
		String keys = "";
		List<EdmKeyPropertyRef> keyPropertyRefs = edmEntityType.getKeyPropertyRefs();
		for (EdmKeyPropertyRef propRef : keyPropertyRefs) {
			keyPropertyName = propRef.getName();
			keyPropertyValue = deleteEntity.getProperty(keyPropertyName).getValue().toString();
			keys = keys + "`" + keyPropertyName + "` = " + keyPropertyValue;
		}
		
		this.query = "DELETE FROM " + database + "." + tab_name 
				+ " WHERE " + keys;
		Logger.getGlobal().log(Level.INFO, this.query);
		boolResult = statement.execute(this.query);
		disconnectDB();
		return boolResult;
	}

}
