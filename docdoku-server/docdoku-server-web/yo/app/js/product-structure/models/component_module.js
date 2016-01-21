/*global _,define,App*/
define(['backbone', 'common-objects/utils/date'],
    function (Backbone, date) {
	    'use strict';
        var ComponentModule = {};

        ComponentModule.Model = Backbone.Model.extend({

            initialize: function () {
                if(this.isAssembly()||App.config.linkType){
                    this.children = new ComponentModule.Collection([], { parentUsageLinkId: this.getPartUsageLinkId(), path: this.getPath() });
                }
                _.bindAll(this);
            },

            defaults: {
                name: null,
                number: null,
                version: null,
                description: null,
                author: null,
                authorLogin: null,
                iteration: null,
                standardPart: false,
                partUsageLinkId: null,
                amount: 0,
                components: [],
                assembly: false,
                mail: null,
                checkOutDate: null,
                checkOutUser: null
            },

            getId: function () {
                return this.getNumber() + '-' + this.getVersion() + '-' + this.getIteration();
            },

            isCheckout: function () {
                return !_.isNull(this.attributes.checkOutDate);
            },

            getCheckoutUser: function () {
                return this.get('checkOutUser');
            },

            getCheckoutDate: function () {
                return this.get('checkOutDate');
            },

            getFormattedCheckoutDate: function () {
                if (this.isCheckout()) {
                    return date.formatTimestamp(
                        App.config.i18n._DATE_FORMAT,
                        this.getCheckoutDate()
                    );
                } else {
                    return false;
                }
            },

            isCheckoutByConnectedUser: function () {
                return this.isCheckout() ? this.getCheckoutUser().login === App.config.login : false;
            },

            hasUnreadModificationNotifications: function () {
                // TODO: get the modification notification collection
                return _.select(this.get('notifications') || [], function(notif) {
                    return !notif.acknowledged;
                }).length;
            },

            isAssembly: function () {
                return this.get('assembly');
            },

            isLeaf: function () {
                return !this.isAssembly();
            },

            getPartUsageLinkId: function () {
                return this.get('partUsageLinkId');
            },

            getPath: function () {
                return this.get('path');
            },

            getAmount: function () {
                return this.get('amount');
            },

            getUnit: function () {
                return this.get('unit');
            },

            getName: function () {
                return this.get('name');
            },

            getNumber: function () {
                return this.get('number');
            },

            getVersion: function () {
                return this.get('version');
            },

            getDescription: function () {
                return this.get('description');
            },

            getAuthor: function () {
                return this.get('author');
            },

            getAuthorLogin: function () {
                return this.get('authorLogin');
            },

            getIteration: function () {
                return this.get('iteration') !== 0 ? this.get('iteration') : null;
            },

            isLastIteration: function (iterationNumber) {
                // return TRUE if the iteration is the very last (check or uncheck)
                return this.get('lastIterationNumber') === iterationNumber;
            },

            isStandardPart: function () {
                return this.get('standardPart');
            },

            isForbidden: function () {
                return this.get('accessDeny');
            },

            isReleased : function(){
                return this.get('released');
            },

            isObsolete : function(){
                return this.get('obsolete');
            },

            isSubstitute : function(){
                return this.get('substitute');
            },

            hasSubstitutes : function(){
                var substitutesIds = this.getSubstituteIds();
                return substitutesIds && substitutesIds.length;
            },

            getSubstituteIds : function(){
                return this.get('substituteIds');
            },

            isSubstituteOf:function(otherComponent){
                if(!this.hasSubstitutes()){
                    return false;
                }
                if(!this.isOnSameBasePath(otherComponent)){
                    return false;
                }
                return this.getSubstituteIds().indexOf(otherComponent.getPartUsageLinkId()) !== -1;
            },

            getBasePath:function(){
                var path = this.getPath();
                var lastDash = path.lastIndexOf('-');
                if(lastDash !== -1){
                    return path.substr(0,lastDash);
                }else{
                    return path;
                }
            },

            isOnSameBasePath:function(otherComponent){
                return this.getBasePath() === otherComponent.getBasePath();
            },

            hasPathData : function(){
                return this.get('hasPathData');
            },

            isOptional : function(){
                return this.get('optional');
            },
            isVirtual : function(){
                return this.get('virtual');
            },
            getPartUsageLinkReferenceDescription:function(){
                return this.get('partUsageLinkReferenceDescription');
            },

            getInstancesUrl: function () {
                return this.getProductUrl() + '/instances?configSpec=' + App.config.productConfigSpec + '&path=' + this.getEncodedPath();
            },

            getEncodedPath : function(){
                return this.getPath();
            },

            getProductUrl: function() {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId;
            },
            getUrlForBom: function () {

                var url;

                if (this.isAssembly()) {
                    url = this.getProductUrl() +
                        '/bom?configSpec=' + App.config.productConfigSpec +
                        '&path=' + this.getEncodedPath();

                    if(App.config.diverge) {
                        url += '&diverge=true';
                    }

                } else {
                    url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/' + this.getNumber() + '-' + this.getVersion();
                }

                return url;
            },

            getRootUrlForBom: function () {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/' + this.getNumber() + '-' + this.getVersion();
            },

            cascadeCheckin: function(callback, iterationNote) {
                var that = this;
                return $.ajax({
                    context: this,
                    type: 'PUT',
                    data:JSON.stringify({iterationNote:iterationNote}),
                    contentType: 'application/json; charset=utf-8',
                    url: that.getProductUrl() + '/cascade-checkin?path='+this.getEncodedPath() + '&configSpec='+App.config.productConfigSpec,
                    success:callback
                });
            },

            cascadeCheckout: function(callback) {
                var that = this;
                return $.ajax({
                    context: this,
                    type: 'PUT',
                    url: that.getProductUrl() + '/cascade-checkout?path='+this.getEncodedPath() + '&configSpec='+App.config.productConfigSpec,
                    success:callback
                });
            },

            cascadeUndoCheckout: function(callback) {
                var that = this;
                return $.ajax({
                    context: this,
                    type: 'PUT',
                    url: that.getProductUrl() + '/cascade-undocheckout?path='+this.getEncodedPath() + '&configSpec='+App.config.productConfigSpec,
                    success:callback
                });
            },

            checkin: function() {
                var that = this;
                return $.ajax({
                    context: this,
                    type: 'PUT',
                    url: that.getRootUrlForBom() + '/checkin'
                });
            },

            checkout: function() {
                var that = this;
                return $.ajax({
                    context: this,
                    type: 'PUT',
                    url: that.getRootUrlForBom() + '/checkout'
                });
            },

            undocheckout: function() {
                var that = this;
                return $.ajax({
                    context: this,
                    type: 'PUT',
                    url: that.getRootUrlForBom() + '/undocheckout'
                });
            }

        });

        ComponentModule.Collection = Backbone.Collection.extend({
            model: ComponentModule.Model,

            initialize: function (models, options) {
                this.isRoot = _.isUndefined(options.isRoot) ? false : options.isRoot;

                if (!this.isRoot) {
                    this.parentUsageLinkId = options.parentUsageLinkId;
                    this.path = options.path;
                } else if(!App.config.linkType){
                    this.parentUsageLinkId = '-1';
                    this.path = '-1';
                }

            },

            url: function () {
                var path = this.path;

                var url = this.urlBase() + '/filter?configSpec=' + App.config.productConfigSpec + '&depth=1';

                if(path){
                    url+= '&path=' + path;
                }

                if(App.config.linkType){
                    url += '&linkType='+App.config.linkType;
                }

                if(App.config.diverge) {
                    url += '&diverge=true';
                }

                return url;
            },

            urlBase: function () {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId;
            },

            parse: function (response) {
                if (response) {
                    if (this.isRoot) {
                        response.path = response.partUsageLinkId;
                        return [response];
                    } else {
                        return response.components;
                    }
                }
            }
        });
        return ComponentModule;
    });
