define([
    "i18n!localization/nls/document-management-strings",
    "common-objects/models/task_model",
    "text!templates/workflows/task_model_editor.html"
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
            "click p.task-name" : "gotoUnfoldState",
            "click i.fa-minus" : "gotoFoldState",
            "change input.task-name" : "titleChanged",
            "change textarea.instructions" : "instructionsChanged",
            "change select.role": "roleSelected"
        },

        States : {
            FOLD: 0,
            UNFOLD: 1
        },

        initialize: function () {
            var self = this;

            this.state = this.States.FOLD;

            if(_.isUndefined(this.model.get("role"))){
                this.model.set({
                    role: this.options.roles.at(0)
                });
            }

            var roles = [];
            _.each(this.options.roles.models, function(role){
                if(self.model.get("role") && self.model.get("role").get("name") == role.get("name")){
                    roles.push({name: role.get("name"), selected: true});
                }
                else{
                    roles.push({name: role.get("name"), selected: false});
                }
            });

            this.template = Mustache.render(template, {cid: this.model.cid, task: this.model.attributes, roles: roles, i18n: i18n});
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
          if(this.inputTitle.val().length == 0){
            this.pTitle.html(i18n.TASK_NAME_PLACEHOLDER);
          }
          else{
            this.pTitle.html(this.inputTitle.val());
          }
        },

        instructionsChanged: function(){
            this.model.set({
                instructions: this.textareaInstructions.val()
            });
        },

        roleSelected: function(e){
            var nameSelected = e.target.value;
            var roleSelected = _.find(this.options.roles.models, function(role){
                return nameSelected == role.get("name");
            });
            this.model.set({
                role: roleSelected
            });
        },

        gotoFoldState: function() {
            this.state = this.States.FOLD;
            this.divTask.removeClass("unfold");
            this.divTask.addClass("fold");
            this.inputTitle.prop('readonly', true);
        },

        gotoUnfoldState: function() {
            this.state = this.States.UNFOLD;
            this.divTask.removeClass("fold");
            this.divTask.addClass("unfold");
            this.inputTitle.prop('readonly', false);
        },

        render: function() {
            this.$el.html(this.template);

            this.bindDomElements();

            return this;
        },

        bindDomElements: function(){
            this.pTitle = this.$("p.task-name");
            this.inputTitle = this.$('input.task-name');
            this.textareaInstructions = this.$('textarea.instructions');
            this.divTask = this.$('div.task');
        },

        unbindAllEvents: function(){
            this.undelegateEvents();
        }

    });
    return TaskModelEditorView;
});
