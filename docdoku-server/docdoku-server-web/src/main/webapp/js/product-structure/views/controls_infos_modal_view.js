define(    [
    "text!templates/controls_infos_modal.html",
    "i18n!localization/nls/product-structure-strings"
],

    function (template, i18n) {

        var ControlsInfosModalView = Backbone.View.extend({

            events: {
                "hidden #controlsInfosModal": "onHidden"
            },

            template: Mustache.compile(template),

            initialize: function() {
                _.bindAll(this);
            },

            render: function() {
                if(this.options.isPLC) {
                    this.$el.html(this.template({i18n: i18n, isPLC:true}));
                } else if (this.options.isTBC) {
                    this.$el.html(this.template({i18n: i18n, isTBC:true}));
                } else if (this.options.isORB) {
                    this.$el.html(this.template({i18n: i18n, isORC:true}));
                }

                this.$modal = this.$("#controlsInfosModal");

                return this;
            },

            openModal: function() {
                this.$modal.modal('show');
            },

            closeModal: function() {
                this.$modal.modal('hide');
            },

            onHidden: function() {
                this.remove();
            }

        });

        return ControlsInfosModalView;
    }

);