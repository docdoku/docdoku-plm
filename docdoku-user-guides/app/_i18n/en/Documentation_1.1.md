

* This will become a table of contents (this text will be scraped).
{:toc}

#Presentation
PLM (Product Lifecycle Management) is an area of ​​activity whose purpose is to create and maintain products throughout their life cycle, since the establishment of the specification of product and related services until the end of life. (source: Wikipedia)

PLM is a strategy that allows companies to share product data allowing all stakeholders (employees, suppliers, customers, etc...) to act collaboratively on product development.

Beyond traditional PLM functionalities, DocDokuPLM offers to view and collaborate on a digital model (Catia, Inventor, AutoCAD, STEP, IFC, COLLADA, OBJ, etc...) on all kind of terminals (PC, Mac, Linux, Tablet, Smartphone) directly in the browser without any installation or plugins.

In addition, DocDokuPLM proposes advanced functions such as fine grained access right management, document templates or out of the box BPM (Business Process Management) through a graphical workflow editor.

DocDokuPLM is a user friendly, ergonomic tool.

DocDokuPLM User Guide describes and explains how to use the software. It is a comprehensive manual useful for any end users.

#User Management
To be able to log into the application, everyone needs an account identified by a login and protected by a password.

Each user which hence represents an actor of the system holds specific access rights for every workspace he belongs to. Users can also participate in business processes.

##User creation
To create an account, click on the link “sign up!” from the home page.

{% image /assets/images/documentation/en/register.png "Registration link"%}

The first step is thus to register. All fields are required.

{% image /assets/images/documentation/en/register2.png "Creating a User"%}

##User settings

{% image /assets/images/documentation/en/settings.png "Account Management"%}

Subsequently all settings can be changed, except the User ID which is immutable.
The account edition page is accessible on the "My Account" submenu which also allows to reset the password.

{% image /assets/images/documentation/en/edition.png "Account Edition"%}

#Workspace Management

##Workspace creation
Once account created, you can instantiate new workspaces.

{% image /assets/images/documentation/en/creation.png "Workspace creation"%}

Workspace is the top level context object that gathers documents, parts, business processes and products. The initial workspace administrator is the user who created it.

The "freeze folder structure except for workspace manager" option must be checked if you do not want other users to modify the directory structure.

##Workspace settings
To edit the workspace properties, click on "Administration".

{% image /assets/images/documentation/en/account.png "Account Management"%}

Then, select the workspace you want to edit.

{% image /assets/images/documentation/en/workspace.png "Manage your workspaces"%}

You manage workspaces access at user level or by creating groups.
Access rights administration is discussed in the next chapter.

###Dashboard
The dashboard provides statistics on your workspaces (disk space, number of documents and parts, checking/checkout per user, etc..).

{% image /assets/images/documentation/en/dashboard.png "Airplane-T01 workspace dashboard"%}

##Collaborative messaging
All users of the same workspace can communicate in real-time with our built-in communication module.
This module offers the ability to engage an instant messaging conversation or a video conference on the fly. Hence, users can easily exchange parts and documents and thus speed up the process of product development.

The co-workers menu lists the current connected users (green). A click on the camera icon will start a video conversation.

{% image /assets/images/documentation/en/coworkers.png "co-workers menu"%}

{% image /assets/images/documentation/en/videochat.png "video chat invitation"%}

In addition to the co-workers menu, whenever a user name appears in blue inside the application, a simple click brings up a contextual menu. A conversation started this way carries out the context to the recipient user so he is informed of what is probably the subject of the communication (document, part).

{% image /assets/images/documentation/en/conversation.png "Co-worker contextual conversation menu"%}

#Access right controls

##Workspace access rights

###User access management
Workspace administrator defines permissions to users which can be either full or read-only access.

It’s also possible to disable users which has the effect to forbid them to log in.

{% image /assets/images/documentation/en/user_management.png "Workspace Users Management"%}

A full access user can:

* Create, modify or delete documents and parts
* Modify the folder structure (if not restricted to administrator)
* Move documents in folders
* Checkin/Checkout documents and parts

A read-only user can:

* View documents and parts
* View product structures and digital mock-ups

A disabled user cannot do anything.

###Managing Groups
Groups are useful when the number of users tends to increase. They can hold the same kind of permissions.

