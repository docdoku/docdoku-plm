/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/change-requests/change_request_list_item.html',
    'views/change-requests/change_request_edition',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, ChangeRequestEditionView,date) {
	'use strict';
	var ChangeRequestListItemView = Backbone.View.extend({

        events: {
            'click input[type=checkbox]': 'selectionChanged',
            'click td.change_request_name': 'openEditionView'
        },

        tagName: 'tr',

        initialize: function () {
            this._isChecked = false;
            this.listenTo(this.model, 'change', this.render);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {model: this.model, i18n: App.config.i18n}));
            this.$checkbox = this.$('input[type=checkbox]');
            this.bindUserPopover();
            date.dateHelper(this.$('.date-popover'));
            this.trigger('rendered', this);
            return this;
        },

        selectionChanged: function () {
            this._isChecked = this.$checkbox.prop('checked');
            this.trigger('selectionChanged', this);
        },

        isChecked: function () {
            return this._isChecked;
        },

        check: function () {
            this.$checkbox.prop('checked', true);
            this._isChecked = true;
            this.trigger('selectionChanged', this);
        },

        unCheck: function () {
            this.$checkbox.prop('checked', false);
            this._isChecked = false;
            this.trigger('selectionChanged', this);
        },

        bindUserPopover: function () {
            this.$('.author-popover').userPopover(this.model.getAuthor(), this.model.getName(), 'left');
            this.$('.assigned-user-popover').userPopover(this.model.getAssignee(), this.model.getName(), 'left');
        },

        openEditionView: function () {
            var _this = this;

            this.model.fetch().success(function () {
                var editionView = new ChangeRequestEditionView({
                    collection: _this.collection,
                    model: _this.model
                });
                window.document.body.appendChild(editionView.render().el);
                editionView.openModal();
            });
        }
    });

    return ChangeRequestListItemView;
});
