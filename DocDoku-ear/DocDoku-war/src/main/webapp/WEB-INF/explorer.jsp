<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <fmt:setBundle basename="com.docdoku.server.localization.explorer_resource"/>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
        <meta name="gwt:property" content="locale=<%=request.getLocale()%>"/>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title><fmt:message key="title"/></title>
        <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>

		<link rel="stylesheet/less" href="<%=request.getContextPath()%>/less/style.less">

		<script src="<%=request.getContextPath()%>/js/lib/date.format.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/less-1.2.1.min.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/mustache-0.5.0-dev.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/jquery-1.7.1.min.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/underscore-1.3.1.min.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/backbone-0.9.1.min.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/bootstrap-2.0.0.min.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/workspace.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/folder.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/folder_root.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/tag.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/document.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/document_iteration.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/template.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/workflow.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/folder.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/folder_root.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/tag.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/document.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/document_root.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/document_checkedout.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/document_iteration.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/document_tag.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/template.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/workflow.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/base.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/modal.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/list.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/alert.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/workspace.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/folder.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/folder_new.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/folder_edit.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/tag_listitem.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/tag_list.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/document_new.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/document_new_template_list.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/document_new_attributes.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/document_listitem.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/document_list.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/document_checkedout_listitem.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/document_checkedout_list.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/document_tag_list.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/template_listitem.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/template_list.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/workflow_listitem.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/workflow_list.js"></script>
		<script src="<%=request.getContextPath()%>/js/router.js"></script>
		<script src="<%=request.getContextPath()%>/js/app.js"></script>
		<script src="<%=request.getContextPath()%>/js/i18n.js"></script>
		<script type="text/javascript">
			$(document).ready(function () {
				app.init({
					workspaceId: "${workspaceID}",
					login: "${login}"
				});
			});
		</script>   
    </head>    
    <body>
        <%@ include file="/WEB-INF/explorer_header.jspf" %>
		<div id="workspace"></div>
		<script id="workspace-tpl" type="text/html">
			<nav>
				<ul class="nav nav-list">
					<li class="nav-header">{{model.id}}</li>
					<li>
						<ul id="folders-container"></ul>
						<div id="tags-container"></div>
					</li>
					<li class="nav-header">{{_.REFERENCES}}</li>
					<li>
						<div><!-- TODO: Remove this temporary hack -->
							<a href="#workflows">
								<span class="icon"></span>
								{{_.WORKFLOWS}}
							</a>
						</div>
						<div><!-- TODO: Remove this temporary hack -->
							<a href="#templates">
								<span class="icon"></span>
								{{_.TEMPLATES}}
							</a>
						</div>
					</li>
					<li class="nav-header">{{_.LINKS}}</li>
					<li>
						<div><!-- TODO: Remove this temporary hack -->
							<a href="#checkedouts">
								<span class="icon"></span>
								{{_.CHECKOUTS}}
							</a>
						</div>
						<div><!-- TODO: Remove this temporary hack -->
							<a href="#tasks">
								<span class="icon"></span>
								{{_.TASKS}}
							</a>
						</div>
					</li>
				</ul>
			</nav>
			<div id="content"></div>
		</script>
		<script id="folder-tpl" type="text/html">
			<div class="header">
				<a class="name" href="#folders/{{model.id}}"
						data-toggle="collapse"
						data-target="#subfolders-{{view_cid}}">
					<span class="icon status"></span>
					<span class="icon type"></span>
					{{model.name}}
				</a>
				<div class="actions btn-group">
					<a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
						<span class="caret"></span>
					</a>
					<ul class="dropdown-menu">
						<li class="new-folder">
							<a><span class="icon"></span>{{_.NEW_FOLDER}}</a>
						</li>
						<li class="edit">
							<a><span class="icon"></span>{{_.RENAME}}</a>
						</li>
						<li class="delete">
							<a><span class="icon"></span>{{_.DELETE}}</a>
						</li>
						<li class="divider"></li>
						<li class="new-document">
							<a><span class="icon"></span>{{_.NEW_DOCUMENT}}</a>
						</li>
					</ul>
				</div>
			</div>
			<ul id="subfolders-{{view_cid}}" class="subfolders collapse"> </ul>
		</script>
		<script id="folder-new-tpl" type="text/html">
			<div class="modal new-folder">
				<div class="modal-header">
					<a class="close" data-dismiss="modal">×</a>
					<h3><span class="icon"></span>{{_.NEW_FOLDER}}</h3>
				</div>
				<div class="modal-body">
					<div class="alerts"></div>
					<form id="new-folder-form" class="form-horizontal">
						<label>{{_.NAME}}</label>
						<input class="name" type="text" value=""
							placeholder="{{_.FOLDER_S_NAME}}" />
					</form>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary" for="new-folder-form">
						{{_.CREATE}}
					</button>
					<button class="btn cancel">{{_.CANCEL}}</button>
				</div>
			</div>
		</script>
		<script id="folder-edit-tpl" type="text/html">
			<div class="modal edit-folder">
				<div class="modal-header">
					<a class="close" data-dismiss="modal">×</a>
					<h3><span class="icon"></span>{{_.RENAME}}</h3>
				</div>
				<div class="modal-body">
					<div class="alerts"></div>
					<form id="edit-folder-form">
						<label>Nom</label>
						<input class="name" type="text" value=""
							placeholder="Nom du dossier" />
					</form>
				</div>
				<div class="modal-footer">
					<button class="btn save btn-primary" for="edit-folder-form">
						{{_.SAVE}}
					</button>
					<button class="btn cancel">{{_.CANCEL}}</button>
				</div>
			</div>
		</script>
		<script id="document-list-tpl" type="text/html">
			<div class="actions">
				<button class="btn new" title="{{_.NEW_DOCUMENT}}">
					<span class="icon"></span>
				</button>
				<button class="btn delete" title="{{_.DELETE}}">
					<span class="icon"></span>
				</button>
				<span class="btn-group checkout-group">
					<button class="btn checkout" title="{{_.CHECKOUT}}">
						<span class="icon"></span>
					</button>
					<button class="btn undocheckout" title="{{_.CANCEL_CHECKOUT}}">
						<span class="icon"></span>
					</button>
					<button class="btn checkin" title="{{_.CHECKIN}}">
						<span class="icon"></span>
					</button>
				</span>
			</div>
			<table class="content table table-striped table-condensed">
				<thead>
					<tr>
						<th><input for="document-list-actions" type="checkbox" class="select" /></th>
						<th>{{_.REFERENCE}}</th>
						<th>{{_.VERSION}}</th>
						<th>{{_.ITERATION}}</th>
						<th>{{_.TYPE}}</th>
						<th>{{_.TITLE}}</th>
						<th>{{_.AUTHOR}}</th>
						<th>{{_.MODIFICATION_DATE}}</th>
						<th>{{_.CHECKOUT_DATE}}</th>
					</tr>
				</thead>
				<tbody class="items"></tbody>
			</table>
		</script>
		<script id="document-list-item-tpl" type="text/html">
			<td><input for="document-list-actions" type="checkbox" class="select" /></td>
			<td>{{model.reference}}</td>
			<td>{{model.version}}</td>
			<td>{{model.lastIteration.iteration}}</td>
			<td>{{model.type}}</td>
			<td>{{model.title}}</td>
			<td>{{model.lastIteration.author.name}}</td>
			<td>{{model.lastIteration.creationDate}}</td>
			<td>{{model.checkOutDate}}</td>
		</script>
		<script id="document-new-tpl" type="text/html">
			<div class="modal new-document">
				<div class="modal-header">
					<a class="close" data-dismiss="modal">×</a>
					<h3><span class="icon"></span>{{_.NEW_DOCUMENT}}</h3>
				</div>
				<div class="modal-body">
					<div class="alerts"></div>
					<div id="modal-form-tab">
						<ul class="nav nav-tabs">
							<li class="active"><a href="#modal-form-tab-main" data-toggle="tab">{{_.GENERAL}}</a></li>
							<li><a href="#modal-form-tab-attributes" data-toggle="tab">{{_.ATTRIBUTES}}</a></li>
						</ul>
						<div class="tab-content">
							<div class="tab-pane active" id="modal-form-tab-main">
								<form id="modal-form-template-list" class="form-horizontal">
								</form>
								<form id="modal-form" class="form-horizontal">
									<div class="control-group">
										<label class="control-label required" for="modal-form-reference">{{_.REFERENCE}}</label>
										<div class="controls">
											<input type="text" class="input-xlarge" id="modal-form-reference">
											<p class="help-block">{{_.DOCUMENT_S_REFERENCE}}</p>
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="modal-form-title">{{_.TITLE}}</label>
										<div class="controls">
											<input type="text" class="input-xlarge" id="modal-form-title">
											<p class="help-block">{{_.DOCUMENT_S_TITLE}}</p>
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="modal-form-description">{{_.DESCRIPTION}}</label>
										<div class="controls">
											<textarea type="text" class="input-xlarge" id="modal-form-description"></textarea>
											<p class="help-block">{{_.DOCUMENT_S_DESCRIPTION}}</p>
										</div>
									</div>
								</form>
							</div>
							<form id="modal-form-tab-attributes" class="tab-pane form-horizontal">
							</form>
						</div>
					</div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary" for="modal-form">
						{{_.CREATE}}
					</button>
					<button class="btn cancel">{{_.CANCEL}}</button>
				</div>
			</div>
		</script>
		<script id="document-new-template-list-tpl" type="text/html">
			<div class="control-group">
				<label class="control-label" for="modal-form-template">{{_.TEMPLATE}}</label>
				<div class="controls">
					<select class="input-xlarge" id="modal-form-template">
						<option value=""></option>
						{{#collection}}
						<option value="{{id}}">{{id}}</option>
						{{/collection}}
					</select>
					<p class="help-block">{{_.DOCUMENT_S_TEMPLATE}}</p>
				</div>
			</div>
		</script>
		<script id="document-new-attributes-tpl" type="text/html">
			<div class="content">
				{{#collection}}
				<div class="control-group">
					<label class="control-label" for="modal-form-attribute-{{name}}">{{name}}</label>
					<div class="controls">
						<input id="modal-form-attribute-{{name}}" type="{{attributeType}}" class="input-xlarge attribute" />
					</div>
				</div>
				{{/collection}}
			</div>
			<!--
			<div class="dropdown">
				<a class="btn" data-toggle="dropdown" href="#">
					{{_.APPEND}}
				</a>
			</div>
			-->
		</script>
		<script id="tag-list-tpl" type="text/html">
			<div class="tag list">
				<div class="header">
					<a class="name" href="#tags"
							data-toggle="collapse"
							data-target="#data-target-tags-{{view_cid}}">
						<span class="icon"></span>
						{{_.TAGS}}
					</a>
				</div>
				<ul id="data-target-tags-{{view_cid}}" class="collapse items"></ul>
			</div>
		</script>
		<script id="tag-list-item-tpl" type="text/html">
			<div class="header">
				<a class="name" href="#tag/{{model.id}}">
					<span class="icon"></span>
					{{model.label}}
				</a>
				<div class="actions btn-group">
					<a class="btn dropdown-toggle" data-toggle="dropdown">
						<span class="caret"></span>
					</a>
					<ul class="dropdown-menu">
						<li class="delete">
							<a href="#"><span class="icon"></span>{{_.DELETE}}</a>
						</li>
					</ul>
				</div>
			</div>
		</script>
		<script id="template-list-tpl" type="text/html">
			<div class="actions">
				<!--
				<button class="btn new" title="{{_.NEW_TEMPLATE}}">
					<span class="icon"></span>
				</button>
				-->
				<button class="btn delete" title="{{_.DELETE}}">
					<span class="icon"></span>
				</button>
			</div>
			<div class="content">
				<table class="table table-striped table-condensed">
					<thead>
						<tr>
							<th><input for="document-list-actions" type="checkbox" class="select" /></th>
							<th>{{_.REFERENCE}}</th>
							<th>{{_.TYPE}}</th>
							<th>{{_.AUTHOR}}</th>
							<th>{{_.CREATION_DATE}}</th>
						</tr>
					</thead>
					<tbody class="items"></tbody>
				</table>
			</div>
		</script>
		<script id="template-list-item-tpl" type="text/html">
			<td><input for="template-list-actions" type="checkbox" class="select" /></td>
			<td>{{model.reference}}</td>
			<td>{{model.documentType}}</td>
			<td>{{model.author.name}}</td>
			<td>{{model.creationDate}}</td>
		</script>
		<script id="workflow-list-tpl" type="text/html">
			<div class="actions">
				<!--
				<button class="btn new" title="{{_.NEW_TEMPLATE}}">
					<span class="icon"></span>
				</button>
				-->
				<button class="btn delete" title="{{_.DELETE}}">
					<span class="icon"></span>
				</button>
			</div>
			<div class="content">
				<table class="table table-striped table-condensed">
					<thead>
						<tr>
							<th><input for="document-list-actions" type="checkbox" class="select" /></th>
							<th>{{_.REFERENCE}}</th>
							<th>{{_.AUTHOR}}</th>
						</tr>
					</thead>
					<tbody class="items"></tbody>
				</table>
			</div>
		</script>
		<script id="workflow-list-item-tpl" type="text/html">
			<td><input for="workflow-list-actions" type="checkbox" class="select" /></td>
			<td>{{model.reference}}</td>
			<td>{{model.author.name}}</td>
		</script>
		<script id="alert-tpl" type="text/html">
			<div class="alert alert-block alert-{{alert.type}} fade in">
				<a class="close" data-dismiss="alert" href="#">×</a>
				<h4 class="alert-heading">{{alert.title}}</h4>
				<p>{{alert.message}}</p>
			</div>
		</script>
    </body>
</html>
