/*global _,$,define,App*/
define(['backbone'], function (Backbone) {
	'use strict';
    var ConfigurationItem = Backbone.Model.extend({

	    idAttribute: '_id',

        urlRoot: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products';
        },

        initialize: function () {
            _.bindAll(this);
        },

        parse: function (response) {
            response._id = response.id;
            return response;
        },

        getId: function () {
            return this.get('id');
        },

        getDesignItemNumber: function () {
            return this.get('designItemNumber');
        },

        getIndexUrl: function () {
            return App.config.contextPath + '/product-structure/#' + App.config.workspaceId + '/' + encodeURIComponent(this.getId());
        },

        getFrameUrl: function () {
            return  App.config.contextPath + '/visualization/#' + App.config.workspaceId + '/' + this.getId() + '/0/10/1000/null/'+App.config.configSpec;
        },

        createBaseline: function (baselineArgs, callbacks) {
            $.ajax({
                type: 'POST',
                url: this.urlRoot() + '/' + this.getId() + '/baselines',
                data: JSON.stringify(baselineArgs),
                contentType: 'application/json; charset=utf-8',
                success: callbacks.success,
                error: callbacks.error
            });
        },

        deleteBaselines: function (baselines) {
            var that = this;
            var errors = [];
            _.each(baselines, function (baseline) {
                that.deleteBaseline(baseline.getId(), {success: function () {
                }, error: function () {
                    errors.push(baseline);
                }});
            });
            return errors;
        },

        deleteBaseline: function (baselineId, callbacks) {
            $.ajax({
                type: 'DELETE',
                async: false,
                url: this.urlRoot() + '/' + this.getId() + '/baselines/' + baselineId,
                contentType: 'application/json; charset=utf-8',
                success: callbacks.success,
                error: callbacks.error
            });
        }

    });

    return ConfigurationItem;

});
