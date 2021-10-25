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

public class Punishment_MPC extends MPC {
	
	public static final String ET_NAME = "Punishment";
	public static final FullQualifiedName ET_FQN = new FullQualifiedName(MetaDataProvider.NAMESPACE, ET_NAME);
	public static final String ES_NAME = "Punishments";
	
	public CsdlEntityType getEntityType() throws ODataException {
				
		// properties
		CsdlProperty id = new CsdlProperty().setName("id").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
		CsdlProperty catalogueId = new CsdlProperty().setName("catalogueId")
				.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
		CsdlProperty name = new CsdlProperty().setName("name")
				.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());	
		CsdlProperty description = new CsdlProperty().setName("description")
				.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
		CsdlProperty amount = new CsdlProperty().setName("amount")
				.setType(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName());
		CsdlProperty type = new CsdlProperty().setName("type")
				.setType(EdmPrimitiveTypeKind.Int16.getFullQualifiedName());
		CsdlProperty punishment = new CsdlProperty().setName("punishment")
				.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
		
		// create CsdlPropertyRef for Key element
		CsdlPropertyRef propertyRef = new CsdlPropertyRef();
		propertyRef.setName("id");

		// configure EntityType
		CsdlEntityType entityType = new CsdlEntityType();
		entityType.setName(ET_NAME);
		entityType.setProperties(Arrays.asList(id, catalogueId, name, description, amount, type, punishment));
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