{% image /assets/images/documentation/en/group_user_management.png "Group Users Management"%}

##Documents and Parts Access Control List
If newly created documents and parts are accessible according to the permissions defined at workspace level, it’s possible to override them by selecting the "Rights" tab. From that panel you can upgrade or downgrade access rights to any user in the workspace for the specific entity (document or part). The lower level is "forbidden" which means that the entity will not be visible by the user or group.

{% image /assets/images/documentation/en/document_creation.png "Document creation, ACL tab"%}

Only the administrator of the workspace and the authors can modify the permissions of existing entities.
Once you've selected an entity the following icon appears in the banner at the top.

{% image /assets/images/documentation/en/permissions.png%}

You can then change the permissions in the same way as creation time.

Entities that hold specific rights display a padlock at the end of the line. Green means you have full access to the entity, yellow means read-only access.

{% image /assets/images/documentation/en/full_access.png "Full access"%}
{% image /assets/images/documentation/en/read_only.png "Read-only"%}

##Combined Access Rights

Important concepts:

* Access rights priority order:
1. User rights on a document / part
2. Group rights on a document / part
3. User rights on the workspace
4. Group rights on the workspace
5. Rights of the most permissive group
* Access rights are not use for users disable on workspace.
* The administrator of the workspace exceeds these rights.

Below the summary tables of possible combinations.

###User present in one groups

| Group rights | User rights on Workspace | Effective rights |
| ------------ | ------------------------ | ---------------- |
| full access  | read only                | read only        |
| full access  | full access              |  full access     |
| read only    | read only                |  read only       |
| read only    | full access              | full access


###User present in several groups

| Group I rights | Group II rights | Effective rights |
| -------------- | --------------- | ---------------- |
| full access    | full access     | full access      |
| full access    | read only       | full access      |
| full access    | disable         | full access      |
| read only      | disable         | read only        |
| read only      | read only       | read only

###User access rights for a document/part

| User rights on Workspace | User Rights on Document/Part | Effective Rights |
| ------------------------ | ---------------------------- | ---------------- |
| full access              | full access                  | full access      |
| full access              | read only                    | read only        |
| full access              | forbidden                    | forbidden        |
| read only                | full access                  | full access      |
| read only                | read only                    | read only        |
| read only                | forbidden                    | forbidden

###Group access rights for a document/part

| Group rights on Workspace | Group Rights on Document/Part | Effective Rights |
| ------------------------- | ----------------------------- | ---------------- |
| full access               | full access                   | full access      |
| full access               | read only                     | read only        |
| full access               | forbidden                     | forbidden        |
| read only                 | full access                   | full access      |
| read only                 | read only                     | read only        |
| read only                 | forbidden                     | forbidden        |

#Product Management
DocDokuPLM is a management system for collaborative product development which purpose is to help members of the same organization create and exchange data around products.

The product management module offers, among others, the following features:

* Configuration management
* Product structure exploration
* 3D digital mock-up visualization
* Parts metadata
* Part-document links

##Product structure
The product structure is a breakdown showing the various items that compose a product.
These components are named parts, they are assemblies if they are made of other parts. Within DocDokuPLM parts can be created from scratch or imported from CAD tools.

###Part template creation
Sometimes, it’s interesting to ensure that parts are always filled with predefined properties or their reference number respects a given formalism. To enforce such constraints you have to create parts from templates.

###Part creation
As seen, part creation panel has an optional template property but there are also several other input fields like name, description, attributes, workflow (see paragraph below) or ACL (Access Control List).

{% image /assets/images/documentation/en/part.png "Part creation"%}

The newly created part will be added to the list.

{% image /assets/images/documentation/en/part_list.png "Part list"%}

From the list, a click on the part number will bring the details window to the front. From that screen, you could modify (if you checked out the part) the following elements: attributes, the CAD file and links to documents.

By selecting a part, you can apply a set of actions including:

* Deletion
* check-out / undo check-out / check-in
* Access rights management

###Part assembly
The assembly tab allows to edit the composition of the assembly if obviously the part is not a leaf of the product structure.

{% image /assets/images/documentation/en/part_assembly.png "Part Assembly"%}

