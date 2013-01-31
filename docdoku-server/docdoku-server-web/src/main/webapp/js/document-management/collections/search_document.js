define([
	"models/document"
], function (
	Document
) {
	var SearchDocumentList = Backbone.Collection.extend({

		model: Document,

        className:"SearchDocumentList",

        setQuery:function(query){
            this.query = query  ;
            return this;
        },

        url: function() {
            baseUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/search";
            return baseUrl + "/" + this.query + "/documents";
        }

	});

	return SearchDocumentList;
});
