

* This will become a table of contents (this text will be scraped).
{:toc}

#Présentation
Le PLM (Product Lifecycle Management, littéralement gestion du cycle de vie du produit) est un
domaine d'activité dont le but est de créer et maintenir des produits tout au long de leur cycle de
vie, depuis l'établissement du cahier des charges du produit et des services associés jusqu'à la
fin de vie. (source: Wikipedia)

Il s’agit d’une stratégie permettant aux entreprises de partager leurs données produit autorisant
l’ensemble des parties prenantes (collaborateurs, fournisseurs, clients, etc.) à intervenir de
façon collaborative sur le développement des produits.

Au-delà des fonctionnalités traditionnelles d’un PLM, DocDokuPLM permet de visualiser et
collaborer sur une maquette numérique et/ou tous types de modèles 3D (Catia, Inventor,
AutoCAD, STEP, IFC, COLLADA, OBJ, etc.) sur tous types de terminaux (PC, Mac, Tablettes,
Smartphones) directement dans le navigateur internet sans aucune installation ou plugin.

De plus, DocDokuPLM offre des fonctions avancées telles que la gestion des droits d’accès, les
modèles de document ou encore le BPM (Business Process Management) au travers d’un
éditeur graphique de workflow.

DocDokuPLM est un outil simple, à l’ergonomie soignée dont la prise en main est très rapide.
L’objectif de ce document est de présenter en détail l’ensemble des possibilités offertes par
cette solution pour une utilisation optimale.

#Gestion des utilisateurs
Dans DocDokuPLM, la notion d'utilisateur ne se restreint pas à un simple accès à l'application.
C'est par exemple à partir de ce même utilisateur que vous pourrez spécifier des droits sur un
document ou encore lui attribuer un rôle dans un workflow.

Vous découvrez DocDokuPLM ? Les chapitres suivants devraient retenir toute votre attention...

##Création d'un utilisateur
Pour créer un compte, cliquez sur le lien inscrivez-vous depuis la page d'accueil.

{% image /assets/images/documentation/fr/image32.png "Lien d’inscription"%}

Chaque nouvel utilisateur doit effectuer cette action.

La toute première étape consiste ainsi à s'enregistrer. Ici, tous les champs sont obligatoires.

{% image /assets/images/documentation/fr/image17.png "Création d'un utilisateur"%}

##Modification de l’utilisateur

{% image /assets/images/documentation/fr/image04.png "Gestion du compte"%}

Dans le sous-menu “Mon Compte” vous pouvez modifier toutes vos caractéristiques à l'exception de votre identifiant utilisateur.

Cette page vous permet également de modifier votre mot de passe.

{% image /assets/images/documentation/fr/image10.png "Édition du compte"%}

#Gestion des espaces de travail

##Création d'un espace de travail
Votre compte désormais créé, vous avez la possibilité de créer un espace de travail.

{% image /assets/images/documentation/fr/image37.png "image37"%}

Une fois cette opération effectuée, vous en serez l’administrateur. Celui-ci regroupera l’ensemble des documents, articles, processus métier et produits.

L’option “geler la structure des répertoires sauf pour le gestionnaire de l’espace de travail” doit être cochée si vous ne souhaitez pas que les autres utilisateurs puissent modifier la structure des répertoires.

##Modification d'un espace de travail
Pour accéder aux options d’un espace de travail, cliquez sur le lien “Administration”.

{% image /assets/images/documentation/fr/image04.png "Gestion du compte"%}

Plusieurs actions sont ici possibles :

L'édition de l'espace de travail permettra tout d'abord d’en modifier l'administrateur ainsi que sa description.

{% image /assets/images/documentation/fr/image27.png "Modification de l'espace de travail"%}

Vous pouvez également gérer les accès sur l'espace de travail, soit au niveau utilisateur, soit en créant des groupes de travail.

Cette gestion des droits est abordée dans le chapitre suivant.

