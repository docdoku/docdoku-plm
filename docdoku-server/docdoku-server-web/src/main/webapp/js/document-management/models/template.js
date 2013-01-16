define(function () {
	var Template = Backbone.Model.extend({
        initialize:function(){
            this.className = "Template";
        },

        toJSON: function(){
            return this.clone().set({attributeTemplates :
                _.reject(this.get("attributeTemplates"),
                    function(attribute){
                        return attribute.name == "";
                    }
                )}, {silent: true}).attributes;
        }
    });
	return Template;
});
