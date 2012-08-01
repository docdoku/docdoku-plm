window.PartNodeView = Backbone.View.extend({
    tagName:'ul',

    initialize:function(){
        this.collection.bind("reset",this.addAllPartItemView,this);
        this.collection.bind("add",this.addPartItemView,this);
        this.options.parentView.append(this.el);
    },

    addAllPartItemView: function(){
        this.collection.each(this.addPartItemView,this);
        $("#product_nav_list ul").treeview({
            animated:true
            //control : "#nav_list_controls"
        });
    },

    addPartItemView:function(partItem){
        var partItemView = new PartItemView({model: partItem});

        this.$el.append(partItemView.render().el);

        if (partItem.isNode()) {
            var subParts = new PartCollection;
            new PartNodeView({collection: subParts, parentView: partItemView.$el});
            subParts.add(partItem.getComponents());
        }

    },

    render: function(){

    }
});