###Product creation
The creation of a product involves supplying an identifier and a part number, filling a description is optional.

The part number designates the root part of your product.

{% image /assets/images/documentation/en/product_creation.png "Product creation form"%}

The new product will be added to the product list. Selecting an item from that list proposes two actions: deletion and baseline creation.

Baselines are kinds of snapshots of the entire product structure at a given version.

{% image /assets/images/documentation/en/image40.png "Baseline creation form"%}

##Product explorer
The product structure is displayed as a tree view where each node represents an assembly which is expandable.

{% image /assets/images/documentation/en/tree_structure.png "Tree structure of the product"%}

The node itself is clickable which has the effect to display its main properties on the bottom right. Sometimes, especially for complex products, locating a part in the tree could be cumbersome. To facilitate this operation, there is a search bar on the top left that allows to easily find a part from its number.

{% image /assets/images/documentation/en/search_bar.png "Search bar"%}

As an alternative, you can select the part directly on the 3D scene.

{% image /assets/images/documentation/en/visualization.png "3D model visualization"%}

One click on a 3D object select the part on the structure (part properties are displayed in the right hand panel) and all its ancestors will be highlighted in yellow.
This feature is useful when the user wants to find a part which he ignores the part number.

The possible actions in the 3D visualization mode are:

* Creating markers for example to report a design issue
* Creating layers that hold a set of markers
* Export 3D visualization of a part. Produces the html code to embed in other web pages (like YouTube, or Google Maps)

#Document Management
DocDokuPLM has a document management module that includes a comprehensive versioning system (master, revision and iteration), functions for sharing, publishing documents, treeview and tags organisation...
In the following paragraph, we will detail all of these functionalities.

{% image /assets/images/documentation/en/image00.png "Document management menu"%}

##Document template

###Document template creation

You can create templates that will be used to instantiate documents. You can choose to restrict document name by filling a mask format and selecting the identifier generation option. This will lead to an automatic creation of identifiers for documents that use the template.

{% image /assets/images/documentation/en/image25.png "Document template creation form"%}

###Adding files and attributes

You can define attribute types in the template as well as attach files to it. All documents created using the same template will have the same attribute set and attached files. Attribute values will be set on those documents and obviously files will evolve as they are simply themselves template files.

{% image /assets/images/documentation/en/image42.png "Attaching files to the template"%}

## Document creation

All documents must belong to a folder. To create a new document in a specific directory, you must select first the directory then click on the ‘new document’ button.

{% image /assets/images/documentation/en/image09.png "Document creation form"%}

When creating a document, you can edit its attributes, specify a workflow and set access rights. Note that you will not be able to upload files or link the current document to another one until you finish the creation process.

Once the document created you can perform the following actions:

* Check in / check out the document
* Delete the document
* Subscribe to document state change notifications. In this case the user receives an email each time a change is made on the document by other users
* Subscribe to iteration change notifications. The user receives a notification when there is a new iteration on the document
* Adding tag to documents, for example, classifying a document as important
* Enabling access rights to documents
* Creating a new version of document
* Public or private document publishing

##Modifying document

In order to modify a document, you must first reserve it. You can access the document modification window by clicking on its name.

The arrows at the bottom left are used to visualize the different changes done in previous iterations.

{% image /assets/images/documentation/en/image10.png "Document details window"%}

##Document viewer

Each document provides a permanent link where you see details of its latest revision. To get there, you should simply click on the title of the document window.

{% image /assets/images/documentation/en/image22.png %}

You can explore all properties of the document as well as visualize their attached files.The viewer supports a large number of formats: pdf, jpg, mp4, doc...

{% image /assets/images/documentation/en/image02.png "Document viewer"%}

Files not supported by the viewer will be downloaded.

##Document conversion

In addition to the mentioned features, DocDokuPLM can convert documents into pdf. The majority of word processing formats are supported.

##Document checkout/checkin

To lock a document for modification, just select it and then push the checkout button:

{% image /assets/images/documentation/en/image16.png "Document lock : check-out / undo check-out / check-in"%}

A checked out document cannot be edited by other users. To validate the modifications you will have to check in it. Otherwise, you can cancel the change with the help of the undo check-out action.

When releasing (check-in operation) the document, you have the opportunity to enter an optional revision note.

