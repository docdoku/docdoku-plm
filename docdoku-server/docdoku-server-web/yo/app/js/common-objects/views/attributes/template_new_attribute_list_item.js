/*global define,App*/
define([
    'common-objects/views/components/list_item',
    'text!common-objects/templates/attributes/template_new_attribute_list_item.html'
], function (ListItemView, template) {
    'use strict';
    var TemplateNewAttributeListItemView = ListItemView.extend({

        template: template,

        tagName: 'div',

        lovNameChoiceDiv : null,

        lovList:null,

        initialize: function () {
            ListItemView.prototype.initialize.apply(this, arguments);
            this.lovList = this.options.lovs,
            this.events['change .type'] = 'typeChanged';
            this.events['change .name'] = 'updateName';
            this.events['click .fa-times'] = 'removeAction';
            this.events['change .attribute-mandatory input'] = 'mandatoryChanged';
            this.events.drop = 'drop';
            this.events['change .lovNameChoice'] = 'lovChoiceChanged';
            this.templateExtraData = {lovs : this.lovList.models};
        },
        rendered: function () {
            var type = this.model.get('attributeType');
            this.$('select.type:first').val(type);
            this.$el.addClass('well');
            this.$('input[required]').customValidity(App.config.i18n.REQUIRED_FIELD);
            this.lovNameChoiceDiv = this.$('.lovNameChoice');

            var lovNameSelected = this.model.get('lovName');
            if(lovNameSelected){
                this.displayLOVList();
                this.$('.lovNameChoice').val(lovNameSelected);
            }

        },
        removeAction: function () {
            this.model.destroy({
                dataType: 'text' // server doesn't send a json hash in the response body
            });
        },
        typeChanged: function (evt) {
            var typeValue = evt.target.value;
            if (typeValue === 'LOV'){
                this.displayLOVList();
                this.setChoice(this.lovList.models[0].getLOVName());
            }else{
                this.hideLovList();
            }
            this.model.set({
                attributeType: typeValue
            });
        },
        updateName: function () {
            this.model.set({
                name: this.$el.find('input.name:first').val()
            });
        },
        mandatoryChanged: function () {
            this.model.set({
                mandatory: this.$el.find('.attribute-mandatory input')[0].checked
            });
        },
        drop: function(event, index) {
            this.$el.trigger('update-sort', [this.model, index]);
        },
        displayLOVList:function(){
            this.$el.addClass('lovChoiceType');
        },
        hideLovList:function(){
            this.$el.removeClass('lovChoiceType');
            this.setChoice(null);
        },
        lovChoiceChanged: function(event){
            this.setChoice(event.target.value);
        },
        setChoice:function(value){
            this.model.set({
                lovName:value
            });
        }
    });
    return TemplateNewAttributeListItemView;
});
