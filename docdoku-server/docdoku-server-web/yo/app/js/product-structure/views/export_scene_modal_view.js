/*global define,App,_*/
define([
        'backbone',
        'mustache',
        'text!templates/export_scene_modal.html'
    ], function (Backbone, Mustache, template) {

        'use strict';

        var ExportSceneModalView = Backbone.View.extend({

            events: {
                'hidden #exportSceneModal': 'onHidden',
                'click textarea': 'onClickTextArea'
            },

            initialize: function () {
                _.bindAll(this);
            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
                this.$modal = this.$('#exportSceneModal');
                this.$textarea = this.$('textarea');
                this.$link = this.$('.frame-link');
                return this;
            },

            openModal: function () {
                this.$modal.modal('show');
                this.$textarea.text('<iframe width="640" height="480" src="' + this.options.iframeSrc + '" frameborder="0"></iframe>');
                this.$link.attr('href',this.options.iframeSrc);

            },

            onClickTextArea: function () {
                this.$textarea.select();
            },

            closeModal: function () {
                this.$modal.modal('hide');
            },

            onHidden: function () {
                this.remove();
            }

        });

        return ExportSceneModalView;
    }
);
