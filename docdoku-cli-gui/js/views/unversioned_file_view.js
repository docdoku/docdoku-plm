define(["text!templates/unVersioned_file.html", "views/part_creation_view", "commander", "storage"], function(template, PartCreationView, Commander, Storage) {

    var UnVersionedlFileView = Backbone.View.extend({

        template: Handlebars.compile(template),

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
            partCreationView.openModal();
            partCreationView.on("part:created",function() {
                self.trigger("part:created");
            })
        }
    });

    return UnVersionedlFileView;
});