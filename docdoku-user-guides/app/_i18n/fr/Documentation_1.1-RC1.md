

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

##Combinaison et priorité des droits

Une notion importante à retenir : les droits définis sur une entité pour un utilisateur prime sur tous les autres droits (à l’exception de l’utilisateur désactivé).

De plus, l’administrateur de l’espace de travail outrepasse ces droits.

Ci-après les tableaux récapitulatifs des combinaisons possibles.

###Utilisateur présent directement dans l’espace de travail ET dans un groupe

| Groupe        | Utilisateur       | Droits effectifs |
| ------------- | ----------------- | ---------------- |
| accès complet | lecture seule     | lecture seule    |
| accès complet | accès complet     |  accès complet   |
| lecture seule | lecture seule     |  read only       |
| lecture seule | accès complet     | accès complet


###Utilisateur présent dans plusieurs groupes

| Groupe I       | Groupe II       | Droits effectifs |
| -------------- | --------------- | ---------------- |
| accès complet  | accès complet   | accès complet    |
| accès complet  | lecture seule   | accès complet    |
| accès complet  | désactivé       | accès complet    |
| lecture seule  | désactivé       | lecture seule    |
| lecture seule  | lecture seule   | lecture seule

###Droits définis au niveau entité (document/article) pour un utilisateur (présent directement dans l’espace de travail)

| Utilisateur    | Droit d’accès entité | Droits effectifs |
| -------------- | -------------------- | ---------------- |
| accès complet  | accès complet        | accès complet    |
| accès complet  | lecture seule        | lecture seule    |
| accès complet  | interdit             | interdit         |
| lecture seule  | accès complet        | accès complet    |
| lecture seule  | lecture seule        | lecture seule    |
| lecture seule  | interdit             | interdit

###Droits définis au niveau entité pour un groupe

| Groupe          | Droit d’accès entité  | Résultat         |
| --------------- | --------------------- | ---------------- |
| accès complet   | accès complet         | accès complet    |
| accès complet   | lecture seule         | lecture seule    |
| accès complet   | interdit              | interdit         |
| lecture seule   | accès complet         | accès complet    |
| lecture seule   | lecture seule         | lecture seule    |
| lecture seule   | interdit              | interdit

#Gestion des produits
DocDokuPLM est donc un système de gestion collaborative de produits dont le but est d’aider les membres d’une même organisation à gérer et partager l'ensemble des actions et des itérations apportées à un produit tout au long de son cycle.

Vous retrouvez, entre autres, les fonctionnalités suivantes :

* Gestion des versions successives d'un même produit (notion de ligne de base) permettant la prise en compte des demandes de modifications ou d’évolutions
* Recherche d'un produit
* Visualisation 3D d’un produit
* Partage de documents liés à des produits

La création d’articles et de produits est expliquée dans le paragraphe qui suit.

##La structure produit
Il s'agit de rendre compte de la structure d'un produit et de l'organisation de ses composants.
Les composants sont appelés articles. DocDokuPLM permet de créer des articles à partir de rien ou bien à partir de modèles existants.

###Création de modèle d’article
DocdokuPLM offre la possibilité de créer des modèles types d’article et de leur associer des attributs, des fichiers et un masque de saisie permettant de faciliter la gestion de nomenclature des articles qui auront ce modèle.
Cette opération permet par la suite de créer des articles reposant sur ces modèles.

###Création d’article
Lors de la création d’un article, vous pouvez donc renseigner un modèle d’article. L’article ainsi créé aura les mêmes propriétés que celles du modèle (fichiers, attributs et liens).

Vous pouvez tout aussi bien lui associer de façon individuelle des attributs, un workflow afin de gérer le cycle de vie de l’article et régir l'accès à cet article pour les autres collaborateurs de l’espace de travail.

{% image /assets/images/documentation/fr/image05.png "Création d'un article"%}

L’article créé sera rajouté à la liste des articles déjà présents.

{% image /assets/images/documentation/fr/image19.png "Liste des articles"%}

En cliquant sur le numéro d’un article, une fenêtre affichera ses détails. Vous pouvez en modifier les éléments suivants : les attributs, le fichier CAD ou encore les liens vers des documents.

En sélectionnant un article, vous pouvez lui appliquer un ensemble d’actions à savoir :

