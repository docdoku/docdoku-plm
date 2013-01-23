define([
    "text!templates/file.html"
], function (template) {
    var FileView = Backbone.View.extend({

        tagName: "li",
        className: 'file',

        editMode: true,

        events: {
            "change input.file-check" : "fileCheckChanged"
        },

        initialize: function() {
            this.editMode = this.options.editMode;
            this.model.url = this.options.deleteBaseUrl+"/files/"+this.model.get("shortName");
        },

        fileCheckChanged: function() {
            if (this.checkbox.is(":checked")) {
                this.fileNameEl.addClass("stroke");
                this.options.filesToDelete.add(this.model);
            } else {
                this.fileNameEl.removeClass("stroke");
                this.options.filesToDelete.remove(this.model);
            }
        },

        render: function() {
            this.$el.html(Mustache.render(template,
                {
                    url: this.options.uploadBaseUrl+this.model.get("shortName"),
                    shortName: this.model.get("shortName"),
                    editMode: this.editMode
                }
            ));

            this.bindDomElements();

            return this;
        },

        bindDomElements: function(){
            this.checkbox = this.$("input.file-check");
            this.fileNameEl = this.$(".fileName");
        }
    });
    return FileView;
});
