package sport.strafenkatalog.service.data.DPC;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.uri.UriParameter;

import sport.strafenkatalog.service.data.DB.DBConnector;
import sport.strafenkatalog.service.data.DB.MySQL.MySqlConnector;
import sport.strafenkatalog.service.metadata.MPC.MPC;
import sport.strafenkatalog.service.util.EntityFactory;
import sport.strafenkatalog.service.util.Util;

public class DPC {

	DBConnector dbConnector;
	MPC modelProvider;
	Entity entity = null;

	public DPC(String tabName) throws Exception {
		dbConnector = new MySqlConnector(tabName);
	}

	public Entity getEntity(ODataRequest request, List<UriParameter> keyPredicates, EdmEntitySet edmEntitySet,
			EdmEntityType edmEntityType) throws ClassNotFoundException, ODataException, SQLException {
//		EdmEntitySet startEdmEntitySet = uriResourceEntitySet.getEntitySet();
		EntityCollection entitySet;
//		try {
		entitySet = getEntitySet(request, edmEntitySet);
//		} catch (ClassNotFoundException | ODataException | SQLException e) {
//			throw new ODataException(e.toString());
//		}
		/* generic approach to find the requested entity */
		Entity requestedEntity = Util.findEntity(edmEntityType, entitySet, keyPredicates);

		if (requestedEntity == null) {
			throw new ODataApplicationException("Entity for requested key doesn't exist",
					HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
		return requestedEntity;
	}

	public EntityCollection getEntitySet(ODataRequest request, EdmEntitySet edmEntitySet)
			throws ClassNotFoundException, SQLException, ODataException {

		EntityCollection entityCollection = new EntityCollection();

		List<Entity> entityList = entityCollection.getEntities();

		ResultSet resultSet = dbConnector.readData();// new MySQLAccess(this.TABNAME).readDataBase();
		int x = 1;
		while (resultSet.next()) {
			entity = null;
			entity = new Entity();
			List<CsdlProperty> modelProperties = modelProvider.getEntityType().getProperties();
			for (CsdlProperty property : modelProperties) {
				// Logger.getGlobal().log(Level.INFO, property.getType());
				if (property.getType().contains("String")) {
					entity.addProperty(new Property(null, property.getName(), ValueType.PRIMITIVE,
							resultSet.getString(property.getName())));
				} else if (property.getType().contains("Int")) {
					entity.addProperty(new Property(null, property.getName(), ValueType.PRIMITIVE,
							resultSet.getInt(property.getName())));
				} else if (property.getType().contains("Decimals")) {
					entity.addProperty(new Property(null, property.getName(), ValueType.PRIMITIVE,
							resultSet.getBigDecimal(property.getName())));
				} else if (property.getType().contains("Date")) {
					entity.addProperty(new Property(null, property.getName(), ValueType.PRIMITIVE,
							resultSet.getDate(property.getName())));
				}
			}
			entity.setId(createId(edmEntitySet.getName(), x));
			entityList.add(entity);
			x++;

		}

		return entityCollection;
	}

	public Entity create(ODataRequest request, EdmEntitySet edmEntitySet, EdmEntityType edmEntityType, Entity entity)
			throws ODataException, SQLException {

		final Entity newEntity = new Entity();
		List<Entity> entityList;
		// Logger.getGlobal().log(Level.INFO, edmEntitySet.getName());
		// Logger.getGlobal().log(Level.INFO, edmEntityType.getName());
		try {
			entityList = getEntitySet(request, edmEntitySet).getEntities();
		} catch (Exception e) {
			throw new ODataException(e.getMessage());
		}

		newEntity.setType(entity.getType());

		// Create the new key of the entity
		int newId = 1;
		// Logger.getGlobal().log(Level.INFO, "Zeile 117");
		while (entityIdExists(newId, entityList)) {
			newId++;
		}
		// Logger.getGlobal().log(Level.INFO, "Zeile 121");
		// Add all provided properties
		newEntity.getProperties().addAll(entity.getProperties());

		// Add the key property
		// newEntity.getProperties().add(new Property(null, "id", ValueType.PRIMITIVE,
		// newId));
		// Logger.getGlobal().log(Level.INFO, "Zeile 128");
		newEntity.setId(createId(edmEntitySet.getName(), newId));

		Logger.getGlobal().log(Level.INFO, "Zeile 134");
		dbConnector.writeData(newEntity);

//		// 2.1.) Apply binding links
//		for (final Link link : entity.getNavigationBindings()) {
//			// Zeile 326-340: src/main/java/myservice/mynamespace/data/Storage.java
//		}
//
//		// 2.2.) Create nested entities
//		for (final Link link : entity.getNavigationLinks()) {
//			// Zeile 342-356: src/main/java/myservice/mynamespace/data/Storage.java
//		}

		return newEntity;
	}

	public Entity update(ODataRequest request, EdmEntitySet edmEntitySet, List<UriParameter> keyParams,
			Entity updateEntity, HttpMethod httpMethod)
			throws ODataApplicationException, ODataException, ClassNotFoundException, SQLException {

		Entity entity = getEntity(request, keyParams, edmEntitySet, edmEntitySet.getEntityType());
		if (entity == null) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(),
					Locale.ENGLISH);
		}

		List<Property> existingProperties = entity.getProperties();
		for (Property existingProp : existingProperties) {
			String propName = existingProp.getName();
			// ignore the key properties, they aren't updateable
			if (Util.isKey(edmEntitySet.getEntityType(), propName)) {
				continue;
			}
			Property updateProperty = updateEntity.getProperty(propName);
			if (updateProperty == null) {
				// if a property has NOT been added to the request payload
				// depending on the HttpMethod, our behavior is different
				if (httpMethod.equals(HttpMethod.PATCH)) {
					// as of the OData spec, in case of PATCH, the existing property is not touched
					continue; // do nothing
				} else if (httpMethod.equals(HttpMethod.PUT)) {
					// as of the OData spec, in case of PUT, the existing property is set to null
					// (or to default value)
					existingProp.setValue(existingProp.getValueType(), null);
					continue;
				}
			}
			// change the value of the properties
			existingProp.setValue(existingProp.getValueType(), updateProperty.getValue());
		}
		Logger.getGlobal().log(Level.INFO, "Zeile 184");
		dbConnector.updateData(updateEntity, edmEntitySet.getEntityType());

		return updateEntity;

	}

