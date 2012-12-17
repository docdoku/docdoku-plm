define(function() {

    var LayerItemView = Backbone.View.extend({

        tagName: 'li',

        events: {
            "click i.start"   : "toggleShow",
            "dblclick"        : "startEditingName",
            "blur .edit"      : "stopEditingName",
            "keypress .edit"  : "stopEditingNameOnEnter",
            "click i.end"     : "toggleEditingMarkers"
        },

        initialize: function() {
            this.listenTo(this.model, 'destroy', this.remove)
                .listenTo(this.model, 'change:editingName change:editingMarkers change:shown', this.render)
                .listenTo(this.model.getMarkers(), 'add remove reset', this.render);
        },

        template: "<i class=\"icon-eye-open start\"></i><span class=\"color\" style=\"background-color:{{getHexaColor}}\">&nbsp;</span><p>{{ attributes.name }} ({{ countMarkers }})</p><i class=\"icon-pushpin end\"></i><input class=\"edit\" type=\"text\" value=\"{{ attributes.name }}\">",

        render: function() {
            this.$el.html(Mustache.render(this.template, this.model));
            this.$el.toggleClass('shown', this.model.get('shown'));
            var editingName = this.model.get('editingName')
            this.$el.toggleClass('editingName', editingName);
            this.input = this.$('.edit');
            if (editingName) {
                this.input.focus();
            }
            this.$el.toggleClass('editingMarkers', this.model.get('editingMarkers'));
            return this;
        },

        toggleShow: function() {
            this.model.toggleShow();
        },

        toggleEditingMarkers: function() {
            this.model.toggleEditingMarkers();
        },

        startEditingName: function() {
            this.model.setEditingName(true);
        },

        stopEditingName: function() {
            var value = this.input.val();
            if (this.model.get("name") != value) {
                this.model.save({
                    name: value,
                    editingName: false
                });
            } else {
                this.model.set('editingName', false);
            }
        },

        stopEditingNameOnEnter: function(e) {
            if (e.keyCode == 13) {
                this.input[0].blur();
            }
        }

    });

    return LayerItemView;

});