/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baseline_detail.html',
    'text!templates/configuration/configuration_choice.html',
    'views/baselines/baselined_part_list',
    'common-objects/views/typedLink/typed-link-item'
], function (Backbone, Mustache, template, choiceTemplate, BaselinePartListView,TypedLinkItemView) {
    'use strict';
    var BaselineDetailView = Backbone.View.extend({

        events: {
            'hidden #baseline_detail_modal': 'onHidden',
            'close-modal-request':'closeModal'
        },

        initialize: function () {
            this.productId = this.options.productId;
        },

        render: function () {
            var that = this;
            this.model.fetch().success(function () {
                that.$el.html(Mustache.render(template, {i18n: App.config.i18n, model: that.model}));
                that.bindDomElements();
                that.initBaselinedPartListView();
                that.initExistingPathToPathView();
                that.renderChoices();
                that.openModal();
            });
            window.document.body.appendChild(this.el);
            return this;
        },

        renderChoices:function(){
            var substitutes = this.model.getSubstitutesParts();
            var optionals = this.model.getOptionalsParts();
            this.$substitutesCount.text(substitutes.length);
            this.$optionalsCount.text(optionals.length);

            _.each(substitutes,this.drawSubstitutesChoice.bind(this));
            _.each(optionals,this.drawOptionalsChoice.bind(this));
        },

        bindDomElements: function () {
            this.$notifications = this.$('.notifications');
            this.$modal = this.$('#baseline_detail_modal');
            this.$tabs = this.$('.nav-tabs li');
            this.$baselinedPartListArea = this.$('.baselinedPartListArea');
            this.$substitutes = this.$('.substitutes-list');
            this.$substitutesCount = this.$('.substitutes-count');
            this.$optionals = this.$('.optionals-list');
            this.$optionalsCount = this.$('.optionals-count');
        },

        initBaselinedPartListView: function () {
            this.baselinePartListView = new BaselinePartListView({
                model: this.model,
                editMode:false
            }).render();
            this.baselinePartListView.renderList();
            this.$baselinedPartListArea.html(this.baselinePartListView.$el);
        },

        drawSubstitutesChoice:function(data){
            this.$substitutes.append(Mustache.render(choiceTemplate, {i18n: App.config.i18n, data: {
                parts:data.parts,
                concernedPart:data.parts.pop()
            }}));
            this.$substitutes.find('i.fa-chevron-right:last-child').remove();
        },

        drawOptionalsChoice:function(data){
            this.$optionals.append(Mustache.render(choiceTemplate, {i18n: App.config.i18n, data: {
                parts:data.parts,
                concernedPart:data.parts.pop()
            }}));
            this.$optionals.find('i.fa-chevron-right:last-child').remove();
        },
         initExistingPathToPathView : function() {

             this.existingPathToPathLinkCollection = [];
             this.availableType = [];
             var self = this;
             var urlForExistingTypedLink = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.model.getConfigurationItemId() + '/baselines/' + this.model.getId() + '/path-to-path-links-types-details';
             $.ajax({
                 type: 'GET',
                 url: urlForExistingTypedLink,
                 contentType: 'application/json',
                 success: function (pathToPathLinkDTOs) {
                     _.each(pathToPathLinkDTOs, function (pathToPathLinkDTO) {
                         self.existingPathToPathLinkCollection.push({
                             source: pathToPathLinkDTO.source,
                             target: pathToPathLinkDTO.target,
                             pathToPath: pathToPathLinkDTO,
                             productId: self.productId,
                             serialNumber: self.model.getConfigurationItemId()
                         });
                     });

                     _.each(self.existingPathToPathLinkCollection, function (pathToPathLink) {
                         var typeLinkItem = new TypedLinkItemView({model: pathToPathLink}).render();
                         self.$('#path-to-path-links').append(typeLinkItem.el);

                         typeLinkItem.on('remove', function () {
                             self.existingPathToPathLinkCollection.splice(self.existingPathToPathLinkCollection.indexOf(pathToPathLink), 1);
                         });
                     });
                 }, error: function (errorMessage) {
                     self.$('#typed-link-alerts').append(new AlertView({
                         type: 'error',
                         message: errorMessage.responseText
                     }).render().$el);
                 }
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
        activeTypedLinkTab: function () {
            this.activateTab(6);
        }

    });

    return BaselineDetailView;
});
