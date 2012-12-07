define(["models/component_module", "views/component_views"], function (ComponentModule, ComponentViews) {

    var PartsTreeView = Backbone.View.extend({

        el:$('#product_nav_list'),

        events: {
            "change input": "checkChildrenInputs",
            "change li": "checkParentsInputs",
            "component_selected a": "onComponentSelected"
        },

        setSelectedComponent: function(component) {
            this.componentSelected = component;
        },

        render: function() {
            var rootCollection = new ComponentModule.Collection([], { isRoot: true });

            rootCollection.on("reset", function(collection) {
                //the default selected component is the root
                this.setSelectedComponent(collection.first());
            }, this);

            new ComponentViews.Components({
                collection: rootCollection,
                resultPathCollection: this.options.resultPathCollection,
                parentView: this.$el,
                parentChecked: false
            });

            return this;
        },

        checkChildrenInputs: function(event) {
            var inputs = event.target.parentNode.querySelectorAll('input');
            for (var i = 0; i < inputs.length; i++) {
                inputs[i].checked = event.target.checked;
            }
        },

        checkParentsInputs: function(event) {
            var relativeInput = event.currentTarget.querySelector('input');
            relativeInput.checked = event.target.checked;

            if (event.target.checked) {
                var childrenUl = event.currentTarget.querySelector('ul');
                if (childrenUl != null) {
                    var inputsChecked = 0;
                    for (var i = 0; i < childrenUl.childNodes.length; i++) {
                        var li = childrenUl.childNodes[i];
                        if (li.querySelector('input').checked) {
                            inputsChecked++;
                        }
                    }
                    if (inputsChecked != childrenUl.childNodes.length) {
                        relativeInput.checked = false;
                    }
                }
            }
        },

        onComponentSelected: function(e, componentModel) {
            e.stopPropagation();
            this.setSelectedComponent(componentModel);
            this.trigger("component_selected");
        }

    });

    return PartsTreeView;

});