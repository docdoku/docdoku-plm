define (
    [
        "text!templates/parts_management.html",
        "views/component_view",
        "i18n!localization/nls/product-structure-strings"
    ],
    function(template,ComponentView,i18n) {
        var PartsManagementView = Backbone.View.extend({

            template: Mustache.compile(template),

            events: {
                "click #createPart": "createPart"
            },

            initialize: function() {
                this.collection.bind("add",this.addPart,this);
                this.collection.bind("remove",this.removePart,this);
            },

            bindTypeahead: function() {

                var that = this;

                this.$("#existingParts").typeahead({
                    source: function(query, process) {
                        $.getJSON('/api/workspaces/' + APP_CONFIG.workspaceId + '/parts?q=' + query, function(data) {
                            process(data);
                        });
                    },
                    updater: function(partKey) {
                        var existingPart = {
                            amount : 1,
                            component : {
                                number : partKey
                            }
                        };

                        that.collection.push(existingPart);

                    }
                });
            },

            render: function() {

                var that = this ;
                this.$el.html(this.template({i18n:i18n}));

                this.componentViews = [];

                this.collection.each(function(model){
                    that.addView(model);
                });

                this.bindTypeahead();

                return this;
            },

            addView : function(model){
                var that = this ;
                var componentView = new ComponentView({model:model, removeHandler:function(){ that.collection.remove(model); }}).render();
                this.componentViews.push(componentView);
                this.$el.append(componentView.$el);
            },

            removePart:function(modelToRemove){

                var viewToRemove = _(this.componentViews).select(function(view) {
                    return view.model === modelToRemove;
                })[0];

                if(viewToRemove != null){
                    this.componentViews = _(this.componentViews).without(viewToRemove);
                    viewToRemove.remove();
                }
            },

            addPart: function(model) {
                this.addView(model);
            },

            createPart: function(e) {
                var newPart = {
                    amount : 1,
                    component:{
                        description:"",
                        standardPart:false
                    }
                };
                this.collection.push(newPart);
            }

        });

        return PartsManagementView;
});