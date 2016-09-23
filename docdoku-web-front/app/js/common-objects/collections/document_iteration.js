/*global _,define*/
define([
    'backbone',
    'common-objects/models/document/document_iteration'
], function (Backbone,DocumentIteration) {
    'use strict';
    var DocumentIterationList = Backbone.Collection.extend({

        model: DocumentIteration,

        setDocument: function (document) {
            this.document = document;
        },

        url: function () {
	        if(this.document.getId()){
                return this.baseUrl();
            }
            return this.document.urlRoot()+ '/iterations';
	    },

	    baseUrl: function(){
			return this.document.urlRoot()+ '/'+this.document.getId()+ '/iterations';
		},

        comparator: function (documentIteration) {
            return documentIteration.getDocKey();
        },

        next: function (iteration) {
            var index = this.indexOf(iteration);
            return this.at(index + 1);
        },

        previous: function (iteration) {
            var index = this.indexOf(iteration);
            return this.at(index - 1);
        },

        hasNextIteration: function (iteration) {
            return !_.isUndefined(this.next(iteration));
        },

        hasPreviousIteration: function (iteration) {
            return !_.isUndefined(this.previous(iteration));
        },

        isLast: function (iteration) {
            return this.last() === iteration;
        }

    });

    return DocumentIterationList;
});
