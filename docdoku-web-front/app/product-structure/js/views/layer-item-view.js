/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/layer_item.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var LayerItemView = Backbone.View.extend({
        tagName: 'li',

        events: {
            'click i.start': 'toggleShow',
            'dblclick': 'startEditingName',
            'blur .edit': 'stopEditingName',
            'keypress .edit': 'stopEditingNameOnEnter',
            'click i.end': 'toggleEditingMarkers',
            'click i.fa-times': 'removeLayer',
            'change input[type="color"]':'colorChanged'
        },

        initialize: function () {
            this.listenTo(this.model, 'destroy', this.remove)
                .listenTo(this.model, 'change:editingName change:editingMarkers change:shown change:color', this.render)
                .listenTo(this.model.getMarkers(), 'add remove reset', this.render);
        },

        render: function () {
            this.$el.html(Mustache.render(template, this.model));
            this.$el.toggleClass('shown', this.model.get('shown'));
            var editingName = this.model.get('editingName');
            this.$el.toggleClass('editingName', editingName);
            this.input = this.$('.edit');
            if (editingName) {
                this.input.focus();
            }
            this.$el.toggleClass('editingMarkers', this.model.get('editingMarkers'));
            return this;
        },

        toggleShow: function () {
            this.model.toggleShow();
        },

        toggleEditingMarkers: function () {
            this.model.toggleEditingMarkers();
        },

        startEditingName: function () {
            this.model.setEditingName(true);
        },

        stopEditingName: function () {
            var value = this.input.val();
            if (this.model.get('name') !== value) {
                this.model.save({
                        name: value,
                        editingName: false
                    },
                    {success: function () {
                        App.collaborativeController.sendLayersRefresh('edit layer name');
                    }}
                );
            } else {
                this.model.set('editingName', false);
            }
        },

        stopEditingNameOnEnter: function (e) {
            if (e.keyCode === 13) {
                this.input[0].blur();
            }
        },

        removeLayer: function () {
            var collection = this.model.collection;
            this.model.setEditingMarkers(false);
            this.model.destroy({
                dataType: 'text', // server doesn't send a json hash in the response body
                success: function () {
                    App.collaborativeController.sendLayersRefresh('layer:remove');
                }
            });
            if (collection.length === 0) {
                collection.onEmpty();
            }

        },
        colorChanged:function(e){
            this.model.setColor(e.target.value.replace('#',''));
            App.sceneManager.reDraw();
            this.model.save();
        }

    });

    return LayerItemView;

});
