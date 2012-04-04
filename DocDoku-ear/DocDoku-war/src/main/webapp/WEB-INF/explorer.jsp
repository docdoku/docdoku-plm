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
		<script src="<%=request.getContextPath()%>/js/lib/less-1.2.1.min.js"></script>

		<script src="<%=request.getContextPath()%>/js/common.js"></script>
		<!-- LIBS -->
		<script src="<%=request.getContextPath()%>/js/lib/date.format.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/mustache-0.5.0-dev.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/jquery-1.7.1.min.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/underscore-1.3.1.min.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/backbone-0.9.2.min.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/bootstrap-2.0.2.min.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/jquery.maskedinput-1.3.js"></script>
		<!-- END LIBS -->
		<!-- MODELS -->
		<script src="<%=request.getContextPath()%>/js/models/workspace.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/folder.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/document.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/tag.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/workflow.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/template.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/document_iteration.js"></script>
		<!-- END MODELS -->
		<!-- COLLECTIONS -->
		<script src="<%=request.getContextPath()%>/js/collections/folder.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/folder_document.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/tag.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/tag_document.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/workflow.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/template.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/checkedout_document.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/document_iteration.js"></script>
		<!-- END COLLECTIONS -->
		<!-- VIEWS -->
		<script src="<%=request.getContextPath()%>/js/views/base.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/modal.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/list.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/list_item.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/alert.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/content.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/content_document_list.js"></script>

		<script src="<%=request.getContextPath()%>/js/views/document_list_item.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/document_list.js"></script>

		<script src="<%=request.getContextPath()%>/js/views/workspace.js"></script>

		<script src="<%=request.getContextPath()%>/js/views/folder_list_item.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/folder_list.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/folder_nav.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/folder_new.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/folder_edit.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/folder_document_list.js"></script>

		<script src="<%=request.getContextPath()%>/js/views/tag_nav.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/tag_list_item.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/tag_list.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/tag_document_list.js"></script>

		<script src="<%=request.getContextPath()%>/js/views/workflow_list_item.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/workflow_list.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/workflow_nav.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/workflow_content_list.js"></script>

		<script src="<%=request.getContextPath()%>/js/views/template_list_item.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/template_list.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/template_nav.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/template_content_list.js"></script>

		<script src="<%=request.getContextPath()%>/js/views/checkedout_nav.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/checkedout_content_list.js"></script>

		<script src="<%=request.getContextPath()%>/js/views/document_new.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/document_new_template_list.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/document_new_attribute_list_item.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/document_new_attribute_list.js"></script>
		<!-- END VIEWS -->
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

		<script id="workspace-tpl" class="template" type="text/html">
			<ul id="nav" class="nav nav-list">
				<li class="nav-header">{{model.id}}</li>
				<li id="folder-nav"></li>
				<li id="tag-nav"></li>
				<li class="nav-header">{{_.REFERENCES}}</li>
				<li id="workflow-nav"></li>
				<li id="template-nav"></li>
				<li class="nav-header">{{_.LINKS}}</li>
				<li id="checkedout-nav"></li>
				<li>
					<a href="#tasks" class="nav-list-entry">
						<span class="icon icon-nav-tasks"></span>
						{{_.TASKS}}
					</a>
				</li>
			</ul>
			<div id="content"></div>
		</script>

		<script id="alert-tpl" class="template" type="text/html">
			<div class="alert alert-block alert-{{model.type}} fade in">
				<a class="close" data-dismiss="alert">×</a>
				<h4 class="alert-heading">{{alert.title}}</h4>
				<p>{{model.message}}</p>
			</div>
		</script>

		<script id="document-list-tpl" class="template" type="text/html">
			<thead>
				<tr>
					<th><input type="checkbox" id="check-toggle-{{view_cid}}" /></th>
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
			<tbody id="items-{{view_cid}}" class="items"></tbody>
		</script>
		<script id="document-list-item-tpl" class="template" type="text/html">
			<td><input type="checkbox" id="check-toggle-{{view_cid}}" /></td>
			<td class="reference">{{model.reference}}</td>
			<td>{{model.version}}</td>
			<td>{{model.lastIteration.iteration}}</td>
			<td>{{model.type}}</td>
			<td>{{model.title}}</td>
			<td>{{model.lastIteration.author.name}}</td>
			<td>{{model.lastIteration.creationDate}}</td>
			<td>{{model.checkOutDate}}</td>
		</script>
		<script id="document-new-tpl" class="template" type="text/html">
			<div class="modal new-document">
				<div class="modal-header">
					<a class="close" data-dismiss="modal">×</a>
					<h3><span class="icon"></span>{{_.NEW_DOCUMENT}}</h3>
				</div>
				<div class="modal-body">
					<div id="alerts-{{view_cid}}"></div>
					<div class="tabs">
						<ul class="nav nav-tabs">
							<li class="active"><a href="#form-{{view_cid}}-tab-main" data-toggle="tab">{{_.GENERAL}}</a></li>
							<li><a href="#form-{{view_cid}}-tab-attributes" data-toggle="tab">{{_.ATTRIBUTES}}</a></li>
						</ul>
						<div class="tab-content">
							<div class="tab-pane active" id="form-{{view_cid}}-tab-main">
								<form id="templates-{{view_cid}}" class="form-horizontal"></form>
								<form id="form-{{view_cid}}" class="form-horizontal">
									<div class="control-group">
										<label class="control-label required" for="form-{{view_cid}}-reference">{{_.REFERENCE}}</label>
										<div class="controls">
											<input type="text" class="input-xlarge reference" id="form-{{view_cid}}-reference">
											<p class="help-block">{{_.DOCUMENT_S_REFERENCE}}</p>
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="form-{{view_cid}}-title">{{_.TITLE}}</label>
										<div class="controls">
											<input type="text" class="input-xlarge" id="form-{{view_cid}}-title">
											<p class="help-block">{{_.DOCUMENT_S_TITLE}}</p>
										</div>
									</div>
									<div class="control-group">
										<label class="control-label" for="form-{{view_cid}}-description">{{_.DESCRIPTION}}</label>
										<div class="controls">
											<textarea type="text" class="input-xlarge" id="form-{{view_cid}}-description"></textarea>
											<p class="help-block">{{_.DOCUMENT_S_DESCRIPTION}}</p>
										</div>
									</div>
								</form>
							</div>
							<div id="form-{{view_cid}}-tab-attributes" class="tab-pane">
								<form id="attributes-{{view_cid}}" class="form-horizontal document-new-attribute-list"></form>
							</div>
						</div>
					</div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary" for="modal-form">
						{{_.CREATE}}
					</button>
					<a class="btn cancel" data-dismiss="modal">{{_.CANCEL}}</a>
				</div>
			</div>
		</script>
		<script id="document-new-template-select-tpl" class="template" type="text/html">
			<div class="control-group">
				<label class="control-label" for="input-{{view_cid}}">{{_.TEMPLATE}}</label>
				<div class="controls">
					<select class="input-xlarge" id="input-{{view_cid}}">
						<option value=""></option>
						{{#collection}}
						<option value="{{id}}">{{id}}</option>
						{{/collection}}
					</select>
					<p class="help-block">{{_.DOCUMENT_S_TEMPLATE}}</p>
				</div>
			</div>
		</script>
		<script id="document-new-attribute-list-tpl" class="template" type="text/html">
			<a class="btn add">
				{{_.APPEND}}
			</a>
		</script>
		<script id="document-new-attribute-list-item-boolean-tpl" class="template" type="text/html">
			{{> document-new-attribute-list-item-tpl}}
			<div class="controls">
				<select class="input-xlarge value">
					<option value="false">{{_.FALSE}}</option>
					<option value="true">{{_.TRUE}}</option>
				</select>
			</div>
		</script>
		<script id="document-new-attribute-list-item-date-tpl" class="template" type="text/html">
			{{> document-new-attribute-list-item-tpl}}
			<div class="controls">
				<input type="date" class="input-xlarge value" placeholder="{{_.VALUE}}" value="{{model.value}}" />
			</div>
		</script>
		<script id="document-new-attribute-list-item-number-tpl" class="template" type="text/html">
			{{> document-new-attribute-list-item-tpl}}
			<div class="controls">
				<input type="number" class="input-xlarge value" placeholder="{{_.VALUE}}" value="{{model.value}}" />
			</div>
		</script>
		<script id="document-new-attribute-list-item-text-tpl" class="template" type="text/html">
			{{> document-new-attribute-list-item-tpl}}
			<div class="controls">
				<input type="text" class="input-xlarge value" placeholder="{{_.VALUE}}" value="{{model.value}}" />
			</div>
		</script>
		<script id="document-new-attribute-list-item-url-tpl" class="template" type="text/html">
			{{> document-new-attribute-list-item-tpl}}
			<div class="controls">
				<input type="url" class="input-xlarge value" placeholder="{{_.VALUE}}" value="{{model.value}}" />
			</div>
		</script>
		<script id="document-new-attribute-list-item-tpl" class="partial" type="text/html">
			<div class="controls">
				<a class="remove">×</a>
			</div>
			<div class="controls">
				<select class="type">
					<option value="URL">{{_.URL}}</option>
					<option value="TEXT">{{_.TEXT}}</option>
					<option value="BOOLEAN">{{_.BOOLEAN}}</option>
					<option value="DATE">{{_.DATE}}</option>
					<option value="NUMBER">{{_.NUMBER}}</option>
				</select>
			</div>
			<div class="controls">
				<input type="text" class="input-xlarge name" placeholder="{{_.NAME}}" value="{{model.name}}"/>
			</div>
		</script>

		<script id="folder-list-item-tpl" class="template" type="text/html">
			<div class="header nav-list-entry">
				<a href="#folders/{{model.path}}" data-target="#items-{{view_cid}}">
					<span class="status"></span>
					<span class="icon"></span>
					{{model.name}}
				</a>
				<div class="actions btn-group">
					<a class="btn dropdown-toggle" data-toggle="dropdown">
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
					</ul>
				</div>
			</div>
			<ul id="items-{{view_cid}}" class="items"></ul>
		</script>
		<script id="folder-nav-tpl" class="template" type="text/html">
			<div class="header nav-list-entry">
				<a href="#folders" data-target="#items-{{view_cid}}">
					<span class="icon icon-nav-documents"></span>
					{{_.FOLDERS}}
				</a>
				<div class="actions btn-group">
					<a class="btn dropdown-toggle" data-toggle="dropdown">
						<span class="caret"></span>
					</a>
					<ul class="dropdown-menu">
						<li class="new-folder">
							<a><span class="icon"></span>{{_.NEW_FOLDER}}</a>
						</li>
					</ul>
				</div>
			</div>
			<ul id="items-{{view_cid}}" class="items"></ul>
		</script>
		<script id="folder-new-tpl" class="template" type="text/html">
			<div class="modal new-folder">
				<div class="modal-header">
					<a class="close" data-dismiss="modal">×</a>
					<h3><span class="icon"></span>{{_.NEW_FOLDER}}</h3>
				</div>
				<div class="modal-body">
					<div id="alerts-{{view_cid}}"></div>
					<form id="new-folder-form" class="form-horizontal">
						<label>{{_.NAME}}</label>
						<input class="name" type="text" autofocus
							placeholder="{{_.FOLDER_S_NAME}}" />
					</form>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary" for="new-folder-form">
						{{_.CREATE}}
					</button>
					<a class="btn cancel" data-dismiss="modal">{{_.CANCEL}}</a>
				</div>
			</div>
		</script>
		<script id="folder-edit-tpl" class="template" type="text/html">
			<div class="modal edit-folder">
				<div class="modal-header">
					<a class="close" data-dismiss="modal">×</a>
					<h3><span class="icon"></span>{{_.RENAME}}</h3>
				</div>
				<div class="modal-body">
					<div id="alerts-{{view_cid}}"></div>
					<form id="edit-folder-form">
						<label>Nom</label>
						<input class="name" type="text" autofocus
							placeholder="{{_.FOLDER_S_NAME}}" />
					</form>
				</div>
				<div class="modal-footer">
					<button class="btn save btn-primary" for="edit-folder-form">
						{{_.SAVE}}
					</button>
					<a class="btn cancel" data-dismiss="modal">{{_.CANCEL}}</a>
				</div>
			</div>
		</script>
		<script id="folder-document-list-tpl" class="template" type="text/html">
			<div id="actions-{{view_cid}}" class="actions">
				<button class="btn new-document" title="{{_.NEW_DOCUMENT}}">
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
			<table id="list-{{view_cid}}" class="table table-striped table-condensed"></table>
		</script>

		<script id="tag-list-item-tpl" class="template" type="text/html">
			<div class="header nav-list-entry">
				<a class="name" href="#tags/{{model.id}}">
					<span class="icon icon-nav-tag"></span>
					{{model.label}}
				</a>
				<div class="actions btn-group">
					<a class="btn dropdown-toggle" data-toggle="dropdown">
						<span class="caret"></span>
					</a>
					<ul class="dropdown-menu">
						<li class="delete">
							<a>
								<span class="icon"></span>
								{{_.DELETE}}
							</a>
						</li>
					</ul>
				</div>
			</div>
		</script>
		<script id="tag-nav-tpl" class="template" type="text/html">
			<div class="header nav-list-entry">
				<a href="#tags" data-target="#items-{{view_cid}}">
					<span class="icon icon-nav-tags"></span>
					{{_.TAGS}}
				</a>
			</div>
			<ul id="items-{{view_cid}}" class="items"></ul>
		</script>
		<script id="content-document-list-tpl" class="template" type="text/html">
			<div id="actions-{{view_cid}}" class="actions">
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
			<table id="list-{{view_cid}}" class="table table-striped table-condensed"></table>
		</script>

		<script id="workflow-nav-tpl" class="template" type="text/html">
			<a href="#workflows" class="nav-list-entry">
				<span class="icon icon-nav-workflows"></span>
				{{_.WORKFLOWS}}
			</a>
		</script>
		<script id="workflow-content-list-tpl" class="template" type="text/html">
			<div class="actions">
				<!--
				<button class="btn new-workflow" title="{{_.NEW_WORKFLOW}}">
					<span class="icon"></span>
				</button>
				-->
				<button class="btn delete" title="{{_.DELETE}}">
					<span class="icon"></span>
				</button>
			</div>
			<table id="list-{{view_cid}}" class="table table-striped table-condensed"></table>
		</script>
		<script id="workflow-list-tpl" class="template" type="text/html">
			<thead>
				<tr>
					<th><input type="checkbox" id="check-toggle-{{view_cid}}" /></th>
					<th>{{_.REFERENCE}}</th>
					<th>{{_.AUTHOR}}</th>
				</tr>
			</thead>
			<tbody id="items-{{view_cid}}" class="items"></tbody>
		</script>
		<script id="workflow-list-item-tpl" class="template" type="text/html">
			<td><input type="checkbox" id="check-toggle-{{view_cid}}" /></td>
			<td>{{model.reference}}</td>
			<td>{{model.author.name}}</td>
		</script>

		<script id="template-nav-tpl" class="template" type="text/html">
			<a href="#templates" class="nav-list-entry">
				<span class="icon icon-nav-templates"></span>
				{{_.TEMPLATE}}
			</a>
		</script>
		<script id="template-content-list-tpl" class="template" type="text/html">
			<div class="actions">
				<!--
				<button class="btn new-template" title="{{_.NEW_WORKFLOW}}">
					<span class="icon"></span>
				</button>
				-->
				<button class="btn delete" title="{{_.DELETE}}">
					<span class="icon"></span>
				</button>
			</div>
			<table id="list-{{view_cid}}" class="table table-striped table-condensed"></table>
		</script>
		<script id="template-list-tpl" class="template" type="text/html">
			<thead>
				<tr>
					<th><input type="checkbox" id="check-toggle-{{view_cid}}" /></th>
					<th>{{_.REFERENCE}}</th>
					<th>{{_.TYPE}}</th>
					<th>{{_.AUTHOR}}</th>
					<th>{{_.CREATION_DATE}}</th>
				</tr>
			</thead>
			<tbody id="items-{{view_cid}}" class="items"></tbody>
		</script>
		<script id="template-list-item-tpl" class="template" type="text/html">
			<td><input type="checkbox" id="check-toggle-{{view_cid}}" /></td>
			<td>{{model.reference}}</td>
			<td>{{model.documentType}}</td>
			<td>{{model.author.name}}</td>
			<td>{{model.creationDate}}</td>
		</script>

		<script id="checkedout-nav-tpl" class="template" type="text/html">
			<a href="#checkedouts" class="nav-list-entry">
				<span class="icon icon-nav-checkedouts"></span>
				{{_.CHECKOUTS}}
			</a>
		</script>
    </body>
</html>
