window.PartItemView = Backbone.View.extend({

    tagName:'li',

    template: _.template("<input type='checkbox' value=''>"+
                        "<a href='#'>" +
                            "<label class='checkbox'>" +
                                "<%= number %>" +
                            "</label>" +
                        "</a>"),

    events: {
        "click li a"   : "showPartMetadata"
    },

    render: function(){

        this.$el.html(this.template(this.model.toJSON()));

        if(this.model.isNode()){
            this.$el.find('label').addClass("isNode");
        }

        return this;
    },

    showPartMetadata:function(e){
        e.stopPropagation();

        $("#part_metadata_container").empty();
        var partMetadataView = new PartMetadataView({model: this.model});
        partMetadataView.render();

        $("#bottom_controls_container").hide();
        $("#part_metadata_container").show();
    }
});