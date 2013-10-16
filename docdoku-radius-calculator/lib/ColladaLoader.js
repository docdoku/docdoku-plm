var fs = require("fs");
var THREE = require("three");
var DOMParser = require('xmldom').DOMParser;

var ColladaLoader2 = function () {
    this._init()
};
ColladaLoader2.indentString = function (e, t) {
    var n, r, i;
    r = "";
    for (n = i = 1; i <= e; n = i += 1) {
        r += "    "
    }
    r += t;
    return r
};
ColladaLoader2.graphNodeString = function (e, t) {
    return ColladaLoader2.indentString(e, "|-" + t)
};
ColladaLoader2.getNodeInfo = function (e, t, n) {
    if (e == null) {
        return""
    }
    if (typeof e === "string") {
        return ColladaLoader2.graphNodeString(t, n + ("'" + e + "'\n"))
    }
    if (typeof e === "number") {
        return ColladaLoader2.graphNodeString(t, n + ("" + e + "\n"))
    }
    if (typeof e === "boolean") {
        return ColladaLoader2.graphNodeString(t, n + ("" + e + "\n"))
    }
    if (e.getInfo != null) {
        return e.getInfo(t, n)
    }
    return ColladaLoader2.graphNodeString(t, n + "<unknown data type>\n")
};
ColladaLoader2.FxTarget = function () {
};
ColladaLoader2.FxTarget.prototype.sid;
ColladaLoader2.FxTarget.prototype.fxScope;
ColladaLoader2.FxScope = function () {
};
ColladaLoader2.FxScope.prototype.sids;
ColladaLoader2.UrlTarget = function () {
};
ColladaLoader2.UrlTarget.prototype.id;
ColladaLoader2.SidTarget = function () {
};
ColladaLoader2.SidTarget.prototype.sid;
ColladaLoader2.SidScope = function () {
};
ColladaLoader2.SidScope.prototype.sidChildren;
ColladaLoader2.Link = function () {
};
ColladaLoader2.Link.prototype.url;
ColladaLoader2.UrlLink = function (e, t) {
    this.file = t;
    this.url = e.trim().replace(/^#/, "");
    this.object = null;
    return this
};
ColladaLoader2.UrlLink.prototype._resolve = function () {
    var e;
    e = this.file.dae.ids[this.url];
    if (e == null) {
        ColladaLoader2._log("Could not resolve URL #" + this.url, ColladaLoader2.messageError)
    }
    return e
};
ColladaLoader2.UrlLink.prototype.getTarget = function () {
    if (this.object == null) {
        this.object = this._resolve()
    }
    return this.object
};
ColladaLoader2.UrlLink.prototype.getInfo = function (e, t) {
    return ColladaLoader2.graphNodeString(e, t + ("<urlLink url='" + this.url + "'>\n"))
};
ColladaLoader2.FxLink = function (e, t, n) {
    this.file = n;
    this.url = e;
    this.scope = t;
    this.object = null;
    return this
};
ColladaLoader2.FxLink.prototype._resolve = function () {
    var e, t;
    t = this.scope;
    e = null;
    while (e == null && t != null) {
        e = t.sids[this.url];
        t = t.fxScope
    }
    if (e == null) {
        ColladaLoader2._log("Could not resolve FX parameter #" + this.url, ColladaLoader2.messageError)
    }
    return e
};
ColladaLoader2.FxLink.prototype.getTarget = function () {
    if (this.object == null) {
        this.object = this._resolve()
    }
    return this.object
};
ColladaLoader2.FxLink.prototype.getInfo = function (e, t) {
    return ColladaLoader2.graphNodeString(e, t + ("<fxLink url='" + this.url + "'>\n"))
};
ColladaLoader2.SidLink = function (e, t, n) {
    this.file = n;
    this.url = t;
    this.parentId = e;
    this.object = null;
    this.id = null;
    this.sids = [];
    this.member = null;
    this.indices = null;
    this.dotSyntax = false;
    this.arrSyntax = false;
    this._parseUrl();
    return this
};
ColladaLoader2.SidLink.prototype._parseUrl = function () {
    var e, t, n, r, i, s, o, u;
    s = this.url.split("/");
    this.id = s.shift();
    if (this.id === ".") {
        this.id = this.parentId
    }
    while (s.length > 1) {
        this.sids.push(s.shift())
    }
    if (s.length > 0) {
        i = s[0];
        n = i.indexOf(".") >= 0;
        t = i.indexOf("(") >= 0;
        if (n) {
            s = i.split(".");
            this.sids.push(s.shift());
            this.member = s.shift();
            this.dotSyntax = true
        } else if (t) {
            e = i.split("(");
            this.sids.push(e.shift());
            this.indices = [];
            for (o = 0, u = e.length; o < u; o++) {
                r = e[o];
                this.indices.push(parseInt(r.replace(/\)/, ""), 10))
            }
            this.arrSyntax = true
        } else {
            this.sids.push(i)
        }
    }
};
ColladaLoader2.SidLink.findSidTarget = function (e, t, n) {
    var r, i, s, o, u, a, f, l, c, h, p;
    s = t;
    r = null;
    for (f = 0, c = n.length; f < c; f++) {
        u = n[f];
        o = [s];
        while (o.length !== 0) {
            i = o.shift();
            if (i.sid === u) {
                r = i;
                break
            }
            if (i.sidChildren != null) {
                p = i.sidChildren;
                for (l = 0, h = p.length; l < h; l++) {
                    a = p[l];
                    o.push(a)
                }
            }
        }
        if (r == null) {
            ColladaLoader2._log("Could not resolve SID #" + e + ", missing SID part " + u, ColladaLoader2.messageError);
            return null
        }
        s = r
    }
    return r
};
ColladaLoader2.SidLink.prototype._resolve = function () {
    var e, t;
    if (this.id == null) {
        ColladaLoader2._log("Could not resolve SID #" + this.url + ", link has no ID", ColladaLoader2.messageError);
        return null
    }
    t = this.file.dae.ids[this.id];
    if (t == null) {
        ColladaLoader2._log("Could not resolve SID #" + this.url + ", missing base ID " + this.id, ColladaLoader2.messageError);
        return null
    }
    e = ColladaLoader2.SidLink.findSidTarget(this.url, t, this.sids);
    return e
};
ColladaLoader2.SidLink.prototype.getTarget = function () {
    if (this.object == null) {
        this.object = this._resolve()
    }
    return this.object
};
ColladaLoader2.SidLink.prototype.getInfo = function (e, t) {
    var n, r;
    r = "<sidLink id='" + this.id + "'";
    if (this.sids.length > 0) {
        r += ", sids='[";
        r += this.sids.join(",");
        r += "]'"
    }
    r += ">\n";
    return n = ColladaLoader2.graphNodeString(e, t + r)
};
ColladaLoader2._getLinkTarget = function (e, t) {
    var n;
    if (e == null) {
        return null
    }
    n = e.getTarget();
    if (n instanceof t) {
        return n
    } else {
        if (n != null) {
            ColladaLoader2._reportInvalidTargetType(e, t)
        }
        return null
    }
};
ColladaLoader2.AnimationTarget = function () {
    this.sid = null;
    this.animTarget = {channels: [], activeChannels: [], dataRows: null, dataColumns: null};
    return this
};
ColladaLoader2.AnimationTarget.prototype.selectAnimation = function (e) {
    var t, n, r, i, s;
    this.animTarget.activeChannels = [];
    s = this.animTarget.channels;
    for (n = r = 0, i = s.length; r < i; n = ++r) {
        t = s[n];
        if (e(t, n)) {
            this.animTarget.activeChannels.push(t)
        }
    }
};
ColladaLoader2.AnimationTarget.prototype.selectAnimationById = function (e) {
    this.selectAnimation(function (t, n) {
        return t.animation.id === e
    })
};
ColladaLoader2.AnimationTarget.prototype.selectAnimationByName = function (e) {
    this.selectAnimation(function (t, n) {
        return t.animation.name === e
    })
};
ColladaLoader2.AnimationTarget.prototype.selectAllAnimations = function () {
    this.selectAnimation(function (e, t) {
        return true
    })
};
ColladaLoader2.AnimationTarget.prototype.applyAnimationKeyframe = function (e) {
    throw new Error("applyAnimationKeyframe() not implemented")
};
ColladaLoader2.AnimationTarget.prototype.initAnimationTarget = function () {
    throw new Error("initAnimationTarget() not implemented")
};
ColladaLoader2.AnimationTarget.prototype.resetAnimation = function () {
    throw new Error("resetAnimation() not implemented")
};
ColladaLoader2.AnimationTarget.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.AnimationTarget)
};
ColladaLoader2.Asset = function () {
    this.unit = 1;
    this.upAxis = null;
    return this
};
ColladaLoader2.Asset.prototype.getInfo = function (e, t) {
    return ColladaLoader2.graphNodeString(e, t + "<asset>\n")
};
ColladaLoader2.VisualScene = function () {
    this.id = null;
    this.children = [];
    this.sidChildren = [];
    return this
};
ColladaLoader2.VisualScene.prototype.getInfo = function (e, t) {
    var n, r, i, s, o;
    r = ColladaLoader2.graphNodeString(e, t + ("<visualScene id='" + this.id + "'>\n"));
    if (this.children != null) {
        o = this.children;
        for (i = 0, s = o.length; i < s; i++) {
            n = o[i];
            r += ColladaLoader2.getNodeInfo(n, e + 1, "child ")
        }
    }
    return r
};
ColladaLoader2.VisualScene.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.VisualScene)
};
ColladaLoader2.VisualSceneNode = function () {
    this.id = null;
    this.sid = null;
    this.name = null;
    this.type = null;
    this.layer = null;
    this.parent = null;
    this.children = [];
    this.sidChildren = [];
    this.transformations = [];
    this.geometries = [];
    this.controllers = [];
    this.lights = [];
    this.cameras = [];
    return this
};
ColladaLoader2.VisualSceneNode.prototype.getInfo = function (e, t) {
    var n, r, i, s, o, u, a, f, l, c, h, p, d, v, m, g, y;
    r = ColladaLoader2.graphNodeString(e, t + ("<visualSceneNode id='" + this.id + "', sid='" + this.sid + "', name='" + this.name + "'>\n"));
    if (this.geometries != null) {
        d = this.geometries;
        for (i = 0, a = d.length; i < a; i++) {
            n = d[i];
            r += ColladaLoader2.getNodeInfo(n, e + 1, "geometry ")
        }
    }
    if (this.controllers != null) {
        v = this.controllers;
        for (s = 0, f = v.length; s < f; s++) {
            n = v[s];
            r += ColladaLoader2.getNodeInfo(n, e + 1, "controller ")
        }
    }
    if (this.lights != null) {
        m = this.lights;
        for (o = 0, l = m.length; o < l; o++) {
            n = m[o];
            r += ColladaLoader2.getNodeInfo(n, e + 1, "light ")
        }
    }
    if (this.cameras != null) {
        g = this.cameras;
        for (u = 0, c = g.length; u < c; u++) {
            n = g[u];
            r += ColladaLoader2.getNodeInfo(n, e + 1, "camera ")
        }
    }
    if (this.children != null) {
        y = this.children;
        for (p = 0, h = y.length; p < h; p++) {
            n = y[p];
            r += ColladaLoader2.getNodeInfo(n, e + 1, "child ")
        }
    }
    return r
};
ColladaLoader2.VisualSceneNode.prototype.getTransformMatrix = function (e) {
    var t, n, r, i, s;
    t = new THREE.Matrix4;
    e.identity();
    s = this.transformations;
    for (r = 0, i = s.length; r < i; r++) {
        n = s[r];
        n.getTransformMatrix(t);
        e.multiplyMatrices(e, t)
    }
};
ColladaLoader2.VisualSceneNode.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.VisualSceneNode)
};
ColladaLoader2.NodeTransform = function () {
    ColladaLoader2.AnimationTarget.call(this);
    this.sid = null;
    this.type = null;
    this.data = null;
    this.originalData = null;
    this.node = null;
    return this
};
ColladaLoader2.NodeTransform.prototype = new ColladaLoader2.AnimationTarget;
ColladaLoader2.NodeTransform.prototype.getTransformMatrix = function (e) {
    var t;
    if (this.data == null) {
        ColladaLoader2._log("Transform data not defined, using identity transform", ColladaLoader2.messageWarning);
        e.identity();
        return
    }
    switch (this.type) {
        case"matrix":
            ColladaLoader2._fillMatrix4RowMajor(this.data, 0, e);
            break;
        case"rotate":
            t = new THREE.Vector3(this.data[0], this.data[1], this.data[2]);
            e.makeRotationAxis(t, this.data[3] * ColladaLoader2.TO_RADIANS);
            break;
        case"translate":
            e.makeTranslation(this.data[0], this.data[1], this.data[2]);
            break;
        case"scale":
            e.makeScale(this.data[0], this.data[1], this.data[2]);
            break;
        default:
            ColladaLoader2._log("Transform type '" + this.type + "' not implemented, using identity transform", ColladaLoader2.messageWarning);
            e.identity()
    }
};
ColladaLoader2.NodeTransform.prototype.applyAnimationKeyframe = function (e) {
    var t, n, r, i, s, o, u, a;
    u = this.animTarget.activeChannels;
    for (i = 0, o = u.length; i < o; i++) {
        t = u[i];
        r = t.outputData;
        for (n = s = 0, a = t.count - 1; s <= a; n = s += 1) {
            this.data[t.offset + n] = r[e * t.stride + n]
        }
    }
};
ColladaLoader2.NodeTransform.prototype.initAnimationTarget = function () {
    var e, t, n, r, i;
    this.originalData = new Float32Array(this.data.length);
    i = this.data;
    for (e = n = 0, r = i.length; n < r; e = ++n) {
        t = i[e];
        this.originalData[e] = this.data[e]
    }
    switch (this.type) {
        case"matrix":
            this.animTarget.dataColumns = 4;
            this.animTarget.dataRows = 4;
            break;
        case"rotate":
            this.animTarget.dataColumns = 4;
            this.animTarget.dataRows = 1;
            break;
        case"translate":
        case"scale":
            this.animTarget.dataColumns = 3;
            this.animTarget.dataRows = 1;
            break;
        default:
            this.animTarget.dataColumns = null;
            this.animTarget.dataRows = null;
            ColladaLoader2._log("Transform type '" + this.type + "' not implemented, animation will be broken", ColladaLoader2.messageWarning)
    }
};
ColladaLoader2.NodeTransform.prototype.resetAnimation = function () {
    var e, t, n, r, i;
    i = this.originalData;
    for (e = n = 0, r = i.length; n < r; e = ++n) {
        t = i[e];
        this.data[e] = this.originalData[e]
    }
};
ColladaLoader2.NodeTransform.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.NodeTransform)
};
ColladaLoader2.InstanceGeometry = function () {
    this.sid = null;
    this.geometry = null;
    this.materials = [];
    this.sidChildren = [];
    return this
};
ColladaLoader2.InstanceGeometry.prototype.getInfo = function (e, t) {
    var n, r, i, s, o;
    r = ColladaLoader2.graphNodeString(e, t + "<instanceGeometry>\n");
    r += ColladaLoader2.getNodeInfo(this.geometry, e + 1, "geometry ");
    o = this.materials;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        r += ColladaLoader2.getNodeInfo(n, e + 1, "material ")
    }
    return r
};
ColladaLoader2.InstanceGeometry.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.InstanceGeometry)
};
ColladaLoader2.InstanceController = function () {
    this.sid = null;
    this.name = null;
    this.controller = null;
    this.skeletons = [];
    this.materials = [];
    this.sidChildren = [];
    return this
};
ColladaLoader2.InstanceController.prototype.getInfo = function (e, t) {
    var n, r, i, s, o, u, a, f, l;
    r = ColladaLoader2.graphNodeString(e, t + "<instanceController>\n");
    r += ColladaLoader2.getNodeInfo(this.controller, e + 1, "controller ");
    f = this.skeletons;
    for (s = 0, u = f.length; s < u; s++) {
        i = f[s];
        r += ColladaLoader2.getNodeInfo(i, e + 1, "skeleton ")
    }
    l = this.materials;
    for (o = 0, a = l.length; o < a; o++) {
        n = l[o];
        r += ColladaLoader2.getNodeInfo(n, e + 1, "material ")
    }
    return r
};
ColladaLoader2.InstanceController.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.InstanceController)
};
ColladaLoader2.InstanceMaterial = function () {
    this.sid = null;
    this.symbol = null;
    this.material = null;
    this.name = null;
    this.vertexInputs = {};
    this.params = {};
    return this
};
ColladaLoader2.InstanceMaterial.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + ("<instanceMaterial sid='" + this.sid + "'>\n"));
    n += ColladaLoader2.getNodeInfo(this.material, e + 1, "material ");
    return n
};
ColladaLoader2.InstanceMaterial.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.InstanceMaterial)
};
ColladaLoader2.InstanceLight = function () {
    this.sid = null;
    this.light = null;
    this.name = null;
    this.sidChildren = [];
    return this
};
ColladaLoader2.InstanceLight.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + "<instanceLight>\n");
    n += ColladaLoader2.getNodeInfo(this.light, e + 1, "light ");
    return n
};
ColladaLoader2.InstanceLight.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.InstanceLight)
};
ColladaLoader2.InstanceCamera = function () {
    this.sid = null;
    this.camera = null;
    this.name = null;
    this.sidChildren = [];
    return this
};
ColladaLoader2.InstanceCamera.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + "<instanceCamera>\n");
    n += ColladaLoader2.getNodeInfo(this.camera, e + 1, "camera ");
    return n
};
ColladaLoader2.InstanceCamera.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.InstanceCamera)
};
ColladaLoader2.Image = function () {
    this.id = null;
    this.initFrom = null;
    return this
};
ColladaLoader2.Image.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + ("<image id='" + this.id + "'>\n"));
    n += ColladaLoader2.getNodeInfo(this.initFrom, e + 1, "initFrom ");
    return n
};
ColladaLoader2.Image.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.Image)
};
ColladaLoader2.Effect = function () {
    this.id = null;
    this.sids = {};
    this.params = [];
    this.technique = null;
    return this
};
ColladaLoader2.Effect.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + ("<effect id='" + this.id + "'>\n"));
    n += ColladaLoader2.getNodeInfo(this.technique, e + 1, "technique ");
    return n
};
ColladaLoader2.Effect.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.Effect)
};
ColladaLoader2.EffectTechnique = function () {
    this.sid = null;
    this.sids = {};
    this.fxScope = null;
    this.params = [];
    this.shading = null;
    this.emission = null;
    this.ambient = null;
    this.diffuse = null;
    this.specular = null;
    this.reflective = null;
    this.transparent = null;
    this.bump = null;
    this.shininess = null;
    this.transparency = null;
    this.reflectivity = null;
    this.index_of_refraction = null;
    this.double_sided = null;
    return this
};
ColladaLoader2.EffectTechnique.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + ("<technique sid='" + this.sid + "'>\n"));
    return n
};
ColladaLoader2.EffectTechnique.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.EffectTechnique)
};
ColladaLoader2.EffectParam = function () {
    this.sid = null;
    this.fxScope = null;
    this.semantic = null;
    this.surface = null;
    this.sampler = null;
    this.floats = null;
    return this
};
ColladaLoader2.EffectParam.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + ("<newparam sid='" + this.sid + "'>\n"));
    n += ColladaLoader2.getNodeInfo(this.surface, e + 1, "surface ");
    n += ColladaLoader2.getNodeInfo(this.sampler, e + 1, "sampler ");
    n += ColladaLoader2.getNodeInfo(this.floats, e + 1, "floats ");
    return n
};
ColladaLoader2.EffectParam.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.EffectParam)
};
ColladaLoader2.EffectSurface = function () {
    this.type = null;
    this.initFrom = null;
    this.format = null;
    this.size = null;
    this.viewportRatio = null;
    this.mipLevels = null;
    this.mipmapGenerate = null;
    return this
};
ColladaLoader2.EffectSurface.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + "<surface>\n");
    n += ColladaLoader2.getNodeInfo(this.initFrom, e + 1, "initFrom ");
    return n
};
ColladaLoader2.EffectSampler = function () {
    this.surface = null;
    this.image = null;
    this.wrapS = null;
    this.wrapT = null;
    this.minfilter = null;
    this.magfilter = null;
    this.borderColor = null;
    this.mipmapMaxLevel = null;
    this.mipmapBias = null;
    return this
};
ColladaLoader2.EffectSampler.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + "<sampler>\n");
    n += ColladaLoader2.getNodeInfo(this.image, e + 1, "image ");
    n += ColladaLoader2.getNodeInfo(this.surface, e + 1, "surface ");
    return n
};
ColladaLoader2.ColorOrTexture = function () {
    this.color = null;
    this.textureSampler = null;
    this.texcoord = null;
    this.opaque = null;
    this.bumptype = null;
    return this
};
ColladaLoader2.Material = function () {
    this.id = null;
    this.name = null;
    this.effect = null;
    return this
};
ColladaLoader2.Material.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + ("<material id='" + this.id + "' name='" + this.name + "'>\n"));
    n += ColladaLoader2.getNodeInfo(this.effect, e + 1, "effect ");
    return n
};
ColladaLoader2.Material.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.Material)
};
ColladaLoader2.Geometry = function () {
    this.id = null;
    this.name = null;
    this.sources = [];
    this.vertices = null;
    this.triangles = [];
    return this
};
ColladaLoader2.Geometry.prototype.getInfo = function (e, t) {
    var n, r, i, s, o, u, a, f, l;
    n = ColladaLoader2.graphNodeString(e, t + ("<geometry id='" + this.id + "' name='" + this.name + "'>\n"));
    f = this.sources;
    for (s = 0, u = f.length; s < u; s++) {
        r = f[s];
        n += ColladaLoader2.getNodeInfo(r, e + 1, "source ")
    }
    n += ColladaLoader2.getNodeInfo(this.vertices, e + 1, "vertices ");
    l = this.triangles;
    for (o = 0, a = l.length; o < a; o++) {
        i = l[o];
        n += ColladaLoader2.getNodeInfo(i, e + 1, "triangles ")
    }
    return n
};
ColladaLoader2.Geometry.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.Geometry)
};
ColladaLoader2.Source = function () {
    this.id = null;
    this.name = null;
    this.sourceId = null;
    this.count = null;
    this.stride = null;
    this.data = null;
    this.params = {};
    return this
};
ColladaLoader2.Source.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + ("<source id='" + this.id + "' name='" + this.name + "'>\n"));
    n += ColladaLoader2.getNodeInfo(this.sourceId, e + 1, "sourceId ");
    return n
};
ColladaLoader2.Source.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.Source)
};
ColladaLoader2.Vertices = function () {
    this.id = null;
    this.name = null;
    this.inputs = [];
    return this
};
ColladaLoader2.Vertices.prototype.getInfo = function (e, t) {
    var n, r, i, s, o;
    r = ColladaLoader2.graphNodeString(e, t + ("<vertices id='" + this.id + "' name='" + this.name + "'>\n"));
    o = this.inputs;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        r += ColladaLoader2.getNodeInfo(n, e + 1, "input ")
    }
    return r
};
ColladaLoader2.Vertices.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.Vertices)
};
ColladaLoader2.Triangles = function () {
    this.name = null;
    this.type = null;
    this.count = null;
    this.material = null;
    this.inputs = [];
    this.indices = null;
    this.vcount = null;
    return this
};
ColladaLoader2.Triangles.prototype.getInfo = function (e, t) {
    var n, r, i, s, o;
    r = ColladaLoader2.graphNodeString(e, t + ("<triangles name='" + this.name + "'>\n"));
    r += ColladaLoader2.getNodeInfo(this.material, e + 1, "material ");
    o = this.inputs;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        r += ColladaLoader2.getNodeInfo(n, e + 1, "input ")
    }
    return r
};
ColladaLoader2.Input = function () {
    this.semantic = null;
    this.source = null;
    this.offset = null;
    this.set = null;
    return this
};
ColladaLoader2.Input.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + ("<input semantic=" + this.semantic + ">\n"));
    n += ColladaLoader2.getNodeInfo(this.source, e + 1, "source ");
    return n
};
ColladaLoader2.Controller = function () {
    this.id = null;
    this.name = null;
    this.skin = null;
    this.morph = null;
    return this
};
ColladaLoader2.Controller.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + ("<controller id='" + this.id + "', name='" + this.name + "'>\n"));
    n += ColladaLoader2.getNodeInfo(this.skin, e + 1, "skin ");
    n += ColladaLoader2.getNodeInfo(this.morph, e + 1, "morph ");
    return n
};
ColladaLoader2.Controller.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.Controller)
};
ColladaLoader2.Skin = function () {
    this.source = null;
    this.bindShapeMatrix = null;
    this.sources = [];
    this.joints = null;
    this.vertexWeights = null;
    return this
};
ColladaLoader2.Skin.prototype.getInfo = function (e, t) {
    var n, r, i, s, o;
    n = ColladaLoader2.graphNodeString(e, t + ("<skin source='" + this.source + "'>\n"));
    n += ColladaLoader2.getNodeInfo(this.bindShapeMatrix, e + 1, "bind_shape_matrix ");
    o = this.sources;
    for (i = 0, s = o.length; i < s; i++) {
        r = o[i];
        n += ColladaLoader2.getNodeInfo(r, e + 1, "source ")
    }
    n += ColladaLoader2.getNodeInfo(this.joints, e + 1, "joints ");
    n += ColladaLoader2.getNodeInfo(this.vertexWeights, e + 1, "vertex_weights ");
    return n
};
ColladaLoader2.Morph = function () {
    return this
};
ColladaLoader2.Joints = function () {
    this.joints = null;
    this.invBindMatrices = null;
    return this
};
ColladaLoader2.Joints.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + "<joints>\n");
    n += ColladaLoader2.getNodeInfo(this.joints, e + 1, "joints ");
    n += ColladaLoader2.getNodeInfo(this.invBindMatrices, e + 1, "invBindMatrices ");
    return n
};
ColladaLoader2.VertexWeights = function () {
    this.inputs = [];
    this.vcount = null;
    this.v = null;
    this.joints = null;
    this.weights = null;
    this.count = null;
    return this
};
ColladaLoader2.VertexWeights.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + "<vertex_weights>\n");
    n += ColladaLoader2.getNodeInfo(this.joints, e + 1, "joints ");
    n += ColladaLoader2.getNodeInfo(this.weights, e + 1, "weights ");
    return n
};
ColladaLoader2.Animation = function () {
    this.id = null;
    this.name = null;
    this.parent = null;
    this.rootId = null;
    this.rootName = null;
    this.animations = [];
    this.sources = [];
    this.samplers = [];
    this.channels = [];
    return this
};
ColladaLoader2.Animation.prototype.getInfo = function (e, t) {
    var n, r, i, s, o, u, a, f, l, c, h, p, d, v, m, g, y;
    i = ColladaLoader2.graphNodeString(e, t + ("<animation id='" + this.id + "', name='" + this.name + "'>\n"));
    v = this.animations;
    for (u = 0, c = v.length; u < c; u++) {
        n = v[u];
        i += ColladaLoader2.getNodeInfo(n, e + 1, "animation ")
    }
    m = this.sources;
    for (a = 0, h = m.length; a < h; a++) {
        o = m[a];
        i += ColladaLoader2.getNodeInfo(o, e + 1, "source ")
    }
    g = this.samplers;
    for (f = 0, p = g.length; f < p; f++) {
        s = g[f];
        i += ColladaLoader2.getNodeInfo(s, e + 1, "sampler ")
    }
    y = this.channels;
    for (l = 0, d = y.length; l < d; l++) {
        r = y[l];
        i += ColladaLoader2.getNodeInfo(r, e + 1, "channel ")
    }
    return i
};
ColladaLoader2.Animation.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.Animation)
};
ColladaLoader2.Sampler = function () {
    this.id = null;
    this.input = null;
    this.outputs = [];
    this.inTangents = [];
    this.outTangents = [];
    this.interpolation = null;
    return this
};
ColladaLoader2.Sampler.prototype.getInfo = function (e, t) {
    var n, r, i, s, o, u, a, f, l, c, h, p;
    r = ColladaLoader2.graphNodeString(e, t + ("<sampler id='" + this.id + "'>\n"));
    r += ColladaLoader2.getNodeInfo(this.input, e + 1, "input ");
    c = this.outputs;
    for (s = 0, a = c.length; s < a; s++) {
        n = c[s];
        r += ColladaLoader2.getNodeInfo(n, e + 1, "output ")
    }
    h = this.inTangents;
    for (o = 0, f = h.length; o < f; o++) {
        i = h[o];
        r += ColladaLoader2.getNodeInfo(i, e + 1, "inTangent ")
    }
    p = this.outTangents;
    for (u = 0, l = p.length; u < l; u++) {
        i = p[u];
        r += ColladaLoader2.getNodeInfo(i, e + 1, "outTangent ")
    }
    r += ColladaLoader2.getNodeInfo(this.interpolation, e + 1, "interpolation ");
    return r
};
ColladaLoader2.Sampler.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.Sampler)
};
ColladaLoader2.Channel = function () {
    this.animation = null;
    this.source = null;
    this.target = null;
    return this
};
ColladaLoader2.Channel.prototype.getInfo = function (e, t) {
    var n;
    n = ColladaLoader2.graphNodeString(e, t + "<channel>\n");
    n += ColladaLoader2.getNodeInfo(this.source, e + 1, "source ");
    n += ColladaLoader2.getNodeInfo(this.target, e + 1, "target ");
    return n
};
ColladaLoader2.Light = function () {
    this.id = null;
    this.name = null;
    this.type = null;
    this.color = null;
    this.params = {};
    this.sidChildren = [];
    return this
};
ColladaLoader2.Light.prototype.getInfo = function (e, t) {
    return ColladaLoader2.graphNodeString(e, t + "<light>\n")
};
ColladaLoader2.Light.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.Light)
};
ColladaLoader2.LightParam = function () {
    this.sid = null;
    this.name = null;
    this.value = null;
    return this
};
ColladaLoader2.LightParam.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.LightParam)
};
ColladaLoader2.Camera = function () {
    this.id = null;
    this.name = null;
    this.type = null;
    this.params = {};
    this.sidChildren = [];
    return this
};
ColladaLoader2.Camera.prototype.getInfo = function (e, t) {
    return ColladaLoader2.graphNodeString(e, t + "<camera>\n")
};
ColladaLoader2.Camera.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.Camera)
};
ColladaLoader2.CameraParam = function () {
    this.sid = null;
    this.name = null;
    this.value = null;
    return this
};
ColladaLoader2.CameraParam.fromLink = function (e) {
    return ColladaLoader2._getLinkTarget(e, ColladaLoader2.CameraParam)
};
ColladaLoader2.ThreejsAnimationChannel = function () {
    this.inputData = null;
    this.outputData = null;
    this.offset = null;
    this.stride = null;
    this.count = null;
    this.semantic = null;
    this.animation = null;
    return this
};
ColladaLoader2.ThreejsSkeletonBone = function () {
    this.index = null;
    this.node = null;
    this.sid = null;
    this.parent = null;
    this.isAnimated = null;
    this.matrix = new THREE.Matrix4;
    this.worldMatrix = new THREE.Matrix4;
    this.invBindMatrix = new THREE.Matrix4;
    this.skinMatrix = new THREE.Matrix4;
    this.worldMatrixDirty = true;
    return this
};
ColladaLoader2.ThreejsSkeletonBone.prototype.getWorldMatrix = function () {
    if (this.worldMatrixDirty) {
        if (this.parent != null) {
            this.worldMatrix.multiplyMatrices(this.parent.getWorldMatrix(), this.matrix)
        } else {
            this.worldMatrix.copy(this.matrix)
        }
        this.worldMatrixDirty = false
    }
    return this.worldMatrix
};
ColladaLoader2.ThreejsSkeletonBone.prototype.applyAnimation = function (e) {
    var t, n, r, i;
    if (this.isAnimated) {
        i = this.node.transformations;
        for (n = 0, r = i.length; n < r; n++) {
            t = i[n];
            t.applyAnimationKeyframe(e)
        }
        this.node.getTransformMatrix(this.matrix)
    }
    this.worldMatrixDirty = true;
    return null
};
ColladaLoader2.ThreejsSkeletonBone.prototype.updateSkinMatrix = function (e) {
    var t;
    t = this.getWorldMatrix();
    this.skinMatrix.multiplyMatrices(t, this.invBindMatrix);
    this.skinMatrix.multiplyMatrices(this.skinMatrix, e);
    return null
};
ColladaLoader2.ThreejsMaterialMap = function () {
    this.materials = [];
    this.indices = {};
    this.needTangents = false;
    return this
};
ColladaLoader2.File = function (e) {
    var t, n, r, i;
    t = null;
    this._url = null;
    this._baseUrl = "";
    this._loader = e;
    this._options = {};
    i = e.options;
    for (n in i) {
        r = i[n];
        this._options[n] = r
    }
    this._readyCallback = null;
    this._progressCallback = null;
    this.dae = {ids: {}, animationTargets: [], libEffects: [], libMaterials: [], libGeometries: [], libControllers: [], libLights: [], libCameras: [], libImages: [], libVisualScenes: [], libAnimations: [], asset: null, scene: null};
    this.threejs = {scene: null, images: [], geometries: [], materials: []};
    this.scene = null;
    return this
};
ColladaLoader2.File.prototype.setUrl = function (e) {
    var t;
    if (e != null) {
        this._url = e;
        t = e.split("/");
        t.pop();
        this._baseUrl = (t.length < 1 ? "." : t.join("/")) + "/"
    } else {
        this._url = "";
        this._baseUrl = ""
    }
};
ColladaLoader2.File.prototype.getLibInfo = function (e, t, n) {
    var r, i, s, o, u;
    if (e == null) {
        return""
    }
    s = ColladaLoader2.graphNodeString(t, n + (" <" + n + ">\n"));
    i = 0;
    for (o = 0, u = e.length; o < u; o++) {
        r = e[o];
        s += ColladaLoader2.getNodeInfo(r, t + 1, "");
        i += 1
    }
    if (i > 0) {
        return s
    } else {
        return""
    }
};
ColladaLoader2.File.prototype.getInfo = function (e, t) {
    var n;
    n = "<collada url='" + this._url + "'>\n";
    n += ColladaLoader2.getNodeInfo(this.dae.asset, e + 1, "asset ");
    n += ColladaLoader2.getNodeInfo(this.dae.scene, e + 1, "scene ");
    n += this.getLibInfo(this.dae.libEffects, e + 1, "library_effects");
    n += this.getLibInfo(this.dae.libMaterials, e + 1, "library_materials");
    n += this.getLibInfo(this.dae.libGeometries, e + 1, "library_geometries");
    n += this.getLibInfo(this.dae.libControllers, e + 1, "library_controllers");
    n += this.getLibInfo(this.dae.libLights, e + 1, "library_lights");
    n += this.getLibInfo(this.dae.libCameras, e + 1, "library_cameras");
    n += this.getLibInfo(this.dae.libImages, e + 1, "library_images");
    n += this.getLibInfo(this.dae.libVisualScenes, e + 1, "library_visual_scenes");
    n += this.getLibInfo(this.dae.libAnimations, e + 1, "library_animations");
    return n
};
ColladaLoader2.File.prototype._getAttributeAsFloat = function (e, t, n, r) {
    var i;
    i = e.getAttribute(t);
    if (i != null) {
        return parseFloat(i)
    } else if (!r) {
        return n
    } else {
        ColladaLoader2._log("Element " + e.nodeName + " is missing required attribute " + t + ". Using default value " + n + ".", ColladaLoader2.messageError);
        return n
    }
};
ColladaLoader2.File.prototype._getAttributeAsInt = function (e, t, n, r) {
    var i;
    i = e.getAttribute(t);
    if (i != null) {
        return parseInt(i, 10)
    } else if (!r) {
        return n
    } else {
        ColladaLoader2._log("Element " + e.nodeName + " is missing required attribute " + t + ". Using default value " + n + ".", ColladaLoader2.messageError);
        return n
    }
};
ColladaLoader2.File.prototype._getAttributeAsString = function (e, t, n, r) {
    var i;
    i = e.getAttribute(t);
    if (i != null) {
        return i
    } else if (!r) {
        return n
    } else {
        ColladaLoader2._log("Element " + e.nodeName + " is missing required attribute " + t + ". Using default value " + n + ".", ColladaLoader2.messageError);
        return n
    }
};
ColladaLoader2.File.prototype._getAttributeAsUrlLink = function (e, t, n) {
    var r;
    r = e.getAttribute(t);
    if (r != null) {
        return new ColladaLoader2.UrlLink(r, this)
    } else {
        if (n) {
            ColladaLoader2._log("Element " + e.nodeName + " is missing required attribute " + t + ".", ColladaLoader2.messageError)
        }
        return null
    }
};
ColladaLoader2.File.prototype._getAttributeAsSidLink = function (e, t, n, r) {
    var i;
    i = e.getAttribute(t);
    if (i != null) {
        return new ColladaLoader2.SidLink(n, i, this)
    } else {
        if (r) {
            ColladaLoader2._log("Element " + e.nodeName + " is missing required attribute " + t + ".", ColladaLoader2.messageError)
        }
        return null
    }
};
ColladaLoader2.File.prototype._getAttributeAsFxLink = function (e, t, n, r) {
    var i;
    i = e.getAttribute(t);
    if (i != null) {
        return new ColladaLoader2.FxLink(i, n, this)
    } else {
        if (r) {
            ColladaLoader2._log("Element " + e.nodeName + " is missing required attribute " + t + ".", ColladaLoader2.messageError)
        }
        return null
    }
};
ColladaLoader2.File.prototype._addUrlTarget = function (e, t, n) {
    var r;
    if (t != null) {
        t.push(e)
    }
    r = e.id;
    if (r == null) {
        if (n) {
            ColladaLoader2._log("Object has no ID.", ColladaLoader2.messageError)
        }
        return
    }
    if (this.dae.ids[r] != null) {
        ColladaLoader2._log("There is already an object with ID " + r + ".", ColladaLoader2.messageError);
        return
    }
    this.dae.ids[r] = e
};
ColladaLoader2.File.prototype._addFxTarget = function (e, t) {
    var n;
    n = e.sid;
    if (n == null) {
        ColladaLoader2._log("Cannot add a FX target: object has no SID.", ColladaLoader2.messageError);
        return
    }
    if (t.sids[n] != null) {
        ColladaLoader2._log("There is already an FX target with SID " + n + ".", ColladaLoader2.messageError);
        return
    }
    e.fxScope = t;
    t.sids[n] = e
};
ColladaLoader2.File.prototype._addSidTarget = function (e, t) {
    if (t.sidChildren == null) {
        t.sidChildren = []
    }
    t.sidChildren.push(e)
};
ColladaLoader2.File.prototype._parseXml = function (e) {
    var t, n, r, i, s;
    if (e.childNodes == null) {
        ColladaLoader2._log("Cannot parse document, no 'childNodes' property (not an XML document?).", ColladaLoader2.messageError)
    }
    if (e.childNodes.length === 0) {
        ColladaLoader2._log("Cannot parse document, document is empty.", ColladaLoader2.messageError)
    } else {
        n = false;
        s = e.childNodes;
        for (r = 0, i = s.length; r < i; r++) {
            t = s[r];
            if (t.nodeType === 1) {
                switch (t.nodeName) {
                    case"COLLADA":
                        if (n) {
                            ColladaLoader2._log("Ignoring unexpected second top level COLLADA element.", ColladaLoader2.messageWarning)
                        } else {
                            n = true;
                            this._parseCollada(t)
                        }
                        break;
                    default:
                        ColladaLoader2._reportUnexpectedChild(t)
                }
            }
        }
        if (!n) {
            ColladaLoader2._log("Cannot parse document, no COLLADA element.", ColladaLoader2.messageError)
        }
    }
};
ColladaLoader2.File.prototype._parseCollada = function (e) {
    var t, n, r, i;
    i = e.childNodes;
    for (n = 0, r = i.length; n < r; n++) {
        t = i[n];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"asset":
                    this._parseAsset(t);
                    break;
                case"scene":
                    this._parseScene(t);
                    break;
                case"library_effects":
                    this._parseLibEffect(t);
                    break;
                case"library_materials":
                    this._parseLibMaterial(t);
                    break;
                case"library_geometries":
                    this._parseLibGeometry(t);
                    break;
                case"library_images":
                    this._parseLibImage(t);
                    break;
                case"library_visual_scenes":
                    this._parseLibVisualScene(t);
                    break;
                case"library_controllers":
                    this._parseLibController(t);
                    break;
                case"library_animations":
                    this._parseLibAnimation(t);
                    break;
                case"library_lights":
                    this._parseLibLight(t);
                    break;
                case"library_cameras":
                    this._parseLibCamera(t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseAsset = function (e) {
    var t, n, r, i;
    if (!this.dae.asset) {
        this.dae.asset = new ColladaLoader2.Asset
    }
    i = e.childNodes;
    for (n = 0, r = i.length; n < r; n++) {
        t = i[n];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"unit":
                    this.dae.asset.unit = this._getAttributeAsFloat(t, "meter", 1, false);
                    break;
                case"up_axis":
                    this.dae.asset.upAxis = t.textContent.toUpperCase().charAt(0);
                    break;
                case"contributor":
                case"created":
                case"modified":
                case"revision":
                case"title":
                case"subject":
                case"keywords":
                    ColladaLoader2._reportUnhandledExtra(t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseScene = function (e) {
    var t, n, r, i;
    i = e.childNodes;
    for (n = 0, r = i.length; n < r; n++) {
        t = i[n];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"instance_visual_scene":
                    this.dae.scene = this._getAttributeAsUrlLink(t, "url", true);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseLibVisualScene = function (e) {
    var t, n, r, i;
    i = e.childNodes;
    for (n = 0, r = i.length; n < r; n++) {
        t = i[n];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"visual_scene":
                    this._parseVisualScene(t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseVisualScene = function (e) {
    var t, n, r, i, s;
    n = new ColladaLoader2.VisualScene;
    n.id = this._getAttributeAsString(e, "id", null, false);
    this._addUrlTarget(n, this.dae.libVisualScenes, true);
    s = e.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        t = s[r];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"node":
                    this._parseSceneNode(n, t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseSceneNode = function (e, t) {
    var n, r, i, s, o;
    r = new ColladaLoader2.VisualSceneNode;
    r.id = this._getAttributeAsString(t, "id", null, false);
    r.sid = this._getAttributeAsString(t, "sid", null, false);
    r.name = this._getAttributeAsString(t, "name", null, false);
    r.type = this._getAttributeAsString(t, "type", null, false);
    r.layer = this._getAttributeAsString(t, "layer", null, false);
    r.parent = e;
    e.children.push(r);
    this._addUrlTarget(r, null, false);
    this._addSidTarget(r, e);
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"instance_geometry":
                    this._parseInstanceGeometry(r, n);
                    break;
                case"instance_controller":
                    this._parseInstanceController(r, n);
                    break;
                case"instance_light":
                    this._parseInstanceLight(r, n);
                    break;
                case"instance_camera":
                    this._parseInstanceCamera(r, n);
                    break;
                case"matrix":
                case"rotate":
                case"translate":
                case"scale":
                    this._parseTransformElement(r, n);
                    break;
                case"node":
                    this._parseSceneNode(r, n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseInstanceGeometry = function (e, t) {
    var n, r, i, s, o;
    r = new ColladaLoader2.InstanceGeometry;
    r.geometry = this._getAttributeAsUrlLink(t, "url", true);
    r.sid = this._getAttributeAsString(t, "sid", null, false);
    e.geometries.push(r);
    this._addSidTarget(r, e);
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"bind_material":
                    this._parseBindMaterial(r, n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseInstanceController = function (e, t) {
    var n, r, i, s, o;
    r = new ColladaLoader2.InstanceController;
    r.controller = this._getAttributeAsUrlLink(t, "url", true);
    r.sid = this._getAttributeAsString(t, "sid", null, false);
    r.name = this._getAttributeAsString(t, "name", null, false);
    e.controllers.push(r);
    this._addSidTarget(r, e);
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"skeleton":
                    r.skeletons.push(new ColladaLoader2.UrlLink(n.textContent, this));
                    break;
                case"bind_material":
                    this._parseBindMaterial(r, n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseBindMaterial = function (e, t) {
    var n, r, i, s;
    s = t.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        n = s[r];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"technique_common":
                    this._parseBindMaterialTechnique(e, n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseBindMaterialTechnique = function (e, t) {
    var n, r, i, s;
    s = t.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        n = s[r];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"instance_material":
                    this._parseInstanceMaterial(e, n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseInstanceMaterial = function (e, t) {
    var n, r, i, s, o;
    r = new ColladaLoader2.InstanceMaterial;
    r.symbol = this._getAttributeAsString(t, "symbol", null, false);
    r.material = this._getAttributeAsUrlLink(t, "target", true);
    e.materials.push(r);
    this._addSidTarget(r, e);
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"bind_vertex_input":
                    this._parseInstanceMaterialBindVertex(r, n);
                    break;
                case"bind":
                    this._parseInstanceMaterialBind(r, n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseInstanceMaterialBindVertex = function (e, t) {
    var n, r, i;
    i = this._getAttributeAsString(t, "semantic", null, true);
    n = this._getAttributeAsString(t, "input_semantic", null, true);
    r = this._getAttributeAsInt(t, "input_set", null, false);
    if (i != null && n != null) {
        e.vertexInputs[i] = {inputSemantic: n, inputSet: r}
    } else {
        ColladaLoader2._log("Skipped a material vertex binding because of missing semantics.", ColladaLoader2.messageWarning)
    }
};
ColladaLoader2.File.prototype._parseInstanceMaterialBind = function (e, t) {
    var n, r;
    n = this._getAttributeAsString(t, "semantic", null, false);
    r = this._getAttributeAsSidLink(t, "target", null, true);
    if (n != null) {
        e.params[n] = {target: r}
    } else {
        ColladaLoader2._log("Skipped a material uniform binding because of missing semantics.", ColladaLoader2.messageWarning)
    }
};
ColladaLoader2.File.prototype._parseTransformElement = function (e, t) {
    var n, r;
    r = new ColladaLoader2.NodeTransform;
    r.sid = this._getAttributeAsString(t, "sid", null, false);
    r.type = t.nodeName;
    r.node = e;
    e.transformations.push(r);
    this._addSidTarget(r, e);
    this.dae.animationTargets.push(r);
    r.data = ColladaLoader2._strToFloats(t.textContent);
    n = 0;
    switch (r.type) {
        case"matrix":
            n = 16;
            break;
        case"rotate":
            n = 4;
            break;
        case"translate":
            n = 3;
            break;
        case"scale":
            n = 3;
            break;
        case"skew":
            n = 7;
            break;
        case"lookat":
            n = 9;
            break;
        default:
            ColladaLoader2._log("Unknown transformation type " + r.type + ".", ColladaLoader2.messageError)
    }
    if (r.data.length !== n) {
        ColladaLoader2._log("Wrong number of elements for transformation type '" + r.type + "': expected " + n + ", found " + r.data.length, ColladaLoader2.messageError)
    }
};
ColladaLoader2.File.prototype._parseInstanceLight = function (e, t) {
    var n, r, i, s, o;
    r = new ColladaLoader2.InstanceLight;
    r.light = this._getAttributeAsUrlLink(t, "url", true);
    r.sid = this._getAttributeAsString(t, "sid", null, false);
    r.name = this._getAttributeAsString(t, "name", null, false);
    e.lights.push(r);
    this._addSidTarget(r, e);
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"extra":
                    ColladaLoader2._reportUnhandledExtra(n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseInstanceCamera = function (e, t) {
    var n, r, i, s, o;
    n = new ColladaLoader2.InstanceCamera;
    n.camera = this._getAttributeAsUrlLink(t, "url", true);
    n.sid = this._getAttributeAsString(t, "sid", null, false);
    n.name = this._getAttributeAsString(t, "name", null, false);
    e.cameras.push(n);
    this._addSidTarget(n, e);
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        r = o[i];
        if (r.nodeType === 1) {
            switch (r.nodeName) {
                case"extra":
                    ColladaLoader2._reportUnhandledExtra(r);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(r)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseLibEffect = function (e) {
    var t, n, r, i;
    i = e.childNodes;
    for (n = 0, r = i.length; n < r; n++) {
        t = i[n];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"effect":
                    this._parseEffect(t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseEffect = function (e) {
    var t, n, r, i, s;
    n = new ColladaLoader2.Effect;
    n.id = this._getAttributeAsString(e, "id", null, true);
    this._addUrlTarget(n, this.dae.libEffects, true);
    s = e.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        t = s[r];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"profile_COMMON":
                    this._parseEffectProfileCommon(n, t);
                    break;
                case"profile":
                    ColladaLoader2._log("Skipped non-common effect profile for effect " + n.id + ".", ColladaLoader2.messageWarning);
                    break;
                case"extra":
                    this._parseTechniqueExtra(n.technique, t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseEffectProfileCommon = function (e, t) {
    var n, r, i, s;
    s = t.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        n = s[r];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"newparam":
                    this._parseEffectNewparam(e, n);
                    break;
                case"technique":
                    this._parseEffectTechnique(e, n);
                    break;
                case"extra":
                    this._parseTechniqueExtra(e.technique, n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseEffectNewparam = function (e, t) {
    var n, r, i, s, o;
    r = new ColladaLoader2.EffectParam;
    r.sid = this._getAttributeAsString(t, "sid", null, false);
    this._addFxTarget(r, e);
    e.params.push(r);
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"semantic":
                    r.semantic = n.textContent;
                    break;
                case"float":
                    r.floats = ColladaLoader2._strToFloats(n.textContent);
                    break;
                case"float2":
                    r.floats = ColladaLoader2._strToFloats(n.textContent);
                    break;
                case"float3":
                    r.floats = ColladaLoader2._strToFloats(n.textContent);
                    break;
                case"float4":
                    r.floats = ColladaLoader2._strToFloats(n.textContent);
                    break;
                case"surface":
                    r.surface = this._parseEffectSurface(e, n);
                    break;
                case"sampler2D":
                    r.sampler = this._parseEffectSampler(e, n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseEffectSurface = function (e, t) {
    var n, r, i, s, o;
    r = new ColladaLoader2.EffectSurface;
    r.type = this._getAttributeAsString(t, "type", null, true);
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"init_from":
                    r.initFrom = new ColladaLoader2.UrlLink(n.textContent, this);
                    break;
                case"format":
                    r.format = n.textContent;
                    break;
                case"size":
                    r.size = ColladaLoader2._strToFloats(n.textContent);
                    break;
                case"viewport_ratio":
                    r.viewportRatio = ColladaLoader2._strToFloats(n.textContent);
                    break;
                case"mip_levels":
                    r.mipLevels = parseInt(n.textContent, 10);
                    break;
                case"mipmap_generate":
                    r.mipmapGenerate = n.textContent;
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
    return r
};
ColladaLoader2.File.prototype._parseEffectSampler = function (e, t) {
    var n, r, i, s, o;
    r = new ColladaLoader2.EffectSampler;
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"source":
                    r.surface = new ColladaLoader2.FxLink(n.textContent, e, this);
                    break;
                case"instance_image":
                    r.image = this._getAttributeAsUrlLink(n, "url", true);
                    break;
                case"wrap_s":
                    r.wrapS = n.textContent;
                    break;
                case"wrap_t":
                    r.wrapT = n.textContent;
                    break;
                case"minfilter":
                    r.minfilter = n.textContent;
                    break;
                case"magfilter":
                    r.magfilter = n.textContent;
                    break;
                case"border_color":
                    r.borderColor = ColladaLoader2._strToFloats(n.textContent);
                    break;
                case"mipmap_maxlevel":
                    r.mipmapMaxLevel = parseInt(n.textContent, 10);
                    break;
                case"mipmap_bias":
                    r.mipmapBias = parseFloat(n.textContent);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
    return r
};
ColladaLoader2.File.prototype._parseEffectTechnique = function (e, t) {
    var n, r, i, s, o;
    r = new ColladaLoader2.EffectTechnique;
    r.sid = this._getAttributeAsString(t, "sid", null, false);
    this._addFxTarget(r, e);
    e.technique = r;
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"blinn":
                case"phong":
                case"lambert":
                case"constant":
                    r.shading = n.nodeName;
                    this._parseTechniqueParam(r, "COMMON", n);
                    break;
                case"extra":
                    this._parseTechniqueExtra(r, n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseTechniqueParam = function (e, t, n) {
    var r, i, s, o;
    o = n.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        r = o[i];
        if (r.nodeType === 1) {
            switch (r.nodeName) {
                case"newparam":
                    this._parseEffectNewparam(e, r);
                    break;
                case"emission":
                    e.emission = this._parseEffectColorOrTexture(e, r);
                    break;
                case"ambient":
                    e.ambient = this._parseEffectColorOrTexture(e, r);
                    break;
                case"diffuse":
                    e.diffuse = this._parseEffectColorOrTexture(e, r);
                    break;
                case"specular":
                    e.specular = this._parseEffectColorOrTexture(e, r);
                    break;
                case"reflective":
                    e.reflective = this._parseEffectColorOrTexture(e, r);
                    break;
                case"transparent":
                    e.transparent = this._parseEffectColorOrTexture(e, r);
                    break;
                case"bump":
                    e.bump = this._parseEffectColorOrTexture(e, r);
                    break;
                case"shininess":
                    e.shininess = parseFloat(r.childNodes[1].textContent);
                    break;
                case"reflectivity":
                    e.reflectivity = parseFloat(r.childNodes[1].textContent);
                    break;
                case"transparency":
                    e.transparency = parseFloat(r.childNodes[1].textContent);
                    break;
                case"index_of_refraction":
                    e.index_of_refraction = parseFloat(r.childNodes[1].textContent);
                    break;
                case"double_sided":
                    e.double_sided = parseFloat(r.textContent) > 0;
                    break;
                default:
                    if (t === "COMMON") {
                        ColladaLoader2._reportUnexpectedChild(r)
                    }
            }
        }
    }
};
ColladaLoader2.File.prototype._parseTechniqueExtra = function (e, t) {
    var n, r, i, s, o;
    if (e == null) {
        ColladaLoader2._log("Ignored element <extra>, because there is no <technique>.", ColladaLoader2.messageWarning);
        return
    }
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"technique":
                    r = this._getAttributeAsString(n, "profile", null, true);
                    this._parseTechniqueParam(e, r, n);
                    break;
                default:
                    ColladaLoader2._reportUnhandledExtra(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseEffectColorOrTexture = function (e, t) {
    var n, r, i, s, o;
    r = new ColladaLoader2.ColorOrTexture;
    r.opaque = this._getAttributeAsString(t, "opaque", null, false);
    r.bumptype = this._getAttributeAsString(t, "bumptype", null, false);
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"color":
                    r.color = ColladaLoader2._strToColor(n.textContent);
                    break;
                case"texture":
                    r.textureSampler = this._getAttributeAsFxLink(n, "texture", e, true);
                    r.texcoord = this._getAttributeAsString(n, "texcoord", null, true);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
    return r
};
ColladaLoader2.File.prototype._parseLibMaterial = function (e) {
    var t, n, r, i;
    i = e.childNodes;
    for (n = 0, r = i.length; n < r; n++) {
        t = i[n];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"material":
                    this._parseMaterial(t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseMaterial = function (e) {
    var t, n, r, i, s;
    n = new ColladaLoader2.Material;
    n.id = this._getAttributeAsString(e, "id", null, true);
    n.name = this._getAttributeAsString(e, "name", null, false);
    this._addUrlTarget(n, this.dae.libMaterials, true);
    s = e.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        t = s[r];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"instance_effect":
                    n.effect = this._getAttributeAsUrlLink(t, "url", true);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseLibGeometry = function (e) {
    var t, n, r, i;
    i = e.childNodes;
    for (n = 0, r = i.length; n < r; n++) {
        t = i[n];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"geometry":
                    this._parseGeometry(t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseGeometry = function (e) {
    var t, n, r, i, s;
    n = new ColladaLoader2.Geometry;
    n.id = this._getAttributeAsString(e, "id", null, true);
    n.name = this._getAttributeAsString(e, "name", null, false);
    this._addUrlTarget(n, this.dae.libGeometries, true);
    s = e.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        t = s[r];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"mesh":
                    this._parseMesh(n, t);
                    break;
                case"convex_mesh":
                case"spline":
                    ColladaLoader2._log("Geometry type " + t.nodeName + " not supported.", ColladaLoader2.messageError);
                    break;
                case"extra":
                    this._parseGeometryExtra(n, t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseMesh = function (e, t) {
    var n, r, i, s;
    s = t.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        n = s[r];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"source":
                    this._parseSource(e, n);
                    break;
                case"vertices":
                    this._parseVertices(e, n);
                    break;
                case"triangles":
                case"polylist":
                case"polygons":
                    this._parseTriangles(e, n);
                    break;
                case"lines":
                case"linestrips":
                case"trifans":
                case"tristrips":
                    ColladaLoader2._log("Geometry primitive type " + n.nodeName + " not supported.", ColladaLoader2.messageError);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseGeometryExtra = function (e, t) {
    var n, r, i, s, o;
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"technique":
                    r = this._getAttributeAsString(n, "profile", null, true);
                    this._parseGeometryExtraTechnique(e, r, n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseGeometryExtraTechnique = function (e, t, n) {
    var r, i, s, o;
    o = n.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        r = o[i];
        if (r.nodeType === 1) {
            ColladaLoader2._reportUnhandledExtra(r)
        }
    }
};
ColladaLoader2.File.prototype._parseSource = function (e, t) {
    var n, r, i, s, o;
    r = new ColladaLoader2.Source;
    r.id = this._getAttributeAsString(t, "id", null, true);
    r.name = this._getAttributeAsString(t, "name", null, false);
    this._addUrlTarget(r, e.sources, true);
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"bool_array":
                    r.sourceId = this._getAttributeAsString(n, "id", null, false);
                    r.data = ColladaLoader2._strToBools(n.textContent);
                    break;
                case"float_array":
                    r.sourceId = this._getAttributeAsString(n, "id", null, false);
                    r.data = ColladaLoader2._strToFloats(n.textContent);
                    break;
                case"int_array":
                    r.sourceId = this._getAttributeAsString(n, "id", null, false);
                    r.data = ColladaLoader2._strToInts(n.textContent);
                    break;
                case"IDREF_array":
                case"Name_array":
                    r.sourceId = this._getAttributeAsString(n, "id", null, false);
                    r.data = ColladaLoader2._strToStrings(n.textContent);
                    break;
                case"technique_common":
                    this._parseSourceTechniqueCommon(r, n);
                    break;
                case"technique":
                    ColladaLoader2._reportUnhandledExtra(n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseVertices = function (e, t) {
    var n, r, i, s, o;
    r = new ColladaLoader2.Vertices;
    r.id = this._getAttributeAsString(t, "id", null, true);
    r.name = this._getAttributeAsString(t, "name", null, false);
    this._addUrlTarget(r, null, true);
    e.vertices = r;
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"input":
                    r.inputs.push(this._parseInput(n, false));
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseTriangles = function (e, t) {
    var n, r, i, s, o;
    r = new ColladaLoader2.Triangles;
    r.name = this._getAttributeAsString(t, "name", null, false);
    r.material = this._getAttributeAsString(t, "material", null, false);
    r.count = this._getAttributeAsInt(t, "count", 0, true);
    r.type = t.nodeName;
    e.triangles.push(r);
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"input":
                    r.inputs.push(this._parseInput(n, true));
                    break;
                case"vcount":
                    r.vcount = ColladaLoader2._strToInts(n.textContent);
                    break;
                case"p":
                    r.indices = ColladaLoader2._strToInts(n.textContent);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
    return r
};
ColladaLoader2.File.prototype._parseSourceTechniqueCommon = function (e, t) {
    var n, r, i, s;
    s = t.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        n = s[r];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"accessor":
                    this._parseAccessor(e, n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseAccessor = function (e, t) {
    var n, r, i, s, o;
    r = this._getAttributeAsString(t, "source", null, true);
    e.count = this._getAttributeAsInt(t, "count", 0, true);
    e.stride = this._getAttributeAsInt(t, "stride", 1, true);
    if (r !== "#" + e.sourceId) {
        ColladaLoader2._log("Non-local sources not supported, source data will be empty", ColladaLoader2.messageError)
    }
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"param":
                    this._parseAccessorParam(e, n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseAccessorParam = function (e, t) {
    var n, r, i, s;
    n = this._getAttributeAsString(t, "name", null, false);
    r = this._getAttributeAsString(t, "semantic", null, false);
    s = this._getAttributeAsString(t, "type", null, true);
    i = this._getAttributeAsString(t, "sid", null, false);
    if (n != null && s != null) {
        e.params[n] = s
    } else if (r != null && s != null) {
        e.params[r] = s
    } else {
        ColladaLoader2._log("Accessor param ignored due to missing type, name, or semantic", ColladaLoader2.messageWarning)
    }
};
ColladaLoader2.File.prototype._parseInput = function (e, t) {
    var n;
    n = new ColladaLoader2.Input;
    n.semantic = this._getAttributeAsString(e, "semantic", null, true);
    n.source = this._getAttributeAsUrlLink(e, "source", true);
    if (t) {
        n.offset = this._getAttributeAsInt(e, "offset", 0, true);
        n.set = this._getAttributeAsInt(e, "set", null, false)
    }
    return n
};
ColladaLoader2.File.prototype._parseLibImage = function (e) {
    var t, n, r, i;
    i = e.childNodes;
    for (n = 0, r = i.length; n < r; n++) {
        t = i[n];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"image":
                    this._parseImage(t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseImage = function (e) {
    var t, n, r, i, s;
    n = new ColladaLoader2.Image;
    n.id = this._getAttributeAsString(e, "id", null, true);
    this._addUrlTarget(n, this.dae.libImages, true);
    s = e.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        t = s[r];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"init_from":
                    n.initFrom = t.textContent;
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseLibController = function (e) {
    var t, n, r, i;
    i = e.childNodes;
    for (n = 0, r = i.length; n < r; n++) {
        t = i[n];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"controller":
                    this._parseController(t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseController = function (e) {
    var t, n, r, i, s;
    n = new ColladaLoader2.Controller;
    n.id = this._getAttributeAsString(e, "id", null, true);
    n.name = this._getAttributeAsString(e, "name", null, false);
    this._addUrlTarget(n, this.dae.libControllers, true);
    s = e.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        t = s[r];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"skin":
                    this._parseSkin(n, t);
                    break;
                case"morph":
                    this._parseMorph(n, t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseMorph = function (e, t) {
    ColladaLoader2._log("Morph controllers not implemented", ColladaLoader2.messageError)
};
ColladaLoader2.File.prototype._parseSkin = function (e, t) {
    var n, r, i, s, o;
    r = new ColladaLoader2.Skin;
    r.source = this._getAttributeAsUrlLink(t, "source", true);
    if (e.skin != null || e.morph != null) {
        ColladaLoader2._log("Controller already has a skin or morph", ColladaLoader2.messageError)
    }
    e.skin = r;
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        n = o[i];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"bind_shape_matrix":
                    this._parseBindShapeMatrix(r, n);
                    break;
                case"source":
                    this._parseSource(r, n);
                    break;
                case"joints":
                    this._parseJoints(r, n);
                    break;
                case"vertex_weights":
                    this._parseVertexWeights(r, n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseBindShapeMatrix = function (e, t) {
    e.bindShapeMatrix = ColladaLoader2._strToFloats(t.textContent)
};
ColladaLoader2.File.prototype._parseJoints = function (e, t) {
    var n, r, i, s, o, u, a, f, l;
    s = new ColladaLoader2.Joints;
    if (e.joints != null) {
        ColladaLoader2._log("Skin already has a joints array", ColladaLoader2.messageError)
    }
    e.joints = s;
    i = [];
    l = t.childNodes;
    for (o = 0, a = l.length; o < a; o++) {
        n = l[o];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"input":
                    i.push(this._parseInput(n, false));
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
    for (u = 0, f = i.length; u < f; u++) {
        r = i[u];
        switch (r.semantic) {
            case"JOINT":
                s.joints = r;
                break;
            case"INV_BIND_MATRIX":
                s.invBindMatrices = r;
                break;
            default:
                ColladaLoader2._log("Unknown joints input semantic " + r.semantic, ColladaLoader2.messageError)
        }
    }
};
ColladaLoader2.File.prototype._parseVertexWeights = function (e, t) {
    var n, r, i, s, o, u, a, f, l;
    s = new ColladaLoader2.VertexWeights;
    s.count = this._getAttributeAsInt(t, "count", 0, true);
    if (e.vertexWeights != null) {
        ColladaLoader2._log("Skin already has a vertex weight array", ColladaLoader2.messageError)
    }
    e.vertexWeights = s;
    i = [];
    l = t.childNodes;
    for (o = 0, a = l.length; o < a; o++) {
        n = l[o];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"input":
                    i.push(this._parseInput(n, true));
                    break;
                case"vcount":
                    s.vcount = ColladaLoader2._strToInts(n.textContent);
                    break;
                case"v":
                    s.v = ColladaLoader2._strToInts(n.textContent);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
    for (u = 0, f = i.length; u < f; u++) {
        r = i[u];
        switch (r.semantic) {
            case"JOINT":
                s.joints = r;
                break;
            case"WEIGHT":
                s.weights = r;
                break;
            default:
                ColladaLoader2._log("Unknown vertex weight input semantic " + r.semantic, ColladaLoader2.messageError)
        }
    }
};
ColladaLoader2.File.prototype._parseLibAnimation = function (e) {
    var t, n, r, i;
    i = e.childNodes;
    for (n = 0, r = i.length; n < r; n++) {
        t = i[n];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"animation":
                    this._parseAnimation(null, t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseAnimation = function (e, t) {
    var n, r, i, s, o;
    n = new ColladaLoader2.Animation;
    n.id = this._getAttributeAsString(t, "id", null, true);
    n.name = this._getAttributeAsString(t, "name", null, false);
    n.parent = e;
    if (e != null) {
        n.rootId = e.rootId;
        n.rootName = e.rootName
    } else {
        n.rootId = n.id;
        n.rootName = n.name
    }
    this._addUrlTarget(n, (e != null ? e.animations : void 0) || this.dae.libAnimations, false);
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        r = o[i];
        if (r.nodeType === 1) {
            switch (r.nodeName) {
                case"animation":
                    this._parseAnimation(n, r);
                    break;
                case"source":
                    this._parseSource(n, r);
                    break;
                case"sampler":
                    this._parseSampler(n, r);
                    break;
                case"channel":
                    this._parseChannel(n, r);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(r)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseSampler = function (e, t) {
    var n, r, i, s, o, u, a, f, l;
    s = new ColladaLoader2.Sampler;
    s.id = this._getAttributeAsString(t, "id", null, false);
    if (s.id != null) {
        this._addUrlTarget(s, e.samplers, false)
    }
    i = [];
    l = t.childNodes;
    for (o = 0, a = l.length; o < a; o++) {
        n = l[o];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"input":
                    i.push(this._parseInput(n, false));
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
    for (u = 0, f = i.length; u < f; u++) {
        r = i[u];
        switch (r.semantic) {
            case"INPUT":
                s.input = r;
                break;
            case"OUTPUT":
                s.outputs.push(r);
                break;
            case"INTERPOLATION":
                s.interpolation = r;
                break;
            case"IN_TANGENT":
                s.inTangents.push(r);
                break;
            case"OUT_TANGENT":
                s.outTangents.push(r);
                break;
            default:
                ColladaLoader2._log("Unknown sampler input semantic " + r.semantic, ColladaLoader2.messageError)
        }
    }
};
ColladaLoader2.File.prototype._parseChannel = function (e, t) {
    var n, r, i, s, o;
    n = new ColladaLoader2.Channel;
    n.source = this._getAttributeAsUrlLink(t, "source", true);
    n.target = this._getAttributeAsSidLink(t, "target", e.id, true);
    e.channels.push(n);
    n.animation = e;
    o = t.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        r = o[i];
        if (r.nodeType === 1) {
            ColladaLoader2._reportUnexpectedChild(r)
        }
    }
};
ColladaLoader2.File.prototype._parseLibLight = function (e) {
    var t, n, r, i;
    i = e.childNodes;
    for (n = 0, r = i.length; n < r; n++) {
        t = i[n];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"light":
                    this._parseLight(t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseLight = function (e) {
    var t, n, r, i, s;
    n = new ColladaLoader2.Light;
    n.id = this._getAttributeAsString(e, "id", null, true);
    n.name = this._getAttributeAsString(e, "name", null, false);
    if (n.id != null) {
        this._addUrlTarget(n, this.dae.libLights, true)
    }
    s = e.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        t = s[r];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"technique_common":
                    this._parseLightTechniqueCommon(t, n);
                    break;
                case"extra":
                    ColladaLoader2._reportUnhandledExtra(t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseLightTechniqueCommon = function (e, t) {
    var n, r, i, s;
    s = e.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        n = s[r];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"ambient":
                    this._parseLightParams(n, "COMMON", t);
                    break;
                case"directional":
                    this._parseLightParams(n, "COMMON", t);
                    break;
                case"point":
                    this._parseLightParams(n, "COMMON", t);
                    break;
                case"spot":
                    this._parseLightParams(n, "COMMON", t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseLightParams = function (e, t, n) {
    var r, i, s, o;
    n.type = e.nodeName;
    o = e.childNodes;
    for (i = 0, s = o.length; i < s; i++) {
        r = o[i];
        if (r.nodeType === 1) {
            switch (r.nodeName) {
                case"color":
                    this._parseLightColor(r, t, n);
                    break;
                case"constant_attenuation":
                    this._parseLightParam(r, t, n);
                    break;
                case"linear_attenuation":
                    this._parseLightParam(r, t, n);
                    break;
                case"quadratic_attenuation":
                    this._parseLightParam(r, t, n);
                    break;
                case"falloff_angle":
                    this._parseLightParam(r, t, n);
                    break;
                case"falloff_exponent":
                    this._parseLightParam(r, t, n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(r)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseLightColor = function (e, t, n) {
    n.color = ColladaLoader2._strToFloats(e.textContent)
};
ColladaLoader2.File.prototype._parseLightParam = function (e, t, n) {
    var r;
    r = new ColladaLoader2.LightParam;
    r.sid = this._getAttributeAsString(e, "sid", null, false);
    r.name = e.nodeName;
    n.params[r.name] = r;
    this._addSidTarget(r, n);
    r.value = parseFloat(e.textContent)
};
ColladaLoader2.File.prototype._parseLibCamera = function (e) {
    var t, n, r, i;
    i = e.childNodes;
    for (n = 0, r = i.length; n < r; n++) {
        t = i[n];
        if (t.nodeType === 1) {
            switch (t.nodeName) {
                case"camera":
                    this._parseCamera(t);
                    break;
                case"extra":
                    ColladaLoader2._reportUnhandledExtra(t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(t)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseCamera = function (e) {
    var t, n, r, i, s;
    t = new ColladaLoader2.Camera;
    t.id = this._getAttributeAsString(e, "id", null, true);
    if (t.id != null) {
        this._addUrlTarget(t, this.dae.libCameras, true)
    }
    t.name = e.getAttribute("name");
    s = e.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        n = s[r];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"asset":
                    ColladaLoader2._reportUnhandledExtra(n);
                    break;
                case"optics":
                    this._parseCameraOptics(n, t);
                    break;
                case"imager":
                    ColladaLoader2._reportUnhandledExtra(n);
                    break;
                case"extra":
                    ColladaLoader2._reportUnhandledExtra(n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseCameraOptics = function (e, t) {
    var n, r, i, s;
    s = e.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        n = s[r];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"technique_common":
                    this._parseCameraTechniqueCommon(n, t);
                    break;
                case"technique":
                    ColladaLoader2._reportUnhandledExtra(n);
                    break;
                case"extra":
                    ColladaLoader2._reportUnhandledExtra(n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseCameraTechniqueCommon = function (e, t) {
    var n, r, i, s;
    s = e.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        n = s[r];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"orthographic":
                    this._parseCameraParams(n, t);
                    break;
                case"perspective":
                    this._parseCameraParams(n, t);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseCameraParams = function (e, t) {
    var n, r, i, s;
    t.type = e.nodeName;
    s = e.childNodes;
    for (r = 0, i = s.length; r < i; r++) {
        n = s[r];
        if (n.nodeType === 1) {
            switch (n.nodeName) {
                case"xmag":
                    this._parseCameraParam(n, t);
                    break;
                case"ymag":
                    this._parseCameraParam(n, t);
                    break;
                case"xfov":
                    this._parseCameraParam(n, t);
                    break;
                case"yfov":
                    this._parseCameraParam(n, t);
                    break;
                case"aspect_ratio":
                    this._parseCameraParam(n, t);
                    break;
                case"znear":
                    this._parseCameraParam(n, t);
                    break;
                case"zfar":
                    this._parseCameraParam(n, t);
                    break;
                case"extra":
                    ColladaLoader2._reportUnhandledExtra(n);
                    break;
                default:
                    ColladaLoader2._reportUnexpectedChild(n)
            }
        }
    }
};
ColladaLoader2.File.prototype._parseCameraParam = function (e, t) {
    var n;
    n = new ColladaLoader2.CameraParam;
    n.sid = this._getAttributeAsString(e, "sid", null, false);
    n.name = e.nodeName;
    t.params[n.name] = n;
    this._addSidTarget(n, t);
    n.value = parseFloat(e.textContent)
};
ColladaLoader2.File.prototype._linkAnimations = function () {
    var e, t, n, r, i, s, o, u;
    o = this.dae.animationTargets;
    for (n = 0, i = o.length; n < i; n++) {
        t = o[n];
        t.initAnimationTarget()
    }
    u = this.dae.libAnimations;
    for (r = 0, s = u.length; r < s; r++) {
        e = u[r];
        this._linkAnimationChannels(e)
    }
};
ColladaLoader2.File.prototype._linkAnimationChannels = function (e) {
    var t, n, r, i, s, o, u, a, f, l, c, h, p, d, v, m;
    v = e.channels;
    for (c = 0, p = v.length; c < p; c++) {
        t = v[c];
        a = ColladaLoader2.AnimationTarget.fromLink(t.target);
        if (a == null) {
            ColladaLoader2._log("Animation channel has an invalid target '" + t.target.url + "', animation ignored", ColladaLoader2.messageWarning);
            continue
        }
        u = ColladaLoader2.Sampler.fromLink(t.source);
        if (u == null) {
            ColladaLoader2._log("Animation channel has an invalid sampler '" + t.source.url + "', animation ignored", ColladaLoader2.messageWarning);
            continue
        }
        r = u.input;
        if (r == null) {
            ColladaLoader2._log("Animation channel has no input, animation ignored", ColladaLoader2.messageWarning);
            continue
        }
        i = ColladaLoader2.Source.fromLink(r.source);
        if (i == null) {
            ColladaLoader2._log("Animation channel has no input data, animation ignored", ColladaLoader2.messageWarning);
            continue
        }
        if (u.outputs.length > 1) {
            ColladaLoader2._log("Animation channel has more than one output, using only the first output", ColladaLoader2.messageWarning)
        }
        s = u.outputs[0];
        if (s == null) {
            ColladaLoader2._log("Animation channel has no output, animation ignored", ColladaLoader2.messageWarning);
            continue
        }
        o = ColladaLoader2.Source.fromLink(s.source);
        if (o == null) {
            ColladaLoader2._log("Animation channel has no output data, animation ignored", ColladaLoader2.messageWarning);
            continue
        }
        l = new ColladaLoader2.ThreejsAnimationChannel;
        l.outputData = o.data;
        if (i.data instanceof Float32Array) {
            l.inputData = i.data
        } else {
            ColladaLoader2._log("Animation channel has non-float input data, animation ignored", ColladaLoader2.messageWarning);
            continue
        }
        l.stride = o.stride;
        l.animation = e;
        f = t.target;
        if (f.dotSyntax) {
            l.semantic = f.member;
            l.count = 1;
            switch (l.semantic) {
                case"X":
                    l.offset = 0;
                    break;
                case"Y":
                    l.offset = 1;
                    break;
                case"Z":
                    l.offset = 2;
                    break;
                case"W":
                    l.offset = 3;
                    break;
                case"R":
                    l.offset = 0;
                    break;
                case"G":
                    l.offset = 1;
                    break;
                case"B":
                    l.offset = 2;
                    break;
                case"U":
                    l.offset = 0;
                    break;
                case"V":
                    l.offset = 1;
                    break;
                case"S":
                    l.offset = 0;
                    break;
                case"T":
                    l.offset = 1;
                    break;
                case"P":
                    l.offset = 2;
                    break;
                case"Q":
                    l.offset = 3;
                    break;
                case"ANGLE":
                    l.offset = 3;
                    break;
                default:
                    ColladaLoader2._log("Unknown semantic for '" + f.url + "', animation ignored", ColladaLoader2.messageWarning);
                    continue
            }
        } else if (t.target.arrSyntax) {
            switch (f.indices.length) {
                case 1:
                    l.offset = f.indices[0];
                    break;
                case 2:
                    l.offset = f.indices[0] * a.animTarget.dataRows + f.indices[1];
                    break;
                default:
                    ColladaLoader2._log("Invalid number of indices for '" + f.url + "', animation ignored", ColladaLoader2.messageWarning);
                    continue
            }
            l.count = 1
        } else {
            l.offset = 0;
            l.count = a.animTarget.dataColumns * a.animTarget.dataRows
        }
        a.animTarget.channels.push(l)
    }
    m = e.animations;
    for (h = 0, d = m.length; h < d; h++) {
        n = m[h];
        this._linkAnimationChannels(n)
    }
};
ColladaLoader2.File.prototype._createSceneGraph = function () {
    var e, t, n, r, i, s;
    t = ColladaLoader2.VisualScene.fromLink(this.dae.scene);
    if (t == null) {
        return
    }
    n = new THREE.Object3D;
    this.threejs.scene = n;
    s = t.children;
    for (r = 0, i = s.length; r < i; r++) {
        e = s[r];
        this._createSceneGraphNode(e, n)
    }
    this.scene = n
};
ColladaLoader2.File.prototype._setNodeTransformation = function (e, t) {
    e.getTransformMatrix(t.matrix);
    t.matrix.decompose(t.position, t.quaternion, t.scale);
    t.rotation.setFromQuaternion(t.quaternion)
};
ColladaLoader2.File.prototype._createSceneGraphNode = function (e, t) {
    var n, r, i, s, o, u, a, f, l, c, h, p, d, v, m, g, y, b, w, E, S, x, T, N, C, k, L, A;
    f = [];
    N = e.geometries;
    for (p = 0, g = N.length; p < g; p++) {
        s = N[p];
        c = this._createStaticMesh(s);
        if (c != null) {
            c.name = e.name != null ? e.name : "";
            f.push(c)
        }
    }
    C = e.controllers;
    for (d = 0, y = C.length; d < y; d++) {
        i = C[d];
        c = this._createAnimatedMesh(i);
        if (c != null) {
            c.name = e.name != null ? e.name : "";
            f.push(c)
        }
    }
    k = e.lights;
    for (v = 0, b = k.length; v < b; v++) {
        o = k[v];
        l = this._createLight(o);
        if (l != null) {
            l.name = e.name != null ? e.name : "";
            f.push(l)
        }
    }
    L = e.cameras;
    for (m = 0, w = L.length; m < w; m++) {
        n = L[m];
        u = this._createCamera(n);
        if (u != null) {
            u.name = e.name != null ? e.name : "";
            f.push(u)
        }
    }
    if (f.length > 1) {
        h = new THREE.Object3D;
        for (x = 0, E = f.length; x < E; x++) {
            a = f[x];
            if (a != null) {
                h.add(a)
            }
        }
        t.add(h)
    } else if (f.length === 1) {
        h = f[0];
        t.add(h)
    } else if (f.length === 0) {
        if (e.type !== "JOINT") {
            ColladaLoader2._log("Collada node " + e.name + " did not produce any threejs nodes", ColladaLoader2.messageWarning)
        }
        h = new THREE.Object3D;
        t.add(h)
    }
    this._setNodeTransformation(e, h);
    A = e.children;
    for (T = 0, S = A.length; T < S; T++) {
        r = A[T];
        this._createSceneGraphNode(r, h)
    }
};
ColladaLoader2.File.prototype._createLight = function (e) {
    var t, n, r, i, s, o, u, a, f, l, c, h, p;
    u = ColladaLoader2.Light.fromLink(e.light);
    if (u == null) {
        ColladaLoader2._log("Light instance has no light, light ignored", ColladaLoader2.messageWarning);
        return null
    }
    if (u.color == null) {
        ColladaLoader2._log("Light has no color, using white", ColladaLoader2.messageWarning);
        i = 16777215
    } else {
        i = ColladaLoader2._colorToHex(u.color)
    }
    t = (f = u.params["constant_attenuation"]) != null ? f.value : void 0;
    n = (l = u.params["linear_attenuation"]) != null ? l.value : void 0;
    r = (c = u.params["quadratic_attenuation"]) != null ? c.value : void 0;
    s = (h = u.params["falloff_angle"]) != null ? h.value : void 0;
    o = (p = u.params["falloff_exponent"]) != null ? p.value : void 0;
    a = null;
    switch (u.type) {
        case"ambient":
            a = new THREE.AmbientLight(i);
            break;
        case"directional":
            a = new THREE.DirectionalLight(i, 1);
            break;
        case"point":
            a = new THREE.PointLight(i, t, n);
            break;
        case"spot":
            a = new THREE.SpotLight(i, t, n, s, o);
            break;
        default:
            ColladaLoader2._log("Unknown light type " + u.type + ", light ignored.", ColladaLoader2.messageError)
    }
    return a
};
ColladaLoader2.File.prototype._createCamera = function (e) {
    var t, n, r, i, s, o, u, a, f, l, c, h, p, d, v, m;
    n = ColladaLoader2.Camera.fromLink(e.camera);
    if (n == null) {
        ColladaLoader2._log("Camera instance has no camera, camera ignored", ColladaLoader2.messageWarning);
        return null
    }
    s = (l = n.params["xmag"]) != null ? l.value : void 0;
    u = (c = n.params["ymag"]) != null ? c.value : void 0;
    i = (h = n.params["xfov"]) != null ? h.value : void 0;
    o = (p = n.params["yfov"]) != null ? p.value : void 0;
    t = (d = n.params["aspect_ratio"]) != null ? d.value : void 0;
    f = (v = n.params["znear"]) != null ? v.value : void 0;
    a = (m = n.params["zfar"]) != null ? m.value : void 0;
    r = null;
    switch (n.type) {
        case"orthographic":
            if (s != null && u != null) {
                t = s / u
            } else if (u != null && t != null) {
                s = u * t
            } else if (s != null && t != null) {
                u = s / t
            } else if (s != null) {
                t = 1;
                u = s
            } else if (u != null) {
                t = 1;
                s = u
            } else {
                ColladaLoader2._log("Not enough field of view parameters for an orthographic camera.", ColladaLoader2.messageError);
                return null
            }
            r = new THREE.OrthographicCamera(-s, +s, -u, +u, f, a);
            break;
        case"perspective":
            if (i != null && o != null) {
                t = i / o
            } else if (o != null && t != null) {
                i = o * t
            } else if (i != null && t != null) {
                o = i / t
            } else if (i != null) {
                t = 1;
                o = i
            } else if (o != null) {
                t = 1;
                i = o
            } else {
                ColladaLoader2._log("Not enough field of view parameters for a perspective camera.", ColladaLoader2.messageError);
                return null
            }
            r = new THREE.PerspectiveCamera(o, t, f, a);
            break;
        default:
            ColladaLoader2._log("Unknown camera type " + n.type + ", camera ignored.", ColladaLoader2.messageError)
    }
    return r
};
ColladaLoader2.File.prototype._createStaticMesh = function (e) {
    var t, n, r, i, s;
    t = ColladaLoader2.Geometry.fromLink(e.geometry);
    if (t == null) {
        ColladaLoader2._log("Geometry instance has no geometry, mesh ignored", ColladaLoader2.messageWarning);
        return null
    }
    n = this._createGeometryAndMaterial(t, e.materials);
    i = n.geometry;
    s = n.material;
    r = new THREE.Mesh(i, s);
    return r
};
ColladaLoader2.File.prototype._createGeometryAndMaterial = function (e, t) {
    var n, r, i, s, o, u, a;
    s = this._createMaterials(t);
    r = this._createGeometry(e, s);
    i = null;
    if (s.materials.length > 1) {
        i = new THREE.MeshFaceMaterial;
        a = s.materials;
        for (o = 0, u = a.length; o < u; o++) {
            n = a[o];
            i.materials.push(n)
        }
    } else if (s.materials.length > 0) {
        i = s.materials[0]
    } else {
        i = this._createDefaultMaterial()
    }
    return{geometry: r, material: i}
};
ColladaLoader2.File.prototype._createAnimatedMesh = function (e) {
    var t;
    t = ColladaLoader2.Controller.fromLink(e.controller);
    if (t == null) {
        ColladaLoader2._log("Controller not found, mesh ignored", ColladaLoader2.messageWarning);
        return null
    }
    if (t.skin != null) {
        return this._createSkinMesh(e, t)
    }
    if (t.morph != null) {
        return this._createMorphMesh(e, t)
    }
    ColladaLoader2._log("Controller has neither a skin nor a morph, mesh ignored", ColladaLoader2.messageWarning);
    return null
};
ColladaLoader2.File.prototype._createSkinMesh = function (e, t) {
    var n, r, i, s, o, u, a, f, l, c, h, p, d, v, m, g, y, b, w, E, S, x, T, N, C, k, L, A, O, M, _;
    o = t.skin;
    if (o == null) {
        ColladaLoader2._log("Controller for a skinned mesh has no skin, mesh ignored", ColladaLoader2.messageError);
        return null
    }
    u = ColladaLoader2.Geometry.fromLink(o.source);
    if (u == null) {
        ColladaLoader2._log("Skin for a skinned mesh has no geometry, mesh ignored", ColladaLoader2.messageError);
        return null
    }
    if (!this._options["useAnimations"]) {
        a = this._createGeometryAndMaterial(u, e.materials);
        y = a.geometry;
        b = a.material;
        return new THREE.Mesh(y, b)
    }
    g = [];
    L = e.skeletons;
    for (w = 0, T = L.length; w < T; w++) {
        m = L[w];
        v = ColladaLoader2.VisualSceneNode.fromLink(m);
        if (v == null) {
            ColladaLoader2._log("Controller instance for a skinned mesh uses unknown skeleton " + v + ", skeleton ignored", ColladaLoader2.messageError);
            continue
        }
        g.push(v)
    }
    if (g.length === 0) {
        ColladaLoader2._log("Controller instance for a skinned mesh has no skeleton, mesh ignored", ColladaLoader2.messageError);
        return null
    }
    h = o.joints;
    if (h == null) {
        ColladaLoader2._log("Skin has no joints, mesh ignored", ColladaLoader2.messageError);
        return null
    }
    s = ColladaLoader2.Source.fromLink((A = h.joints) != null ? A.source : void 0);
    if (s == null || s.data == null) {
        ColladaLoader2._log("Skin has no joints source, mesh ignored", ColladaLoader2.messageError);
        return null
    }
    i = ColladaLoader2.Source.fromLink((O = h.invBindMatrices) != null ? O.source : void 0);
    if (i == null || i.data == null) {
        ColladaLoader2._log("Skin has no inverse bind matrix source, mesh ignored", ColladaLoader2.messageError);
        return null
    }
    if (s.data.length * 16 !== i.data.length) {
        ColladaLoader2._log("Skin has an inconsistent length of joint data sources, mesh ignored", ColladaLoader2.messageError);
        return null
    }
    if (!(i.data instanceof Float32Array)) {
        ColladaLoader2._log("Skin inverse bind matrices use a non-numeric data source, mesh ignored", ColladaLoader2.messageError);
        return null
    }
    r = [];
    M = s.data;
    for (E = 0, N = M.length; E < N; E++) {
        c = M[E];
        l = this._findJointNode(c, g);
        if (l == null) {
            ColladaLoader2._log("Joint " + c + " not found for skin with skeletons " + g.map(function (e) {
                return e.id
            }).join(", ") + ", mesh ignored", ColladaLoader2.messageError);
            return null
        }
        n = this._createBone(l, c, r);
        ColladaLoader2._fillMatrix4RowMajor(i.data, n.index * 16, n.invBindMatrix)
    }
    if (this._options["verboseMessages"]) {
        ColladaLoader2._log("Skin contains " + r.length + " bones", ColladaLoader2.messageInfo)
    }
    f = 0;
    while (f < r.length) {
        n = r[f];
        f = f + 1;
        for (S = 0, C = r.length; S < C; S++) {
            d = r[S];
            if (n.node.parent === d.node) {
                n.parent = d;
                break
            }
        }
        if (n.node.parent != null && n.node.parent instanceof ColladaLoader2.VisualSceneNode && n.parent == null) {
            n.parent = this._createBone(n.node.parent, "", r)
        }
    }
    if (this._options["verboseMessages"]) {
        ColladaLoader2._log("Skeleton contains " + r.length + " bones", ColladaLoader2.messageInfo)
    }
    if (o.vertexWeights == null) {
        ColladaLoader2._log("Skin has no vertex weight data, mesh ignored", ColladaLoader2.messageError);
        return null
    }
    if (o.vertexWeights.joints.source.url !== o.joints.joints.source.url) {
        ColladaLoader2._log("Skin uses different data sources for joints in <joints> and <vertex_weights>, this is not supported by this loader, mesh ignored", ColladaLoader2.messageError);
        return null
    }
    a = this._createGeometryAndMaterial(u, e.materials);
    y = a.geometry;
    b = a.material;
    if (this._options["convertSkinsToMorphs"]) {
        if (this._addSkinMorphTargets(y, o, r, b)) {
            return new THREE.MorphAnimMesh(y, b)
        } else {
            return new THREE.Mesh(y, b)
        }
    } else {
        if (this._addSkinBones(y, o, r, b)) {
            p = new THREE.SkinnedMesh(y, b);
            p.boneInverses = [];
            _ = y.bones;
            for (x = 0, k = _.length; x < k; x++) {
                n = _[x];
                p.boneInverses.push(n.inverse)
            }
            return p
        } else {
            return new THREE.Mesh(y, b)
        }
    }
};
ColladaLoader2.File.prototype._findJointNode = function (e, t) {
    var n, r, i, s, o;
    n = null;
    for (s = 0, o = t.length; s < o; s++) {
        i = t[s];
        r = e.split("/");
        n = ColladaLoader2.SidLink.findSidTarget(e, i, r);
        if (n != null) {
            break
        }
    }
    if (n instanceof ColladaLoader2.VisualSceneNode) {
        return n
    } else {
        return null
    }
};
ColladaLoader2.File.prototype._createBone = function (e, t, n) {
    var r, i, s, o, u;
    r = new ColladaLoader2.ThreejsSkeletonBone;
    r.sid = t;
    r.node = e;
    u = e.transformations;
    for (s = 0, o = u.length; s < o; s++) {
        i = u[s];
        if (i.animTarget.channels.length > 0) {
            r.isAnimated = true;
            break
        }
    }
    r.matrix = new THREE.Matrix4;
    e.getTransformMatrix(r.matrix);
    r.index = n.length;
    n.push(r);
    return r
};
ColladaLoader2.File.prototype._addSkinMorphTargets = function (e, t, n, r) {
    var i, s, o, u, a, f, l, c, h, p, d, v, m, g, y, b, w, E, S, x, T, N, C, k, L, A, O, M, _, D, P, H, B, j;
    m = this._prepareAnimations(n);
    if (!m > 0) {
        return false
    }
    p = e.vertices;
    b = p.length;
    T = t.vertexWeights.v;
    N = t.vertexWeights.vcount;
    x = ColladaLoader2.Source.fromLink(t.vertexWeights.joints.source);
    k = ColladaLoader2.Source.fromLink(t.vertexWeights.weights.source);
    S = x != null ? x.data : void 0;
    C = k != null ? k.data : void 0;
    if (C == null) {
        ColladaLoader2._log("Skin has no weights data, no morph targets added for mesh", ColladaLoader2.messageError);
        return false
    }
    i = new THREE.Matrix4;
    if (t.bindShapeMatrix != null) {
        i = ColladaLoader2._floatsToMatrix4RowMajor(t.bindShapeMatrix, 0)
    }
    v = new THREE.Vector3;
    l = true;
    f = true;
    for (c = O = 0, B = m - 1; O <= B; c = O += 1) {
        this._updateSkinMatrices(n, i, c);
        w = [];
        for (M = 0, P = p.length; M < P; M++) {
            d = p[M];
            w.push(new THREE.Vector3)
        }
        E = 0;
        for (c = _ = 0, H = w.length; _ < H; c = ++_) {
            y = w[c];
            h = p[c];
            A = N[c];
            g = 0;
            for (L = D = 0, j = A - 1; D <= j; L = D += 1) {
                o = T[E];
                a = T[E + 1];
                E += 2;
                u = C[a];
                g += u;
                if (o >= 0) {
                    s = n[o];
                    v.copy(h);
                    v.applyMatrix4(s.skinMatrix);
                    v.multiplyScalar(u);
                    y.add(v)
                } else {
                    v.copy(h);
                    v.applyMatrix4(i);
                    v.multiplyScalar(u);
                    y.add(v)
                }
            }
            if (A === 0) {
                y.copy(h);
                if (l) {
                    ColladaLoader2._log("Skinned vertex not influenced by any bone, some vertices will be unskinned", ColladaLoader2.messageWarning);
                    l = false
                }
            } else if (!(.01 < g && g < 1e6)) {
                y.copy(h);
                if (f) {
                    ColladaLoader2._log("Zero or infinite total weight for skinned vertex, some vertices will be unskinned", ColladaLoader2.messageWarning);
                    f = false
                }
            } else {
                y.multiplyScalar(1 / g)
            }
        }
        if (E !== T.length) {
            ColladaLoader2._log("Skinning did not consume all weights", ColladaLoader2.messageError)
        }
        e.morphTargets.push({name: "target", vertices: w})
    }
    e.computeMorphNormals();
    this._materialEnableMorphing(r);
    return true
};
ColladaLoader2.File.prototype._materialEnableMorphing = function (e) {
    var t, n, r, i;
    if (e instanceof THREE.MeshFaceMaterial) {
        i = e.materials;
        for (n = 0, r = i.length; n < r; n++) {
            t = i[n];
            t.morphTargets = true;
            t.morphNormals = true
        }
    } else {
        e.morphTargets = true;
        e.morphNormals = true
    }
};
ColladaLoader2.File.prototype._materialEnableSkinning = function (e) {
    var t, n, r, i;
    if (e instanceof THREE.MeshFaceMaterial) {
        i = e.materials;
        for (n = 0, r = i.length; n < r; n++) {
            t = i[n];
            t.skinning = true
        }
    } else {
        e.skinning = true
    }
};
ColladaLoader2.File.prototype._prepareAnimations = function (e) {
    var t, n, r, i, s, o, u, a, f, l, c, h, p, d;
    s = null;
    for (u = 0, l = e.length; u < l; u++) {
        t = e[u];
        i = false;
        p = t.node.transformations;
        for (a = 0, c = p.length; a < c; a++) {
            o = p[a];
            o.resetAnimation();
            o.selectAllAnimations();
            d = o.animTarget.activeChannels;
            for (f = 0, h = d.length; f < h; f++) {
                n = d[f];
                i = true;
                r = n.inputData.length;
                if (s != null && r !== s) {
                    ColladaLoader2._log("Inconsistent number of time steps, no morph targets added for mesh. Resample all animations to fix this.", ColladaLoader2.messageError);
                    return null
                }
                s = r
            }
        }
        if (this._options["verboseMessages"] && !i) {
            ColladaLoader2._log("Joint '" + t.sid + "' has no animation channel", ColladaLoader2.messageWarning)
        }
    }
    return s
};
ColladaLoader2.File.prototype._updateSkinMatrices = function (e, t, n) {
    var r, i, s, o, u;
    for (i = 0, o = e.length; i < o; i++) {
        r = e[i];
        r.applyAnimation(n)
    }
    for (s = 0, u = e.length; s < u; s++) {
        r = e[s];
        r.updateSkinMatrix(t)
    }
};
ColladaLoader2.File.prototype._addSkinBones = function (e, t, n, r) {
    var i, s, o, u, a, f, l, c, h, p, d, v, m, g, y, b, w, E, S, x, T, N, C, k, L, A, O, M, _, D, P, H, B, j, F, I, q, R, U, z, W, X, V, $, J, K, Q, G, Y, Z;
    C = this._prepareAnimations(n);
    if (!C > 0) {
        return false
    }
    b = e.vertices;
    A = b.length;
    D = t.vertexWeights.v;
    P = t.vertexWeights.vcount;
    _ = ColladaLoader2.Source.fromLink(t.vertexWeights.joints.source);
    B = ColladaLoader2.Source.fromLink(t.vertexWeights.weights.source);
    M = _ != null ? _.data : void 0;
    H = B != null ? B.data : void 0;
    if (H == null) {
        ColladaLoader2._log("Skin has no weights data, no skin added for mesh", ColladaLoader2.messageError);
        return false
    }
    i = new THREE.Matrix4;
    if (t.bindShapeMatrix != null) {
        i = ColladaLoader2._floatsToMatrix4RowMajor(t.bindShapeMatrix, 0)
    }
    m = new THREE.Vector3;
    g = new THREE.Quaternion;
    y = new THREE.Vector3;
    c = true;
    l = true;
    T = [];
    N = [];
    O = 0;
    f = 4;
    p = [0, 0, 0, 0];
    I = [0, 0, 0, 0];
    for (h = q = 0, W = b.length; q < W; h = ++q) {
        L = b[h];
        F = P[h];
        if (F > f) {
            if (c) {
                ColladaLoader2._log("Too many bones influence a vertex, some influences will be discarded. Threejs supports only " + f + " bones per vertex.", ColladaLoader2.messageWarning);
                c = false
            }
            F = f
        }
        k = 0;
        for (j = R = 0, Q = F - 1; R <= Q; j = R += 1) {
            o = D[O];
            a = D[O + 1];
            O += 2;
            u = H[a];
            k += u;
            p[j] = o;
            I[j] = u
        }
        for (j = U = I, G = f - 1; U <= G; j = U += 1) {
            p[j] = 0;
            I[j] = 0
        }
        if (!(.01 < k && k < 1e6)) {
            if (l) {
                ColladaLoader2._log("Zero or infinite total weight for skinned vertex, skin will be broken", ColladaLoader2.messageWarning);
                l = false
            }
        } else {
            for (j = z = 0, Y = f - 1; z <= Y; j = z += 1) {
                I[j] /= k
            }
        }
        T.push(new THREE.Vector4(p[0], p[1], p[2], p[3]));
        N.push(new THREE.Vector4(I[0], I[1], I[2], I[3]))
    }
    e.skinIndices = T;
    e.skinWeights = N;
    x = [];
    for ($ = 0, X = n.length; $ < X; $++) {
        s = n[$];
        E = {};
        if (s.parent != null) {
            E["parent"] = s.parent.index
        } else {
            E["parent"] = -1
        }
        s.matrix.decompose(m, g, y);
        E["name"] = s.node.name;
        E["pos"] = [m.x, m.y, m.z];
        E["scl"] = [y.x, y.y, y.z];
        E["rotq"] = [g.x, g.y, g.z, g.w];
        E["rot"] = null;
        E.inverse = new THREE.Matrix4;
        E.inverse.multiplyMatrices(s.invBindMatrix, i);
        x.push(E)
    }
    e.bones = x;
    w = {name: "animation", hierarchy: [], fps: 30, length: C - 1};
    e.animation = w;
    for (J = 0, V = n.length; J < V; J++) {
        s = n[J];
        S = {parent: s.index, keys: []};
        w["hierarchy"].push(S);
        for (v = K = 0, Z = C - 1; K <= Z; v = K += 1) {
            s.applyAnimation(v);
            s.updateSkinMatrix(i);
            s.matrix.decompose(m, g, y);
            d = {time: v, pos: [m.x, m.y, m.z], scl: [y.x, y.y, y.z], rot: [g.x, g.y, g.z, g.w]};
            S["keys"].push(d)
        }
    }
    this._materialEnableSkinning(r);
    return true
};
ColladaLoader2.File.prototype._createMorphMesh = function (e, t) {
    ColladaLoader2._log("Morph animated meshes not supported, mesh ignored", ColladaLoader2.messageError);
    return null
};
ColladaLoader2.File.prototype._createGeometry = function (e, t) {
    var n, r, i, s, o, u;
    r = new THREE.Geometry;
    u = e.triangles;
    for (s = 0, o = u.length; s < o; s++) {
        i = u[s];
        if (i.material != null) {
            n = t.indices[i.material];
            if (n == null) {
                ColladaLoader2._log("Material symbol " + i.material + " has no bound material instance, using material with index 0", ColladaLoader2.messageError);
                n = 0
            }
        } else {
            ColladaLoader2._log("Missing material index, using material with index 0", ColladaLoader2.messageError);
            n = 0
        }
        this._addTrianglesToGeometry(e, i, n, r)
    }
    r.computeFaceNormals();
    r.computeCentroids();
    if (t.needTangents) {
        r.computeTangents()
    }
    r.computeBoundingBox();
    return r
};
ColladaLoader2.File.prototype._addTrianglesToGeometry = function (e, t, n, r) {
    var i, s, o, u, a, f, l, c, h, p, d, v, m, g, y, b, w, E, S, x, T, N, C, k, L, A, O, M, _, D, P, H, B, j, F, I, q, R, U, z, W, X, V, $, J, K, Q, G, Y, Z, et, tt, nt, rt, it, st, ot, ut, at, ft, lt, ct, ht, pt, dt, vt, mt, gt, yt, bt, wt, Et, St = this;
    N = null;
    x = null;
    S = null;
    T = [];
    yt = t.inputs;
    for (st = 0, ft = yt.length; st < ft; st++) {
        E = yt[st];
        switch (E.semantic) {
            case"VERTEX":
                N = E;
                break;
            case"NORMAL":
                x = E;
                break;
            case"COLOR":
                S = E;
                break;
            case"TEXCOORD":
                T.push(E);
                break;
            default:
                ColladaLoader2._log("Unknown triangles input semantic " + E.semantic + " ignored", ColladaLoader2.messageWarning)
        }
    }
    U = ColladaLoader2.Vertices.fromLink(N.source);
    if (U == null) {
        ColladaLoader2._log("Geometry " + e.id + " has no vertices", ColladaLoader2.messageError);
        return
    }
    q = ColladaLoader2.Source.fromLink(x != null ? x.source : void 0);
    I = ColladaLoader2.Source.fromLink(S != null ? S.source : void 0);
    R = T.map(function (e) {
        return ColladaLoader2.Source.fromLink(e != null ? e.source : void 0)
    });
    L = null;
    k = null;
    C = null;
    A = [];
    bt = U.inputs;
    for (ot = 0, lt = bt.length; ot < lt; ot++) {
        E = bt[ot];
        switch (E.semantic) {
            case"POSITION":
                L = E;
                break;
            case"NORMAL":
                k = E;
                break;
            case"COLOR":
                C = E;
                break;
            case"TEXCOORD":
                A.push(E);
                break;
            default:
                ColladaLoader2._log("Unknown vertices input semantic " + E.semantic + " ignored", ColladaLoader2.messageWarning)
        }
    }
    X = ColladaLoader2.Source.fromLink(L.source);
    if (X == null) {
        ColladaLoader2._log("Geometry " + e.id + " has no vertex positions", ColladaLoader2.messageError);
        return
    }
    W = ColladaLoader2.Source.fromLink(k != null ? k.source : void 0);
    z = ColladaLoader2.Source.fromLink(C != null ? C.source : void 0);
    V = A.map(function (e) {
        return ColladaLoader2.Source.fromLink(e != null ? e.source : void 0)
    });
    v = this._createVector3Array(X);
    d = this._createVector3Array(W);
    c = this._createVector3Array(q);
    p = this._createColorArray(z);
    l = this._createColorArray(I);
    m = V.map(function (e) {
        return St._createUVArray(e)
    });
    h = R.map(function (e) {
        return St._createUVArray(e)
    });
    r.vertices = v;
    F = m.length + h.length;
    B = r.faceVertexUvs.length;
    j = t.count;
    H = r.faces.length;
    wt = r.faceVertexUvs;
    for (b = ut = 0, ct = wt.length; ut < ct; b = ++ut) {
        y = wt[b];
        if (b < F) {
            O = y.length - r.faces.length;
            this._addEmptyUVs(y, O)
        } else {
            O = y.length - r.faces.length + j;
            this._addEmptyUVs(y, O)
        }
    }
    while (r.faceVertexUvs.length < F) {
        y = [];
        this._addEmptyUVs(y, H);
        r.faceVertexUvs.push(y)
    }
    if (t.type !== "triangles") {
        rt = t.vcount;
        for (at = 0, ht = rt.length; at < ht; at++) {
            u = rt[at];
            if (u !== 3) {
                ColladaLoader2._log("Geometry " + e.id + " has non-triangle polygons, geometry ignored", ColladaLoader2.messageError);
                return
            }
        }
    }
    w = t.indices;
    Z = w.length / t.count;
    it = Z / 3;
    for (Y = vt = 0, Et = t.count - 1; vt <= Et; Y = vt += 1) {
        G = Y * Z;
        i = G + 0 * it;
        s = G + 1 * it;
        o = G + 2 * it;
        et = w[i + N.offset];
        tt = w[s + N.offset];
        nt = w[o + N.offset];
        if (d != null) {
            P = [d[et], d[tt], d[nt]]
        } else if (c != null) {
            M = w[i + x.offset];
            _ = w[s + x.offset];
            D = w[o + x.offset];
            P = [c[M], c[_], c[D]]
        } else {
            P = null
        }
        if (p != null) {
            a = [p[et], p[tt], p[nt]]
        } else if (l != null) {
            M = w[i + S.offset];
            _ = w[s + S.offset];
            D = w[o + S.offset];
            a = [l[M], l[_], l[D]]
        } else {
            a = null
        }
        g = new THREE.Face3(et, tt, nt, P, a);
        if (n != null) {
            g.materialIndex = n
        }
        r.faces.push(g);
        for (b = mt = 0, pt = m.length; mt < pt; b = ++mt) {
            f = m[b];
            if (f == null) {
                r.faceVertexUvs[b].push("abv");
                r.faceVertexUvs[b].push([new THREE.Vector2(0, 0), new THREE.Vector2(0, 0), new THREE.Vector2(0, 0)])
            } else {
                Q = [f[et], f[tt], f[nt]];
                r.faceVertexUvs[b].push(Q)
            }
        }
        for (b = gt = 0, dt = h.length; gt < dt; b = ++gt) {
            f = h[b];
            if (f == null) {
                r.faceVertexUvs[b].push([new THREE.Vector2(0, 0), new THREE.Vector2(0, 0), new THREE.Vector2(0, 0)])
            } else {
                $ = w[i + T[b].offset];
                J = w[s + T[b].offset];
                K = w[o + T[b].offset];
                Q = [f[$], f[J], f[K]];
                r.faceVertexUvs[b].push(Q)
            }
        }
    }
};
ColladaLoader2.File.prototype._addEmptyUVs = function (e, t) {
    var n, r, i;
    for (n = r = 0, i = t - 1; r <= i; n = r += 1) {
        e.push(new THREE.Vector2(0, 0))
    }
};
ColladaLoader2.File.prototype._createVector3Array = function (e) {
    var t, n, r, i, s;
    if (e == null) {
        return null
    }
    if (e.stride !== 3) {
        ColladaLoader2._log("Vector source data does not contain 3D vectors", ColladaLoader2.messageError);
        return null
    }
    t = [];
    r = e.data;
    for (n = i = 0, s = r.length - 1; i <= s; n = i += 3) {
        t.push(new THREE.Vector3(r[n], r[n + 1], r[n + 2]))
    }
    return t
};
ColladaLoader2.File.prototype._createColorArray = function (e) {
    var t, n, r, i, s, o;
    if (e == null) {
        return null
    }
    if (e.stride < 3) {
        ColladaLoader2._log("Color source data does not contain 3+D vectors", ColladaLoader2.messageError);
        return null
    }
    t = [];
    r = e.data;
    for (n = i = 0, s = r.length - 1, o = e.stride; o > 0 ? i <= s : i >= s; n = i += o) {
        t.push((new THREE.Color).setRGB(r[n], r[n + 1], r[n + 2]))
    }
    return t
};
ColladaLoader2.File.prototype._createUVArray = function (e) {
    var t, n, r, i, s, o;
    if (e == null) {
        return null
    }
    if (e.stride < 2) {
        ColladaLoader2._log("UV source data does not contain 2+D vectors", ColladaLoader2.messageError);
        return null
    }
    t = [];
    r = e.data;
    for (n = i = 0, s = r.length - 1, o = e.stride; o > 0 ? i <= s : i >= s; n = i += o) {
        t.push(new THREE.Vector2(r[n], 1 - r[n + 1]))
    }
    return t
};
ColladaLoader2.File.prototype._createMaterials = function (e) {
    var t, n, r, i, s, o, u;
    r = new ColladaLoader2.ThreejsMaterialMap;
    n = 0;
    for (o = 0, u = e.length; o < u; o++) {
        t = e[o];
        i = t.symbol;
        if (i == null) {
            ColladaLoader2._log("Material instance has no symbol, material skipped.", ColladaLoader2.messageError);
            continue
        }
        if (r.indices[i] != null) {
            ColladaLoader2._log("Geometry instance tried to map material symbol " + i + " multiple times", ColladaLoader2.messageError);
            continue
        }
        s = this._createMaterial(t);
        if (s.bumpMap != null || s.normalMap != null) {
            r.needTangents = true
        }
        this.threejs.materials.push(s);
        r.materials.push(s);
        r.indices[i] = n++
    }
    return r
};
ColladaLoader2.File.prototype._createMaterial = function (e) {
    var t, n;
    n = ColladaLoader2.Material.fromLink(e.material);
    if (n == null) {
        ColladaLoader2._log("Material not found, using default material", ColladaLoader2.messageWarning);
        return this._createDefaultMaterial()
    }
    t = ColladaLoader2.Effect.fromLink(n.effect);
    if (t == null) {
        ColladaLoader2._log("Material effect not found, using default material", ColladaLoader2.messageWarning);
        return this._createDefaultMaterial()
    }
    return this._createBuiltInMaterial(t)
};
ColladaLoader2.File.prototype._createShaderMaterial = function (e) {
    var t, n, r, i, s, o, u, a;
    r = e.technique;
    n = THREE.ShaderUtils.lib["normal"];
    a = THREE.UniformsUtils.clone(n.uniforms);
    o = this._loadThreejsTexture(r.bump);
    if (o != null) {
        a["tNormal"].texture = o;
        a["uNormalScale"].value = .85
    }
    i = this._loadThreejsTexture(r.diffuse);
    if (i != null) {
        a["tDiffuse"].texture = i;
        a["enableDiffuse"].value = true
    } else {
        a["enableDiffuse"].value = false
    }
    u = this._loadThreejsTexture(r.specular);
    if (u != null) {
        a["tSpecular"].texture = u;
        a["enableSpecular"].value = true
    } else {
        a["enableSpecular"].value = false
    }
    s = this._loadThreejsTexture(r.emission);
    if (s != null) {
        a["tAO"].texture = s;
        a["enableAO"].value = true
    } else {
        a["enableAO"].value = false
    }
    this._setUniformColor(a, "uDiffuseColor", r.diffuse);
    this._setUniformColor(a, "uSpecularColor", r.specular);
    this._setUniformColor(a, "uAmbientColor", r.ambient);
    if (r.shininess != null) {
        a["uShininess"].value = r.shininess
    }
    if (this._hasTransparency(e)) {
        a["uOpacity"].value = this._getOpacity(e)
    }
    t = new THREE.ShaderMaterial({fragmentShader: n.fragmentShader, vertexShader: n.vertexShader, uniforms: a, lights: true});
    return t
};
ColladaLoader2.File.prototype._setUniformColor = function (e, t, n) {
    if (n != null && n.color != null) {
        e[t].value.setHex(ColladaLoader2._colorToHex(n.color))
    }
};
ColladaLoader2.File.prototype._getOpacity = function (e) {
    var t, n, r, i, s, o;
    n = e.technique;
    i = n.transparent;
    t = i != null ? i.opaque : void 0;
    if (t != null && t !== "A_ONE") {
        ColladaLoader2._log("Opacity mode " + t + " not supported, transparency will be broken", ColladaLoader2.messageWarning)
    }
    if ((i != null ? i.textureSampler : void 0) != null) {
        ColladaLoader2._log("Separate transparency texture not supported, transparency will be broken", ColladaLoader2.messageWarning)
    }
    s = (i != null ? (o = i.color) != null ? o[3] : void 0 : void 0) || 1;
    r = n.transparency || 1;
    return s * r
};
ColladaLoader2.File.prototype._hasTransparency = function (e) {
    var t, n, r;
    t = e.technique;
    r = t.transparent;
    n = t.transparency;
    return(r != null ? r.textureSampler : void 0) != null || n != null && n !== 1
};
ColladaLoader2.File.prototype._isDoubleSided = function (e) {
    var t, n;
    n = e.technique;
    if (n.double_sided != null) {
        return n.double_sided
    }
    t = this._getDoubleSidedParam(e.params);
    if (t != null) {
        return t
    }
    t = this._getDoubleSidedParam(e.technique.params);
    if (t != null) {
        return t
    }
    return false
};
ColladaLoader2.File.prototype._getDoubleSidedParam = function (e) {
    var t, n, r;
    for (n = 0, r = e.length; n < r; n++) {
        t = e[n];
        if (t.semantic === "DOUBLE_SIDED") {
            if (t.floats != null) {
                return t.floats[0] > 0
            } else {
                ColladaLoader2._log("Missing value for DOUBLE_SIDED parameter, assuming 'true'", ColladaLoader2.messageWarning);
                return true
            }
        }
    }
    return null
};
ColladaLoader2.File.prototype._createBuiltInMaterial = function (e) {
    var t, n, r, i;
    i = e.technique;
    r = {};
    this._setThreejsMaterialColor(r, i.diffuse, "diffuse", "map", false);
    this._setThreejsMaterialColor(r, i.emission, "emissive", null, false);
    this._setThreejsMaterialColor(r, i.ambient, "ambient", "lightMap", false);
    this._setThreejsMaterialColor(r, i.specular, "specular", "specularMap", false);
    this._setThreejsMaterialColor(r, i.bump, null, "normalMap", false);
    if (r["bumpMap"]) {
        r["bumpScale"] = 1
    }
    if (r["normalMap"]) {
        r["normalScale"] = new THREE.Vector2(1, 1)
    }
    if (r["map"] != null) {
        r["diffuse"] = 16777215
    }
    if (r["specularMap"] != null) {
        r["specular"] = 16777215
    }
    if (r["diffuse"] == null) {
        r["diffuse"] = 16777215
    }
    if (i.shininess != null) {
        r["shininess"] = i.shininess
    }
    if (i.reflectivity != null) {
        r["reflectivity"] = i.reflectivity
    }
    t = this._hasTransparency(e);
    if (t) {
        r["transparent"] = true;
        n = this._getOpacity(e);
        r["opacity"] = n;
        r["alphaTest"] = .001
    }
    if (this._isDoubleSided(e)) {
        r["side"] = THREE.DoubleSide
    }
    r["shading"] = THREE.SmoothShading;
    r["perPixel"] = true;
    switch (i.shading) {
        case"blinn":
        case"phong":
            r["color"] = r["diffuse"];
            return new THREE.MeshPhongMaterial(r);
        case"lambert":
            r["color"] = r["diffuse"];
            return new THREE.MeshLambertMaterial(r);
        case"constant":
            r["color"] = r["emission"];
            return new THREE.MeshBasicMaterial(r);
        default:
            return this._createDefaultMaterial()
    }
};
ColladaLoader2.File.prototype._createDefaultMaterial = function () {
    return new THREE.MeshLambertMaterial({color: 14540253, shading: THREE.FlatShading})
};
ColladaLoader2.File.prototype._setThreejsMaterialColor = function (e, t, n, r, i) {
    var s;
    if (t == null) {
        return
    }
    if (t.color != null && n != null) {
        if (!i && e[n] != null) {
            return
        }
        e[n] = ColladaLoader2._colorToHex(t.color)
    } else if (t.textureSampler != null && r != null) {
        if (!i && e[r] != null) {
            return
        }
        s = this._loadThreejsTexture(t);
        if (s != null) {
            e[r] = s
        }
    }
};
ColladaLoader2.File.prototype._loadThreejsTexture = function (e) {
    var t, n, r, i, s;
    if (e.textureSampler == null) {
        return null
    }
    i = ColladaLoader2.EffectParam.fromLink(e.textureSampler);
    if (i == null) {
        ColladaLoader2._log("Texture sampler not found, texture will be missing", ColladaLoader2.messageWarning);
        return null
    }
    i = i.sampler;
    if (i == null) {
        ColladaLoader2._log("Texture sampler param has no sampler, texture will be missing", ColladaLoader2.messageWarning);
        return null
    }
    r = null;
    if (i.image != null) {
        r = ColladaLoader2.Image.fromLink(i.image);
        if (r == null) {
            ColladaLoader2._log("Texture image not found, texture will be missing", ColladaLoader2.messageWarning);
            return null
        }
    } else if (i.surface != null) {
        s = ColladaLoader2.EffectParam.fromLink(i.surface);
        if (s == null) {
            ColladaLoader2._log("Texture surface not found, texture will be missing", ColladaLoader2.messageWarning);
            return null
        }
        s = s.surface;
        if (s == null) {
            ColladaLoader2._log("Texture surface param has no surface, texture will be missing", ColladaLoader2.messageWarning);
            return null
        }
        r = ColladaLoader2.Image.fromLink(s.initFrom);
        if (r == null) {
            ColladaLoader2._log("Texture image not found, texture will be missing", ColladaLoader2.messageWarning);
            return null
        }
    }
    if (r.initFrom == null) {
        ColladaLoader2._log("Texture image has no source url, texture will be missing", ColladaLoader2.messageWarning);
        return null
    }
    t = this._baseUrl + r.initFrom;
    n = this._loader._loadTextureFromURL(t);
    return n
};
ColladaLoader2.prototype._imageCache;
ColladaLoader2.prototype.options;
ColladaLoader2.prototype._init = function () {
    this._imageCache = {};
    return this.options = {useAnimations: true, convertSkinsToMorphs: false, verboseMessages: false, localImageMode: false}
};
ColladaLoader2.prototype.setLog = function (e) {
    return ColladaLoader2.setLog(e)
};
ColladaLoader2.prototype.addCachedTextures = function (e) {
    var t, n;
    for (t in e) {
        n = e[t];
        this._imageCache[t] = n
    }
};
ColladaLoader2.prototype.load = function (e, t, n) {
    var r, i, s, o = this;
    r = 0;

    fs.readFile(e, function (err, data) {
	try{
       		var xml = new DOMParser().parseFromString(data.toString(),"text/plain");
       		o.parse(xml, t, e);
	}catch(ex){
		console.log("Error on parsing collada, is file corrupted ?");
        callback(0);
	}
    });

};
ColladaLoader2.prototype.parse = function (e, t, n) {
    var r;
    r = new ColladaLoader2.File(this);
    r.setUrl(n);
    r._readyCallback = t;
    r._parseXml(e);
    r._linkAnimations();
    r._createSceneGraph();
    if (r._readyCallback) {
        r._readyCallback(r)
    }
    return r
};
ColladaLoader2.prototype._loadTextureFromURL = function (e) {
    var t;
    t = this._imageCache[e];
    if (t != null) {
        return t
    }
    if (this.options.localImageMode) {
        t = this._loadImageLocal(e)
    }
    if (t == null) {
        t = this._loadImageSimple(e)
    }
    if (t != null) {
        this._imageCache[e] = t
    } else {
        ColladaLoader2._log("Texture " + e + " could not be loaded, texture will be ignored.", ColladaLoader2.messageError)
    }
    return t
};
ColladaLoader2.prototype._loadImageThreejs = function (e) {
    var t;
    t = THREE.ImageUtils.loadTexture(e);
    t.flipY = false;
    return t
};
ColladaLoader2.prototype._loadImageSimple = function (e) {
return null;
    var t, n;
    t = new Image;
    n = new THREE.Texture(t);
    n.flipY = false;
    n.wrapS = THREE.RepeatWrapping;
    n.wrapT = THREE.RepeatWrapping;
    t.onload = function () {
        return n.needsUpdate = true
    };
    t.crossOrigin = "anonymous";
    t.src = e;
    return n
};
ColladaLoader2.prototype._loadImageLocal = function (e) {
    var t, n, r, i, s, o, u;
    i = null;
    n = this._removeSameDirectoryPath(e);
    o = this._imageCache;
    for (r in o) {
        s = o[r];
        t = this._removeSameDirectoryPath(r);
        if (n.indexOf(t) >= 0) {
            i = s;
            break
        }
    }
    n = this._removeSameDirectoryPath(this._removeFileExtension(e));
    if (i == null) {
        u = this._imageCache;
        for (r in u) {
            s = u[r];
            t = this._removeSameDirectoryPath(this._removeFileExtension(r));
            if (n.indexOf(t) >= 0) {
                i = s;
                break
            }
        }
    }
    return i
};
ColladaLoader2.prototype._removeFileExtension = function (e) {
    return e.substr(0, e.lastIndexOf(".")) || e
};
ColladaLoader2.prototype._removeSameDirectoryPath = function (e) {
    return e.replace(/^.\//, "")
};
ColladaLoader2.messageTrace = 0;
ColladaLoader2.messageInfo = 1;
ColladaLoader2.messageWarning = 2;
ColladaLoader2.messageError = 3;
ColladaLoader2.messageTypes = ["TRACE", "INFO", "WARNING", "ERROR"];
ColladaLoader2.setLog = function (e) {
    ColladaLoader2._log = e || ColladaLoader2._colladaLogConsole
};
ColladaLoader2._colladaLogConsole = function (e, t) {
};
ColladaLoader2._log = ColladaLoader2._colladaLogConsole;
ColladaLoader2._reportUnexpectedChild = function (e) {
    ColladaLoader2._log("Skipped unknown element " + ColladaLoader2._getNodePath(e) + ".", ColladaLoader2.messageWarning)
};
ColladaLoader2._reportUnhandledExtra = function (e) {
    ColladaLoader2._log("Skipped element " + ColladaLoader2._getNodePath(e) + ". Element is legal, but not handled by this loader.", ColladaLoader2.messageWarning)
};
ColladaLoader2._reportInvalidTargetType = function (e, t) {
    ColladaLoader2._log("Link " + e.url + " does not point to a " + t.name, ColladaLoader2.messageError)
};
ColladaLoader2._getNodePath = function (e) {
    var t, n, r;
    r = "<" + e.nodeName + ">";
    t = 1;
    n = 10;
    while (e.parentNode != null) {
        e = e.parentNode;
        if (e.nodeName.toUpperCase() === "COLLADA") {
            break
        } else if (t >= n) {
            r = ".../" + r;
            break
        } else {
            r = "<" + e.nodeName + ">/" + r;
            t += 1
        }
    }
    return r
};
ColladaLoader2._strToStrings = function (e) {
    var t;
    if (e.length > 0) {
        t = e.trim();
        return t.split(/\s+/)
    } else {
        return[]
    }
};
ColladaLoader2._strToFloats = function (e) {
    var t, n, r, i, s, o;
    i = ColladaLoader2._strToStrings(e);
    t = new Float32Array(i.length);
    for (n = s = 0, o = i.length; s < o; n = ++s) {
        r = i[n];
        t[n] = parseFloat(r)
    }
    return t
};
ColladaLoader2._strToInts = function (e) {
    var t, n, r, i, s, o;
    i = ColladaLoader2._strToStrings(e);
    t = new Int32Array(i.length);
    for (n = s = 0, o = i.length; s < o; n = ++s) {
        r = i[n];
        t[n] = parseInt(r, 10)
    }
    return t
};
ColladaLoader2._strToBools = function (e) {
    var t, n, r, i, s, o, u;
    i = ColladaLoader2._strToStrings(e);
    t = new Uint8Array(i.length);
    for (n = s = 0, o = i.length; s < o; n = ++s) {
        r = i[n];
        t[n] = (u = r === "true" || r === "1") != null ? u : {1: 0}
    }
    return t
};
ColladaLoader2._strToColor = function (e) {
    var t;
    t = ColladaLoader2._strToFloats(e);
    if (t.length === 4) {
        return t
    } else {
        return null
    }
};
ColladaLoader2._colorToHex = function (e) {
    return Math.floor(e[0] * 255) << 16 ^ Math.floor(e[1] * 255) << 8 ^ Math.floor(e[2] * 255)
};
ColladaLoader2._floatsToMatrix4ColumnMajor = function (e, t) {
    return new THREE.Matrix4(e[0 + t], e[4 + t], e[8 + t], e[12 + t], e[1 + t], e[5 + t], e[9 + t], e[13 + t], e[2 + t], e[6 + t], e[10 + t], e[14 + t], e[3 + t], e[7 + t], e[11 + t], e[15 + t])
};
ColladaLoader2._floatsToMatrix4RowMajor = function (e, t) {
    return new THREE.Matrix4(e[0 + t], e[1 + t], e[2 + t], e[3 + t], e[4 + t], e[5 + t], e[6 + t], e[7 + t], e[8 + t], e[9 + t], e[10 + t], e[11 + t], e[12 + t], e[13 + t], e[14 + t], e[15 + t])
};
ColladaLoader2._fillMatrix4ColumnMajor = function (e, t, n) {
    n.set(e[0 + t], e[4 + t], e[8 + t], e[12 + t], e[1 + t], e[5 + t], e[9 + t], e[13 + t], e[2 + t], e[6 + t], e[10 + t], e[14 + t], e[3 + t], e[7 + t], e[11 + t], e[15 + t])
};
ColladaLoader2._fillMatrix4RowMajor = function (e, t, n) {
    n.set(e[0 + t], e[1 + t], e[2 + t], e[3 + t], e[4 + t], e[5 + t], e[6 + t], e[7 + t], e[8 + t], e[9 + t], e[10 + t], e[11 + t], e[12 + t], e[13 + t], e[14 + t], e[15 + t])
};
ColladaLoader2._checkMatrix4 = function (e) {
    var t, n, r, i;
    i = e.elements;
    if (i[3] !== 0 || i[7] !== 0 || i[11] !== 0 || i[15] !== 1) {
        throw new Error("Last row isnt [0,0,0,1]")
    }
    t = Math.sqrt(i[0] * i[0] + i[1] * i[1] + i[2] * i[2]);
    n = Math.sqrt(i[4] * i[4] + i[5] * i[5] + i[6] * i[6]);
    r = Math.sqrt(i[8] * i[8] + i[9] * i[9] + i[10] * i[10]);
    if (t < .9 || t > 1.1) {
        throw new Error("First column has significant scaling")
    }
    if (n < .9 || n > 1.1) {
        throw new Error("Second column has significant scaling")
    }
    if (r < .9 || r > 1.1) {
        throw new Error("Third column has significant scaling")
    }
};
ColladaLoader2._floatsToVec3 = function (e) {
    return new THREE.Vector3(e[0], e[1], e[2])
};
ColladaLoader2.TO_RADIANS = Math.PI / 180;
if (typeof module !== "undefined" && module !== null) {
    module["exports"] = ColladaLoader2
} else if (typeof window !== "undefined" && window !== null) {
    window["ColladaLoader2"] = ColladaLoader2
}

THREE.ColladaLoader = ColladaLoader2;

