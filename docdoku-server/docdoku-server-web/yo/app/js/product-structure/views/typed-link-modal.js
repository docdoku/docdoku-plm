/*global define,App,_,$*/
define([
    'backbone',
    'mustache',
    'async',
    'text!templates/typed-link-modal.html',
    'views/typed-link-item',
    'common-objects/views/alert'
], function (Backbone, Mustache, Async, template, TypedLinkItemView, AlertView){
    'use strict';

    var TypedLinkModalView = Backbone.View.extend({

        className:'modal hide typed-link-modal',

        events: {
            'hidden': 'onHidden',
            'click .add-type-btn': 'onAddTypedLink',
            'submit form': 'onSaveTypedLinks',
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
            this.typedLinkItemViews = [];

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
                        editionMode:['wip','latest','latest-released'].indexOf(App.config.configSpec)!==-1
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

            var urlForAvailableType = '';

            if(this.serialNumber) {
                urlForAvailableType = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.productId + '/product-instances/' + this.serialNumber + '/path-to-path-links-types';
            }
            else if(this.baselineId){
                urlForAvailableType = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.productId + '/baselines/' + this.baselineId + '/path-to-path-links-types';
            }
            else{
                urlForAvailableType = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.productId + '/path-to-path-links-types';
            }

            return urlForAvailableType;
        },

        getUrlForExistingTypedLink: function(){
            var urlForExistingTypedLink = '';

            if(this.serialNumber){
                urlForExistingTypedLink = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.productId + '/product-instances/' + this.serialNumber + '/path-to-path-links/source/' + this.pathSelected[0].getEncodedPath() + '/target/' + this.pathSelected[1].getEncodedPath();
            }
            else if(this.baselineId){
                urlForExistingTypedLink = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.productId + '/baselines/' + this.baselineId + '/path-to-path-links/source/' + this.pathSelected[0].getEncodedPath() + '/target/' + this.pathSelected[1].getEncodedPath();
            }
            else{
                urlForExistingTypedLink = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.productId + '/path-to-path-links/source/' + this.pathSelected[0].getEncodedPath() + '/target/' + this.pathSelected[1].getEncodedPath();
            }

            return urlForExistingTypedLink;
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
                success: function(pathToPathLinkDTOs){
                    _.each(pathToPathLinkDTOs, function(pathToPathLinkDTO){
                        self.availableType.push(pathToPathLinkDTO.type);
                    });
                    self.getExistingPathToPath();
                },
                error: function(errorMessage){
                    self.$('#typed-link-alerts').append(new AlertView({
                        type: 'error',
                        message: errorMessage.responseText
                    }).render().$el);
                }
            });
        },

        getExistingPathToPath: function(){
            var self = this;
            var urlForExistingTypedLink = this.getUrlForExistingTypedLink();
            console.log(urlForExistingTypedLink)
            $.ajax({
                type : 'GET',
                url : urlForExistingTypedLink,
                contentType: 'application/json',
                success: function(pathToPathLinkDTOs){

                    _.each(pathToPathLinkDTOs, function(pathToPathLinkDTO){
                        self.existingPathToPathLinkCollection.push({
                            sourceModel : self.pathSelected[0].getEncodedPath() === pathToPathLinkDTO.sourcePath ? self.pathSelected[0] : self.pathSelected[1],
                            targetModel : self.pathSelected[1].getEncodedPath() === pathToPathLinkDTO.targetPath ? self.pathSelected[1] : self.pathSelected[0],
                            pathToPath : pathToPathLinkDTO,
                            creationMode : false,
                            editionMode : !self.baselineId && !self.serialNumber,
                            availableType : self.availableType,
                            productId : self.productId,
                            serialNumber : self.serialNumber,
                            sourceComponents:pathToPathLinkDTO.sourceComponents,
                            targetComponents:pathToPathLinkDTO.targetComponents
                        });
                    });

                    _.each(self.existingPathToPathLinkCollection, function(pathToPathLink){
                        var typedLinkItemView = new TypedLinkItemView({model:pathToPathLink}).render();
                        self.$('#path-to-path-links').append(typedLinkItemView.el);
                        self.typedLinkItemViews.push(typedLinkItemView);
                    });

                },
                error: function(errorMessage){
                    self.$('#typed-link-alerts').append(new AlertView({
                        type: 'error',
                        message: errorMessage.responseText
                    }).render().$el);
                }
            });
        },

        onSaveTypedLinks: function() {
            var _this = this;

            Async.each(this.typedLinkItemViews, function(typedLinkItemView, callback) {

                typedLinkItemView.save(callback);

            }, function(err) {
                if (!err) {
                    _this.closeModal();
                }
            });
        },

        onAddTypedLink: function(){
            var newTypedLinkItemView = new TypedLinkItemView({
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

            this.$('#path-to-path-links').append(newTypedLinkItemView.el);
            this.typedLinkItemViews.push(newTypedLinkItemView);
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

    return TypedLinkModalView;
});
