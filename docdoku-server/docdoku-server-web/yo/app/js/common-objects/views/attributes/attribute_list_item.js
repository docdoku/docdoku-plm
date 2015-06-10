/*global define,App*/
define([
    'mustache',
    'common-objects/views/components/list_item'
], function (Mustache, ListItemView) {
    'use strict';
    var AttributeListItemView = ListItemView.extend({

        tagName: 'div',

        editMode: true,

        attributesLocked: false,

        lovs: null,

        setEditMode: function (editMode) {
            this.editMode = editMode;
        },

        initialize: function () {
            ListItemView.prototype.initialize.apply(this, arguments);
            this.events['change .type'] = 'typeChanged';
            this.events['change .name'] = 'updateName';
            this.events['change .value'] = 'updateValue';
            this.events['click .fa-times'] = 'removeAction';
            this.events.drop = 'drop';
            this.lovs = this.options.lovs;
        },

        drop: function (event, index) {
            this.$el.trigger('update-sort', [this.model, index]);
        },

        rendered: function () {
            var type = this.model.get('type');
            if (this.editMode && !this.attributesLocked) {
                this.$el.find('select.type').val(type);
            }
            else {
                this.$el.find('div.type').html(App.config.i18n[type]);
            }

            if (this.model.get('locked') === true) {
                this.$el.find('div.type').html(App.config.i18n[type]);
            }

            this.$el.addClass('well');
            this.$('input[required]').customValidity(App.config.i18n.REQUIRED_FIELD);
        },

        removeAction: function () {
            this.model.destroy({
                dataType: 'text' // server doesn't send a json hash in the response body
            });
        },

        typeChanged: function (evt) {
            var type = evt.target.value;

            if (type !== 'TEXT' && type !== 'NUMBER' && type !== 'BOOLEAN' && type !== 'DATE' && type !== 'URL') {
                this.model.set({
                    type: 'LOV',
                    lovName: type,
                    value: '' // TODO: Validate and convert if possible between types
                });
            } else {
                this.model.set({
                    type: type,
                    value: '' // TODO: Validate and convert if possible between types
                });
            }
            this.model.collection.trigger('reset');
        },

        updateName: function () {
            this.model.set({
                name: this.$el.find('input.name:first').val()
            });
        },

        updateValue: function () {
            var el = this.$el.find('input.value:first');
            this.model.set({
                value: this.getValue(el)
            });
        },

        getValue: function (el) {
            return el.val();
        },

        render: function () {
            this.deleteSubViews();
            var partials = this.partials ? this.partials : null;
            var data = this.renderData();
            data.frozenMode = !this.editMode || this.attributesLocked;
            data.editMode = this.editMode;
            data.attribute = this.model.attributes;
            data.lovs = this.lovs;
            data.items = this.model.get('items');
            this.$el.html(Mustache.render(this.template, data, partials));
            this.rendered();
            this.setVisibility(data.frozenMode);
            return this;
        },

        setAttributesLocked: function (attributesLocked) {
            this.attributesLocked = attributesLocked;
        },

        setVisibility: function (frozenMode) {
            if (frozenMode) {
                this.$el.find('i.fa-bars:first').addClass('invisible');
                this.$el.find('a.fa-times:first').addClass('invisible');
            } else if (this.model.get('locked')) {
                this.$el.find('a.fa-times:first').addClass('invisible');
            }
        }

    });

    return AttributeListItemView;
});
