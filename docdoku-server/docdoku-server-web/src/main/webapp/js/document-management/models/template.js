define([
    "common-objects/collections/file/attached_file_collection"
], function (AttachedFileCollection) {
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
            return "/files/" + this.get("workspaceId") + "/document-templates/" + this.get("id")+"/";
        },

        isAttributesLocked:function(){
            return this.get("attributesLocked");
        }

    });
	return Template;
});
