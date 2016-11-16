/*global define,App,_,$*/
define([
    'backbone',
    'mustache',
    'async',
    'text!templates/path_to_path_link_modal.html',
    'views/path_to_path_link_item',
    'common-objects/views/alert'
], function (Backbone, Mustache, Async, template, PathToPathLinkItemView, AlertView){
    'use strict';

    var PathToPathLinkModalView = Backbone.View.extend({

        className:'modal hide path-to-path-link-modal',

        events: {
            'hidden': 'onHidden',
            'click .add-path-to-path-link-btn': 'onAddPathToPathLink',
            'submit form': 'onSavePathToPathLinks',
            'click .cancel-button': 'closeModal'
        },

        initialize: function () {
            this.pathSelected = this.options.pathSelected;
            this.productId = this.options.productId;
            this.serialNumber = this.options.serialNumber;
            this.baselineId = this.options.baselineId;
        },

        bindDOMElements:function(){
            this.$modal = this.$el;
        },

        render: function () {
            this.pathToPathLinkItemViews = [];

            var self = this;

            var sourcePath = this.pathSelected[0].getEncodedPath();
            var targetPath = this.pathSelected[1].getEncodedPath();
            var getComponents = this.getComponents.bind(this);

            getComponents(sourcePath,function(sourceComponents){
                getComponents(targetPath,function(targetComponents){

                    self.sourceComponents = sourceComponents;
                    self.targetComponents = targetComponents;

                    self.$el.html(Mustache.render(template, {
                        i18n: App.config.i18n,
                        editionMode:['wip','latest','latest-released'].indexOf(App.config.productConfigSpec)!==-1
                    }));

                    self.getExistingPathToPathAndType();

                });
            });

            this.bindDOMElements();
            return this;
        },

        getComponents:function(path,next){
            $.ajax(this.getUrlForComponents(path),{success:next});
        },

        getUrlForComponents : function(path){
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.productId + '/decode-path/' + path;
        },

        getUrlForAvailableType: function(){

            var url = '';

            if(this.serialNumber) {
                url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/product-instances/' + this.productId + '/instances/' + this.serialNumber + '/path-to-path-links-types';
            }
            else if(this.baselineId){
                url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/product-baselines/' + this.productId + '/baselines/' + this.baselineId + '/path-to-path-links-types';
            }
            else{
                url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.productId + '/path-to-path-links-types';
            }

            return url;
        },

        getUrlForExistingPathToPathLink: function(){
            var url = '';

            if(this.serialNumber){
                url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/product-instances/' + this.productId + '/instances/' + this.serialNumber + '/path-to-path-links/source/' + this.pathSelected[0].getEncodedPath() + '/target/' + this.pathSelected[1].getEncodedPath();
            }
            else if(this.baselineId){
                url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/product-baselines/' + this.productId + '/baselines/' + this.baselineId + '/path-to-path-links/source/' + this.pathSelected[0].getEncodedPath() + '/target/' + this.pathSelected[1].getEncodedPath();
            }
            else{
                url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.productId + '/path-to-path-links/source/' + this.pathSelected[0].getEncodedPath() + '/target/' + this.pathSelected[1].getEncodedPath();
            }

            return url;
        },

        getExistingPathToPathAndType:function(){
            var self = this;

            var urlForAvailableTypes = this.getUrlForAvailableType();

            this.existingPathToPathLinkCollection = [];
            this.availableType = [];

            $.ajax({
                type : 'GET',
                url : urlForAvailableTypes,
                contentType: 'application/json',
                success: function(pathToPathLinks){
                    _.each(pathToPathLinks, function(pathToPathLink){
                        self.availableType.push(pathToPathLink.type);
                    });
                    self.getExistingPathToPath();
                },
                error: function(errorMessage){
                    self.$('#path-to-path-link-alerts').append(new AlertView({
                        type: 'error',
                        message: errorMessage.responseText
                    }).render().$el);
                }
            });
        },

        getExistingPathToPath: function(){
            var self = this;
            var url = this.getUrlForExistingPathToPathLink();
            $.ajax({
                type : 'GET',
                url : url,
                contentType: 'application/json',
                success: function(pathToPathLinks){

                    _.each(pathToPathLinks, function(pathToPathLink){
                        self.existingPathToPathLinkCollection.push({
                            sourceModel : self.pathSelected[0].getEncodedPath() === pathToPathLink.sourcePath ? self.pathSelected[0] : self.pathSelected[1],
                            targetModel : self.pathSelected[1].getEncodedPath() === pathToPathLink.targetPath ? self.pathSelected[1] : self.pathSelected[0],
                            pathToPath : pathToPathLink,
                            creationMode : false,
                            editionMode : !self.baselineId && !self.serialNumber,
                            availableType : self.availableType,
                            productId : self.productId,
                            serialNumber : self.serialNumber,
                            sourceComponents:pathToPathLink.sourceComponents,
                            targetComponents:pathToPathLink.targetComponents
                        });
                    });

                    _.each(self.existingPathToPathLinkCollection, function(pathToPathLink){
                        var pathToPathLinkItemView = new PathToPathLinkItemView({model:pathToPathLink}).render();
                        self.$('#path-to-path-links').append(pathToPathLinkItemView.el);
                        self.pathToPathLinkItemViews.push(pathToPathLinkItemView);
                    });

                },
                error: function(errorMessage){
                    self.$('#path-to-path-link-alerts').append(new AlertView({
                        type: 'error',
                        message: errorMessage.responseText
                    }).render().$el);
                }
            });
        },

        onSavePathToPathLinks: function(e) {
            var _this = this;

            Async.each(this.pathToPathLinkItemViews, function(pathToPathLinkItemView, callback) {

                pathToPathLinkItemView.save(callback);

            }, function(err) {
                if (!err) {
                    _this.closeModal();
                }
            });
            e.preventDefault();
            return false;
        },

        onAddPathToPathLink: function(){
            var newPathToPathLinkItemView = new PathToPathLinkItemView({
                model:{
                    sourceModel : this.pathSelected[0],
                    targetModel : this.pathSelected[1],
                    creationMode : true,
                    editionMode : true,
                    availableType : this.availableType,
                    productId : this.productId,
                    serialNumber : this.serialNumber,
                    sourceComponents:this.sourceComponents,
                    targetComponents:this.targetComponents
                }
            }).render();

            this.$('#path-to-path-links').append(newPathToPathLinkItemView.el);
            this.pathToPathLinkItemViews.push(newPathToPathLinkItemView);
        },


        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
            App.baselineSelectView.fetchPathToPathLinkTypes();
        },

        onHidden: function () {
            this.remove();
        }

    });

    return PathToPathLinkModalView;
});
