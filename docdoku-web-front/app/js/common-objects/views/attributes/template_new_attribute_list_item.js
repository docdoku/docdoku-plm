/*global define,App*/
define([
    'common-objects/views/components/list_item',
    'text!common-objects/templates/attributes/template_new_attribute_list_item.html'
], function (ListItemView, template) {
    'use strict';
    var TemplateNewAttributeListItemView = ListItemView.extend({

        template: template,

        tagName: 'div',

        lovList:null,

        initialize: function () {
            ListItemView.prototype.initialize.apply(this, arguments);
            this.lovList = this.options.lovs;
            this.editMode = this.options.editMode;
            this.events['change .type'] = 'typeChanged';
            this.events['change .name'] = 'updateName';
            this.events['click .fa-times'] = 'removeAction';
            this.events['change .attribute-mandatory input'] = 'mandatoryChanged';
            this.events['change .attribute-locked input'] = 'lockedChanged';
            this.events.drop = 'drop';
            this.templateExtraData = {
                lovs : this.lovList.models,
                editMode : this.editMode,
                attributesLocked: this.options.attributesLocked
            };
        },
        rendered: function () {
            this.$el.addClass('well');
            this.$('input[required]').customValidity(App.config.i18n.REQUIRED_FIELD);

            var type = this.model.get('attributeType');
            if (type !== 'LOV'){
                this.$('select.type:first').val(type);
            }else{
                var lovNameSelected = this.model.get('lovName');
                this.$('select.type:first').val(lovNameSelected);
            }

            this.setVisibility();
        },
        removeAction: function () {
            this.model.destroy({
                dataType: 'text' // server doesn't send a json hash in the response body
            });
        },
        typeChanged: function (evt) {
            var typeValue = evt.target.value;
            if (typeValue === 'TEXT' || typeValue === 'LONG_TEXT' || typeValue === 'BOOLEAN' || typeValue === 'DATE' || typeValue === 'PART_NUMBER' || typeValue === 'NUMBER' || typeValue === 'URL' ){
                this.model.set({
                    attributeType: typeValue
                });
                this.setChoice(null);
            }else{
                this.model.set({
                    attributeType: 'LOV'
                });
                this.setChoice(typeValue);
            }
        },
        updateName: function () {
            this.model.set({
                name: this.$el.find('input.name:first').val()
            });
        },
        mandatoryChanged: function () {

            var mandatory = this.$el.find('.attribute-mandatory input')[0].checked;
            if(this.attributesLocked) {
                this.model.set({
                    mandatory: mandatory
                });
            } else {
                this.model.set({
                    mandatory: mandatory,
                    locked: this.model.get('locked') || mandatory
                });
            }

        },

        lockedChanged: function(){
            var locked = this.$el.find('.attribute-locked input')[0].checked;
            this.model.set({
                mandatory : !locked ? false : this.model.get('mandatory'),
                locked: locked
            });

        },

        setAttributesLocked: function(attributesLocked) {
            this.attributesLocked = attributesLocked;
            this.attributesLocked = attributesLocked;
            this.templateExtraData = {
                lovs : this.lovList.models,
                editMode : this.editMode,
                attributesLocked: this.attributesLocked
            };
            this.render();
        },

        setVisibility: function () {
            if (!this.editMode) {
                this.$el.find('i.fa-bars:first').addClass('invisible');
                this.$el.find('a.fa-times:first').addClass('invisible');
            }
        },

        drop: function(event, index) {
            this.$el.trigger('update-sort', [this.model, index]);
        },
        setChoice:function(value){
            this.model.set({
                lovName:value
            });
        }
    });
    return TemplateNewAttributeListItemView;
});
