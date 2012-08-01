window.Part = Backbone.Model.extend({
        defaults: {
            "name":  null,
            "number":     null,
            "version":    null,
            "description":  null,
            "instances":     null,
            "files":    null,
            "components": null,
            "parent":null,
            "isNode":false
        },

        idAttribute: "number",

        initialize : function(){
            this.className = "Part";
            if (Array.isArray(this.get('components'))) {
                var subParts = new PartCollection;
                subParts.add(this.get('components'));
                this.set({components: subParts});
                this.set({isNode: true});
            }

        },

        getName : function() {
            return this.get('name');
        },

        getNumber : function() {
            return this.get('number');
        },

        getVersion : function() {
            return this.get('version');
        },

        getDescription : function() {
            return this.get('description');
        },

        getInstances : function() {
            return this.get('instances');
        },

        getFiles : function() {
            return this.get('files');
        },

        getComponents : function() {
            return this.get('components');
        },

        getWorkspaceId : function() {
            return this.get('worksapceId');
        },

        getIteration : function() {
            return this.get('iteration');
        },

        getStandardPart : function() {
            return this.get('standarPart');
        }
    });
