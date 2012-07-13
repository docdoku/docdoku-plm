#How to setup the environment before installing the software

##Introduction

This page covers the installation and configuration of the softwares required to run the DocDoku application.

###Prerequisite Softwares

To run DocDoku, you need a JEE 1.6 application server and a database system.

Glassfish and mysql are our preferable choices but any 1.6 JEE compliant server works as well as SQLServer or Oracle for the RDMS part.

####Glassfish

Because DocDoku DMS need a Java EE 6 server you should download Glassfish version 3 at least.
http://glassfish.java.net/public/downloadsindex.html

####MySQL

To download MySQL version 5 just follow this http://dev.mysql.com/downloads/mysql/ link. Don't forget to copy a version of the jdbc driver into "{glassfish install dir}/lib" folder.

####OpenOffice

To be able to generate PDF files, we need to install OpenOffice version 3. To download this software, we go http://www.openoffice.org.
On Linux system, OpenOffice is provided as a regular package, for example on debian based distrib you can just enter:
_sudo apt-get install openoffice.org_

However, check carefully that it's version 3 that can be found on the repository.

####SWFTools

SWFTools is also needed (to generate the document flash viewer). A Windows and Linux version are downloadable at http://www.swftools.org/.
On Linux, this piece of software can be simply installed with sudo apt-get install SWFTools.
Be aware that the swftools package may have moved to the partner repository.
Thus, we should add this line "deb http://archive.canonical.com/ubuntu lucid partner " to the /etc/apt/sources.list file.

----

###MySQL Configuration

Edit your mysql configuration file (my.cnf) and check that you have:

default-storage-engine=INNODB#skip-networking must be commented. 

Log into MySQL to create the docdoku database with the following command:

_CREATE DATABASE docdoku;_ 

Then create the MySQL user for the DocDoku application: 

_GRANT ALL PRIVILEGES ON docdoku.`*` TO 'docdoku_dbuser'@'localhost' IDENTIFIED BY 'password';_

Of course, you may specify a better password or change 'localhost' to the hostname of the glassfish server if it's not installed on the same machine than MySQL.

----

###Glassfish Configuration

Edit the "{glassfish install dir}/domains/domain1/config/login.conf" file and add the line below at the end :
```
 docdokuRealm {
     com.sun.enterprise.security.auth.login.JDBCLoginModule required;
 }; 
```

Then start the glassfish server and log into the admin console (http://localhost:4848/).

 
####JDBC

Configure the jdbc connection pool. Go under "Resources> JDBC> Connection Pools" and click on "New". Create your connection pool, you can name it "DocDokuPool", for the resource type select "javax.sql.ConnectionPoolDataSource", for the Driver Provider Database select "Mysql", then click "Next".

On the next screen, check the "Connection Validation" and "Allow Non Component Callers" options. For the "Transaction Isolation" choose "repeatable-read". Fill now the parameters like that:

```
 databaseName = docdoku
 serverName = localhost
 user = docdoku_dbuser
 password = {db password}
```

Save and check your configuration by clicking on "Ping". 

Go under "Resources> JDBC> JDBC Resources" and create the jdbc resource associated with the "DocDokuPool", enter "jdbc/docdokuPU" as the JNDI name. 

####Mail

On "Resources> JavaMail Sessions", add a mail session with "mail/docdokuSMTP" for the JNDI name, and puts "serveur" in défault server, and noreplay in "Sender address by default". Save your configuration by clicking on "Validate".
 
####Security

Now, we need to create a security realm in "Configuration> Server configuration>domaines". We name it "docdokuRealm", its type should be "com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm" with the following name/value parameters:

```
jaas-context docdokuRealm
digest-algorithm MD5
group-name-column GROUPNAME
group-table USERGROUPMAPPING
user-table CREDENTIAL
user-name-column LOGIN
datasource-jndi jdbc/docdokuPU
password-column PASSWORD
```

Depending on the glassfish distribution you're using, the parameters may appear as follow:

```
JAAS context = docdokuRealm
JNDI = jdbc/docdokuPU
User Table = CREDENTIAL
User Name Column = LOGIN 
Password Column = PASSWORD
Group Table = USERGROUPMAPPING 
Group Name Column = GROUPNAME 
Digest Algorithm = md5
```

Caution: Sometimes, there's a bug that prevents glassfish to keep those settings. In this case, edit the xml file located in ```glassfish_home/domains/domains1/domain.xml``` and then in section ```<auth-realm classname="com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm" name="docdokuRealm">```, add:

```
<property name="user-name-column" value="LOGIN"/>
<property name="digest-algorithm" value="MD5"/>
<property name="password-column" value="PASSWORD"/>
<property name="group-name-column" value="GROUPNAME"/>
<property name="datasource-jndi" value="jdbc/docdokuPU"/>
<property name="user-table" value="CREDENTIAL"/>
<property name="group-table" value="USERGROUPMAPPING"/>
<property name="jaas-context" value="jdbcRealm"/> 
```
