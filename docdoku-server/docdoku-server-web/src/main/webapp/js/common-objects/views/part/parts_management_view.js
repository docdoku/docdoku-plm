define (
    [
        "text!common-objects/templates/part/parts_management.html",
        "common-objects/views/part/component_view",
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
                        $.getJSON('/api/workspaces/' + APP_CONFIG.workspaceId + '/parts/numbers?q=' + query, function(data) {
                            var partNumbers = [];
                            _(data).each(function(d){
                                partNumbers.push(d.partNumber);
                            })
                            process(partNumbers);
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
                this.$el.html(this.template({i18n:i18n, editMode:this.options.editMode}));

                this.componentViews = [];

                this.collection.each(function(model){
                    that.addView(model);
                });

                if(this.options.editMode){
                    this.bindTypeahead();
                }

                return this;
            },

            addView : function(model){
                var that = this ;
                var componentView = new ComponentView({model:model, editMode:this.options.editMode, removeHandler:function(){ that.collection.remove(model); }}).render();
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
                model.set("cadInstances",[{tx:0,ty:0,tz:0,rx:0,ry:0,rz:0,positioning:"ABSOLUTE"}]);
                this.addView(model);
            },

            createPart: function(e) {
                var newPart = {
                    amount : 1,
                    component:{
                        description:"",
                        standardPart:false
                    },
                    cadInstances:[
                        {tx:0,ty:0,tz:0,rx:0,ry:0,rz:0,positioning:"ABSOLUTE"}
                    ]
                };
                this.collection.push(newPart);
            }

        });

        return PartsManagementView;
});