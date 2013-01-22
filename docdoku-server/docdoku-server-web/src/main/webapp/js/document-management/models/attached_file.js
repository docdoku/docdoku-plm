define([
    "i18n!localization/nls/document-management-strings",
    "models/document_iteration"
], function (
    i18n,
    iteration
    ) {
    var AttachedFile = Backbone.Model.extend({
        initialize: function () {
            this.className = "AttachedFile";
        },

        toString : function(){
            return this.get("shortName");
        },

        isCreated : function(){
            var result = kumo.isNotEmpty(this.get("fullName"));
            return result;
        },

        isNew : function(){
            return false;
        }
    });
    return AttachedFile;
});