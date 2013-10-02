define(["text!templates/content.html"], function(template) {

    var ContentView =  Backbone.View.extend({
        template: Handlebars.compile(template),
        render:function() {
            this.$el.html(this.template({}));
            return this;
        },
        onConfigurationError:function(){
            this.$el.html(this.template({error:true}));
        }
    });

    return ContentView;
});