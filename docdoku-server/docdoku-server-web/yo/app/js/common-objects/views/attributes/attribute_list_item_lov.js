/*global $,_,define,App*/
define([
    'common-objects/views/attributes/attribute_list_item',
    'text!common-objects/templates/attributes/attribute_list_item.html',
    'text!common-objects/templates/attributes/attribute_list_item_lov.html'
], function (AttributeListItemView, attributeListItem, template) {
    'use strict';
    var AttributeListItemLOVView = AttributeListItemView.extend({

        template: template,
        partials: {
            attributeListItem: attributeListItem
        },

        possibleValues : null,

        initialize: function () {
            AttributeListItemView.prototype.initialize.apply(this, arguments);
        },

        rendered:function(){

            this.modelChange = function(){};

            var type = this.model.get('type') || this.model.get('attributeType');
            var items = this.model.get('items');
            var typeCopy = type;
            if (type === 'LOV') {
                type = this.model.get('lovName');
            }
            if ((this.editMode && !this.attributesLocked && !this.model.get('locked') && !this.model.get('mandatory')) || this.displayOnly) {
                this.$el.find('select.type').val(type);
            } else {
                this.$('div.type').html(type);
            }

            if (this.model.get('locked') === true && !type) {
                this.$el.find('div.type').html(App.config.i18n.LOV);
            }

            this.$el.addClass('well');
            this.$('input[required]').customValidity(App.config.i18n.REQUIRED_FIELD);

            var that = this;
            if (!items) {
                if (typeCopy === 'LOV') {
                    $.ajax({
                        type: 'GET',
                        url: App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/lov/' + type,
                        contentType: 'application/json; charset=utf-8',
                        success: function (data) {
                            var possibleValues = data.values;
                            that.model.set('items', possibleValues);
                            that.addOptions(possibleValues);
                        },
                        error: function () {
                        }
                    });
                }
            } else {
                if (typeCopy === 'LOV') {
                    var lovName = this.model.get('lovName');
                    if (!lovName) {
                        if (this.editMode && !this.attributesLocked) {
                            this.$el.find('select.type').parent().addClass('listOfValues');
                            this.$el.find('select.type').parent().html(App.config.i18n.LOV);
                        } else {
                            this.$('div.type').html(App.config.i18n.LOV);
                        }

                        if (this.model.get('locked') === true) {
                            this.$el.find('div.type').html(App.config.i18n.LOV);
                        }
                    }
                    this.addOptions(items);
                }
            }

        },

        addOptions: function(items){
            var select = this.$('.value');
            var defaultValue = 0;
            select.html('');
            _.each(items, function(item, index){
                if(index === 0){
                    defaultValue = item.name;
                }
                select.append('<option value="'+index+'">'+item.name+'</option>');
            });
            var selectedValue = this.model.get('value');
            if(selectedValue){
                select.val(selectedValue);
            }else{
                select.val(defaultValue);
            }

            this.updateValue();
        },

        typeChanged: function(){
            this.model.set('items', null);
            AttributeListItemView.prototype.typeChanged.apply(this, arguments);
        },

        updateValue: function () {
            var el = this.$el.find('.value');
            this.model.set({
                value: el.val()
            });
        }
    });
    return AttributeListItemLOVView;
});
