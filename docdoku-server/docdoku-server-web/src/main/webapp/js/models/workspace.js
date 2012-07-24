define(function () {
	var Workspace = Backbone.Model.extend({
        initialize:function(){
            this.className = "Workspace";
        }
    });
	return Workspace;
});
