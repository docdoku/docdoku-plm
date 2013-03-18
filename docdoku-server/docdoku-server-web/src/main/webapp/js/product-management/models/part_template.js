define([
    "common-objects/collections/file/attached_file_collection",
    "common-objects/utils/date",
    "i18n!localization/nls/document-management-strings"
], function (AttachedFileCollection,Date,i18n) {
	var Template = Backbone.Model.extend({
        initialize:function(){
            this.className = "Template";
        },

        parse: function(response) {
            var filesMapping = _.map(response.attachedFiles, function(fullName){
                return {
                    "fullName":fullName,
                    shortName : _.last(fullName.split("/")),
                    created : true
                };
            });
            response.attachedFiles = new AttachedFileCollection(filesMapping);
            return response;
        },

        defaults :{
            attachedFiles :[]
        },

        toJSON: function(){
            return this.clone().set({attributeTemplates :
                _.reject(this.get("attributeTemplates"),
                    function(attribute){
                        return attribute.name == "";
                    }
                )}, {silent: true}).attributes;
        },

        getUploadBaseUrl: function () {
            return "/files/" + this.get("workspaceId") + "/part-templates/" + this.get("id")+"/";
        },

        getId:function(){
            return this.get("id");
        },

        getAuthorName:function(){
            return this.get("author").name;
        },

        getCreationDate:function(){
            return this.get("creationDate");
        },

        getFormattedCreationDate:function(){
            return Date.formatTimestamp(
                i18n._DATE_FORMAT,
                this.getCreationDate()
            );
        },

        getPartType:function(){
            return this.get("partType");
        },

        url: function(){
            if(this.get("id")){
                return "/api/workspaces/" + APP_CONFIG.workspaceId + "/part-templates/" + this.get("id");
            }else{
                return "/api/workspaces/" + APP_CONFIG.workspaceId + "/part-templates";
            }
        }
    });
	return Template;
});
