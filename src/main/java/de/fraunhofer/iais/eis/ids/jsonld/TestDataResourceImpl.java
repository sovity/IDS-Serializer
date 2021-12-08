
package de.fraunhofer.iais.eis.ids.jsonld;


import javax.validation.constraints.NotNull;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.UriOrModelClass;
import de.fraunhofer.iais.eis.util.VocabUtil;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@JsonTypeName("ids:TestDataResource")
public class TestDataResourceImpl implements TestDataResource {
    @JsonProperty("@id")
    @JsonAlias({"@id", "id"})
    @NotNull
    protected URI                id    = VocabUtil.getInstance().createRandomUrl("dataResource");
    @JsonIgnore
    protected List<TypedLiteral>  label   = Arrays.asList(new TypedLiteral("Data Resource", "en"));
    @JsonIgnore
    protected List<TypedLiteral>  comment = Arrays
            .asList(new TypedLiteral("Resource (at least partially) comprising data content.", "en"));
    @JsonIgnore
    protected Map<String, Object> properties;
    @JsonAlias({"http://purl.org/dc/terms/accessRights", "accessRights"})
    protected List<AccessRights>  _accessRights = new ArrayList();
    @JsonAlias({"http://purl.org/dc/terms/rights", "rights"})
    protected List<MdpLicense>    _rights       = new ArrayList();
    @JsonAlias({"http://www.w3.org/ns/adms#status", "status"})
    protected List<Status> _status = new ArrayList();
    @JsonIgnore
    @JsonAlias({"http://www.w3.org/ns/dcat#contactPoint", "contactPoint"})
    protected UriOrModelClass _contactPoint;
    @JsonAlias({"http://www.w3.org/ns/dcat#contactPointAsObject", "contactPointAsObject"})
    protected List<Agent> _contactPointAsObject = new ArrayList();
    @JsonAlias({"http://www.w3.org/ns/dcat#contactPointAsUri", "contactPointAsUri"})
    protected List<URI> _contactPointAsUri = new ArrayList();
    @JsonAlias({"https://eueip.github.io/napDCAT-AP/conditionForUse", "conditionForUse"})
    protected List<String> _conditionForUse = new ArrayList();
    @JsonAlias({"ids:accrualPeriodicity", "accrualPeriodicity"})
    protected Frequency _accrualPeriodicity;
    @JsonAlias({"ids:contentPart", "contentPart"})
    protected List<DigitalContent> _contentPart = new ArrayList();
    @JsonAlias({"ids:contentStandard", "contentStandard"})
    protected URI _contentStandard;
    @JsonAlias({"ids:contentType", "contentType"})
    protected ContentType _contentType;
    @JsonAlias({"ids:contractOffer", "contractOffer"})
    protected List<ContractOffer> _contractOffer = new ArrayList();
    @JsonAlias({"ids:created", "created"})
    protected XMLGregorianCalendar _created;
    @JsonAlias({"ids:customLicense", "customLicense"})
    protected URI _customLicense;
    @JsonAlias({"ids:defaultRepresentation", "defaultRepresentation"})
    protected List<Representation> _defaultRepresentation = new ArrayList();
    @JsonAlias({"ids:description", "description"})
    protected List<TypedLiteral> _description = new ArrayList();
    @JsonAlias({"ids:keyword", "keyword"})
    protected List<TypedLiteral> _keyword = new ArrayList();
    @JsonAlias({"ids:language", "language"})
    protected List<Language>       _language = new ArrayList();
    @JsonAlias({"ids:modified", "modified"})
    protected XMLGregorianCalendar _modified;
    @JsonAlias({"ids:paymentModality", "paymentModality"})
    protected PaymentModality      _paymentModality;
    @JsonAlias({"ids:publisher", "publisher"})
    protected URI _publisher;
    @JsonAlias({"ids:representation", "representation"})
    protected List<Representation> _representation = new ArrayList();
    @JsonAlias({"ids:resourceEndpoint", "resourceEndpoint"})
    protected List<ConnectorEndpoint> _resourceEndpoint = new ArrayList();
    @JsonAlias({"ids:resourcePart", "resourcePart"})
    protected List<Resource> _resourcePart = new ArrayList();
    @JsonAlias({"ids:sample", "sample"})
    protected List<Resource> _sample = new ArrayList();
    @JsonAlias({"ids:shapesGraph", "shapesGraph"})
    protected URI _shapesGraph;
    @JsonAlias({"ids:sovereign", "sovereign"})
    protected URI _sovereign;
    @JsonAlias({"ids:spatialCoverage", "spatialCoverage"})
    protected List<Location> _spatialCoverage = new ArrayList();
    @JsonAlias({"ids:standardLicense", "standardLicense"})
    protected URI _standardLicense;
    @JsonAlias({"ids:temporalCoverage", "temporalCoverage"})
    protected List<TemporalEntity> _temporalCoverage = new ArrayList();
    @JsonAlias({"ids:temporalResolution", "temporalResolution"})
    protected Frequency _temporalResolution;
    @JsonAlias({"ids:theme", "theme"})
    protected List<URI> _theme = new ArrayList();
    @JsonAlias({"ids:title", "title"})
    protected List<TypedLiteral> _title = new ArrayList();
    @JsonAlias({"ids:variant", "variant"})
    protected Resource _variant;
    @JsonAlias({"ids:version", "version"})
    protected String _version;
    @JsonAlias({"https://w3id.org/mdp/schema#NASTAssessment", "nASTAssessment"})
    protected List<TypedLiteral> _nASTAssessment = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#dataCategory", "dataCategory"})
    protected List<DataCategory> _dataCategory = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#dataCategoryDetail", "dataCategoryDetail"})
    protected List<DataCategoryDetail> _dataCategoryDetail = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#dataModel", "dataModel"})
    protected List<DataModel> _dataModel = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#geoReferenceMethod", "geoReferenceMethod"})
    protected List<GeoReferenceMethod> _geoReferenceMethod = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#legalCheckEnabled", "legalCheckEnabled"})
    protected List<Boolean> _legalCheckEnabled = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#mdpBrokering", "mdpBrokering"})
    protected List<Boolean> _mdpBrokering = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#metaDataLanguage", "metaDataLanguage"})
    protected List<Language> _metaDataLanguage = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#networkCoverage", "networkCoverage"})
    protected List<NetworkCoverage> _networkCoverage = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#originalSource", "originalSource"})
    protected List<String> _originalSource = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#originalSourceFormat", "originalSourceFormat"})
    protected List<SourceFormat> _originalSourceFormat = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#originatedFrom", "originatedFrom"})
    protected List<Portal> _originatedFrom = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#providerEndpoint", "providerEndpoint"})
    protected List<ConnectorEndpoint> _providerEndpoint = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#publicationId", "publicationId"})
    protected List<Long> _publicationId = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#qualityDescription", "qualityDescription"})
    protected List<TypedLiteral> _qualityDescription = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#resourceId", "resourceId"})
    protected List<String>       _resourceId                     = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#retentionPeriod", "retentionPeriod"})
    protected List<BigInteger>   _retentionPeriod                = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#roadNetworkCoverageDescription", "roadNetworkCoverageDescription"})
    protected List<TypedLiteral> _roadNetworkCoverageDescription = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#schemaValidationEnabled", "schemaValidationEnabled"})
    protected List<Boolean> _schemaValidationEnabled = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#similarTo", "similarTo"})
    protected List<DataResource> _similarTo = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#transportMode", "transportMode"})
    protected List<TransportMode> _transportMode = new ArrayList();
    @JsonAlias({"https://w3id.org/mdp/schema#virusCheckEnabled", "virusCheckEnabled"})
    protected List<Boolean> _virusCheckEnabled = new ArrayList();

