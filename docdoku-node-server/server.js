'use strict';

var express = require("express");
var BoxCalculator = require("./box-calculator");

var app = express();

app.use(express.bodyParser());

app.post('/box', BoxCalculator.handlePost);

app.listen(8888);

console.log('Listening on port 8888');
