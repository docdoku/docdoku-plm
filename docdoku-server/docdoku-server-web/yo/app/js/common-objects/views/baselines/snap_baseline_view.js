/*global define*/
'use strict';
define([
    'backbone',
    "mustache",
    'text!common-objects/templates/baselines/snap_baseline_view.html'
], function (Backbone, Mustache, template) {
    var SnapLatestBaselineView = Backbone.View.extend({
        events: {
            'submit #baseline_creation_form': 'onSubmitForm',
            'hidden #baseline_creation_modal': 'onHidden'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n, model: this.model, isReleased: this.options.type === 'RELEASED', isLatest: this.options.type == 'LATEST' }));
            this.bindDomElements();
            if (this.options.type) {
                this.$inputBaselineType.val(this.options.type);
            }
            return this;
        },

        bindDomElements: function () {
            this.$modal = this.$('#baseline_creation_modal');
            this.$inputBaselineName = this.$('#inputBaselineName');
            this.$inputBaselineType = this.$('#inputBaselineType');
            this.$inputBaselineDescription = this.$('#inputBaselineDescription');
        },

        onSubmitForm: function (e) {
            this.model.createBaseline(
                {
                    name: this.$inputBaselineName.val(),
                    type: this.$inputBaselineType.val(),
                    description: this.$inputBaselineDescription.val()
                },
                {
                    success: this.onBaselineCreated,
                    error: this.onError
                }
            );
            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onBaselineCreated: function () {
            this.closeModal();
        },

        onError: function (error) {
            alert(APP_CONFIG.i18n.CREATION_ERROR + ' : ' + error.responseText);
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

    return SnapLatestBaselineView;
});