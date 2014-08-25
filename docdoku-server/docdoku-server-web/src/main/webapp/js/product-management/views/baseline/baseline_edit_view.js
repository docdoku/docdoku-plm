'use strict';
define(
    [
        "text!templates/baseline/baseline_edit.html",
        "i18n!localization/nls/baseline-strings",
        "views/baseline/baselined_part_list"
    ],function(
        template,
        i18n,
        BaselinePartListView
        ){

    var BaselineEditView = Backbone.View.extend({

        events: {
            "submit #baseline_edit_form" : "onSubmitForm",
            "hidden #baseline_edit_modal": "onHidden"
        },

        template:Mustache.compile(template),

        initialize:function(){
            this.productId = this.options.productId;
        },

        render:function(){
            var that = this;
            this.model.fetch().success(function(){
                that.$el.html(that.template({i18n: i18n, model: that.model}));
                that.bindDomElements();
                that.initBaselinedPartListView();
                that.openModal();
            });
            $("body").append(this.$el);
            return this ;
        },

        bindDomElements:function(){
            this.$modal = this.$("#baseline_edit_modal");
            this.$baselinedPartListArea = this.$("#baselinedPartListArea");
        },

        initBaselinedPartListView:function(){
            this.baselinePartListView = new BaselinePartListView({model:this.model, isForBaseline:true}).render();
            this.$baselinedPartListArea.html(this.baselinePartListView.$el);
        },

        onSubmitForm:function(e){
            var that = this;
            this.model.save({baselinedParts:this.baselinePartListView.getBaselinedParts()},{
                success:function(){
                    that.closeModal();
                },
                error:function(status,err){
                    alert(err.responseText);
                }
            });

            e.preventDefault();
            e.stopPropagation();
            return false;

        },

        onError: function(model, error) {
            alert(i18n.CREATION_ERROR + " : " + error.responseText);
        },

        openModal: function() {
            this.$modal.modal('show');
        },

        closeModal: function() {
            this.$modal.modal('hide');
        },

        onHidden: function() {
            this.remove();
        }

    });

    return BaselineEditView;
});