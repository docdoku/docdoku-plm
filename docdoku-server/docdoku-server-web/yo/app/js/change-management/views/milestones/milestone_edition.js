/*global _,define,App,window*/
define([
    'backbone',
    'mustache',
    'text!templates/milestones/milestone_edition.html',
    'common-objects/views/linked/linked_requests',
    'common-objects/collections/linked/linked_change_item_collection',
    'common-objects/views/linked/linked_orders',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, LinkedRequestsView, LinkedChangeItemCollection, LinkedOrdersView, date) {
    'use strict';
    var MilestoneEditionView = Backbone.View.extend({
        events: {
            'submit #milestone_edition_form': 'onSubmitForm',
            'hidden #milestone_edition_modal': 'onHidden'
        },


        initialize: function () {
            this._subViews = [];
            this.model.fetch();
            _.bindAll(this);
            this.$el.on('remove', this.removeSubviews);                                                                 // Remove cascade
        },

        removeSubviews: function () {
            _(this._subViews).invoke('remove');
        },

        render: function () {
            var hasRequest = this.model.getNumberOfRequests() > 0;
            var hasOrder = this.model.getNumberOfOrders() > 0;
            this.removeSubviews();
            this.editMode = this.model.isWritable();
            this.$el.html(Mustache.render(template, {
                timeZone: App.config.timeZone,
                language : App.config.locale,
                i18n: App.config.i18n,
                hasRequest: hasRequest,
                hasOrder: hasOrder,
                model: this.model
            }));
            this.bindDomElements();
            this.initValue();
            this.linkManagement();
            return this;
        },

        bindDomElements: function () {
            this.$modal = this.$('#milestone_edition_modal');
            this.$inputMilestoneTitle = this.$('#inputMilestoneTitle');
            this.$inputMilestoneDescription = this.$('#inputMilestoneDescription');
            this.$inputMilestoneDueDate = this.$('#inputMilestoneDueDate');
        },

        initValue: function () {
            this.$inputMilestoneTitle.val(this.model.getTitle());
            this.$inputMilestoneDueDate.val(this.model.getDueDateDatePicker());
            this.$inputMilestoneDescription.val(this.model.getDescription());
        },

        linkManagement: function () {
            var that = this;
            var $affectedRequestsLinkZone = this.$('#requests-affected-links');

            var affectedRequestsCollection = new LinkedChangeItemCollection();
            affectedRequestsCollection.url = that.model.url() + '/requests';
            affectedRequestsCollection.fetch({
                success: function () {
                    var linkedRequestsView = new LinkedRequestsView({
                        editMode: false,
                        collection: affectedRequestsCollection
                    }).render();

                    that._subViews.push(linkedRequestsView);
                    $affectedRequestsLinkZone.html(linkedRequestsView.el);
                }
            });

            var $affectedOrdersLinkZone = this.$('#orders-affected-links');

            var affectedOrdersCollection = new LinkedChangeItemCollection();
            affectedOrdersCollection.url = that.model.url() + '/orders';
            affectedOrdersCollection.fetch({
                success: function () {
                    var linkedOrdersView = new LinkedOrdersView({
                        editMode: false,
                        collection: affectedOrdersCollection
                    }).render();

                    that._subViews.push(linkedOrdersView);
                    $affectedOrdersLinkZone.html(linkedOrdersView.el);
                }
            });

        },

        onSubmitForm: function (e) {
            var data = {
                title: this.$inputMilestoneTitle.val(),
                description: this.$inputMilestoneDescription.val(),
                dueDate: date.toUTCWithTimeZoneOffset(this.$inputMilestoneDueDate.val())
            };

            this.model.save(data, {
                success: this.closeModal,
                error: this.onError,
                wait: true
            });

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onError: function (model, error) {
            window.alert(App.config.i18n.EDITION_ERROR + ' : ' + error.responseText);
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onHidden: function () {
            this.remove();
        }
    });

    return MilestoneEditionView;
});
