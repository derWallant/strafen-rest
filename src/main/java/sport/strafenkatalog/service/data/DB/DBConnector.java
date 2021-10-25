package sport.strafenkatalog.service.data.DB;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntityType;

public abstract class DBConnector {
		
	public DBConnector() {
	}
	
	public abstract void connectDB() throws Exception;
	
	public abstract void disconnectDB() throws Exception;
	
	public abstract ResultSet readData() throws SQLException, ClassNotFoundException;
	
	public abstract Boolean writeData(Entity newEntit)  throws SQLException;
	
	public abstract Boolean updateData(Entity newEntit, EdmEntityType edmEntityType)  throws SQLException;
	
	public abstract Boolean deleteData(Entity deleteEntit, EdmEntityType edmEntityType) throws SQLException;
}
