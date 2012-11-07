define(function () {
	var Workflow = Backbone.Model.extend({
        initialize:function(){
            this.className = "Workflow";
        }
    });
	return Workflow;
});
