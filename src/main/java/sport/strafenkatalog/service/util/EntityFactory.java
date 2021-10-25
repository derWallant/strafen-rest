package sport.strafenkatalog.service.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.ex.ODataException;

import sport.strafenkatalog.service.data.DPC.DPC;
import sport.strafenkatalog.service.metadata.MetaDataProvider;
import sport.strafenkatalog.service.metadata.MPC.Catalogue_MPC;
import sport.strafenkatalog.service.metadata.MPC.MPC;
import sport.strafenkatalog.service.metadata.MPC.Player_MPC;
import sport.strafenkatalog.service.metadata.MPC.Punishment_MPC;
import sport.strafenkatalog.service.metadata.MPC.Sentence_MPC;
import sport.strafenkatalog.service.metadata.MPC.Team_MPC;
import sport.strafenkatalog.service.metadata.MPC.User_MPC;

public class EntityFactory {

	protected static ArrayList<CsdlEntityType> entityTypes = null;
	protected static ArrayList<CsdlEntitySet> entitySets = null;

	public static ArrayList<CsdlEntityType> getEntityTypes() throws ODataException {
		if (entityTypes == null) {

			entityTypes = new ArrayList<CsdlEntityType>();

			entityTypes.add(new Team_MPC().getEntityType());
			entityTypes.add(new Player_MPC().getEntityType());
			entityTypes.add(new User_MPC().getEntityType());
			entityTypes.add(new Catalogue_MPC().getEntityType());
			entityTypes.add(new Punishment_MPC().getEntityType());
			entityTypes.add(new Sentence_MPC().getEntityType());
		}
		return entityTypes;
	}

	public static CsdlEntityContainer getEntityContainer() throws ODataException {

		// create EntityContainer
		CsdlEntityContainer entityContainer = new CsdlEntityContainer();
		entityContainer.setName(MetaDataProvider.CONTAINER_NAME);
		entityContainer.setEntitySets(getEntitySets());

		return entityContainer;

	}

	public static List<CsdlEntitySet> getEntitySets() throws ODataException {
		if (entitySets == null) {
			entitySets = new ArrayList<CsdlEntitySet>();
			entitySets.add(new Team_MPC().getEntitySet());
			entitySets.add(new Player_MPC().getEntitySet());
			entitySets.add(new User_MPC().getEntitySet());
			entitySets.add(new Catalogue_MPC().getEntitySet());
			entitySets.add(new Punishment_MPC().getEntitySet());
			entitySets.add(new Sentence_MPC().getEntitySet());
			
		}

		return entitySets;
	}

	public static DPC getDataProvider(String entityType) throws ODataException {

		try {
			String className = "sport.strafenkatalog.service.data.DPC." + entityType + "_DPC";
			Class<?> clazz;			
			clazz = Class.forName(className);
			java.lang.reflect.Constructor<?> constructor = clazz.getConstructors()[0];
			Object instance;			
			Logger.getGlobal().log(Level.INFO, constructor.getName());
			instance = constructor.newInstance();
			Logger.getGlobal().log(Level.INFO, "Zeile 85");
			return (DPC) instance;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			Logger.getGlobal().log(Level.INFO, "Hau raus das Ding");
			throw new ODataException("EntityType doesn't exist. (" + entityType + ")["+ e.toString() +"]");
		}

	}

	public static MPC getMetaDataProviderByEntityType(String entityType) throws ODataException {
		try {
			String className = "sport.strafenkatalog.service.metadata.MPC." + entityType + "_MPC";
			Class<?> clazz = Class.forName(className);
			java.lang.reflect.Constructor<?> constructor = clazz.getConstructors()[0];
			Object instance = constructor.newInstance();

			return (MPC) instance;
		} catch (ClassNotFoundException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new ODataException("EntityType doesn't exist.");
		}
	}

	public static MPC getMetaDataProviderByEntitySet(String entitySet) throws ODataException {
		String entityType = entitySet.substring(0, entitySet.length() - 1);
		return getMetaDataProviderByEntityType(entityType);
	}
}
