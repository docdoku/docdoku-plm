/*global define*/
define([
    'common-objects/views/components/list',
    'common-objects/views/attributes/template_new_attribute_list_item'
], function (ListView, TemplateNewAttributeListItemView) {
    'use strict';
	var TemplateNewAttributeListView = ListView.extend({

        initialize: function () {
            ListView.prototype.initialize.apply(this, arguments);
            this.lovs = this.options.lovs;
            this.editMode = this.options.editMode;
        },

        itemViewFactory: function (model) {
            return new TemplateNewAttributeListItemView({
                model: model,
                lovs:this.lovs,
                editMode:this.editMode
            });
        },
        setAttributesLocked: function(attributesLocked) {
            this.attributesLocked = attributesLocked;
            $.each(this.subViews,function(index,view) {
                view.setAttributesLocked(attributesLocked);
            });
        }
    });
    return TemplateNewAttributeListView;
});
