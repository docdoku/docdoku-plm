define([
	"models/document_iteration"
], function (
	DocumentIteration
) {
	var DocumentIterationList = Backbone.Collection.extend({

		model: DocumentIteration,

        setDocument: function(document) {
            this.document = document;
        },

		url: function() {
		    return this.document.url() + "/iterations";
		}

	});

	return DocumentIterationList;
});
