define([
    "common-objects/utils/date",
    "i18n!localization/nls/document-management-strings",
    "text!templates/document/document_lifecycle_task.html"
], function(date, i18n, template) {

    var LifecycleTaskView = Backbone.View.extend({

        tagName: 'div',
        className:'task well',

        events: {
            "click i.toggle-comment" : "toggleComment",
            "click i.approve-task" : "approveTaskButtonClicked",
            "click i.reject-task" : "rejectTaskButtonClicked",
            "submit .closure-comment form":"submitClosure",
            "click .closure-comment .cancel":"cancelClosure"
        },

        initialize: function() {
            this.APPROVE_MODE = "1" ;
            this.REJECT_MODE = "2" ;
        },

        setTask:function(task){
            this.task = task;

            if(this.task.closureDate){
                this.task.formattedClosureDate = date.formatTimestamp(
                    i18n._DATE_FORMAT,
                    this.task.closureDate
                );
            }

            this.task.isAcceptableOrRejectable = (
                this.task.worker.login == APP_CONFIG.login &&
                    this.task.status.toLowerCase() == "in_progress"
            );

            return this;
        },

        render: function() {
            this.$el.html(Mustache.render(template, {i18n: i18n, task:this.task}));
            this.$el.addClass(this.task.status.toLowerCase());
            this.$(".user-popover").userPopover(this.task.worker.login,this.task.title,"top");
            this.bindDomElements();
            return this;
        },

        bindDomElements:function(){
            this.$comment = this.$(".task-comment");
            this.$closureComment = this.$(".closure-comment");
            this.$closureCommentTitle = this.$closureComment.find("h5");
            this.$closureTypeInput = this.$closureComment.find("input[name=closure-type]");
            this.$commentInput = this.$closureComment.find("input[name=closure-comment-input]");
        },

        toggleComment:function(){
            this.$comment.toggleClass("toggled");
        },

        approveTaskButtonClicked:function (){
            this.$closureComment.addClass("toggled");
            this.$closureTypeInput.val(this.APPROVE_MODE);
            this.$closureCommentTitle.text(i18n.APPROVE_TASK);
        },

        rejectTaskButtonClicked:function() {
            this.$closureComment.addClass("toggled");
            this.$closureTypeInput.val(this.REJECT_MODE);
            this.$closureCommentTitle.text(i18n.REJECT_TASK);
        },

        submitClosure:function(e){

            var processUrl = "/api/workspaces/"
                + APP_CONFIG.workspaceId
                + "/tasks/process?"
                +"activityWorkflowId="+this.task.parentWorkflowId
                +"&index="+this.task.index
                +"&activityStep="+this.task.parentActivityStep;

            var closureComment = this.$commentInput.val() ;
            var closureType = this.$closureTypeInput.val() ;

            if(closureType == this.APPROVE_MODE){

                $.ajax({
                    context: this,
                    type: "POST",
                    url: processUrl+"&action=approve",
                    data : closureComment,
                    contentType: "text/plain",
                    success: function() {
                        this.task.closureDate = new Date().getTime();
                        this.task.closureComment = closureComment;
                        this.task.status = "approved";
                        this.refreshTask();
                    },
                    error:function(error){
                        alert(error.responseText);
                    }
                });

            }else if(closureType == this.REJECT_MODE){

                $.ajax({
                    context: this,
                    type: "POST",
                    url: processUrl+"&action=reject",
                    data : closureComment,
                    contentType: "text/plain",
                    success: function() {
                        this.task.closureDate = new Date().getTime();
                        this.task.closureComment = closureComment;
                        this.task.status = "rejected";
                        this.refreshTask();
                    },
                    error:function(error){
                        alert(error.responseText);
                    }
                });

            }

            e.stopPropagation();
            e.preventDefault();
            return false ;
        },

        cancelClosure:function(e){
            this.$closureComment.removeClass("toggled");
            e.stopPropagation();
            e.preventDefault();
            return false ;
        },

        refreshTask:function(){
            this.trigger("task:change");
           // this.setTask(this.task);
          //  this.render();
        }

    });
    return LifecycleTaskView;

});