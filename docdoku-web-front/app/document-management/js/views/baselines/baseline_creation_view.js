/*global _,define,App*/
define([
	'backbone',
	'mustache',
	'common-objects/collections/baselines',
    'common-objects/models/document_baseline',
	'text!templates/baselines/baseline_creation_view.html',
    'common-objects/views/alert',
    'views/baselines/document_revision_list'
], function (Backbone, Mustache, Baselines, DocumentBaseline, template, AlertView, DocumentRevisionListView) {

    'use strict';

	var BaselineCreationView = Backbone.View.extend({

		events: {
			'submit #baseline_creation_form': 'onSubmitForm',
			'hidden #baseline_creation_modal': 'onHidden',
            'change select#inputBaselineType': 'changeBaselineType',
            'close-modal-request': 'closeModal'
		},

		initialize: function () {
			_.bindAll(this);

            this.choiceView = new DocumentRevisionListView().render();
            this.listenTo(this.choiceView,'update',this.notifyUpdate.bind(this))
		},

		render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                edition: this.options.mode,
                model: App.config.documentBaselineInProgress
            }));

			this.bindDomElements();

            if (this.options.mode === 'edit') {
                this.fillDocumentsChoiceView();
            }

            this.hideLoader();

            this.$baselineDocumentsChoiceListArea.html(this.choiceView.$el);
            this.$inputBaselineName.customValidity(App.config.i18n.REQUIRED_FIELD);

            return this;
		},

		bindDomElements: function () {
			this.$modal = this.$('#baseline_creation_modal');
            this.$notifications = this.$el.find('.notifications').first();
			this.$inputBaselineName = this.$('#inputBaselineName');
			this.$inputBaselineDescription = this.$('#inputBaselineDescription');
            this.$inputBaselineType = this.$('#inputBaselineType');
            this.$baselineDocumentsChoiceListArea = this.$('.baselineDocumentsChoiceListArea');
            this.$loader = this.$('.loader');
		},

        changeBaselineType: function () {
            var type = this.$inputBaselineType.val();

            // TODO
            //this.resetViews();
            //this.fetchDocumentRevisions(type);
        },

        resetViews: function () {
            this.choiceView.clear();
        },

        fillDocumentsChoiceView: function () {
            this.hideLoader();
            this.choiceView.renderList(App.config.documentBaselineInProgress.getBaselinedDocuments());
        },

        onRequestsError:function(xhr,type,message){
            this.$loader.hide();
            this.$notifications.append(new AlertView({
                type:'error',
                message:message
            }).render().$el);
        },

        showLoader:function(){
            this.$loader.show();
        },
        hideLoader:function(){
            this.$loader.hide();
        },

		onSubmitForm: function (e) {
            this.$notifications.empty();

            this.model = new DocumentBaseline({
                name: this.$inputBaselineName.val(),
                description: this.$inputBaselineDescription.val(),
                type: this.$inputBaselineType.val(),
                baselinedDocuments: []
            });

            if (this.options.mode === 'edit') {
                this.model.setBaselinedDocuments(App.config.documentBaselineInProgress.getBaselinedDocuments());
                var saveOptions = {};

                this.model.save(saveOptions, {
                    wait: true,
                    success: this.onBaselineCreated,
                    error: this.onError
                });

            } else {
                App.config.documentBaselineInProgress = this.model;
                App.appView.showBaselineTooltip();
            }

			e.preventDefault();
			e.stopPropagation();

            if (this.options.mode === 'edit') {
                return false;
            } else {
                this.closeModal();
            }
		},

		onBaselineCreated: function (e) {
            if (e.message) {
                this.trigger('warning', e.message);
            }

            App.config.documentBaselineInProgress = undefined;
            this.closeModal();
		},

		onError: function (model, error) {
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
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

        notifyUpdate:function(){
            this.trigger('update');
        }

	});

	return BaselineCreationView;
});
