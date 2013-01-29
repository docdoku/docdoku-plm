define(function () {
	var Tag = Backbone.Model.extend({
        initialize: function(){
            this.className = "Tag";
        }
    });
	return Tag;
});
