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
                _.each(instances, function(instanceRaw) {

                    //do something only if this instance is not on scene
                    if (!sceneManager.isOnScene(instanceRaw.id)) {

                        //if we deal with this partIteration for the fist time, we need to create it
                        if (!sceneManager.hasPartIteration(instanceRaw.partIterationId)) {
                            sceneManager.addPartIteration(new PartIteration(instanceRaw));
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