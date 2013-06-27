define([] , function() {
    var UnVersionedFileModel = Backbone.Model.extend({

        initialize:function() {
            _.bindAll(this);
        },

        getFileName:function() {
            return this.get("name");
        },

        getFullPath:function() {
            return this.get("path") + "/" + this.getFileName();
        },

        getMTime:function() {
            return this.get("mtime");
        },

        getMTimeParsed:function() {
            return moment(this.getMTime()).format("YYYY-MM-DD HH:MM:ss");
        },

        setMTime:function(mTime) {
            this.set("mtime", mTime);
        }
    });

    return UnVersionedFileModel;
});