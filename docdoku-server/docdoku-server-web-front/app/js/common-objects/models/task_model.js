/*global _,define*/
define([
    'backbone',
    'common-objects/models/role'
], function (Backbone, Role) {
	'use strict';
    var TaskModel = Backbone.Model.extend({

        defaults: {
            duration: 25
        },

        initialize: function () {
            if (!_.isUndefined(this.attributes.role)) {
                this.attributes.role = new Role(this.attributes.role);
            }
        },

        toJSON: function () {
            var index = this.collection.indexOf(this);
            var clonedAttributes = _.clone(this.attributes);
            clonedAttributes.role = clonedAttributes.role.toJSON();
            _.extend(clonedAttributes, {num: index});
            return clonedAttributes;
        }

    });

    return TaskModel;
});
