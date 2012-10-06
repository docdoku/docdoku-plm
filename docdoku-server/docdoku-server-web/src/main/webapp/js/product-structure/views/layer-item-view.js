define(function() {

    var LayerItemView = Backbone.View.extend({

        tagName: 'li',

        events: {
            "click i.start"   : "toggleShow",
            "dblclick"        : "toggleEditing",
            "blur .edit"      : "stopEditing",
            "keypress .edit"  : "stopEditingOnEnter",
            "click i.end"     : "toggleEditing"
        },

        initialize: function() {
            this.model.bind('destroy', this.remove, this);
            this.model.bind('change', this.render, this);
            this.model.getMarkers().on('add remove reset', this.render, this);
        },

        template: "<i class=\"icon-eye-open start\"></i><span class=\"color\" style=\"background-color:{{getHexaColor}}\">&nbsp;</span><p>{{ attributes.name }} ({{ countMarkers }})</p><i class=\"icon-pencil end\"></i><input class=\"edit\" type=\"text\" value=\"{{ attributes.name }}\">",

        render: function() {
            this.$el.html(Mustache.render(this.template, this.model));
            this.$el.toggleClass('shown', this.model.get('shown'));
            var editing = this.model.get('editing')
            this.$el.toggleClass('editing', editing);
            this.input = this.$('.edit');
            if (editing) {
                this.input.focus();
            }
            return this;
        },

        toggleShow: function() {
            this.model.toggleShow();
        },

        toggleEditing: function() {
            this.model.toggleEditing();
        },

        stopEditing: function() {
            var value = this.input.val();
            this.model.save({
                name: value,
                editing: false
            });
        },

        stopEditingOnEnter: function(e) {
            if (e.keyCode == 13) this.stopEditing();
        }

    });

    return LayerItemView;

});