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

public class Sentence_MPC extends MPC {

	public static final String ET_NAME = "Sentence";
	public static final FullQualifiedName ET_FQN = new FullQualifiedName(MetaDataProvider.NAMESPACE, ET_NAME);
	public static final String ES_NAME = "Sentences";

	public CsdlEntityType getEntityType() throws ODataException {

		// properties
		CsdlProperty id = new CsdlProperty().setName("id")
				.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
		CsdlProperty playerId = new CsdlProperty().setName("playerId")
				.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
		CsdlProperty punishmentId = new CsdlProperty().setName("punishmentId")
				.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
		CsdlProperty date = new CsdlProperty().setName("date")
				.setType(EdmPrimitiveTypeKind.Date.getFullQualifiedName());
		CsdlProperty clearedOffAt = new CsdlProperty().setName("clearedOffAt")
				.setType(EdmPrimitiveTypeKind.Date.getFullQualifiedName());
		CsdlProperty clearedOffTo = new CsdlProperty().setName("clearedOffTo")
				.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
		CsdlProperty comment = new CsdlProperty().setName("comment")
				.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

		// create CsdlPropertyRef for Key element
		CsdlPropertyRef propertyRef = new CsdlPropertyRef();
		propertyRef.setName("id");

		// configure EntityType
		CsdlEntityType entityType = new CsdlEntityType();
		entityType.setName(ET_NAME);
		entityType.setProperties(Arrays.asList(id, playerId, punishmentId, date, clearedOffAt, clearedOffTo, comment));
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
