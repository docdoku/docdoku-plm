define(["text!templates/local_unversioned_file.html", "views/part_creation"], function(template, PartCreationView) {

    var LocalUnVersionedlFileView = Backbone.View.extend({

        template: Handlebars.compile(template),

        className: "unversionedFile",

        events: {
            "click .icon-plus" : "newPart"
        },

        render:function() {
            this.$el.html(this.template({model: this.model}));

            return this;
        },

        newPart:function(e){
            var self = this;
            var partCreationView = new PartCreationView({model : this.model});
            $("body").append(partCreationView.render().el);
            partCreationView.on("part:created",function(model) {
                self.trigger("part:created",model);
            })
        }
    });

    return LocalUnVersionedlFileView;
});