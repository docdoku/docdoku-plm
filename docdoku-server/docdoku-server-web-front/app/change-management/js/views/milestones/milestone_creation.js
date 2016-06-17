/*global _,define,App,window*/
define([
    'backbone',
    'mustache',
    'text!templates/milestones/milestone_creation.html',
    'models/milestone',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, MilestoneModel,date) {
    'use strict';
    var MilestoneCreationView = Backbone.View.extend({
        events: {
            'click .modal-footer .btn-primary': 'interceptSubmit',
            'submit #milestone_creation_form': 'onSubmitForm',
            'hidden #milestone_creation_modal': 'onHidden',
            'shown #milestone_creation_modal': 'onShown'
        },


        initialize: function () {
            _.bindAll(this);
            this.model = new MilestoneModel();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                timeZone:App.config.timeZone,
                language : App.config.locale,
                i18n: App.config.i18n
            }));
            this.bindDomElements();
            this.$('input[required]').customValidity(App.config.i18n.REQUIRED_FIELD);
            return this;
        },

        bindDomElements: function () {
            this.$modal = this.$('#milestone_creation_modal');
            this.$inputMilestoneTitle = this.$('#inputMilestoneTitle');
            this.$inputMilestoneDescription = this.$('#inputMilestoneDescription');
            this.$inputMilestoneDueDate = this.$('#inputMilestoneDueDate');
        },
        interceptSubmit : function(){
            this.isValid = ! this.$('#milestone_creation_form').invalidFormTabSwitcher();
        },

        onSubmitForm: function (e) {
            if(this.isValid) {
                var data = {
                    title: this.$inputMilestoneTitle.val(),
                    description: this.$inputMilestoneDescription.val(),
                    dueDate: date.toUTCWithTimeZoneOffset(this.$inputMilestoneDueDate.val())
                };

                this.model.save(data, {
                    success: this.onMilestoneCreated,
                    error: this.onError,
                    wait: true
                });
            }

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onMilestoneCreated: function (model) {
            this.collection.push(model);
            this.closeModal();
        },

        onError: function (model, error) {
            window.alert(App.config.i18n.CREATION_ERROR + ' : ' + error.responseText);
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onShown: function () {
            this.$modal.addClass('ready');
        },

        onHidden: function () {
            this.remove();
        }
    });

    return MilestoneCreationView;
});
