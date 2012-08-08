var sceneManager;

$(document).ready(function() {

    sceneManager = new SceneManager();
    var allParts = new PartCollection;
    var partNodeView = new PartNodeView({collection:allParts, parentView: $("#product_nav_list")});
    allParts.fetch();

    sceneManager.init();
});
