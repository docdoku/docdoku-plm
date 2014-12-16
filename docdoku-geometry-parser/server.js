'use strict';

var express = require("express");
var app = express();
var fs = require('fs');

app.use(express.bodyParser());

app.post('/box', function(req, res, next) {

    var GeometryParser = require("./geometry-parser");
    var filename = req.body.filename;

    if (!filename) {
        console.log("Filename not specified. Exiting.");
        res.end();
        return;
    }

    try {
        fs.statSync(filename);
    } catch (ex) {
        console.log("File '" + filename + "' does not exists. Exiting.");
        res.end();
        return;
    }

    fs.readFile(filename, 'utf-8' ,function (err, data) {
        if (err) {
            console.log("Cannot read the file, is it corrupted or in an other format ?");
            res.end();
        }
        else{
            GeometryParser.calculateBox(data,function(box){
                res.write(JSON.stringify(box));
                res.end();
            });
        }
    });

});

app.listen(8888);

console.log('Listening on port 8888');
