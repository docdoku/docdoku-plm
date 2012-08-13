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
            kumo.assertNotEmpty(this.get("created"), "created attribute not set at AttachedFile creation");

            //_.bindAll(this);
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

        //used to delete a file
        url : function(){
            return this.getDocumentIteration().url()+"/files/"+this.getShortName();
        },

        //Url used for downloading (it's the same servlet as upload)
        getUrl : function(){
            return this.getDocumentIteration().getUploadUrl(this.getShortName());
        },


        //TODO check if delete
        fileUploadUrl: function () {
            var baseUrl = this.getDocumentIteration().getUrl();
            return baseUrl +"/files/"+this.getShortName();
        },

        toString : function(){
            return this.getShortName();
        },

        isCreated : function(){

            var result = kumo.isNotEmpty(this.getFullName());
            return result;
        },

        isNew : function(){
            return false;
        }
    });
    return AttachedFile;
});