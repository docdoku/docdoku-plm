define([
    "i18n",
    "models/task_model",
    "text!templates/task_model_editor.html"
], function (
    i18n,
    TaskModel,
    template
    ) {
    var TaskModelEditorView = Backbone.View.extend({

        tagName: "li",

        className: "task-section",

        events: {
            "click button.delete-task" : "deleteTaskAction",
            "change input.task-name" : "titleChanged",
            "change textarea.instructions" : "instructionsChanged",
            "change select.worker": "workerSelected"
        },

        initialize: function () {
            var self = this;

            if(_.isUndefined(this.model.get("worker")))
                this.model.set({
                    worker: this.options.users.at(0)
                });

            var users = [];
            _.each(this.options.users.models, function(user){
                if(self.model.get("worker") && self.model.get("worker").get("login") == user.get("login"))
                    users.push({login: user.get("login"), selected: true});
                else
                    users.push({login: user.get("login"), selected: false});
            });

            this.template = Mustache.render(template, {cid: this.model.cid, task: this.model.attributes, users: users, i18n: i18n});
        },

        deleteTaskAction: function(){
            this.model.collection.remove(this.model);
            this.unbindAllEvents();
            this.remove();
        },

        titleChanged: function(){
          this.model.set({
              title: this.inputTitle.val()
          });
        },

        instructionsChanged: function(){
            this.model.set({
                instructions: this.textareaInstructions.val()
            });
        },

        workerSelected: function(e){
            var loginSelected = e.target.value;
            var userSelected = _.find(this.options.users.models, function(user){
                return loginSelected == user.get("login");
            });
            this.model.set({
                worker: userSelected
            });
        },

        render: function() {
            this.$el.html(this.template);

            this.bindDomElements();

            return this;
        },

        bindDomElements: function(){
            this.inputTitle = this.$('input.task-name');
            this.textareaInstructions = this.$('textarea.instructions');
        },

        unbindAllEvents: function(){
            this.undelegateEvents();
        }

    });
    return TaskModelEditorView;
});
