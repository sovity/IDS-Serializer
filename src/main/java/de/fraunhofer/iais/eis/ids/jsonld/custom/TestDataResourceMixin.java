package de.fraunhofer.iais.eis.ids.jsonld.custom;

import java.math.BigInteger;
import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.annotation.*;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.UriOrModelClass;

@JsonTypeName( "TestDataResource" )
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "@type"
)
public interface TestDataResourceMixin {
    @JsonIgnore
    List<Agent> getContactPointAsObject();

    @JsonIgnore
    List<URI> getContactPointAsUri();

    @JsonIgnore( false )
    @JsonGetter( "http://www.w3.org/ns/dcat#contactPoint" )
    UriOrModelClass getContactPoint();

    @JsonIgnore
    void setContactPointAsObject( List<Agent> var1 );

    @JsonIgnore
    void setContactPointAsUri( List<URI> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#dataCategory" )
    List<DataCategory> getDataCategory();

    @JsonProperty( "https://w3id.org/mdp/schema#dataCategory" )
    void setDataCategory( List<DataCategory> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#dataCategoryDetail" )
    List<DataCategoryDetail> getDataCategoryDetail();

    @JsonProperty( "https://w3id.org/mdp/schema#dataCategoryDetail" )
    void setDataCategoryDetail( List<DataCategoryDetail> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#transportMode" )
    List<TransportMode> getTransportMode();

    @JsonProperty( "https://w3id.org/mdp/schema#transportMode" )
    void setTransportMode( List<TransportMode> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#mdpBrokering" )
    List<Boolean> getMdpBrokering();

    @JsonProperty( "https://w3id.org/mdp/schema#mdpBrokering" )
    void setMdpBrokering( List<Boolean> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#networkCoverage" )
    List<NetworkCoverage> getNetworkCoverage();

    @JsonProperty( "https://w3id.org/mdp/schema#networkCoverage" )
    void setNetworkCoverage( List<NetworkCoverage> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#roadNetworkCoverageDescription" )
    List<TypedLiteral> getRoadNetworkCoverageDescription();

    @JsonProperty( "https://w3id.org/mdp/schema#roadNetworkCoverageDescription" )
    void setRoadNetworkCoverageDescription( List<TypedLiteral> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#geoReferenceMethod" )
    List<GeoReferenceMethod> getGeoReferenceMethod();

    @JsonProperty( "https://w3id.org/mdp/schema#geoReferenceMethod" )
    void setGeoReferenceMethod( List<GeoReferenceMethod> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#dataModel" )
    List<DataModel> getDataModel();

    @JsonProperty( "https://w3id.org/mdp/schema#dataModel" )
    void setDataModel( List<DataModel> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#metaDataLanguage" )
    List<Language> getMetaDataLanguage();

    @JsonProperty( "https://w3id.org/mdp/schema#metaDataLanguage" )
    void setMetaDataLanguage( List<Language> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#providerEndpoint" )
    List<ConnectorEndpoint> getProviderEndpoint();

    @JsonProperty( "https://w3id.org/mdp/schema#providerEndpoint" )
    void setProviderEndpoint( List<ConnectorEndpoint> var1 );

    @JsonProperty( "http://purl.org/dc/terms/accessRights" )
    List<AccessRights> getAccessRights();

    @JsonProperty( "http://purl.org/dc/terms/accessRights" )
    void setAccessRights( List<AccessRights> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#qualityDescription" )
    List<TypedLiteral> getQualityDescription();

    @JsonProperty( "https://w3id.org/mdp/schema#qualityDescription" )
    void setQualityDescription( List<TypedLiteral> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#NASTAssessment" )
    List<TypedLiteral> getNASTAssessment();

    @JsonProperty( "https://w3id.org/mdp/schema#NASTAssessment" )
    void setNASTAssessment( List<TypedLiteral> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#originalSource" )
    List<String> getOriginalSource();

    @JsonProperty( "https://w3id.org/mdp/schema#originalSource" )
    void setOriginalSource( List<String> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#originalSourceFormat" )
    List<SourceFormat> getOriginalSourceFormat();

    @JsonProperty( "https://w3id.org/mdp/schema#originalSourceFormat" )
    void setOriginalSourceFormat( List<SourceFormat> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#similarTo" )
    List<DataResource> getSimilarTo();

    @JsonProperty( "https://w3id.org/mdp/schema#similarTo" )
    void setSimilarTo( List<DataResource> var1 );

    @JsonProperty( "https://eueip.github.io/napDCAT-AP/conditionForUse" )
    List<String> getConditionForUse();

    @JsonProperty( "https://eueip.github.io/napDCAT-AP/conditionForUse" )
    void setConditionForUse( List<String> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#retentionPeriod" )
    List<BigInteger> getRetentionPeriod();

    @JsonProperty( "https://w3id.org/mdp/schema#retentionPeriod" )
    void setRetentionPeriod( List<BigInteger> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#schemaValidationEnabled" )
    List<Boolean> getSchemaValidationEnabled();

    @JsonProperty( "https://w3id.org/mdp/schema#schemaValidationEnabled" )
    void setSchemaValidationEnabled( List<Boolean> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#virusCheckEnabled" )
    List<Boolean> getVirusCheckEnabled();

    @JsonProperty( "https://w3id.org/mdp/schema#virusCheckEnabled" )
    void setVirusCheckEnabled( List<Boolean> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#legalCheckEnabled" )
    List<Boolean> getLegalCheckEnabled();

    @JsonProperty( "https://w3id.org/mdp/schema#legalCheckEnabled" )
    void setLegalCheckEnabled( List<Boolean> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#publicationId" )
    List<Long> getPublicationId();

    @JsonProperty( "https://w3id.org/mdp/schema#publicationId" )
    void setPublicationId( List<Long> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#resourceId" )
    List<String> getResourceId();

    @JsonProperty( "https://w3id.org/mdp/schema#resourceId" )
    void setResourceId( List<String> var1 );

    @JsonProperty( "https://w3id.org/mdp/schema#originatedFrom" )
    List<Portal> getOriginatedFrom();

    @JsonProperty( "https://w3id.org/mdp/schema#originatedFrom" )
    void setOriginatedFrom( List<Portal> var1 );

    @JsonProperty( "http://www.w3.org/ns/adms#status" )
    List<Status> getStatus();

    @JsonProperty( "http://www.w3.org/ns/adms#status" )
    void setStatus( List<Status> var1 );
}

