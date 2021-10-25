package sport.strafenkatalog.service.metadata.MPC;

import java.util.Arrays;
import java.util.Collections;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.ex.ODataException;

import sport.strafenkatalog.service.metadata.MetaDataProvider;

public class Player_MPC extends MPC {
	
	public static final String ET_NAME = "Player";
	public static final FullQualifiedName ET_FQN = new FullQualifiedName(MetaDataProvider.NAMESPACE, ET_NAME);
	public static final String ES_NAME = "Players";
	
	public CsdlEntityType getEntityType() throws ODataException {
				
		// properties
		CsdlProperty id = new CsdlProperty().setName("id").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
		CsdlProperty firstname = new CsdlProperty().setName("firstname")
				.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
		CsdlProperty lastname = new CsdlProperty().setName("lastname")
				.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
		CsdlProperty userId = new CsdlProperty().setName("userId")
				.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
		CsdlProperty teamId = new CsdlProperty().setName("teamId")
				.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
		
		// create CsdlPropertyRef for Key element
		CsdlPropertyRef propertyRef = new CsdlPropertyRef();
		propertyRef.setName("id");

		// configure EntityType
		CsdlEntityType entityType = new CsdlEntityType();
		entityType.setName(ET_NAME);
		entityType.setProperties(Arrays.asList(id, firstname, lastname, userId, teamId));
		entityType.setKey(Collections.singletonList(propertyRef));

		return entityType;

	}

	public CsdlEntitySet getEntitySet() throws ODataException {
		CsdlEntitySet entitySet = new CsdlEntitySet();
		entitySet.setName(ES_NAME);
		entitySet.setType(ET_FQN);

		return entitySet;
	}

}
