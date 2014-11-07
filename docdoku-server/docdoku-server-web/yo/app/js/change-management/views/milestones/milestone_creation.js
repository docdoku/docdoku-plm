/*global _,define,App*/
define([
        'backbone',
        'mustache',
        'text!templates/milestones/milestone_creation.html',
        'models/milestone'
],
function (Backbone, Mustache, template, MilestoneModel) {
    'use strict';
    var MilestoneCreationView = Backbone.View.extend({
        events: {
            'submit #milestone_creation_form': 'onSubmitForm',
            'hidden #milestone_creation_modal': 'onHidden'
        },


        initialize: function () {
            _.bindAll(this);
            this.model = new MilestoneModel();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            return this;
        },

        bindDomElements: function () {
            this.$modal = this.$('#milestone_creation_modal');
            this.$inputMilestoneTitle = this.$('#inputMilestoneTitle');
            this.$inputMilestoneDescription = this.$('#inputMilestoneDescription');
            this.$inputMilestoneDueDate = this.$('#inputMilestoneDueDate');
        },

        onSubmitForm: function (e) {
            var data = {
                title: this.$inputMilestoneTitle.val(),
                description: this.$inputMilestoneDescription.val(),
                dueDate: this.$inputMilestoneDueDate.val() + 'T00:00:00'
            };

            this.model.save(data, {
                success: this.onMilestoneCreated,
                error: this.onError,
                wait: true
            });

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onMilestoneCreated: function (model) {
            this.collection.push(model);
            this.closeModal();
        },

        onError: function (model, error) {
            alert(App.config.i18n.CREATION_ERROR + ' : ' + error.responseText);
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

    return MilestoneCreationView;
});
