define([
	"common-objects/views/base",
	"text!templates/workspace.html"
], function (
	BaseView,
	template
) {
	var WorkspaceView = BaseView.extend({

		template: Mustache.compile(template),

        menuResizable:function(){
            this.$("#document-menu").resizable({
                containment: this.$el,
                handles: 'e',
                autoHide: true,
                stop: function(e, ui) {
                    var parent = ui.element.parent();
                    ui.element.css({
                        width: ui.element.width()/parent.width()*100+"%",
                        height: ui.element.height()/parent.height()*100+"%"
                    });
                }
            });
        }

	});
	return WorkspaceView;
});
