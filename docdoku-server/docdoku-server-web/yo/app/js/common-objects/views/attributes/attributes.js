/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/views/base',
    'common-objects/views/attributes/attribute_list',
    'text!common-objects/templates/attributes/attributes.html',
    'common-objects/collections/lovs'
], function (Backbone,Mustache, BaseView, AttributeListView, template, LOVCollection) {
    'use strict';
	var AttributesView = BaseView.extend({

        template: template,

        editMode: true,

        attributesLocked: false,

        lovs:new LOVCollection(),

        collection: function () {
            return new Backbone.Collection();
        },

        setEditMode: function (editMode) {
            this.editMode = editMode;
        },

        setAttributesLocked: function (attributesLocked) {
            this.attributesLocked = attributesLocked;
        },

        initialize: function () {
            _.bindAll(this);
            BaseView.prototype.initialize.apply(this, arguments);
            this.events['click .add'] = this.addAttribute;
            this.events['update-sort'] = this.updateSort;
        },

        updateSort: function(event,model,index){
            this.collection.remove(model,{silent:true});
            this.collection.add(model, {at: index,silent:true});
        },

        render: function () {
            var data = {
                view: this.viewToJSON(),
                frozenMode: !this.editMode || this.attributesLocked,
                i18n: App.config.i18n
            };
            this.$el.html(Mustache.render(template, data));
            var that = this;
            this.lovs.fetch().success(function(){
                that.rendered();
            });
            return this;
        },

        rendered: function () {
            var attributesViewList = new AttributeListView({
                el: this.$('#items-' + this.cid),
                collection: this.collection,
                lovs:this.lovs
            });
            this.attributesView = this.addSubView(
                attributesViewList
            );

            this.attributesView.setEditMode(this.editMode);
            this.attributesView.setAttributesLocked(this.attributesLocked);
            if(this.editMode){
                this.attributesView.$el.sortable({
                    handle: '.sortable-handler',
                    placeholder: 'list-item well highlight',
                    stop: function(event, ui) {
                        ui.item.trigger('drop', ui.item.index());
                    }
                });
            }

            attributesViewList.collectionReset();
        },

        addAttribute: function () {
            this.collection.add({
                mandatory: false,
                name: '',
                type: 'TEXT',
                value: '',
                lovName:null,
                locked:false
            });
        },

        addAndFillAttribute: function (attribute) {
            this.collection.add({
                mandatory: attribute.isMandatory(),
                name: attribute.getName(),
                type: attribute.getType(),
                value: attribute.getValue(),
                lovName: attribute.getLOVName(),
                items:attribute.getItems(),
                locked:attribute.getLocked()
            });
        }

    });

    return AttributesView;
});
