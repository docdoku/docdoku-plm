window.PartCollection = Backbone.Collection.extend({

        model: Part,
        url:"/api/workspaces/" + APP_CONFIG.workspaceId + "/products/test?configSpec=latest",
        tagName: 'ul',
        initialize : function() {
            this.bind('add', this.addOne, this);
            this.bind('reset', this.addAll, this);
        },

        addAll:function(){
            console.log('add all collection');
        },

        addOne: function(part){
            console.log('Add One Part : '+part.toJSON().number);
            App.addOnePart(part);
        }
});

window.Parts = new PartCollection;
