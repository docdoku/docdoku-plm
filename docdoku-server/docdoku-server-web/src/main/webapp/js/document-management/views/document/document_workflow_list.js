define([
	"collections/workflow_models",
	"common-objects/views/base",
	"text!templates/document/document_workflow_select.html"
], function (
	WorkflowList,
	BaseView,
	template
) {
	var DocumentWorkflowListView = BaseView.extend({

		template: Mustache.compile(template),

        initialize: function() {
            BaseView.prototype.initialize.apply(this, arguments);
            this.events["change #select-" + this.cid] = "onChange";
        },

        onChange:function(){
            this.trigger("workflow:change",this.selected());
        },

		collection: function () {
            var collection = new WorkflowList();
            collection.fetch();
			return collection;
		},

		collectionReset: function () {
			this.render();
		},

		collectionToJSON: function () {
			var data = BaseView.prototype.collectionToJSON.call(this);
			data.unshift({
				id: ""
			});
			return data;
		},

		selected: function () {
			var id = $("#select-" + this.cid).val();
			var model = this.collection.get(id);
			return model;
		}

	});

	return DocumentWorkflowListView;
});
