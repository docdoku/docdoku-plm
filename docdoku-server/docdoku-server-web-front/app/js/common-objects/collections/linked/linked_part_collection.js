/*global define*/
define([
    'backbone',
    'common-objects/models/linked/linked_part'
], function (Backbone, LinkedPart) {
	'use strict';
    var LinkedPartCollection = Backbone.Collection.extend({

        model: LinkedPart,

        comparator: function (linkedPart) {
            return linkedPart.getPartKey();
        }

    });

    return LinkedPartCollection;
});
