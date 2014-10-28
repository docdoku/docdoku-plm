/*global define*/
define([
    'backbone',
    "common-objects/utils/date",
    "common-objects/collections/attribute_collection",
    "common-objects/collections/file/attached_file_collection"
], function (Backbone, date, AttributeCollection, AttachedFileCollection) {

    var PartIteration = Backbone.Model.extend({

        idAttribute: "iteration",

        initialize: function () {

            this.className = "PartIteration";

            var attributes = new AttributeCollection(this.get("instanceAttributes"));
            this.set("instanceAttributes", attributes);

            this.resetNativeCADFile();

        },

        resetNativeCADFile: function () {
            var nativeCADFullName = this.get("nativeCADFile");
            if (nativeCADFullName) {
                var nativeCad = {
                    fullName: nativeCADFullName,
                    shortName: _.last(nativeCADFullName.split("/")),
                    created: true
                };
                this._nativeCADFile = new AttachedFileCollection(nativeCad);
            } else {
                this._nativeCADFile = new AttachedFileCollection();
            }
        },

        defaults: {
            instanceAttributes: []
        },

        getAttributes: function () {
            return this.get("instanceAttributes");
        },

        getWorkspace: function () {
            return this.get("workspaceId");
        },

        getReference: function () {
            return this.getPartKey() + "-" + this.getIteration();
        },

        getIteration: function () {
            return this.get("iteration");
        },

        getPartKey: function () {
            return  this.get("number") + "-" + this.get("version");
        },

        getAttachedFiles: function () {
            return this.get("nativeCADFile");
        },

        getBaseName: function () {
            return App.config.contextPath + "/files/" + this.getWorkspace() + "/parts/" + this.get("number") + "/" + this.get("version") + "/" + this.get("iteration");
        },

        getNumber: function () {
            return this.collection.part.getNumber();
        },

        getVersion: function () {
            return this.collection.part.getVersion();
        },

        getComponents: function () {
            return this.get("components");
        },

        getLinkedDocuments: function () {
            return this.get("linkedDocuments");
        },


        /**
         * file Upload uses the old servlet, not the JAXRS Api         *
         * return /files/{workspace}/parts/{docId}/{version}/{iteration}/
         * @returns string
         */
        getUploadBaseUrl: function () {
            return  App.config.contextPath + "/files/" + this.getWorkspace() + "/parts/" + this.getNumber() + "/" + this.getVersion() + "/" + this.get("iteration") + "/nativecad/";
        }

    });

    return PartIteration;

});