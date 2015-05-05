/*global define,App,_,$*/
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
            'click .delete-item' : 'onDeleteItem',
            'click .save-button': 'onSave'
        },

        initialize: function(){
            this.model = this.options.model;
            this.isEditMode = this.model.isEditMode;
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
                isEditMode : this.model.isEditMode,
                source : this.model.sourceModel,
                target : this.model.targetModel,
                availableType: this.model.availableType,
                description : this.model.pathToPath.description,
                type : this.model.pathToPath.type
            };

            this.$el.html(Mustache.render(template, data));
            this.bindDOMElements();

            return this;
        },

        bindDOMElements:function(){
        },

        onInvertSourceTarget: function(){
            var copy = this.model.sourceModel;
            this.model.sourceModel = this.model.targetModel;
            this.model.targetModel = copy;
            this.model.pathToPath['source'] = this.model.sourceModel.getEncodedPath();
            this.model.pathToPath['target'] = this.model.targetModel.getEncodedPath();
            this.render();
        },

        onDeleteItem : function(){
            if(this.model.isEditMode){
                this.remove();
            }else{
                var self = this;
                var urlToDelete = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.model.productId
                    + '/path-to-path-links/' + this.model.pathToPath.id;

                $.ajax({
                    type: 'DELETE',
                    url: urlToDelete,
                    contentType: 'application/json',
                    success:function(){
                        self.remove();
                    },
                    error: function(errorMessage){
                        self.$('.error-div').append(new AlertView({
                            type: 'error',
                            message: errorMessage.responseText
                        }).render().$el);
                    }
                });
            }
        },

        onSave: function(){

            var self = this;
            var urlToPost = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.model.productId
                + '/path-to-path-links';

            this.model.pathToPath.type = this.$('.type-select').val() !== ''? this.$('.type-select').val() : this.$('.add-type-input').val();
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
                success:function(pathToPathLink){
                    self.model.pathToPath.id = pathToPathLink.id;
                    self.model.isEditMode = false;
                    self.render();
                },
                error: function(errorMessage){
                    self.$('.error-div').append(new AlertView({
                        type: 'error',
                        message: errorMessage.responseText
                    }).render().$el);
                }
            });

        }

    });

    return TypedLinkItemView;
});