* Suppression
* Réservation  / annulation de la réservation / libération
* Gestion de ses droits d’accès

###Assemblage d’articles
Lors de la modification d’un article vous pouvez définir son assemblage.

Un assemblage est composé d'autres articles.
{% image /assets/images/documentation/fr/image24.png "Définition de l’assemblage"%}

###Création de produit
Au moment de la création d’un produit l’identifiant du produit ainsi que le numéro de l’article sont obligatoires, la description est par contre optionnelle.

Le numéro d’article est l’élément de tête de votre produit. Celui-ci peut être un simple article ou un assemblage d’articles.

{% image /assets/images/documentation/fr/image12.png "Formulaire de création de produit"%}

Le produit créé sera rajouté à la liste des produits. En sélectionnant un produit dans la liste, les actions possibles sont sa suppression et la création d’une ligne de base.

La création d’une ligne de base permet d’enregistrer à un instant donné une version du produit et donc de gérer différentes versions d’un même produit.

{% image /assets/images/documentation/fr/image02.png "Création d’une ligne de base"%}

##Explorateur produit
Via la structure produit, la possibilité vous est offerte de rechercher et localiser l'information relative à un article donné. Cependant, pour les produits complexes comprenant un très grand nombre d'articles, la tâche peut s'avérer fastidieuse.

Prenez l'exemple du parcours de la structure produit représentée ci-dessous.

{% image /assets/images/documentation/fr/image08.png "Structure produit"%}

Afin de facilite l'opération, DocDokuPLM intègre une barre de recherche qui permet de trouver facilement un article à partir de son numéro.

Une alternative au moteur de recherche est la navigation dans le modèle géométrique 3D permettant ainsi de localiser rapidement un article. En cliquant sur une pièce, les résultats s’affichent sur la partie gauche de la page. L’article associé à cette pièce sera mis en valeur (surligné en jaune) ainsi que tous ses ascendants. Les informations relatives à cet article sont aussi affichées dans le menu de droite.

Cette solution peut s’avérer utile dans le cas où l’utilisateur ne connaît pas l’identifant de l’article, dans ce cas l’utilisateur peut cliquer directement sur la pièce dans le rendu 3D.

{% image /assets/images/documentation/fr/image22.png "Navigation dans le modèle 3D"%}

Parmi les actions possibles dans le mode de visualisation en 3D, vous retrouverez :

* Creating markers for example to report a design issue
* La création de marqueurs permettant par exemple de rapporter un dysfonctionnement d’une pièce sur le schéma 3D. Notez qu’un calque peut comporter plusieurs marqueurs.
* Export de l’article qui produit le code html à intégrer dans d’autres pages web.

#Gestion des documents
DocDokuPLM offre aussi une gestion complète de documents avec prise en compte des versions, le partage et la publication, une organisation arborescente ainsi qu'une configuration fine des droits d’accès.

Dans ce qui suit nous détaillons l’ensemble de ces fonctions.

{% image /assets/images/documentation/fr/image09.png "Menu de gestion de documents"%}

##Modèles de document

###Création d’un modèle de document

Vous pouvez créer des modèles de documents qui serviront à la création des documents. Au moment de la création d’un modèle vous pouvez choisir de gérer la nomenclature des documents en déterminant un masque de saisie et en cochant la génération de l’identifiant. Ceci entraînera la création automatique d’identifiants pour les documents qui utiliseront ce modèle.

{% image /assets/images/documentation/fr/image31.png "Formulaire de création de modèle de document"%}

###Ajout de fichiers et d’attributs

Vous pouvez définir des types d’attributs dans le modèle et lui associer des fichiers. Tous les documents qui seront créés avec ce modèle auront ces mêmes attributs, il faudra alors renseigner leur valeur, et fichiers. Ces derniers pourront bien sûr évoluer, le fichier positionné au niveau du modèle n’étant qu’un squelette.

{% image /assets/images/documentation/fr/image41.png "Rattachement de fichiers au modèle"%}

## Création de document

Tout document doit appartenir à un dossier. Afin de créer un document dans un répertoire spécifique, il faut se placer dessus en cliquant sur son nom dans le menu de gauche.

{% image /assets/images/documentation/fr/image35.png "Formulaire de création de document"%}

