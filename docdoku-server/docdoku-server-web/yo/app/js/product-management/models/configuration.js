/*global _,$,define,App*/
define([
    'backbone',
    'common-objects/utils/date',
    'common-objects/utils/acl-checker'
], function (Backbone,date, ACLChecker) {
    'use strict';
    var Configuration = Backbone.Model.extend({
        urlRoot: function () {
            if (this.configurationItemId) {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.configurationItemId + '/configurations';
            }
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/configurations';
        },
        initialize: function () {
            _.bindAll(this);
            this.configurationItemId = this.get('configurationItemId');
        },

        getName:function(){
            return this.get('name');
        },
        setName:function(name){
            this.set('name',name);
        },

        getConfigurationItemId:function(){
            return this.get('configurationItemId');
        },

        getDescription:function(){
            return this.get('description');
        },
        setDescription:function(description){
            this.set('description',description);
        },

        getSubstituteLinks:function(){},

        setSubstituteLinks:function(substituteLinks){},

        getOptionalUsageLinks:function(){},

        getSubstitutesParts:function(){
            return this.get('substitutesParts');
        },
        getOptionalsParts:function(){
            return this.get('optionalsParts');
        },

        setOptionalUsageLinks:function(optionalUsageLinks){},

        getCreatedDate:function(){
            return this.get('createdDate');
        },

        getFormattedCreationDate:function(){
            return date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                this.getCreatedDate()
            );
        },

        getACL: function () {
            return this.get('acl');
        },

        updateACL: function (args) {
            $.ajax({
                type: 'PUT',
                url: this.url() + '/acl',
                data: JSON.stringify(args.acl),
                contentType: 'application/json; charset=utf-8',
                success: args.success,
                error: args.error
            });
        },

        hasACLForCurrentUser: function () {
            return this.getACLPermissionForCurrentUser() !== false;
        },

        isForbidden: function () {
            return this.getACLPermissionForCurrentUser() === 'FORBIDDEN';
        },

        isReadOnly: function () {
            return this.getACLPermissionForCurrentUser() === 'READ_ONLY';
        },

        isFullAccess: function () {
            return this.getACLPermissionForCurrentUser() === 'FULL_ACCESS';
        },

        getACLPermissionForCurrentUser: function () {
            return ACLChecker.getPermission(this.getACL());
        }

    });
    return Configuration;
});