    public TestDataResourceImpl() {
    }

    @JsonProperty("@id")
    public final URI getId() {
        return this.id;
    }

    public String toRdf() {
        return VocabUtil.getInstance().toRdf(this);
    }

    public String toString() {
        return this.toRdf();
    }

    public List<TypedLiteral> getLabel() {
        return this.label;
    }

    public List<TypedLiteral> getComment() {
        return this.comment;
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        if (this.properties == null) {
            return null;
        } else {
            Iterator<String> iter = this.properties.keySet().iterator();
            HashMap resultset = new HashMap();

            while(iter.hasNext()) {
                String key = (String)iter.next();
                resultset.put(key, this.urifyObjects(this.properties.get(key)));
            }

            return resultset;
        }
    }

    public Object urifyObjects(Object value) {
        if (value instanceof String && value.toString().startsWith("http")) {
            try {
                value = new URI(value.toString());
            } catch (Exception var3) {
            }
        } else {
            if (value instanceof ArrayList) {
                ArrayList<Object> result_array = new ArrayList();
                ((ArrayList)value).forEach((x) -> {
                    result_array.add(this.urifyObjects(x));
                });
                return result_array;
            }

            if (value instanceof Map) {
                Map<String, Object> result_map = new HashMap();
                ((Map)value).forEach((k, v) -> {
                    result_map.put(k.toString(), this.urifyObjects(v));
                });
                return result_map;
            }
        }

        return value;
    }

