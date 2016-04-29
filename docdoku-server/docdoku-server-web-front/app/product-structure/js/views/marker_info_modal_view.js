/*global define,App,_*/
define([
	'backbone',
	'mustache',
	'text!templates/marker_info_modal.html'
], function (Backbone, Mustache, template) {
	'use strict';
	var MarkerInfoModalView = Backbone.View.extend({

		events: {
			'click .destroy-marker-btn': 'destroyMarker',
			'hidden #markerModal': 'onHidden'
		},

		initialize: function () {
			_.bindAll(this);
		},

		render: function () {
			this.$el.html(Mustache.render(template, {i18n: App.config.i18n, title: this.model.getTitle()}));
			this.$modal = this.$('#markerModal');
			this.$('#markerDesc').html(this.model.getDescription().nl2br());

			return this;
		},

		destroyMarker: function () {
			if (this.model) {
				this.model.destroy({
					dataType: 'text', // server doesn't send a json hash in the response body
					success: function () {
                        if(App.collaborativeController){
                            App.collaborativeController.sendMarkersRefresh('remove marker');
                        }
					}
				});
			}
			this.closeModal();
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

	return MarkerInfoModalView;
});
