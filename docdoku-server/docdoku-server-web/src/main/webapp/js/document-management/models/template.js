define(function () {
	var Template = Backbone.Model.extend({
        initialize:function(){
            this.className = "Template";
        }
    });
	return Template;
});