###Tableau de bord
Le tableau de bord donne accès à des statistiques sur votre espace de travail (espace disque, nombre de documents et d'articles, taux de réservation/utilisateur, etc.).

{% image /assets/images/documentation/fr/image39.png "Tableau de bord de l’espace de travail Airplane-T01"%}

##Messagerie collaborative
L’ensemble des utilisateurs d’un même espace de travail peuvent échanger en temps réel grâce aux modules de communication intégrés dans DocDokuPLM.

Ces modules offrent la possibilité d’avoir des conversations de type messagerie instantanée et visioconférence. Il est par ailleurs possible de partager les visualisations d’articles entre collaborateurs dans l’objectif de faciliter l’échange d’informations et d’accélérer le processus de développement de produits.

Depuis le menu Collaborateurs, vous visualisez en temps réel les utilisateurs connectés (en vert). En cliquant sur l’icône de la caméra, vous lancez une conversation vidéo.

{% image /assets/images/documentation/fr/image07.png "Menu collaborateurs"%}

{% image /assets/images/documentation/fr/image16.png "Invitation session vidéo"%}

En complément du menu Collaborateurs, chaque fois qu’un nom d’utilisateur apparaît en bleu dans l’application, cliquez dessus et un menu de conversation contextualisé avec le nom de l’espace de travail et le nom du l’élément concerné apparaît. Ce contexte est indiqué dans le titre de chaque fenêtre de conversation.

{% image /assets/images/documentation/fr/image29.png "Menu collaboratif contextuel"%}

#Gestion des contrôles d’accès

##Droits appliqués au niveau de l’espace de travail

###Gestion des accès utilisateurs
L’administrateur de l’espace de travail peut définir des droits par défaut aux utilisateurs. Un utilisateur peut avoir un accès complet en lecture/écriture, un accès en lecture seule, ou bien n’avoir aucun droit.

{% image /assets/images/documentation/fr/image44.png "Gestion des utilisateurs et des groupes "%}

Par ailleurs, les utilisateurs peuvent être répartis au sein de groupes qui porteront les droits.

Un utilisateur en accès complet peut :

* Créer, modifier ou supprimer des documents et des articles
* Modifier la structure des dossiers dans la gestion des documents
* Déplacer des documents dans les dossiers
* Réserver des documents et articles

Un utilisateur en lecture seule peut :

* Accéder aux documents et articles en lecture
* Visualiser la structure des produits

Un utilisateur désactivé ne peut accéder ni aux documents ni aux articles.

Pour changer les droits d’un utilisateur ou d’un groupe, il faut cliquer sur la case à cocher correspondante, puis cliquer sur un bouton d’action (supprimer, désactiver, activer, ...) en bas de la liste.

###Gestion des groupes
Pour gérer les utilisateurs d’un groupe, vous devez cliquer sur son nom. Vous pouvez alors ajouter ou enlever des utilisateurs au groupe sélectionné.

{% image /assets/images/documentation/fr/image33.png "Gestion des utilisateurs d’un groupe"%}

##Droits appliqués au niveau des documents et des articles
A la création d’un document ou d’un article, les droits appliqués à ceux-ci sont par défaut ceux définis sur l’espace de travail. Pour redéfinir spécialement les droits de l’entité à créer, vous devez utiliser l’onglet “Droits”. Vous pouvez alors modifier les droits spécifiquement pour cette entité.

Vous pouvez ainsi donner des droits supplémentaires aux utilisateurs en lecture seule au niveau de l’espace de travail sur des entités ciblées, ou au contraire restreindre l’accès à des données à des utilisateurs en accès complet.

En choisissant “interdit” pour un utilisateur sur une entité, celle-ci ne sera pas visible dans la liste qui lui est servie.

{% image /assets/images/documentation/fr/image01.png "Formulaire de création de document, onglet de gestion de droits d'accès "%}

Ces droits sont modifiables, seuls l’administrateur de l’espace de travail et l’auteur du document ou de l’article peuvent le faire.

Une fois l’entité sélectionnée l’icône suivante apparaît dans le bandeau du haut.

{% image /assets/images/documentation/fr/image13.png%}

Vous pouvez alors modifier les droits associés au document ou à l’article ou bien vous pouvez choisir de les désactiver.

Quand des droits sont appliqués sur un élément, un cadenas est visible en fin de ligne. La couleur verte signifie que vous avez un accès complet sur l’entité, la couleur jaune signifie un accès en lecture seule.

{% image /assets/images/documentation/fr/image15.png "accès complet"%}
{% image /assets/images/documentation/fr/image21.png "lecture seule "%}

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
* ...

##Product structure
The product structure is a breakdown showing the various items that compose a product.
These components are named parts, they are assemblies if they are made of other parts. Within DocDokuPLM parts can be created from scratch or imported from CAD tools.

###Part template creation
Sometimes, it’s interesting to ensure that parts are always filled with predefined properties or their reference number respects a given formalism. To enforce such constraints you have to create parts from templates.

###Part creation
As seen, part creation panel has an optional template property but there are also several other input fields like name, description, attributes, workflow (see paragraph below) or ACL (Access Control List).

{% image /assets/images/documentation/fr/part.png "Part creation"%}

The newly created part will be added to the list.

{% image /assets/images/documentation/fr/part_list.png "Part list"%}

From the list, a click on the part number will bring the details window to the front. From that screen, you could modify (if you checked out the part) the following elements: attributes, the CAD file and links to documents.

By selecting a part, you can apply a set of actions including:

* Deletion
* check-out / undo check-out / check-in
* Access rights management

###Part assembly
The assembly tab allows to edit the composition of the assembly if obviously the part is not a leaf of the product structure.

{% image /assets/images/documentation/fr/part_assembly.png "Part Assembly"%}

###Product creation
The creation of a product involves supplying an identifier and a part number, filling a description is optional.
The part number designates the root part of your product.

{% image /assets/images/documentation/fr/product_creation.png "Product creation form"%}

##Product explorer
The product structure is displayed as a tree view where each node represents an assembly which is expandable.

{% image /assets/images/documentation/fr/tree_structure.png "Tree structure of the product"%}

The node itself is clickable which has the effect to display its main properties on the bottom right. Sometimes, especially for complex products, locating a part in the tree could be cumbersome. To facilitate this operation, there is a search bar on the top left that allows to easily find a part from its number.

{% image /assets/images/documentation/fr/search_bar.png "Search bar"%}

As an alternative, you can select the part directly on the 3D scene.

{% image /assets/images/documentation/fr/visualization.png "3D model visualization"%}

One click on a 3D object select the part on the structure (part properties are displayed in the right hand panel) and all its ancestors will be highlighted in yellow.
This feature is useful when the user wants to find a part which he ignores the part number.

The possible actions in the 3D visualization mode are:

* Creating markers for example to report a design issue
* Creating layers that hold a set of markers
* Export 3D visualization of a part. Produces the html code to embed in other web pages (like YouTube, or Google Maps)

#Document Management
DocDokuPLM has a document management module that includes a comprehensive versioning system (master, revision and iteration), functions for sharing, publishing documents, treeview and tags organisation...
In the following paragraph, we will detail all of these functionalities.

{% image /assets/images/documentation/fr/image00.png "Document management menu"%}

##Document template

###Document template creation

You can create templates that will be used to instantiate documents. You can choose to restrict document name by filling a mask format and selecting the identifier generation option. This will lead to an automatic creation of identifiers for documents that use the template.

{% image /assets/images/documentation/fr/image25.png "Document template creation form"%}

###Adding files and attributes

You can define attribute types in the template as well as attach files to it. All documents created using the same template will have the same attribute set and attached files. Attribute values will be set on those documents and obviously files will evolve as they are simply themselves template files.

{% image /assets/images/documentation/fr/image42.png "Attaching files to the template"%}

## Document creation

All documents must belong to a folder. To create a new document in a specific directory, you must select first the directory then click on the ‘new document’ button.

{% image /assets/images/documentation/fr/image09.png "Document creation form"%}

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

{% image /assets/images/documentation/fr/image10.png "Document details window"%}

##Document viewer

Each document provides a permanent link where you see details of its latest revision. To get there, you should simply click on the title of the document window.

{% image /assets/images/documentation/fr/image22.png "Document details window"%}

You can explore all properties of the document as well as visualize their attached files.The viewer supports a large number of formats: pdf, jpg, mp4, doc...

{% image /assets/images/documentation/fr/image02.png "Document viewer"%}

Files not supported by the viewer will be downloaded.

##Document conversion

In addition to the mentioned features, DocDokuPLM can convert documents into pdf. The majority of word processing formats are supported.

##Document checkout/checkin

To lock a document for modification, just select it and then push the checkout button:

{% image /assets/images/documentation/fr/image16.png "Document lock : check-out / undo check-out / check-in"%}

A checked out document cannot be edited by other users. To validate the modifications you will have to check in it. Otherwise, you can cancel the change with the help of the undo check-out action.

When releasing (check-in operation) the document, you have the opportunity to enter an optional revision note.

{% image /assets/images/documentation/fr/image19.png "Window of revision note"%}

##Search a document

We distinguish two types of search:

###Quick search

The quick search bar appears on top of the document list. It lets you quickly find a document from its name.

{% image /assets/images/documentation/fr/image08.png "Quick search bar"%}

###Advanced search

You can access the advanced search in two ways:

* Through the “Search” link from the left menu
* Through the small arrow from the quick search bar

{% image /assets/images/documentation/fr/image30.png "Advanced search of documents"%}

This advanced search allows you to find a document from its title, type, reference, version, author, creation date or content files.

##Tags

You have the possibility to tag documents. To do so, select one or more documents and push the tag icon:

{% image /assets/images/documentation/fr/image27.png %}

From the tag management window, you can affect existing tags or create new ones.

{% image /assets/images/documentation/fr/image34.png "Tag window"%}

To display the documents which have been tagged, select the given tag from the left menu.

{% image /assets/images/documentation/fr/image37.png "Selection of a tag"%}

You can delete a tag by clicking on the right arrow into the area of the tag, then click on “Delete”. Only the tag will be removed, the associated documents will remain unchanged.

{% image /assets/images/documentation/fr/image39.png "Delete a label"%}

##“Checked out” and “Tasks” links

To ensure fast access to documents, you will find from the left menu two shortcut links:

* Checked out: this link displays all the documents reserved by the current user
* Tasks: Shows the documents on which the user is directly involved in a workflow

{% image /assets/images/documentation/fr/image06.png "Checked out and Tasks links"%}

#Workflow Management

A workflow is a representation of the various tasks that need to be completed and their interactions. These operations are assigned to different users of the same workspace and are related to an identified document or part.

##Roles

DocDokuPLM Workflows are role-based. That means to increase the applicability of any workflow models, task assignees are not expressed directly by user names but rather by roles.

Thus, the first step involved in workflow creations is to define the roles used inside the workspace. These roles can optionally be mapped to a default user assignee. Anyway when the workflow model will be instantiated and attached to a document or a part we will have the opportunity to refine those mappings.

{% image /assets/images/documentation/fr/image32.png "Roles definition panel"%}

##Workflow template

A workflow template or model consists of a serie of activities. Each activity contains tasks to be done and an associated label which represents the state the entity when the workflow current step will be the activity. Those tasks can be performed in series or in parallel.

For a serial activity, the tasks are performed sequentially. If one task is rejected, the current activity is stopped.

For a parallel activity, the tasks are open simultaneously and as a consequence can be closed in any order. A rejected task does not necessarily lead to stop the current activity. In effect, parallel activities have an additional property which is the number of done tasks needed to progress to the next activity. This number ranges from 1 to the total number of tasks.

A validated activity results to start the next one. An unvalidated activity results in the suspension of the entire workflow.

In case of an unvalidated activity, the workflow will continue to the recovery activity if this one has been previously defined on the template.

A workflow template can be changed at any time, but that will not have any incidence to the already instantiated workflows.

{% image /assets/images/documentation/fr/image12.png "Creation of a workflow template"%}

##Workflow instance

When creating a document or a part, the author can choose the workflow template to apply. All the roles implied have then to be resolved.

{% image /assets/images/documentation/fr/image41.png "Definition of the roles during the creation of a document"%}

Immediately after the creation of a document (or a part), the associated workflow (if any) starts on the first activity.

##Lifecycle state

Once an activity is started, every task assignee receives a mail which includes a full description of the task to complete.

{% image /assets/images/documentation/fr/image46.png "Workflow instantiated on a document"%}

A running task can be marked as done or rejected if:

* the responsible of the task has downloaded at least once the document file
* the document or the part is released (not checked out)

All the users who have subscribed to state change notification will be informed.

#Sharing and publishing

##Publishing

Each document and part can be published. To do so, click on the  icon to the right of the table line, then the following window will appear.

{% image /assets/images/documentation/fr/image21.png "Publishing window"%}

As soon as the ON/OFF button will be pressed, the document or part will be publicly accessible from the Internet.

##Private access

You may also want to generate a private link protected by a password and with an optional expiration date. For that, just fill the password and/or expiration date and push the share button.

{% image /assets/images/documentation/fr/image07.png "Private share"%}

