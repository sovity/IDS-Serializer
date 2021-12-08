package de.fraunhofer.iais.eis.ids.jsonld;

import java.math.BigInteger;
import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.Beta;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.UriOrModelClass;

@JsonTypeInfo(
        use = Id.NAME,
        property = "@type"
)
@JsonSubTypes({@Type( TestDataResourceImpl.class)})
public interface TestDataResource {
    @Beta
    DataResource deepCopy();

    List<DataCategory> getDataCategory();

    void setDataCategory(List<DataCategory> var1);

    List<DataCategoryDetail> getDataCategoryDetail();

    void setDataCategoryDetail(List<DataCategoryDetail> var1);

    List<TransportMode> getTransportMode();

    void setTransportMode(List<TransportMode> var1);

    List<Boolean> getMdpBrokering();

    void setMdpBrokering(List<Boolean> var1);

    List<NetworkCoverage> getNetworkCoverage();

    void setNetworkCoverage(List<NetworkCoverage> var1);

    List<TypedLiteral> getRoadNetworkCoverageDescription();

    void setRoadNetworkCoverageDescription(List<TypedLiteral> var1);

    List<GeoReferenceMethod> getGeoReferenceMethod();

    void setGeoReferenceMethod(List<GeoReferenceMethod> var1);

    List<DataModel> getDataModel();

    void setDataModel(List<DataModel> var1);

    List<Language> getMetaDataLanguage();

    void setMetaDataLanguage(List<Language> var1);

    List<ConnectorEndpoint> getProviderEndpoint();

    void setProviderEndpoint(List<ConnectorEndpoint> var1);

    List<AccessRights> getAccessRights();

    void setAccessRights(List<AccessRights> var1);

    List<TypedLiteral> getQualityDescription();

    void setQualityDescription(List<TypedLiteral> var1);

    List<TypedLiteral> getNASTAssessment();

    void setNASTAssessment(List<TypedLiteral> var1);

    List<Agent> getContactPointAsObject();

    void setContactPointAsObject(List<Agent> var1);

    List<URI> getContactPointAsUri();

    void setContactPointAsUri(List<URI> var1);

    @JsonIgnore
    UriOrModelClass getContactPoint();

    List<String> getOriginalSource();

    void setOriginalSource(List<String> var1);

    List<SourceFormat> getOriginalSourceFormat();

    void setOriginalSourceFormat(List<SourceFormat> var1);

    List<DataResource> getSimilarTo();

    void setSimilarTo(List<DataResource> var1);

    List<String> getConditionForUse();

    void setConditionForUse(List<String> var1);

    List<BigInteger> getRetentionPeriod();

    void setRetentionPeriod(List<BigInteger> var1);

    List<Boolean> getSchemaValidationEnabled();

    void setSchemaValidationEnabled(List<Boolean> var1);

    List<Boolean> getVirusCheckEnabled();

    void setVirusCheckEnabled(List<Boolean> var1);

    List<Boolean> getLegalCheckEnabled();

    void setLegalCheckEnabled(List<Boolean> var1);

    List<Long> getPublicationId();

    void setPublicationId(List<Long> var1);

    List<String> getResourceId();

    void setResourceId(List<String> var1);

    List<Portal> getOriginatedFrom();

    void setOriginatedFrom(List<Portal> var1);

    List<Status> getStatus();

    void setStatus(List<Status> var1);
}

