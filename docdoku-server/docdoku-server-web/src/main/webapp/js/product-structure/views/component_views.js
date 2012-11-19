define(["models/component_module", "views/part_metadata_view"], function (ComponentModule, PartMetadataView) {

    var ComponentViews = {}

    ComponentViews.Components = Backbone.View.extend({

        tagName:'ul',

        initialize:function(){
            this.collection.bind("reset", this.addAllComponentsView,this);
            this.collection.bind("add", this.addComponentView,this);
            this.options.parentView.append(this.el);
            this.collection.fetch();
        },

        addAllComponentsView: function() {
            this.collection.each(this.addComponentView,this);
        },

        addComponentView: function(component) {

            var isLast = component == this.collection.last();

            var optionsForComponentView = {
                model: component,
                isLast: isLast,
                checkedAtInit: this.options.parentChecked
            }

            var componentView = component.isAssembly() ? new ComponentViews.Assembly(optionsForComponentView) : new ComponentViews.Leaf(optionsForComponentView);

            this.$el.append(componentView.render().el);

        }

    });

    ComponentViews.Leaf = Backbone.View.extend({

        tagName:'li',

        template: _.template("<input type='checkbox' <%if (checkedAtInit) {%>checked='checked'<%}%>><a href='#'><label class='checkbox'><%= number %> (<%= amount %>)</label></a>"),

        events: {
            "click a": "showPartMetadata",
            "change input:first": "onChangeCheckbox"
        },

        initialize: function() {
            _.bindAll(this, ["onChangeCheckbox"]);
        },

        onChangeCheckbox: function(event) {
            this.model.putOnScene();
        },

        render: function() {

            this.$el.html(this.template({number: this.model.attributes.number, amount: this.model.getAmount(), checkedAtInit: this.options.checkedAtInit}));

            if (this.options.isLast) {
                this.$el.addClass('last');
            }

            return this;
        },

        showPartMetadata:function(e) {
            e.stopPropagation();

            $("#part_metadata_container").empty();
            new PartMetadataView({model: this.model}).render();

            $("#bottom_controls_container").hide();
            $("#part_metadata_container").show();
        }

    });

    ComponentViews.Assembly = Backbone.View.extend({

        tagName:'li',

        className: 'expandable',

        template: _.template("<div class=\"hitarea expandable-hitarea\"></div><input type='checkbox' <%if (checkedAtInit) {%>checked='checked'<%}%>><a href='#'><label class='checkbox isNode'><%= number %> (<%= amount %>)</label></a>"),

        events: {
            "click a:first": "showPartMetadata",
            "change input:first": "onChangeCheckbox",
            "click .hitarea:first": "onToggleExpand"
        },

        initialize: function() {
            this.isExpanded = false;
            _.bindAll(this, ["onChangeCheckbox"]);
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

            return this;
        },

        onToggleExpand: function() {
            var needLoading = this.toggleExpand();
            if (needLoading) new ComponentViews.Components({
                collection: this.model.children,
                parentView: this.$el,
                parentChecked: this.isChecked()
            });
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

            return this.isExpanded && childrenNode.length == 0;
        },

        showPartMetadata:function(e) {
            e.stopPropagation();

            $("#part_metadata_container").empty();
            new PartMetadataView({model: this.model}).render();

            $("#bottom_controls_container").hide();
            $("#part_metadata_container").show();

        },

        isChecked: function() {
            return this.input.prop('checked');
        }


    });

    return ComponentViews;
});
