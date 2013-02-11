define(    [
        "text!templates/marker_info_modal.html",
        "i18n!localization/nls/product-structure-strings"
    ],

    function (template, i18n) {

        var MarkerInfoModalView = Backbone.View.extend({

            events: {
                "click .destroy-marker-btn" : "destroyMarker",
                "hidden #markerModal": "onHidden"
            },

            template: Mustache.compile(template),

            initialize: function() {
                _.bindAll(this);
            },

            render: function() {
                this.$el.html(this.template({i18n: i18n, title:this.model.getTitle()}));
                this.$modal = this.$("#markerModal");
                this.$('#markerDesc').html(this.model.getDescription().nl2br());

                return this;
            },

            destroyMarker: function() {
                if(this.model) this.model.destroy();
                this.closeModal();
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

        return MarkerInfoModalView;
    }

);