/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/lov/lov_modal.html',
    'common-objects/collections/lovs',
    'common-objects/views/lov/lov_item',
    'common-objects/models/lov/lov',
    'async',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, LOVCollection, LOVItemView, LOVModel, async, AlertView) {
    'use strict';
    var LOVModalView = Backbone.View.extend({

        template: template,

        events: {
            'hidden .modal.list_lov': 'onHidden',
            'click .addLOVButton': 'addLov',
            'submit form': 'onSaveLovs',
            'click .btn-saveLovs': 'interceptSubmit'
        },

        collection: new LOVCollection(),

        initialize: function () {
            _.bindAll(this);
            this.deletedLovModel = [];
            this.lovViews = [];
            this.listenTo(this.collection, 'reset', this.onCollectionReset);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.$modal = this.$('.modal.list_lov');
            this.$lovListDiv = this.$('.modal .modal-body .list_of_lov');
            this.$notifications = this.$('.notifications');
            this.collection.fetch({reset: true});
            return this;
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onHidden: function () {
            this.remove();
        },

        onCollectionReset: function() {
            this.collection.each(this.addLovView.bind(this, false));
        },

        addLovView: function(isExpand, lov){
            var lovView = new LOVItemView({
                model: lov,
                isExpand: isExpand
            });
            this.collection.push(lov);
            lovView.on('remove', this.removeLovView.bind(this, lovView));
            this.lovViews.push(lovView);
            lovView.render();
            this.$lovListDiv.append(lovView.$el);
        },

        addLov: function(){
            var newModel = new LOVModel(
                {
                    name:'',
                    values:[{name:'', value:''}],
                    workspaceId:App.config.workspaceId,
                    deletable: true
                }
            );
            //newModel.setNew(true);

            this.addLovView(true, newModel);
        },

        removeLovView:function(lovView){
            if(!lovView.model.isNew()){
                this.deletedLovModel.push(lovView.model);
            }
            this.collection.remove(lovView.model);
            this.lovViews = _.without(this.lovViews, lovView);
        },

        interceptSubmit:function(){
            this.isValid = this.checkEmptyHiddenFields();
        },

        checkEmptyHiddenFields:function(){
            var invalidItems = this.$('input:invalid');
            invalidItems.closest('.lovItem').addClass('edition');
            return !invalidItems.length;
        },

        onSaveLovs: function(event){
            if(this.isValid){
                var nbError = 0;

                var that = this;

                var errorFunction = function(response, callback){
                    that.$notifications.append(new AlertView({
                        type: 'error',
                        message: response.responseText
                    }).render().$el);
                    nbError++;
                    callback();
                };

                var queueDelete = async.queue(function(model, callback){
                    model.destroy({dataType : 'text'}).success(function(){
                        that.deletedLovModel = _.without(that.deletedLovModel,model);
                        callback();
                    }).error(function(response){
                        errorFunction(response, callback);
                    });
                });

                var queueSave = async.queue(function(model,callback){
                    model.save().success(function(){
                        callback();
                    }).error(function(response){
                        errorFunction(response, callback);
                    });
                });

                queueSave.drain = function(){
                    if(!nbError){
                        that.closeModal();
                    }
                };

                queueDelete.drain = function(){
                    if(!nbError && that.collection.models.length === 0){
                        that.closeModal();
                    }else{
                        queueSave.push(that.collection.models);
                    }
                };

                if(this.deletedLovModel.length === 0){
                    queueSave.push(this.collection.models);
                }else{
                    queueDelete.push(this.deletedLovModel);
                }
            }

            event.preventDefault();
            return false;
        }
    });
    return LOVModalView;
});
