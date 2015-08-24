/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baseline_detail.html',
    'text!common-objects/templates/path/path.html',
    'common-objects/views/pathToPathLink/path_to_path_link_item'
], function (Backbone, Mustache, template, pathTemplate, PathToPathLinkItemView) {
    'use strict';
    var BaselineDetailView = Backbone.View.extend({

        events: {
            'hidden #baseline_detail_modal': 'onHidden',
            'close-modal-request': 'closeModal'
        },

        initialize: function () {
            this.productId = this.options.productId;
        },

        render: function () {
            var that = this;
            that.$el.html(Mustache.render(template, {i18n: App.config.i18n, model: that.model}));
            that.bindDomElements();
            that.initExistingPathToPathView();
            that.renderChoices();
            that.openModal();

            window.document.body.appendChild(this.el);
            return this;
        },

        renderChoices: function () {
            var substitutes = this.model.getSubstitutesParts();
            var optionals = this.model.getOptionalsParts();
            this.$substitutesCount.text(substitutes.length);
            this.$optionalsCount.text(optionals.length);

            _.each(substitutes, this.drawSubstitutesChoice.bind(this));
            _.each(optionals, this.drawOptionalsChoice.bind(this));
        },

        bindDomElements: function () {
            this.$notifications = this.$('.notifications');
            this.$modal = this.$('#baseline_detail_modal');
            this.$tabs = this.$('.nav-tabs li');
            this.$substitutes = this.$('.substitutes-list');
            this.$substitutesCount = this.$('.substitutes-count');
            this.$optionals = this.$('.optionals-list');
            this.$optionalsCount = this.$('.optionals-count');
        },

        drawSubstitutesChoice: function (data) {
            this.$substitutes.append(Mustache.render(pathTemplate, {
                i18n: App.config.i18n,
                partLinks:data.partLinks
            }));
            this.$substitutes.find('.well i.fa-long-arrow-right').last().remove();
        },

        drawOptionalsChoice: function (data) {
            this.$optionals.append(Mustache.render(pathTemplate, {
                i18n: App.config.i18n,
                partLinks:data.partLinks
            }));
            this.$optionals.find('.well i.fa-long-arrow-right').last().remove();
        },

        initExistingPathToPathView: function () {

            this.existingPathToPathLinkCollection = [];
            var self = this;

            _.each(self.model.getPathToPathLinks(), function (pathToPathLink) {
                self.existingPathToPathLinkCollection.push({
                    pathToPath: pathToPathLink,
                    productId: self.productId,
                    serialNumber: self.model.getConfigurationItemId()
                });
            });

            _.each(self.existingPathToPathLinkCollection, function (pathToPathLink) {
                var pathToPathLinkItem = new PathToPathLinkItemView({model: pathToPathLink}).render();
                self.$('#path-to-path-links').append(pathToPathLinkItem.el);

                pathToPathLinkItem.on('remove', function () {
                    self.existingPathToPathLinkCollection.splice(self.existingPathToPathLinkCollection.indexOf(pathToPathLink), 1);
                });
            });


        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onHidden: function () {
            this.remove();
        },

        activateTab: function (index) {
            this.$tabs.eq(index).children().tab('show');
        },

        activePathToPathLinkTab: function () {
            this.activateTab(2);
        }

    });

    return BaselineDetailView;
});