    @JsonAnySetter
    public void setProperty(String property, Object value) {
        if (this.properties == null) {
            this.properties = new HashMap();
        }

        if (!property.startsWith("@")) {
            this.properties.put(property, value);
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this._dataCategory, this._dataCategoryDetail, this._transportMode, this._mdpBrokering, this._networkCoverage, this._roadNetworkCoverageDescription, this._geoReferenceMethod, this._dataModel, this._metaDataLanguage, this._providerEndpoint, this._accessRights, this._qualityDescription, this._nASTAssessment, this._contactPoint, this._contactPointAsUri, this._originalSource, this._originalSourceFormat, this._similarTo, this._conditionForUse, this._retentionPeriod, this._schemaValidationEnabled, this._virusCheckEnabled, this._legalCheckEnabled, this._publicationId, this._resourceId, this._originatedFrom, this._status, this._standardLicense, this._customLicense, this._resourcePart, this._resourceEndpoint, this._contractOffer, this._paymentModality, this._publisher, this._sovereign, this._sample, this._variant, this._rights, this._contentType, this._contentPart, this._representation, this._defaultRepresentation, this._theme, this._keyword, this._temporalCoverage, this._temporalResolution, this._spatialCoverage, this._accrualPeriodicity, this._contentStandard, this._language, this._created, this._modified, this._title, this._description, this._shapesGraph, this._version});
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            TestDataResourceImpl other = (TestDataResourceImpl)obj;
            return Objects.equals(this._dataCategory, other._dataCategory) && Objects.equals(this._dataCategoryDetail, other._dataCategoryDetail) && Objects.equals(this._transportMode, other._transportMode) && Objects.equals(this._mdpBrokering, other._mdpBrokering) && Objects.equals(this._networkCoverage, other._networkCoverage) && Objects.equals(this._roadNetworkCoverageDescription, other._roadNetworkCoverageDescription) && Objects.equals(this._geoReferenceMethod, other._geoReferenceMethod) && Objects.equals(this._dataModel, other._dataModel) && Objects.equals(this._metaDataLanguage, other._metaDataLanguage) && Objects.equals(this._providerEndpoint, other._providerEndpoint) && Objects.equals(this._accessRights, other._accessRights) && Objects.equals(this._qualityDescription, other._qualityDescription) && Objects.equals(this._nASTAssessment, other._nASTAssessment) && Objects.equals(this._contactPoint, other._contactPoint) && Objects.equals(this._contactPointAsUri, other._contactPointAsUri) && Objects.equals(this._originalSource, other._originalSource) && Objects.equals(this._originalSourceFormat, other._originalSourceFormat) && Objects.equals(this._similarTo, other._similarTo) && Objects.equals(this._conditionForUse, other._conditionForUse) && Objects.equals(this._retentionPeriod, other._retentionPeriod) && Objects.equals(this._schemaValidationEnabled, other._schemaValidationEnabled) && Objects.equals(this._virusCheckEnabled, other._virusCheckEnabled) && Objects.equals(this._legalCheckEnabled, other._legalCheckEnabled) && Objects.equals(this._publicationId, other._publicationId) && Objects.equals(this._resourceId, other._resourceId) && Objects.equals(this._originatedFrom, other._originatedFrom) && Objects.equals(this._status, other._status) && Objects.equals(this._standardLicense, other._standardLicense) && Objects.equals(this._customLicense, other._customLicense) && Objects.equals(this._resourcePart, other._resourcePart) && Objects.equals(this._resourceEndpoint, other._resourceEndpoint) && Objects.equals(this._contractOffer, other._contractOffer) && Objects.equals(this._paymentModality, other._paymentModality) && Objects.equals(this._publisher, other._publisher) && Objects.equals(this._sovereign, other._sovereign) && Objects.equals(this._sample, other._sample) && Objects.equals(this._variant, other._variant) && Objects.equals(this._rights, other._rights) && Objects.equals(this._contentType, other._contentType) && Objects.equals(this._contentPart, other._contentPart) && Objects.equals(this._representation, other._representation) && Objects.equals(this._defaultRepresentation, other._defaultRepresentation) && Objects.equals(this._theme, other._theme) && Objects.equals(this._keyword, other._keyword) && Objects.equals(this._temporalCoverage, other._temporalCoverage) && Objects.equals(this._temporalResolution, other._temporalResolution) && Objects.equals(this._spatialCoverage, other._spatialCoverage) && Objects.equals(this._accrualPeriodicity, other._accrualPeriodicity) && Objects.equals(this._contentStandard, other._contentStandard) && Objects.equals(this._language, other._language) && Objects.equals(this._created, other._created) && Objects.equals(this._modified, other._modified) && Objects.equals(this._title, other._title) && Objects.equals(this._description, other._description) && Objects.equals(this._shapesGraph, other._shapesGraph) && Objects.equals(this._version, other._version);
        }
    }

    public DataResource deepCopy() {return  null;}

    public List<DataCategory> getDataCategory() {
        return this._dataCategory;
    }

    public void setDataCategory(List<DataCategory> _dataCategory_) {
        this._dataCategory = _dataCategory_;
    }

    public List<DataCategoryDetail> getDataCategoryDetail() {
        return this._dataCategoryDetail;
    }

    public void setDataCategoryDetail(List<DataCategoryDetail> _dataCategoryDetail_) {
        this._dataCategoryDetail = _dataCategoryDetail_;
    }

    public List<TransportMode> getTransportMode() {
        return this._transportMode;
    }

    public void setTransportMode(List<TransportMode> _transportMode_) {
        this._transportMode = _transportMode_;
    }

    public List<Boolean> getMdpBrokering() {
        return this._mdpBrokering;
    }

    public void setMdpBrokering(List<Boolean> _mdpBrokering_) {
        this._mdpBrokering = _mdpBrokering_;
    }

    public List<NetworkCoverage> getNetworkCoverage() {
        return this._networkCoverage;
    }

    public void setNetworkCoverage(List<NetworkCoverage> _networkCoverage_) {
        this._networkCoverage = _networkCoverage_;
    }

    public List<TypedLiteral> getRoadNetworkCoverageDescription() {
        return this._roadNetworkCoverageDescription;
    }

    public void setRoadNetworkCoverageDescription(List<TypedLiteral> _roadNetworkCoverageDescription_) {
        this._roadNetworkCoverageDescription = _roadNetworkCoverageDescription_;
    }

    public List<GeoReferenceMethod> getGeoReferenceMethod() {
        return this._geoReferenceMethod;
    }

    public void setGeoReferenceMethod(List<GeoReferenceMethod> _geoReferenceMethod_) {
        this._geoReferenceMethod = _geoReferenceMethod_;
    }

    public List<DataModel> getDataModel() {
        return this._dataModel;
    }

    public void setDataModel(List<DataModel> _dataModel_) {
        this._dataModel = _dataModel_;
    }

    public List<Language> getMetaDataLanguage() {
        return this._metaDataLanguage;
    }

    public void setMetaDataLanguage(List<Language> _metaDataLanguage_) {
        this._metaDataLanguage = _metaDataLanguage_;
    }

    public List<ConnectorEndpoint> getProviderEndpoint() {
        return this._providerEndpoint;
    }

    public void setProviderEndpoint(List<ConnectorEndpoint> _providerEndpoint_) {
        this._providerEndpoint = _providerEndpoint_;
    }

    public List<AccessRights> getAccessRights() {
        return this._accessRights;
    }

    public void setAccessRights(List<AccessRights> _accessRights_) {
        this._accessRights = _accessRights_;
    }

    public List<TypedLiteral> getQualityDescription() {
        return this._qualityDescription;
    }

    public void setQualityDescription(List<TypedLiteral> _qualityDescription_) {
        this._qualityDescription = _qualityDescription_;
    }

    public List<TypedLiteral> getNASTAssessment() {
        return this._nASTAssessment;
    }

    public void setNASTAssessment(List<TypedLiteral> _nASTAssessment_) {
        this._nASTAssessment = _nASTAssessment_;
    }

    public List<Agent> getContactPointAsObject() {
        return this._contactPointAsObject;
    }

    public void setContactPointAsObject(List<Agent> _contactPoint_) {
        this._contactPointAsObject = _contactPoint_;
        //this._contactPointAsUri = new ArrayList();
    }

    public List<URI> getContactPointAsUri() {
        return this._contactPointAsUri;
    }

    public void setContactPointAsUri(List<URI> _contactPoint_) {
        this._contactPointAsUri = _contactPoint_;
        //this._contactPointAsObject = new ArrayList();
    }

    public UriOrModelClass getContactPoint() {
        if (!this._contactPointAsUri.isEmpty()) {
            return new UriOrModelClass(this._contactPointAsUri);
        } else {
            return !this._contactPointAsObject.isEmpty() ? new UriOrModelClass(this._contactPointAsObject) : null;
        }
    }

    public List<String> getOriginalSource() {
        return this._originalSource;
    }

    public void setOriginalSource(List<String> _originalSource_) {
        this._originalSource = _originalSource_;
    }

    public List<SourceFormat> getOriginalSourceFormat() {
        return this._originalSourceFormat;
    }

    public void setOriginalSourceFormat(List<SourceFormat> _originalSourceFormat_) {
        this._originalSourceFormat = _originalSourceFormat_;
    }

    public List<DataResource> getSimilarTo() {
        return this._similarTo;
    }

    public void setSimilarTo(List<DataResource> _similarTo_) {
        this._similarTo = _similarTo_;
    }

    public List<String> getConditionForUse() {
        return this._conditionForUse;
    }

    public void setConditionForUse(List<String> _conditionForUse_) {
        this._conditionForUse = _conditionForUse_;
    }

    public List<BigInteger> getRetentionPeriod() {
        return this._retentionPeriod;
    }

    public void setRetentionPeriod(List<BigInteger> _retentionPeriod_) {
        this._retentionPeriod = _retentionPeriod_;
    }

    public List<Boolean> getSchemaValidationEnabled() {
        return this._schemaValidationEnabled;
    }

    public void setSchemaValidationEnabled(List<Boolean> _schemaValidationEnabled_) {
        this._schemaValidationEnabled = _schemaValidationEnabled_;
    }

    public List<Boolean> getVirusCheckEnabled() {
        return this._virusCheckEnabled;
    }

    public void setVirusCheckEnabled(List<Boolean> _virusCheckEnabled_) {
        this._virusCheckEnabled = _virusCheckEnabled_;
    }

    public List<Boolean> getLegalCheckEnabled() {
        return this._legalCheckEnabled;
    }

    public void setLegalCheckEnabled(List<Boolean> _legalCheckEnabled_) {
        this._legalCheckEnabled = _legalCheckEnabled_;
    }

    public List<Long> getPublicationId() {
        return this._publicationId;
    }

    public void setPublicationId(List<Long> _publicationId_) {
        this._publicationId = _publicationId_;
    }

    public List<String> getResourceId() {
        return this._resourceId;
    }

    public void setResourceId(List<String> _resourceId_) {
        this._resourceId = _resourceId_;
    }

    public List<Portal> getOriginatedFrom() {
        return this._originatedFrom;
    }

    public void setOriginatedFrom(List<Portal> _originatedFrom_) {
        this._originatedFrom = _originatedFrom_;
    }

    public List<Status> getStatus() {
        return this._status;
    }

    public void setStatus(List<Status> _status_) {
        this._status = _status_;
    }

    public URI getStandardLicense() {
        return this._standardLicense;
    }

    public void setStandardLicense(URI _standardLicense_) {
        this._standardLicense = _standardLicense_;
    }

    public URI getCustomLicense() {
        return this._customLicense;
    }

    public void setCustomLicense(URI _customLicense_) {
        this._customLicense = _customLicense_;
    }

    public List<Resource> getResourcePart() {
        return this._resourcePart;
    }

    public void setResourcePart(List<Resource> _resourcePart_) {
        this._resourcePart = _resourcePart_;
    }

    public List<ConnectorEndpoint> getResourceEndpoint() {
        return this._resourceEndpoint;
    }

    public void setResourceEndpoint(List<ConnectorEndpoint> _resourceEndpoint_) {
        this._resourceEndpoint = _resourceEndpoint_;
    }

    public List<ContractOffer> getContractOffer() {
        return this._contractOffer;
    }

    public void setContractOffer(List<ContractOffer> _contractOffer_) {
        this._contractOffer = _contractOffer_;
    }

    public PaymentModality getPaymentModality() {
        return this._paymentModality;
    }

    public void setPaymentModality(PaymentModality _paymentModality_) {
        this._paymentModality = _paymentModality_;
    }

    public URI getPublisher() {
        return this._publisher;
    }

    public void setPublisher(URI _publisher_) {
        this._publisher = _publisher_;
    }

    public URI getSovereign() {
        return this._sovereign;
    }

    public void setSovereign(URI _sovereign_) {
        this._sovereign = _sovereign_;
    }

    public List<Resource> getSample() {
        return this._sample;
    }

    public void setSample(List<Resource> _sample_) {
        this._sample = _sample_;
    }

    public Resource getVariant() {
        return this._variant;
    }

    public void setVariant(Resource _variant_) {
        this._variant = _variant_;
    }

    public List<MdpLicense> getRights() {
        return this._rights;
    }

    public void setRights(List<MdpLicense> _rights_) {
        this._rights = _rights_;
    }

    public ContentType getContentType() {
        return this._contentType;
    }

    public void setContentType(ContentType _contentType_) {
        this._contentType = _contentType_;
    }

    public List<DigitalContent> getContentPart() {
        return this._contentPart;
    }

    public void setContentPart(List<DigitalContent> _contentPart_) {
        this._contentPart = _contentPart_;
    }

    public List<Representation> getRepresentation() {
        return this._representation;
    }

    public void setRepresentation(List<Representation> _representation_) {
        this._representation = _representation_;
    }

    public List<Representation> getDefaultRepresentation() {
        return this._defaultRepresentation;
    }

    public void setDefaultRepresentation(List<Representation> _defaultRepresentation_) {
        this._defaultRepresentation = _defaultRepresentation_;
    }

    public List<URI> getTheme() {
        return this._theme;
    }

    public void setTheme(List<URI> _theme_) {
        this._theme = _theme_;
    }

    public List<TypedLiteral> getKeyword() {
        return this._keyword;
    }

    public void setKeyword(List<TypedLiteral> _keyword_) {
        this._keyword = _keyword_;
    }

    public List<TemporalEntity> getTemporalCoverage() {
        return this._temporalCoverage;
    }

    public void setTemporalCoverage(List<TemporalEntity> _temporalCoverage_) {
        this._temporalCoverage = _temporalCoverage_;
    }

    public Frequency getTemporalResolution() {
        return this._temporalResolution;
    }

    public void setTemporalResolution(Frequency _temporalResolution_) {
        this._temporalResolution = _temporalResolution_;
    }

    public List<Location> getSpatialCoverage() {
        return this._spatialCoverage;
    }

    public void setSpatialCoverage(List<Location> _spatialCoverage_) {
        this._spatialCoverage = _spatialCoverage_;
    }

    public Frequency getAccrualPeriodicity() {
        return this._accrualPeriodicity;
    }

    public void setAccrualPeriodicity(Frequency _accrualPeriodicity_) {
        this._accrualPeriodicity = _accrualPeriodicity_;
    }

    public URI getContentStandard() {
        return this._contentStandard;
    }

    public void setContentStandard(URI _contentStandard_) {
        this._contentStandard = _contentStandard_;
    }

    public List<Language> getLanguage() {
        return this._language;
    }

    public void setLanguage(List<Language> _language_) {
        this._language = _language_;
    }

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSzzz"
    )
    public XMLGregorianCalendar getCreated() {
        return this._created;
    }

    public void setCreated(XMLGregorianCalendar _created_) {
        this._created = _created_;
    }

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSzzz"
    )
    public XMLGregorianCalendar getModified() {
        return this._modified;
    }

    public void setModified(XMLGregorianCalendar _modified_) {
        this._modified = _modified_;
    }

    public List<TypedLiteral> getTitle() {
        return this._title;
    }

    public void setTitle(List<TypedLiteral> _title_) {
        this._title = _title_;
    }

    public List<TypedLiteral> getDescription() {
        return this._description;
    }

    public void setDescription(List<TypedLiteral> _description_) {
        this._description = _description_;
    }

    public URI getShapesGraph() {
        return this._shapesGraph;
    }

    public void setShapesGraph(URI _shapesGraph_) {
        this._shapesGraph = _shapesGraph_;
    }

    public String getVersion() {
        return this._version;
    }

    public void setVersion(String _version_) {
        this._version = _version_;
    }
}
