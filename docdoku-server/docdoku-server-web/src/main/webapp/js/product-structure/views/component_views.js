define(function() {

    var ComponentViews = {}

    ComponentViews.Components = Backbone.View.extend({

        tagName:'ul',

        initialize: function() {
            this.options.parentView.append(this.el);
            if (this.collection.isEmpty()) {
                this.listenTo(this.collection, 'reset', this.addAllComponentsView)
                    .listenTo(this.collection, 'add', this.addComponentView);
                this.collection.fetch();
            } else {
                this.addAllComponentsView();
            }
        },

        addAllComponentsView: function() {
            this.collection.each(this.addComponentView, this);
        },

        addComponentView: function(component) {

            var isLast = component == this.collection.last();

            var optionsForComponentView = {
                model: component,
                isLast: isLast,
                checkedAtInit: this.options.parentChecked,
                resultPathCollection: this.options.resultPathCollection
            };

            var componentView = component.isAssembly() ? new ComponentViews.Assembly(optionsForComponentView) : new ComponentViews.Leaf(optionsForComponentView);

            this.$el.append(componentView.render().el);

        }

    });

    ComponentViews.Leaf = Backbone.View.extend({

        tagName:'li',

        template: _.template("<input type='checkbox' <%if (checkedAtInit) {%>checked='checked'<%}%>><a href='#'><label class='checkbox'><%= number %> (<%= amount %>)</label></a>"),

        events: {
            "click a": "onComponentSelected",
            "change input:first": "onChangeCheckbox"
        },

        initialize: function() {
            _.bindAll(this, ["onChangeCheckbox"]);
            this.listenTo(this.options.resultPathCollection, 'reset', this.onAllResultPathAdded);
        },

        onAllResultPathAdded: function() {
            if (this.options.resultPathCollection.contains(this.model.attributes.partUsageLinkId)) {
                this.$el.addClass("resultPath");
            } else {
                this.$el.removeClass("resultPath");
            }
        },

        onChangeCheckbox: function(event) {
            if (event.target.checked)
                this.model.putOnScene();
            else
                this.model.removeFromScene();
        },

        render: function() {

            this.$el.html(this.template({number: this.model.attributes.number, amount: this.model.getAmount(), checkedAtInit: this.options.checkedAtInit}));

            if (this.options.isLast) {
                this.$el.addClass('last');
            }

            this.onAllResultPathAdded();

            return this;
        },

        onComponentSelected: function(e) {
            e.stopPropagation();
            this.$("a").trigger("component_selected", [this.model]);
        }

    });

    ComponentViews.Assembly = Backbone.View.extend({

        tagName:'li',

        className: 'expandable',

        template: _.template("<div class=\"hitarea expandable-hitarea\"></div><input type='checkbox' <%if (checkedAtInit) {%>checked='checked'<%}%>><a href='#'><label class='checkbox isNode'><%= number %> (<%= amount %>)</label></a>"),

        events: {
            "click a:first": "onComponentSelected",
            "change input:first": "onChangeCheckbox",
            "click .hitarea:first": "onToggleExpand"
        },

        initialize: function() {
            this.isExpanded = false;
            _.bindAll(this, ["onChangeCheckbox"]);
            this.listenTo(this.options.resultPathCollection, 'reset', this.onAllResultPathAdded);
        },

        onAllResultPathAdded: function() {
            if (this.options.resultPathCollection.contains(this.model.attributes.partUsageLinkId)) {
                this.$el.addClass("resultPath");
            } else {
                this.$el.removeClass("resultPath");
            }
        },

        onChangeCheckbox: function(event) {
            if (event.target.checked)
                this.model.putOnScene();
            else
                this.model.removeFromScene();
        },

        render: function() {

            this.$el.html(this.template({number: this.model.attributes.number, amount: this.model.getAmount(), checkedAtInit: this.options.checkedAtInit}));

            this.input = this.$(">input");

            if (this.options.isLast) {
                this.$el.addClass('lastExpandable')
                    .children('.hitarea')
                    .addClass('lastExpandable-hitarea');
            }

            this.onAllResultPathAdded();

            return this;
        },

        onToggleExpand: function() {
            this.toggleExpand();
            if (!this.hasChildrenNodes()) {
                new ComponentViews.Components({
                    collection: this.model.children,
                    parentView: this.$el,
                    parentChecked: this.isChecked(),
                    resultPathCollection: this.options.resultPathCollection
                });
            }
        },

        toggleExpand: function() {
            this.$el.toggleClass('expandable collapsable')
                .children('.hitarea')
                .toggleClass('expandable-hitarea collapsable-hitarea');

            if (this.options.isLast) {
                this.$el.toggleClass('lastExpandable lastCollapsable')
                    .children('.hitarea')
                    .toggleClass('lastExpandable-hitarea lastCollapsable-hitarea');
            }

            this.isExpanded = !this.isExpanded;

            var childrenNode = this.$(">ul");

            this.isExpanded ? childrenNode.show() : childrenNode.hide();
        },

        hasChildrenNodes: function() {
            var childrenNode = this.$(">ul");
            return childrenNode.length > 0;
        },

        onComponentSelected: function(e) {
            e.stopPropagation();
            this.$("a").trigger("component_selected", [this.model]);
        },

        isChecked: function() {
            return this.input.prop('checked');
        }

    });

    return ComponentViews;
});
