/*global _,define*/
define([
    'backbone',
    'common-objects/views/base',
    'common-objects/views/attributes/template_new_attribute_list',
    'text!common-objects/templates/attributes/template_new_attributes.html',
    'common-objects/collections/lovs'
], function (Backbone,BaseView, TemplateNewAttributeListView, template, LOVCollection) {
	'use strict';
    var TemplateNewAttributesView = BaseView.extend({

        template: template,

        attributesLocked: false,
        //boolean to check if the list of attributes can be frozen
        unfreezable: false,

        collection: function () {
            return new Backbone.Collection();
        },

        initialize: function () {
            BaseView.prototype.initialize.apply(this, arguments);
            this.lovs = new LOVCollection();
            this.events['click .add'] = this.addAttribute;
            this.events['change .lock input'] = this.attributesLockedChange;
            this.events['update-sort'] = this.updateSort;
            if (this.options.attributesLocked) {
                this.attributesLocked = this.options.attributesLocked;
            }

            if(this.options.unfreezable) {
                this.unfreezable = this.options.unfreezable;
            }

            if(this.options.editMode){
                this.editMode = this.options.editMode;
            }

            this.templateExtraData = {
                editMode : this.editMode,
                unfreezable: this.unfreezable
            };
        },

        updateSort: function(event,model,index){
            this.collection.remove(model,{silent:true});
            this.collection.add(model, {at: index,silent:true});
        },

        rendered: function () {
            this.lovs.fetch().success(this.displayAttribute);
        },

        displayAttribute: function(){
            var listViewAttributs = new TemplateNewAttributeListView({
                el: '#items-' + this.cid,
                collection: this.collection,
                lovs:this.lovs,
                editMode: this.editMode,
                freezable: this.freezable
            });
            this.attributesView = this.addSubView(
                listViewAttributs
            );
            listViewAttributs.collectionReset();

            var lockInput = this.$el.find('.lock input');

            if(lockInput){
                lockInput.checked = this.attributesLocked;
            }

            this.$el.toggleClass('attributes-locked', this.attributesLocked);

            this.attributesView.$el.sortable({
                handle: '.sortable-handler',
                placeholder: 'list-item well highlight',
                stop: function(event, ui) {
                    ui.item.trigger('drop', ui.item.index());
                }
            });

        },

        addAttribute: function () {
            this.collection.add({
                name: '',
                attributeType: 'TEXT'
            });
        },

        attributesLockedChange: function (e) {
            this.attributesLocked = e.target.checked;
            this.attributesView.setAttributesLocked(this.attributesLocked);
            if (!this.attributesLocked) {
                _.map(this.collection.models, function (attribute) {
                    attribute.set('locked', false);
                    return attribute.set('mandatory', false);
                });
                this.$el.find('.attribute-locked').show();
                this.$el.find('.attribute-locked input').prop('checked', false);
            } else {
                _.map(this.collection.models, function (attribute) {
                    //attribute.set('locked', true);
                });
                //this.$el.find('.attribute-locked input').prop('checked', true)
                this.$el.find('.attribute-locked').hide();
            }

            this.$el.toggleClass('attributes-locked', this.attributesLocked);
        },

        isAttributesLocked: function () {
            return this.attributesLocked;
        }
    });
    return TemplateNewAttributesView;
});
