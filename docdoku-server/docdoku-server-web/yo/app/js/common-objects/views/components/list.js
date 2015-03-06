/*global define*/
define([
    'common-objects/views/base'
], function (BaseView) {
    'use strict';
    var ListView = BaseView.extend({
        collectionReset: function () {
            this.clear();
            this.render();
            this.collection.each(this.createItemView);
            this.trigger('_ready');
        },
        collectionAdd: function () {
            this.collectionReset();
        },
        collectionRemove: function () {
            this.collectionReset();
        },
        createItemView: function (model) {
            var view = this.addSubView(
                this.itemViewFactory(model)
            );
            this.$el.append(view.el);
            view.render();
        }
    });
    return ListView;
});
