define (["text!templates/part_meta_data.html"],function(template) {
    var PartMetadataView = Backbone.View.extend({

        el:"#part_metadata_container",

        template: Mustache.compile(template),

        events: {
            "click .toggle": "togglePartMetadata"
        },

        initialize: function() {
            this.listenTo(this.model, 'change' , this.render);
        },

        setModel:function(model){
            this.model = model;
        },

        render: function() {

            this.$el.html(Mustache.render(template, this.model));

            this.$(".author-popover").userPopover(this.model.getAuthorLogin(),this.model.getNumber(),"top");
            this.$(".icon-user").userPopover(this.model.getAuthorLogin(),this.model.getNumber(),"top");

            return this;
        },

        togglePartMetadata: function() {
            this.$el.toggleClass('minimized');
        }
        
    });

    return PartMetadataView;
});