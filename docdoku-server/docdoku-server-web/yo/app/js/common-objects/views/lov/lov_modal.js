/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/lov/lov_modal.html',
    'common-objects/collections/lovs',
    'common-objects/views/lov/lov_item',
    'common-objects/models/lov/lov',
], function (Backbone, Mustache, template, LOVCollection, LOVItemView, LOVModel) {
    'use strict';
    var LOVModalView = Backbone.View.extend({

        template: template,

        events: {
            'hidden .modal.list_lov': 'onHidden',
            'click .addLOVButton': 'addLov',
            'click .btn-saveLovs': 'onSaveLovs'
        },

        collection: new LOVCollection(),

        lovViews: [],

        lovListDiv: null,

        initialize: function () {
            _.bindAll(this);
            this.listenTo(this.collection, 'reset', this.onCollectionReset);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.$modal = this.$('.modal.list_lov');
            this.lovListDiv = this.$('.modal .modal-body .list_of_lov');
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
            this.lovListDiv.append(lovView.$el);
        },

        addLov: function(){
            var newModel = new LOVModel(
            {name:"",
                values:[{name:"", value:""}],
                workspaceId:App.config.workspaceId}
            );
            newModel.setNew(true);

            this.addLovView(true, newModel);
        },

        removeLovView:function(lovView){
            this.collection.remove(lovView.model);
            this.lovViews = _.without(this.lovViews, lovView);
        },

        onSaveLovs: function(){
            _.invoke(this.collection.models, 'save');
        }


    });
    return LOVModalView;
});
