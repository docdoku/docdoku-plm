'use strict';
define([
	'models/document_iteration'
], function (
	DocumentIteration
) {
	var DocumentIterationList = Backbone.Collection.extend({

		model: DocumentIteration,

        setDocument: function(document) {
            this.document = document;
        },

		url: function() {
		    return this.document.url() + '/iterations';
		},

        comparator: function(documentIteration) {
            return documentIteration.getDocKey();
        },

        next: function(iteration) {
            var index = this.indexOf(iteration);
            return this.at(index + 1);
        },

        previous: function(iteration) {
            var index = this.indexOf(iteration);
            return this.at(index - 1);
        },

        hasNextIteration: function(iteration) {
            return !_.isUndefined(this.next(iteration));
        },

        hasPreviousIteration: function(iteration) {
            return !_.isUndefined(this.previous(iteration));
        },

        isLast: function(iteration) {
            return this.last() === iteration;
        }

	});

	return DocumentIterationList;
});
