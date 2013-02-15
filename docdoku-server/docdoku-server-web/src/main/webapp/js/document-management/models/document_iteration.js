define([
	"i18n!localization/nls/document-management-strings",
    "common-objects/utils/date",
    "collections/attribute_collection",
    "collections/attached_file_collection"
], function (
	i18n,
	date,
    AttributeCollection,
    AttachedFileCollection
) {
	var DocumentIteration = Backbone.Model.extend({

        url: function() {
            return this.collection.url()+"/"+this.getIteration();
        },

		initialize: function () {
            this.id = this.getReference();

            this.className = "DocumentIteration";

            var attributes = new AttributeCollection(this.get("instanceAttributes"));

            var filesMapping = _.map(this.get("attachedFiles"), function(fullName){
                return {
                    "fullName":fullName,
                    shortName : _.last(fullName.split("/")),
                    created : true
                }
            });
            var attachedFiles = new AttachedFileCollection(filesMapping);

            //'attributes' is a special name for Backbone
            this.set("instanceAttributes", attributes);
            this.set("attachedFiles",  attachedFiles);
		},

        defaults :{
            attachedFiles :[],
            instanceAttributes : []
        },

        getAttachedFiles : function(){
          return this.get("attachedFiles");
        },

        getAttributes : function(){
            return this.get("instanceAttributes");
        },

        getWorkspace : function(){
            return this.get("workspaceId");
        },

        getReference : function(){
            return this.getDocKey() + "-" + this.getIteration();
        },

        getIteration : function(){
            return this.get("iteration");
        },

        getDocumentMasterId : function(){
            return  this.get("documentMasterId");
        },

        getDocumentMasterVersion : function(){
            return  this.get("documentMasterVersion");
        },

        getDocKey : function(){
            return  this.getDocumentMasterId()+"-"+this.getDocumentMasterVersion();
        },

        getLinkedDocuments : function(){
            return this.get("linkedDocuments");
        },

        getDocumentMasterPermalink : function(){
            return encodeURI(
                window.location.origin
                    + "/documents/"
                    + this.getWorkspace()
                    + "/"
                    + this.getDocumentMasterId()
                    + "/"
                    + this.getDocumentMasterVersion()
            );
        },

        /**
         * file Upload uses the old servlet, not the JAXRS Api         *
         * return /files/{workspace}/documents/{docId}/{version}/{iteration}/
         * @returns string
         */
        getUploadBaseUrl: function () {
            return "/files/" + this.getWorkspace() + "/documents/" + this.get("documentMasterId") + "/" + this.get("documentMasterVersion") + "/"+ this.getIteration()+"/";
        }
	});
	return DocumentIteration;
});
