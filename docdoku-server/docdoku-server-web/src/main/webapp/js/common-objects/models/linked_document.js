define(function () {
    var linkedDocument = Backbone.Model.extend({

        initialize: function () {
            this.id = this.getReference();
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
        }
    });
    return linkedDocument;
});
