define([
	"collections/task_document",
	"views/content_document_list",
    "text!templates/content_document_list_checkout_button_group.html",
    "text!templates/content_document_list_tags_button.html",
    "text!templates/content_document_list_new_version_button.html",
    "text!templates/content_document_list_acl_button.html",
    "text!templates/search_document_form.html",
    "text!templates/status_filter.html",
    "text!templates/task_document_list.html"
], function (
	TaskDocumentList,
	ContentDocumentListView,
    checkout_button_group,
    tags_button,
    new_version_button,
    acl_button,
    search_form,
    status_filter,
    template
) {
	var TaskDocumentListView = ContentDocumentListView.extend({

        template: Mustache.compile(template),

        partials: {
            checkout_button_group: checkout_button_group,
            tags_button:tags_button,
            new_version_button: new_version_button,
            search_form:search_form,
            acl_button:acl_button,
            status_filter:status_filter
        },

		collection: function () {
            var c = new TaskDocumentList();
            c.setFilterStatus(this.options.filter);
            return c;
		},

		initialize: function () {
			ContentDocumentListView.prototype.initialize.apply(this, arguments);
			if (this.model) {
				this.collection.parent = this.model;
			}
		},
        rendered:function(){
            ContentDocumentListView.prototype.rendered.apply(this, arguments);
            var filter = this.options.filter || "all";
            this.$("button.filter[value="+filter+"]").addClass("active")
        }
    });
	return TaskDocumentListView;
});