{% image /assets/images/documentation/en/image19.png "Window of revision note"%}

##Search a document

We distinguish two types of search:

###Quick search

The quick search bar appears on top of the document list. It lets you quickly find a document from its name.

{% image /assets/images/documentation/en/image08.png "Quick search bar"%}

###Advanced search

You can access the advanced search in two ways:

* Through the “Search” link from the left menu
* Through the small arrow from the quick search bar

{% image /assets/images/documentation/en/image30.png "Advanced search of documents"%}

This advanced search allows you to find a document from its title, type, reference, version, author, creation date or content files.

##Tags

You have the possibility to tag documents. To do so, select one or more documents and push the tag icon:

{% image /assets/images/documentation/en/image27.png %}

From the tag management window, you can affect existing tags or create new ones.

{% image /assets/images/documentation/en/image34.png "Tag window"%}

To display the documents which have been tagged, select the given tag from the left menu.

{% image /assets/images/documentation/en/image37.png "Selection of a tag"%}

You can delete a tag by clicking on the right arrow into the area of the tag, then click on “Delete”. Only the tag will be removed, the associated documents will remain unchanged.

{% image /assets/images/documentation/en/image39.png "Delete a label"%}

##“Checked out” and “Tasks” links

To ensure fast access to documents, you will find from the left menu two shortcut links:

* Checked out: this link displays all the documents reserved by the current user
* Tasks: Shows the documents on which the user is directly involved in a workflow

{% image /assets/images/documentation/en/image06.png "Checked out and Tasks links"%}

#Workflow Management

A workflow is a representation of the various tasks that need to be completed and their interactions. These operations are assigned to different users of the same workspace and are related to an identified document or part.

##Roles

DocDokuPLM Workflows are role-based. That means to increase the applicability of any workflow models, task assignees are not expressed directly by user names but rather by roles.

Thus, the first step involved in workflow creations is to define the roles used inside the workspace. These roles can optionally be mapped to a default user assignee. Anyway when the workflow model will be instantiated and attached to a document or a part we will have the opportunity to refine those mappings.

{% image /assets/images/documentation/en/image32.png "Roles definition panel"%}

##Workflow template

A workflow template or model consists of a serie of activities. Each activity contains tasks to be done and an associated label which represents the state the entity when the workflow current step will be the activity. Those tasks can be performed in series or in parallel.

For a serial activity, the tasks are performed sequentially. If one task is rejected, the current activity is stopped.

For a parallel activity, the tasks are open simultaneously and as a consequence can be closed in any order. A rejected task does not necessarily lead to stop the current activity. In effect, parallel activities have an additional property which is the number of done tasks needed to progress to the next activity. This number ranges from 1 to the total number of tasks.

A validated activity results to start the next one. An unvalidated activity results in the suspension of the entire workflow.

In case of an unvalidated activity, the workflow will continue to the recovery activity if this one has been previously defined on the template.

A workflow template can be changed at any time, but that will not have any incidence to the already instantiated workflows.

{% image /assets/images/documentation/en/image12.png "Creation of a workflow template"%}

##Workflow instance

When creating a document or a part, the author can choose the workflow template to apply. All the roles implied have then to be resolved.

{% image /assets/images/documentation/en/image41.png "Definition of the roles during the creation of a document"%}

Immediately after the creation of a document (or a part), the associated workflow (if any) starts on the first activity.

##Lifecycle state

Once an activity is started, every task assignee receives a mail which includes a full description of the task to complete.

{% image /assets/images/documentation/en/image46.png "Workflow instantiated on a document"%}

A running task can be marked as done or rejected if:

* the responsible of the task has downloaded at least once the document file
* the document or the part is released (not checked out)

All the users who have subscribed to state change notification will be informed.

#Sharing and publishing

##Publishing

Each document and part can be published. To do so, click on the  icon to the right of the table line, then the following window will appear.

{% image /assets/images/documentation/en/image21.png "Publishing window"%}

As soon as the ON/OFF button will be pressed, the document or part will be publicly accessible from the Internet.

##Private access

You may also want to generate a private link protected by a password and with an optional expiration date. For that, just fill the password and/or expiration date and push the share button.

{% image /assets/images/documentation/en/image07.png "Private share"%}
