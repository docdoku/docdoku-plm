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
            _.extend(this.attributes, {num: index});

            return _.clone(this.attributes);
        }

    });

    return TaskModel;
});
