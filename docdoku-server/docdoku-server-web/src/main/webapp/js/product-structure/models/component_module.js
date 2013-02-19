define(["models/part_iteration_visualization", "common-objects/utils/date", "i18n!localization/nls/product-structure-strings"], function (PartIterationVisualization, date, i18n) {

    var ComponentModule = {};

    ComponentModule.Model = Backbone.Model.extend({

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

        isCheckout: function() {
            return !_.isNull(this.attributes.checkOutDate);
        },

        getCheckoutUser: function() {
            return this.get('checkOutUser');
        },

        getCheckoutDate: function() {
            return this.get('checkOutDate');
        },

        getFormattedCheckoutDate: function() {
            if (this.isCheckout()) {
                return date.formatTimestamp(
                    i18n._DATE_FORMAT,
                    this.getCheckoutDate()
                );
            } else {
                return false;
            }
        },

        isCheckoutByConnectedUser: function() {
            return this.isCheckout() ? this.getCheckoutUser().login == APP_CONFIG.login : false;
        },

        isAssembly: function() {
            return this.get('assembly');
        },

        isLeaf: function() {
            return !this.isAssembly();
        },

        getPartUsageLinkId: function() {
            return this.get('partUsageLinkId');
        },

        getPath: function() {
            return this.get('path');
        },

        initialize: function() {

            if (this.isAssembly()) {
                this.children = new ComponentModule.Collection([], { parentUsageLinkId: this.getPartUsageLinkId(), path: this.getPath() });
            }

        },

        getAmount: function() {
            return this.get('amount');
        },

        getName: function() {
            return this.get('name');
        },

        getNumber: function() {
            return this.get('number');
        },

        getVersion: function() {
            return this.get('version');
        },

        getDescription: function() {
            return this.get('description');
        },

        getAuthor: function() {
            return this.get('author');
        },

        getAuthorLogin: function() {
            return this.get('authorLogin');
        },

        getIteration: function() {
            return this.get('iteration') != 0 ?  this.get('iteration') : null;
        },

        isStandardPart: function() {
            return this.get('standardPart');
        },

        getInstancesUrl: function() {
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "/instances?configSpec=latest&path=" + this.getPath();
        },

        putOnScene: function() {
            $.getJSON(this.getInstancesUrl(), function(instances) {
                _.each(instances, function(instanceRaw) {

                    //do something only if this instance is not on scene
                    if (!sceneManager.isOnScene(instanceRaw.id)) {

                        //if we deal with this partIteration for the fist time, we need to create it
                        if (!sceneManager.hasPartIteration(instanceRaw.partIterationId)) {
                            sceneManager.addPartIteration(new PartIterationVisualization(instanceRaw));
                        }

                        var partIteration = sceneManager.getPartIteration(instanceRaw.partIterationId);

                        //finally we create the instance and add it to the scene
                        sceneManager.addInstanceOnScene(new Instance(
                            instanceRaw.id,
                            partIteration,
                            instanceRaw.tx*10,
                            instanceRaw.ty*10,
                            instanceRaw.tz*10,
                            instanceRaw.rx,
                            instanceRaw.ry,
                            instanceRaw.rz
                        ));
                    }

                });
            });
        },

        removeFromScene: function() {
            $.getJSON(this.getInstancesUrl(), function(instances) {
                _.each(instances, function(instanceRaw) {
                    if (sceneManager.isOnScene(instanceRaw.id)) {
                        sceneManager.removeInstanceFromScene(instanceRaw.id);
                    }
                });
            });
        },

        getUrlForBom: function() {

            if(this.isAssembly()) {
                return "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "/bom?configSpec=latest&partUsageLink=" + this.getPartUsageLinkId();
            } else {
                return "/api/workspaces/" + APP_CONFIG.workspaceId + "/parts/" + this.getNumber()+ "-" + this.getVersion();
            }

        },

        getRootUrlForBom: function() {
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/parts/" + this.getNumber()+ "-" + this.getVersion();
        }

    });

    ComponentModule.Collection = Backbone.Collection.extend({

        model: ComponentModule.Model,

        initialize: function(models, options) {
            this.isRoot = _.isUndefined(options.isRoot) ? false : options.isRoot;
            if (!this.isRoot) {
                this.parentUsageLinkId = options.parentUsageLinkId;
                this.path = options.path;
            }
        },

        url: function() {
            if (this.isRoot) {
                return this.urlBase + "?configSpec=latest&depth=0";
            } else {
                return this.urlBase + "?configSpec=latest&partUsageLink=" + this.parentUsageLinkId + "&depth=1";
            }
        },

        urlBase: "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId,

        parse: function(response) {
            if (this.isRoot) {
                response.path = null;
                return [response];
            } else {
                var self = this;
                return _.map(response.components, function(component) {
                    var path = self.path == null ? component.partUsageLinkId : self.path + '-' + component.partUsageLinkId;
                    return _.extend(component, {path: path})
                });
            }
        }

    });

    return ComponentModule;

});