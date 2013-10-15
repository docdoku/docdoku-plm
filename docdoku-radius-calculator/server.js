var express = require("express");
var app = express();

app.use(express.bodyParser());

app.post('/radius', function(req, res, next) {
    var RadiusCalculator = require("./radius-calculator");
    var filename = req.body.filename;
    RadiusCalculator.execute(filename,function(radius){
        var result = {radius:radius.toFixed(8)};
        res.write(JSON.stringify(result));
        res.end();
    });
});

app.listen(8888);
