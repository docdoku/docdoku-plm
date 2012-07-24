define([
	"i18n",
	"common/date",
    "collections/attached_file_collection"
], function (
	i18n,
	date,
    AttachedFileCollection
) {
	var DocumentIteration = Backbone.Model.extend({
		idAttribute: "iteration",
		initialize: function () {
            var self = this;
            this.className = "DocumentIteration";
			_.bindAll(this);

            var attachedFiles = new AttachedFileCollection(this.get("attachedFiles"));
            this.set("attachedFiles",  attachedFiles);
            attachedFiles.forEach(function(file){
               file.set("documentIteration", self);
            });


            //For the moment, DocumentIteration is built BEFORE the document
            //kumo.assertNotEmpty(this.getDocument(), "no valid document assigned");
            //kumo.assertNotEmpty(this.getIteration(), "no iteration assigned");

            kumo.assert(this.getIteration() == this.id, "id attribute should be the iteration");
            kumo.assertNotAny([ this.get("workspaceId"), this.get("documentMasterId"), this.get("documentMasterVersion")]);

		},
        defaults :{
            attachedFiles :[]
        },

        getWorkspace : function(){
            return this.get("workspaceId");
        },

        getDocument : function(){
            console.log ("Warning : document is Null on this iteration object "+this.cid);
            return this.get("document");
        },
        getIteration : function(){
            return this.get("iteration");
        },
        getDocKey : function(){
            return  this.get("documentMasterId")+"-"+this.get("documentMasterVersion");
        },
        getUrl : function(){
            kumo.assertNotAny([ this.get("workspaceId"), this.get("documentMasterId"), this.get("documentMasterVersion")]);
            var baseUrl ="/api/workspaces/" + this.get("workspaceId")+ "/documents/"+this.getDocKey();
            return baseUrl+"/iterations/"+this.getIteration();
        },
        /**
         *
         * file Upload uses the old servlet, not the JAXRS Api         *
         * return /files/{workspace}/documents/{docId}/{version}/{iteration}/{shortName}
         * @param shortName shortName of the file
         * @returns string
         */
        getUploadUrl: function (shortName) {
            var doc = this.collection.document;

            return "/files/"
                + this.getWorkspace()
                + "/documents/"
                + this.getDocKey().split("-").join("/") // 'doc-B' gives 'doc/B'
                + "/"+this.getIteration()+"/"+shortName
        }
	});
	return DocumentIteration;
});
