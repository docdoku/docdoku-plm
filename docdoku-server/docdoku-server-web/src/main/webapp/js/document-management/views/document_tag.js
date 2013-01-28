define(
    [
        "text!templates/document/document_tag.html"
    ],
    function(template){ 

        var DocumentTagView = Backbone.View.extend({

            tagName : "li",

            initialize : function(){
                return this ;
            },

            render:function(){
                $(this.el).html(Mustache.render(template,{tag:this.model}));
                return this ;
            }

        });


    return DocumentTagView;
});