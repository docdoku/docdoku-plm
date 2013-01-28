define(
    [
        "text!templates/document/document_tag.html"
    ],
    function(template){ 

        var DocumentTagView = Backbone.View.extend({

            tagName : "li",
            className:"pull-left",

             events : {
               "click a":"clicked"
             },

            initialize : function(){
                return this ;
            },

            render:function(){

                this.$el.html(Mustache.render(template,{tag:this.model, iconClass:this.options.iconClass}));
                return this ;
            },

            clicked : function(){

                if(this.options.clicked){
                    this.options.clicked();
                }
            }



        });


    return DocumentTagView;
});