	public Entity delete(ODataRequest request, EdmEntitySet edmEntitySet, List<UriParameter> keyParams)
			throws ODataException, ClassNotFoundException, SQLException {

		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

		Entity entity = getEntity(request, keyParams, edmEntitySet, edmEntityType);// getEntity(edmEntityType,
																					// keyParams, entityList);
		if (entity == null) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(),
					Locale.ENGLISH);
		}
		dbConnector.deleteData(entity, edmEntitySet.getEntityType());

		return entity;
	}

	protected URI createId(String entitySetName, Object id) {
		try {
			return new URI(entitySetName + "(" + String.valueOf(id) + ")");
		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
		}
	}

	private URI createId(Entity entity, String idPropertyName) {
		return createId(entity, idPropertyName, null);
	}

	private URI createId(Entity entity, String idPropertyName, String navigationName) {
		try {
			String entitySetName = EntityFactory.getMetaDataProviderByEntityType(entity.getType()).getEntitySet()
					.getName();

			StringBuilder sb = new StringBuilder(entitySetName).append("(");
			final Property property = entity.getProperty(idPropertyName);
			sb.append(property.asPrimitive()).append(")");
			if (navigationName != null) {
				sb.append("/").append(navigationName);
			}
			return new URI(sb.toString());
		} catch (URISyntaxException | ODataException e) {
			throw new ODataRuntimeException("Unable to create (Atom) id for entity: " + entity, e);
		}
	}

	private boolean entityIdExists(int id, List<Entity> entityList) {

		for (Entity entity : entityList) {
			Integer existingID = (Integer) entity.getProperty("id").getValue();
			if (existingID.intValue() == id) {
				return true;
			}
		}

		return false;
	}

}
