package sport.strafenkatalog.service.data;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.MediaEntityProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;

import sport.strafenkatalog.service.data.DPC.DPC;
import sport.strafenkatalog.service.util.EntityFactory;
import sport.strafenkatalog.service.util.Util;

public class DataProvider implements EntityCollectionProcessor, EntityProcessor, MediaEntityProcessor {
	private OData odata;
	private ServiceMetadata serviceMetadata;


	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}

	public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

		// 1st we have retrieve the requested EntitySet from the uriInfo object
		// (representation of the parsed service URI)
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); // in our example, the
																									// first segment is
																									// the EntitySet
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		EntityCollection entitySet = null;
		// 2nd: fetch the data from backend for this requested EntitySetName
		// it has to be delivered as EntitySet object
		// EntityCollection entitySet = getData(edmEntitySet);

		try {
			DPC dataPrvd = EntityFactory.getDataProvider(edmEntityType.getName());
			entitySet = dataPrvd.getEntitySet(request, edmEntitySet);

		} catch (Exception e) {

			throw new ODataApplicationException(e.toString(), 500, null);
		}

		// 3rd: create a serializer based on the requested format (json)
		ODataSerializer serializer = odata.createSerializer(responseFormat);

		// 4th: Now serialize the content: transform from the EntitySet object to
		// InputStream;
		ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();

		final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
		EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().id(id).contextURL(contextUrl)
				.build();
		SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, entitySet,
				opts);
		InputStream serializedContent = serializerResult.getContent();

		// Finally: configure the response object: set the body, headers and status code
		response.setContent(serializedContent);
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	@Override
	public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
			throws ODataApplicationException, ODataLibraryException {
		UriResource uriResource = uriInfo.getUriResourceParts().get(0);

		if (uriResource instanceof UriResourceEntitySet) {
			try {
				readEntityInternal(request, response, uriInfo, responseFormat);
			} catch (ODataException e) {
				// TODO Auto-generated catch block
				throw new ODataApplicationException(e.getMessage(), 500, null);
			}
		} else if (uriResource instanceof UriResourceFunction) {
			readFunctionImportInternal(request, response, uriInfo, responseFormat);
		} else {
			throw new ODataApplicationException("Only EntitySet is supported",
					HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
		}

	}

	private void readFunctionImportInternal(final ODataRequest request, final ODataResponse response,
			final UriInfo uriInfo, final ContentType responseFormat)
			throws ODataApplicationException, SerializerException {

	}

	private void readEntityInternal(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType responseFormat) throws ODataException {

		Entity responseEntity = null; // required for serialization of the response body
		EdmEntitySet responseEdmEntitySet = null; // we need this for building the contextUrl

		// 1st step: retrieve the requested Entity: can be "normal" read operation, or
		// navigation (to-one)
		List<UriResource> resourceParts = uriInfo.getUriResourceParts();
		int segmentCount = resourceParts.size();

		UriResource uriResource = resourceParts.get(0); // in our example, the first segment is the EntitySet
		if (!(uriResource instanceof UriResourceEntitySet)) {
			throw new ODataApplicationException("Only EntitySet is supported",
					HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
		}

		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriResource;
		EdmEntitySet startEdmEntitySet = uriResourceEntitySet.getEntitySet();


		// Analyze the URI segments
		if (segmentCount == 1) { // no navigation
			responseEdmEntitySet = startEdmEntitySet; // since we have only one segment

			List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
			EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
			EdmEntityType edmEntityType = responseEdmEntitySet.getEntityType();	
								
			DPC dataPrvd;
			try {
				dataPrvd = EntityFactory.getDataProvider(edmEntityType.getName());
				responseEntity = dataPrvd.getEntity(request, keyPredicates, edmEntitySet, edmEntityType);
			} catch ( Exception e) {
				throw new ODataException(e.toString());
			}
			

			// handle $select
			SelectOption selectOption = uriInfo.getSelectOption();
			// in our example, we don't have performance issues, so we can rely upon the
			// handling in the Olingo lib
			// nothing else to be done

			// handle $expand
			ExpandOption expandOption = uriInfo.getExpandOption();
			// Nested system query options are not implemented
			validateNestedExpxandSystemQueryOptions(expandOption);

			// 4. serialize

			// we need the property names of the $select, in order to build the context URL
			String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, expandOption,
					selectOption);
			ContextURL contextUrl = ContextURL.with().entitySet(responseEdmEntitySet).selectList(selectList)
					.suffix(Suffix.ENTITY).build();

			// make sure that $expand and $select are considered by the serializer
			// adding the selectOption to the serializerOpts will actually tell the lib to
			// do the job
			EntitySerializerOptions opts = EntitySerializerOptions.with().contextURL(contextUrl).select(selectOption)
					.expand(expandOption).build();

			ODataSerializer serializer = this.odata.createSerializer(responseFormat);
			SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntityType, responseEntity, opts);

			// 5. configure the response object
			response.setContent(serializerResult.getContent());
			response.setStatusCode(HttpStatusCode.OK.getStatusCode());
			response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

			// responseEntity = storage.readEntityData(startEdmEntitySet, keyPredicates);
		}
	}

	@Override
	public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
			ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
	    // 1. Retrieve the entity type from the URI
	    EdmEntitySet edmEntitySet = Util.getEdmEntitySet(uriInfo);
	    EdmEntityType edmEntityType = edmEntitySet.getEntityType();
	    
	    InputStream requestInputStream = request.getBody();
	    ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
	    DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
	    Entity requestEntity = result.getEntity();
	    // 2.2 do the creation in backend, which returns the newly created entity
	    Entity createdEntity = null;
	    
	    DPC dataPrvd;
		try {
			dataPrvd = EntityFactory.getDataProvider(edmEntityType.getName());
			createdEntity = dataPrvd.create(request, edmEntitySet, edmEntityType, requestEntity);
		} catch (ODataException | SQLException e) {
			Logger.getGlobal().log(Level.INFO, e.getMessage());
			throw new ODataApplicationException(e.getMessage(), 500, null);
		}

	    // 3. serialize the response (we have to return the created entity)
	    ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
	    EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build(); 
	    ODataSerializer serializer = this.odata.createSerializer(responseFormat);
	    Logger.getGlobal().log(Level.INFO, "Zeile 247");
	    Logger.getGlobal().log(Level.INFO, createdEntity.getType());
	    SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, options);

	    // 4. configure the response object
	    Logger.getGlobal().log(Level.INFO, "Zeile 251");
	    final String location = request.getRawBaseUri() + '/'
	        + odata.createUriHelper().buildCanonicalURL(edmEntitySet, createdEntity);
	    
	    Logger.getGlobal().log(Level.INFO, "Zeile 255");
	    response.setHeader(HttpHeader.LOCATION, location);
	    response.setContent(serializedResponse.getContent());
	    response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
	    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	@Override
	public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
			ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

	    // 1. Retrieve the entity set which belongs to the requested entity
	    List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
	    // Note: only in our example we can assume that the first segment is the EntitySet
	    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
	    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
	    EdmEntityType edmEntityType = edmEntitySet.getEntityType();

	    // 2. update the data in backend
	    // 2.1. retrieve the payload from the PUT request for the entity to be updated
	    InputStream requestInputStream = request.getBody();
	    ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
	    DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
	    Entity requestEntity = result.getEntity();
	    // 2.2 do the modification in backend
	    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
	    // Note that this updateEntity()-method is invoked for both PUT or PATCH operations
	    HttpMethod httpMethod = request.getMethod();	    
	   
	 //   storage.updateEntityData(edmEntitySet, keyPredicates, requestEntity, httpMethod);
	    DPC dataPrvd;
	    try {
			dataPrvd = EntityFactory.getDataProvider(edmEntityType.getName());
			dataPrvd.update(request, edmEntitySet, keyPredicates, requestEntity, httpMethod);
		} catch (ODataException | ClassNotFoundException | SQLException e) {
			Logger.getGlobal().log(Level.INFO, e.getMessage());
			throw new ODataApplicationException(e.getMessage(), 500, null);
		}
	    
	    // 3. configure the response object
	    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());	    
	    
	}

	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
			throws ODataApplicationException, ODataLibraryException {

	    // 1. Retrieve the entity set which belongs to the requested entity
	    List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
	    // Note: only in our example we can assume that the first segment is the EntitySet
	    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
	    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
	    EdmEntityType edmEntityType = edmEntitySet.getEntityType();
	    
	    // 2. delete the data in backend
	    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
//	    storage.deleteEntityData(edmEntitySet, keyPredicates);
	    DPC dataPrvd;
	    try {
			dataPrvd = EntityFactory.getDataProvider(edmEntityType.getName());
			dataPrvd.delete(request, edmEntitySet, keyPredicates);
		} catch (ODataException | ClassNotFoundException | SQLException e) {
			Logger.getGlobal().log(Level.INFO, e.getMessage());
			throw new ODataApplicationException(e.getMessage(), 500, null);
		}
	    // 3. configure the response object
	    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	  }
		
	@Override
	public void readMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void createMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType requestFormat, ContentType responseFormat)
			throws ODataApplicationException, ODataLibraryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType requestFormat, ContentType responseFormat)
			throws ODataApplicationException, ODataLibraryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
			throws ODataApplicationException, ODataLibraryException {
		// TODO Auto-generated method stub

	}

	private void validateNestedExpxandSystemQueryOptions(final ExpandOption expandOption)
			throws ODataApplicationException {
		if (expandOption == null) {
			return;
		}

		for (final ExpandItem item : expandOption.getExpandItems()) {
			if (item.getCountOption() != null || item.getFilterOption() != null || item.getLevelsOption() != null
					|| item.getOrderByOption() != null || item.getSearchOption() != null
					|| item.getSelectOption() != null || item.getSkipOption() != null || item.getTopOption() != null) {

				throw new ODataApplicationException("Nested expand system query options are not implemented",
						HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
			}
		}
	}

}
