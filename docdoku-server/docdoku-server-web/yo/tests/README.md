# DocdokuPLM Webapp tests

# Requirements

node
npm
git

# Running tests

Edit config.local.json according to your environment, then run 'grunt test' with cwd 'docdoku-plm/docdoku-server/docdoku-server-web/yo/'

# Scenario

## 1 : Sign up, workspace creation

Navigate to home page
Find the subscribe link, click it.
Register to the application, fill all input fields and submit
We should be redirected on same page, and find the link :
<a href="/faces/admin/workspace/workspaceCreationForm.xhtml">create</a>
Click it we should be redirected on workspaceCreationForm.xhtml
Fill all input fields and submit
