define([
    "common-objects/utils/date",
    "i18n!localization/nls/document-management-strings",
    "text!templates/document/document_lifecycle_task.html"
], function(date, i18n, template) {

    var LifecycleTaskView = Backbone.View.extend({

        tagName: 'div',
        className:'task well',

        events: {
            "click i" : "toggleComment"
        },

        initialize: function() {
        },

        setTask:function(task){
            this.task = task;

            if(this.task.closureDate){
                this.task.formattedClosureDate = date.formatTimestamp(
                    i18n._DATE_SHORT_FORMAT,
                    this.task.closureDate
                );
            }

            return this;
        },

        render: function() {

            this.$el.html(Mustache.render(template, {i18n: i18n, task:this.task}));
            this.$el.addClass(this.task.status.toLowerCase());
            this.$(".user-popover").userPopover(this.task.worker.login,this.task.title,"top");
            return this;
        },

        toggleComment:function(){
            this.$(".task-comment p").toggleClass("toggled");
        }

    });
    return LifecycleTaskView;

});