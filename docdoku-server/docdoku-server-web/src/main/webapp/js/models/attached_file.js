define([
    "i18n",
    "models/document_iteration"
], function (
    i18n,
    iteration
    ) {
    var AttachedFile = Backbone.Model.extend({
        initialize: function () {
            this.className = "AttachedFile";
            //expected : documentIteration
            kumo.assert(_.size(this.attributes)== 1,
                    "attributes : "+JSON.stringify(this.attributes)+" ; "+_.size(this.attributes)+" ; Size of this.attributes should be 1, or recheck the name assignation procedure");
            this.set("fullName", _.keys(this.attributes)[0]);
            this.set("shortName", _.last(this.getFullName().split("/")));

            _.bindAll(this);
        },

        getDocumentIteration : function(){
           return this.get("documentIteration");
        },

        getFullName : function(){
            return this.get("fullName");
        },

        getShortName : function(){
            return this.get("shortName");
        },

        getUrl : function(){
            var doc = this.documentIteration.collection.document;
            var docKey = doc.id+"-"+doc.version;
            //documentIterationUrl should be : "/workspaces/"+APP_CONFIG.workspaceId+"/documents/"+docKey+"/iteration/"+this.documentIteration.iteration;
            return this.documentIteration.url+"/files/"+this.name;
        },

        fileUploadUrl: function () {
            var baseUrl = this.getDocumentIteration().getUrl();
            return baseUrl +"/files/"+this.getShortName();
        }
    });
    return AttachedFile;
});