Vous pouvez éditer les attributs, spécifier un workflow et définir des droits d’accès dès la création du document. Le chargement des fichiers et l’édition des liens vers d’autres documents ne pourra se faire qu’une fois le document créé.

Une fois créé vous pourrez effectuer les actions suivantes :

* Réservation du document / annulation de la réservation/ libération du document
* Suppression du document
* Abonnement aux changements d’état du document. Dans ce cas l’utilisateur reçoit un mail à chaque fois qu’une modification est apportée sur le document par d'autres utilisateurs
* Abonnement aux changements d’itération du document. L’utilisateur reçoit également un mail de notification  en cas de nouvelle itération sur le document
* Ajout d'un libellé au document, par exemple         le classer comme important
* Activation des droits d’accès à ce document
* Création d’une nouvelle version du document
* Publication du document selon deux modes public ou privé

##Modification d’un document

Pour modifier un document, il vous faut d'abord le réserver. Vous pouvez accéder à la fenêtre de modification en cliquant sur le nom d’un document dans une liste.

Les flèches situées en bas à gauche servent à visualiser les différents changements apportés lors des itérations précédentes.

Vous pouvez accéder au dossier qui contient le document en cliquant sur le lien à droite de “Dossier”.

{% image /assets/images/documentation/fr/image28.png "Fenêtre d’informations relatives à un document"%}

##Visualisation de document

Pour chaque document, il est possible d’ouvrir un nouvel onglet contenant les détails de sa dernière révision. Pour y accéder, il vous suffit de cliquer sur le titre contenu dans la fenêtre de détails.

{% image /assets/images/documentation/fr/image06.png %}

Vous pouvez visualiser les différents documents créés ainsi que les fichiers qui leur sont rattachés. La visionneuse prend en charge un nombre important de formats : pdf, jpg, mp4, doc…

{% image /assets/images/documentation/fr/image30.png "Visionneur de document"%}

Les fichiers non pris en charge par la visionneuse seront téléchargés.

##Conversion de document

Outre les fonctionnalités citées, DocDokuPLM peut convertir les documents en pdf. La majorité des formats utilisés avec les logiciels de traitement de texte sont pris en charge.

##Réservation des documents

En sélectionnant un document via sa case à cocher, vous pouvez le réserver.

{% image /assets/images/documentation/fr/image14.png "Réservation d’un document : réserver / annuler la réservation / libérer"%}

Une fois réservé le document ne sera pas éditable par les autres utilisateurs. Il vous faudra alors le libérer une fois les modifications effectuées, ou annuler la réservation si vous ne souhaitez pas enregistrer les changements apportés.

Si vous choisissez de libérer le document, une fenêtre vous proposera d’entrer la note de révision. Cette information n’est pas obligatoire, vous pouvez passer cette étape en cliquant sur “Ignorer”.

{% image /assets/images/documentation/fr/image03.png "Fenêtre de note de révision"%}

##Recherche de document

Nous distinguons deux types de recherche :

###Recherche rapide

La barre de recherche rapide se présente au-dessus de la liste des documents. Elle permet de trouver rapidement un document à partir de son nom.

{% image /assets/images/documentation/fr/image42.png "Barre de recherche rapide"%}

###Recherche avancée

Vous pouvez accéder à la recherche avancée de deux manières :

* En cliquant sur le lien ‘Rechercher’ présent sur le menu gauche.
* En cliquant sur la petite flèche sur la barre de ‘Recherche Rapide’.

{% image /assets/images/documentation/fr/image25.png "Recherche avancée de documents"%}

Cette recherche avancée permet de trouver un document à partir de son titre, son type, sa référence, sa version, son auteur, sa date de création ou encore le contenu des fichiers chargés.

##Libellés

Vous pouvez associer des libellés à vos documents. En sélectionnant un document via sa case à cocher l’icône suivante apparaît :

{% image /assets/images/documentation/fr/image26.png %}

Une fois cliqué, une fenêtre s’affiche et vous pouvez associer des libellés à ce document. Vous pouvez choisir parmi les libellés existants, ou en créer de nouveaux.

{% image /assets/images/documentation/fr/image11.png "Fenêtre des libellés"%}

Pour afficher tous les documents liés à un libellé, il vous suffit de choisir le libellé en question dans le menu de gauche.

