/*global _,define,App,$*/
define([
    'backbone',
    'mustache',
    'common-objects/models/part',
    'text!templates/part/part_list_item.html',
    'common-objects/views/share/share_entity',
    'common-objects/views/part/part_modal_view',
    'common-objects/utils/date'
], function (Backbone, Mustache, Part, template, ShareView, PartModalView, date) {
    'use strict';
    var PartListItemView = Backbone.View.extend({

        events: {
            'click input[type=checkbox]': 'selectionChanged',
            'click td.modification_notification i': 'toPartModalOnNotificationsTab',
            'click td.part_number': 'toPartModal',
            'click td.part-revision-share i': 'sharePart',
            'click td.part-attached-files i': 'toPartModalOnFilesTab',
            'dragstart a.parthandle': 'dragStart',
            'dragend a.parthandle': 'dragEnd'
        },

        tagName: 'tr',

        initialize: function () {
            _.bindAll(this);
            this._isChecked = false;

            this.listenTo(this.model, 'change', this.render);
            this.listenTo(this.model, 'sync', this.render);

            // jQuery creates it's own event object, and it doesn't have a
            // dataTransfer property yet. This adds dataTransfer to the event object.
            $.event.props.push('dataTransfer');
        },

        render: function () {
            this.$el.html(Mustache.render(template, {model: this.model, i18n: App.config.i18n}));
            this.$checkbox = this.$('input[type=checkbox]');
            if (this.isChecked()) {
                this.check();
                this.trigger('selectionChanged', this);
            }
            this.bindUserPopover();
            this.bindDescriptionPopover();
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
        },

        unCheck: function () {
            this.$checkbox.prop('checked', false);
            this._isChecked = false;
        },

        toPartModal: function () {
            var self = this;
            self.model.fetch().success(function () {
                var partModalView = new PartModalView({
                    model: self.model
                });
                partModalView.show();
            });
        },

        toPartModalOnFilesTab: function () {
            var model = this.model;
            model.fetch().success(function () {
                var partModalView = new PartModalView({
                    model: model
                });
                partModalView.show();
                partModalView.activateFileTab();

            });
        },

        toPartModalOnNotificationsTab: function () {
            var model = this.model;
            model.fetch().success(function () {
                var partModalView = new PartModalView({
                    model: model
                });
                partModalView.show();
                partModalView.activateNotificationsTab();
            });
        },

        bindDescriptionPopover: function() {
            if(this.model.getDescription() !== undefined && this.model.getDescription() !== null && this.model.getDescription() !== '') {
                var self = this;
                this.$('.part_number')
                    .popover({
                        title: App.config.i18n.DESCRIPTION,
                        html: true,
                        content: self.model.getDescription(),
                        trigger: 'hover',
                        placement: 'top',
                        container: 'body'
                    });
            }
        },

        bindUserPopover: function () {
            this.$('.author-popover').userPopover(this.model.getAuthorLogin(), this.model.getNumber(), 'left');
            if (this.model.isCheckout()) {
                this.$('.checkout-user-popover').userPopover(this.model.getCheckOutUserLogin(), this.model.getNumber(), 'left');
            }
        },

        sharePart: function () {
            var shareView = new ShareView({model: this.model, entityType: 'parts'});
            window.document.body.appendChild(shareView.render().el);
            shareView.openModal();
        },

        dragStart: function (e) {
            var that = this;
            this.$el.addClass('moving');

            Backbone.Events.on('part-moved', function () {
                Backbone.Events.off('part-moved');
                Backbone.Events.off('part-error-moved');
                that.model.collection.remove(that.model);
            });
            Backbone.Events.on('part-error-moved', function () {
                Backbone.Events.off('part-moved');
                Backbone.Events.off('part-error-moved');
                that.$el.removeClass('moving');
            });
            var data = JSON.stringify(this.model.toJSON());
            e.dataTransfer.setData('part:text/plain', data);
            e.dataTransfer.dropEffect = 'none';
            e.dataTransfer.effectAllowed = 'copyMove';
            return e;
        },

        dragEnd: function (e) {
            if (e.dataTransfer.dropEffect === 'none') {
                Backbone.Events.off('part-moved');
                Backbone.Events.off('part-error-moved');
            }
            this.$el.removeClass('moving');
        }

    });

    return PartListItemView;

});
