/*global define,App,$*/
define([
    'backbone',
    'mustache',
    'text!templates/typed-link-item.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, AlertView){
    'use strict';

    var TypedLinkItemView = Backbone.View.extend({

        className:'well',

        events:{
            'click .invert-source-target' : 'onInvertSourceTarget',
            'click .delete-item' : 'onDeleteItem'
        },

        initialize: function(){
            this.model = this.options.model;
            this.creationMode = this.model.creationMode;
            this.deleted = false;

            if(!this.model.pathToPath){
                this.model.pathToPath = {
                    source: this.model.sourceModel.getEncodedPath(),
                    target: this.model.targetModel.getEncodedPath()
                };
            }
        },

        render: function () {

            var data = {
                i18n: App.config.i18n,
                creationMode : this.model.creationMode,
                canSuppress : ['wip','latest','latest-released'].indexOf(App.config.configSpec)!==-1,
                source : this.model.sourceModel,
                target : this.model.targetModel,
                availableType: this.model.availableType,
                description : this.model.pathToPath.description,
                type : this.model.pathToPath.type,
                sourceComponents:this.model.sourceComponents,
                targetComponents:this.model.targetComponents
            };

            this.$el.html(Mustache.render(template, data));
            this.$('.link-source i:last-of-type').remove();
            this.$('.link-target i:last-of-type').remove();
            this.bindDOMElements();

            return this;
        },

        bindDOMElements:function(){
        },

        onInvertSourceTarget: function(){
            var copy = this.model.sourceModel;
            this.model.sourceModel = this.model.targetModel;
            this.model.targetModel = copy;
            this.model.pathToPath.source = this.model.sourceModel.getEncodedPath();
            this.model.pathToPath.target = this.model.targetModel.getEncodedPath();
            var copyComponents = this.model.sourceComponents;
            this.model.sourceComponents = this.model.targetComponents;
            this.model.targetComponents = copyComponents;
            this.render();
        },

        onDeleteItem : function() {
            this.remove();
            this.deleted = true;
        },

        onDelete : function (callback) {
            var self = this;
            var urlToDelete = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.model.productId + '/path-to-path-links/' + this.model.pathToPath.id;

            $.ajax({
                type: 'DELETE',
                url: urlToDelete,
                contentType: 'application/json',
                success: function() {
                    self.remove();
                    callback();
                },
                error: function(errorMessage) {
                    self.showNotification('error', errorMessage.responseText);
                }
            });
        },

        onSave: function (callback) {

            var self = this;
            var urlToPost = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.model.productId + '/path-to-path-links';

            this.model.pathToPath.description = this.$('.path-to-path-description').val();

            var data = {
                sourcePath : this.model.pathToPath.source,
                targetPath : this.model.pathToPath.target,
                type : this.model.pathToPath.type,
                description : this.model.pathToPath.description
            };

            $.ajax({
                type: 'POST',
                url: urlToPost,
                dataType:'json',
                contentType: 'application/json',
                data : JSON.stringify(data),
                success: function(pathToPathLink) {
                    self.model.pathToPath.id = pathToPathLink.id;
                    self.model.creationMode = false;
                    self.creationMode = false;
                    self.render();
                    callback();
                },
                error: function(errorMessage) {
                    self.showNotification('error', errorMessage.responseText);
                }
            });

        },

        determineType: function () {
            this.model.pathToPath.type = this.$('.type-select').val() !== ''? this.$('.type-select').val() : this.$('.add-type-input').val();
            return this.model.pathToPath.type;
        },

        showNotification: function (type, message) {
            this.$('.error-div').append(new AlertView({
                type: type,
                message: message
            }).render().$el);
        }

    });

    return TypedLinkItemView;
});
