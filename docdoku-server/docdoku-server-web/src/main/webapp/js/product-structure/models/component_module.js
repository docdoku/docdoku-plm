define(["models/part_iteration"], function (PartIteration) {

    var ComponentModule = {}

    ComponentModule.Model = Backbone.Model.extend({

        defaults: {
            name: null,
            number: null,
            version: null,
            description: null,
            author: null,
            iteration: null,
            standardPart: false,
            partUsageLinkId: null,
            amount: 0,
            components: [],
            assembly: false
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

        getWebRtcUrlRoom: function() {
            //TODO find a better system for room number
            var getIntFromString = function(str) {
                var count = 0;
                for (var i = 0 ; i < str.length ; i++){
                    count += str.charCodeAt(i)*60*i;
                }
                return count;
            }
            return "/webRTCRoom?r=" + getIntFromString(this.getAuthor());
        },

        getIteration: function() {
            return this.get('iteration');
        },

        isStandardPart: function() {
            return this.get('standardPart');
        },

        getInstancesUrl: function() {
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "/instances?configSpec=latest&path=" + this.getPath();
        },

        putOnScene: function() {
            $.getJSON(this.getInstancesUrl(), function(instances) {
                _.each(instances, function(instance) {
                    if (!sceneManager.hasPartIteration(instance.partIterationId)) {
                        sceneManager.addPartIteration(new PartIteration(instance));
                    }
                    sceneManager.getPartIteration(instance.partIterationId).addInstance(instance);
                });
            });
        },

        removeFromScene: function() {
            $.getJSON(this.getInstancesUrl(), function(instances) {
                _.each(instances, function(instance) {
                    var partIteration = sceneManager.getPartIteration(instance.partIterationId);
                    if (partIteration != null) {
                        partIteration.removeInstance(instance);
                    }
                });
            });
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
                return "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "?configSpec=latest&depth=0";
            } else {
                return "/api/workspaces/" + APP_CONFIG.workspaceId + "/products/" + APP_CONFIG.productId + "?configSpec=latest&partUsageLink=" + this.parentUsageLinkId + "&depth=1";
            }
        },

        parse: function(response) {
            if (this.isRoot) {
                return [response];
            } else {
                var self = this;
                return _.map(response.components, function(component) {
                    var path = _.isUndefined(self.path) ? component.partUsageLinkId : self.path + '-' + component.partUsageLinkId;
                    return _.extend(component, {path: path})
                });
            }
        }

    });

    return ComponentModule;

});