define(    [
    "text!templates/export_scene_modal.html",
    "i18n!localization/nls/product-structure-strings"
],

    function (template, i18n) {

        var ExportSceneModalView = Backbone.View.extend({

            events: {
                "hidden #exportSceneModal": "onHidden",
                "click textarea": "onClickTextArea"
            },

            template: Mustache.compile(template),

            initialize: function() {
                _.bindAll(this);
            },

            render: function() {
                this.$el.html(this.template({i18n: i18n}));
                this.$modal = this.$("#exportSceneModal");
                this.$textarea = this.$('textarea');
                return this;
            },

            openModal: function() {
                this.$modal.modal('show');
                this.$textarea.text('<iframe width="640" height="480" src="' + this.options.iframeSrc + '" frameborder="0"></iframe>');

            },

            onClickTextArea: function() {
                this.$textarea.select();
            },

            closeModal: function() {
                this.$modal.modal('hide');
            },

            onHidden: function() {
                this.remove();
            }

        });

        return ExportSceneModalView;
    }
);