{% image /assets/images/documentation/fr/image34.png "Sélection d’un libellé"%}

Vous pouvez supprimer un libellé en cliquant sur la flèche dans la zone du libellé, puis sur “Supprimer”. Les documents associés à ce libellé ne seront pas supprimés.

{% image /assets/images/documentation/fr/image38.png "Suppression d’un libellé"%}

##Liens réservés et tâches

Afin d’assurer un accès rapide aux documents qui concerne un utilisateur donné, vous trouverez dans le menu de gauche deux liens :

* Réservés : ce lien affichera l’ensemble des documents réservés par l’utilisateur.
* Tâches : sert à visualiser les différents documents sur lesquels l’utilisateur est directement impliqué dans un workflow.

#Gestion des workflows

Un workflow, ou flux des travaux, est la représentation des opérations affectées à un document ou un article. Ces opérations peuvent être effectuées par différents utilisateurs d’un même espace de travail.

##Les rôles

Pour créer un modèle de workflow, il faut au préalable créer des rôles dans l’espace de travail. Ces rôles peuvent être assignés par défaut à des utilisateurs, mais peuvent être redéfinis lors de l'affectation du workflow à la création d'un document ou d'un article.

{% image /assets/images/documentation/fr/image00.png "Création des rôles"%}

##Modèle de workflow

Un modèle de workflow est composé d'un état initial, d'une série d'activités et d'un état final. Chaque activité contient des tâches à effectuer. Ces tâches peuvent être effectuées en série, ou en parallèle.

Pour une activité de type série, les tâches sont réalisées dans l'ordre et le rejet d'une tâche entraîne l'arrêt de l'activité courante.

Pour une activité en parallèle, les tâches sont déroulées indépendamment de l'ordre dans lequel elles ont été créées. Il faut alors définir un nombre de tâche à compléter validant l’activité.

Une activité validée entraîne le démarrage de la suivante. Une activité invalidée entraînera la suspension du workflow.

En cas d’invalidation, le workflow reprendra à l’activité de relance si celle ci a été auparavant définie.

Un modèle de workflow peut être modifié à n’importe quel instant mais cela n’entrainera pas la modification des workflows instanciés depuis celui-ci.

{% image /assets/images/documentation/fr/image20.png "Création d'un modèle de workflow"%}

##Instance de workflow

Chaque modèle de workflow créé peut être instancié à la création d'un document ou d'un article. Les rôles peuvent être alors redéfinis.

{% image /assets/images/documentation/fr/image23.png "Définition des rôles à la création d’un document"%}

Une fois le document (ou l’article) créé, le workflow démarre sur la première activité. Lorsqu'une tâche est ouverte, un mail est envoyé au responsable de la tâche en cours. Le responsable peut alors l'approuver ou la rejeter et signer.

##Etat du cycle de vie

Une fois la première activité démarrée, les utilisateurs désignés effectuent les opérations renseignées.

{% image /assets/images/documentation/fr/image43.png "Workflow instancié sur un document"%}

Les autres utilisateurs peuvent aussi s'abonner aux changements d'état du workflow[m], un mail de notification leur est alors envoyé.

Une tâche peut être approuvée ou rejetée si :

* Le responsable a téléchargé au moins une fois un fichier associé
* Le document ou l'article est libéré.


#Options de partage et de publication

##Publication

Chaque document et article peut être publié. Pour cela il faut cliquer sur l’icône située à droite de chaque ligne, la fenêtre suivante apparaît alors.

{% image /assets/images/documentation/fr/image45.png "Fenêtre de publication d’un document"%}

Une fois le document publié en cliquant sur le bouton ON/OFF, il sera accessible depuis internet sans qu’une authentification soit nécessaire.

##Accès privé

Vous pouvez aussi protéger vos publications en utilisant l’accès privé. L’opération se fait dans la même fenêtre que le partage publique. Vous pouvez choisir une date d’expiration et un mot de passe. Ces deux champs ne sont pas obligatoires, mais seront vérifiés à l’accès si vous les renseignés.

Ensuite vous devez cliquer sur “Partager” pour générer un lien. Ce lien est offusqué, il n’est pas devinable.

{% image /assets/images/documentation/fr/image40.png "Lien généré lors d’un partage privé"%}

